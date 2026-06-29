# 同步架构全链路文档

> 双向增量同步引擎：Room（本地）↔ Supabase（云端），LWW 冲突策略，Realtime 实时推送。

---

## 一、总体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                           客户端 A                                   │
│                                                                     │
│  ViewModel ──► Repository ──► Room (dao.update/insert)              │
│                    │              │                                  │
│                    ▼              ▼                                  │
│           syncMeta.pendingChange  sync_metadata 表                   │
│                    │                                                │
│                    ▼                                                │
│           SyncEngine.push() ──── Supabase PostgREST ────┐          │
│                                                         │          │
├─────────────────────────────────────────────────────────┼──────────┤
│                           客户端 B                       │          │
│                                                         ▼          │
│                   Supabase Realtime ──► RealtimeManager             │
│                         │                    │                      │
│                   SyncEngine.pull()    applyRemoteChange()          │
│                         │                    │                      │
│                         ▼                    ▼                      │
│                   Room (dao.update)    Room (dao.update/insert)     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心数据结构

### 2.1 业务表通用字段

所有业务表均有以下同步相关字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | INT (PK) | Room 自增主键 |
| `uuid` | TEXT | 全局唯一标识，跨设备关联记录 |
| `updatedAt` | LONG (epoch) | 最后修改时间，LWW 冲突裁决依据 |
| `deletedAt` | LONG (epoch) | 软删除时间戳，NULL = 未删除 |
| `family_id` | TEXT | Supabase 端使用的家庭隔离字段（RLS） |

### 2.2 sync_metadata 表

```sql
sync_metadata (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tableName   TEXT NOT NULL,       -- 业务表名
    localId     INTEGER NOT NULL,    -- 业务记录 Room id
    remoteUuid  TEXT,                -- Supabase uuid
    syncStatus  TEXT,                -- pending | synced | conflict
    updatedAt   INTEGER,            -- 业务记录 updatedAt（排序用）
    lastSyncAt  INTEGER             -- 全局增量同步时间锚点
)
```

### 2.3 涉及的表（共 10 张业务表 + 1 张元数据表）

| 表名 | 说明 |
|---|---|
| `babies` | 宝宝信息 |
| `feedings` | 喂养记录 |
| `sleeps` | 睡眠记录 |
| `growths` | 生长数据 |
| `vaccinations` | 疫苗接种 |
| `health_records` | 健康记录 |
| `diapers` | 尿布记录 |
| `messages` | 消息中心 |
| `development_assessments` | 发育评估 |
| `reminders` | 提醒事项 |
| `family_members` | 家庭成员（仅 Realtime 监听，不存 Room） |

---

## 三、本地写操作 → sync_metadata

### 3.1 Repository 层增删改

所有 CRUD 操作通过 Repository → DAO，每次都会调用 `syncMeta.pendingChange()`：

```
Repository.insert(domain)
  │
  ├─ domain.copy(uuid = newUuid(), updatedAt = now) → entity
  ├─ dao.insert(entity) → localId
  └─ syncMeta.pendingChange(tableName, localId, uuid, updatedAt)
      └─ INSERT INTO sync_metadata (tableName, localId, remoteUuid, syncStatus='pending', updatedAt)

Repository.update(domain)
  │
  ├─ domain.copy(updatedAt = now) → entity
  ├─ dao.update(entity)  ← id 保留
  └─ syncMeta.pendingChange(tableName, domain.id, uuid, updatedAt)

Repository.delete(domain)
  │
  ├─ domain.copy(deletedAt = now, updatedAt = now) → entity
  ├─ dao.update(entity)
  └─ syncMeta.pendingChange(tableName, domain.id, uuid, updatedAt)
```

### 3.2 特殊：宝宝级联删除

`BabyRepository.delete()` 会先级联软删除 8 张子表，再标记 sync_metadata：

```
BabyRepository.delete(baby)
  │
  ├─ baby.copy(deletedAt = now, updatedAt = now) → dao.update()
  │
  ├─ cascadeSoftDelete(baby.id)
  │   ├─ 查询各子表：SELECT id, uuid FROM $table WHERE baby_id = $id AND deletedAt IS NULL
  │   ├─ 批量 SQL：UPDATE $table SET deletedAt = now, updatedAt = now WHERE baby_id = $id
  │   └─ 逐条标记：syncMeta.pendingChange(table, id, uuid, now)
  │
  └─ syncMeta.pendingChange("babies", baby.id, uuid, updatedAt)
```

### 3.3 同步元数据生命周期

```
         insert/update/delete
                │
                ▼
         syncStatus = 'pending'
                │
        ┌───────┴───────┐
        ▼               ▼
    push 成功         push 失败
        │               │
        ▼               ▼
   syncStatus =     syncStatus =
   'synced'         'conflict'
        │               │
        │   markExistingPending()  ───→ 重置为 'pending'
        │
   下一次 pull 不会重复上传此记录
```

---

## 四、上行同步（Push）

### 4.1 流程

```
SyncEngine.push()
  │
  ├─ 检查 _syncState != SYNCING（互斥锁）
  │
  ├─ syncMeta.getPendingChanges()
  │   └─ SELECT * FROM sync_metadata WHERE syncStatus = 'pending' ORDER BY updatedAt ASC
  │
  ├─ for each pending meta:
  │   │
  │   ├─ dao.getById(meta.localId) → entity
  │   │   └─ 如果 entity 不存在（被物理删除？），跳过
  │   │
  │   ├─ entityToJson(tableName, entity) → JSON payload
  │   │   ├─ 转换所有业务字段
  │   │   ├─ babyId → babyUuid（本地 int → 全局 uuid）
  │   │   └─ 包含 deletedAt（如果有）
  │   │
  │   ├─ injectFamilyId(payload) → payload + family_id
  │   │   └─ 注入 currentFamilyId，满足 Supabase RLS 家庭隔离
  │   │
  │   └─ Supabase 写入：
  │       ├─ remoteUuid != null → upsert(payload) { onConflict = "uuid" }
  │       └─ remoteUuid == null → insert(payload)
  │       │
  │       └─ 成功：syncMeta.markSynced(meta.id, uuid, now)
  │           └─ UPDATE sync_metadata SET syncStatus='synced', remoteUuid=?, updatedAt=? WHERE id=?
  │          失败：syncMeta.markConflict(meta.id, now)
  │           └─ UPDATE sync_metadata SET syncStatus='conflict', updatedAt=? WHERE id=?
  │
  └─ syncMeta.updateLastSyncAt(now)
```

### 4.2 关键细节

- **互斥**：push 时 `_syncState = SYNCING`，其他 push/pull 调用返回 0
- **防重复**：Supabase 端以 `uuid` 为冲突键，同一记录多次 push 会自动覆盖（幂等）
- **带 deletedAt 推送**：删除操作通过设置 `deletedAt` 的 upsert 传播，不是物理 DELETE
- **冲突恢复**：失败记录标记为 `conflict`，下次 `markExistingPending()` 重置为 `pending`

---

## 五、下行同步（Pull）

### 5.1 流程

```
SyncEngine.pull()
  │
  ├─ 检查 _syncState != SYNCING
  ├─ 检查 currentFamilyId != null（无家庭不拉取）
  │
  ├─ lastSyncAt = syncMeta.getLastSyncAt()
  │   └─ SELECT MAX(lastSyncAt) FROM sync_metadata
  │
  ├─ for each tableName in [10 张业务表]:
  │   │
  │   └─ Supabase 查询：
  │       SELECT * FROM $tableName
  │       WHERE family_id = $fid
  │         AND updatedAt >= $lastSyncAt    -- 增量拉取（lastSyncAt 为空则全量）
  │       │
  │       └─ for each row: applyRemoteChange(tableName, row)
  │
  └─ syncMeta.updateLastSyncAt(now)
```

### 5.2 applyRemoteChange 核心逻辑

```
applyRemoteChange(tableName, remoteRow)
  │
  ├─ 提取 remoteUuid, remoteUpdatedAt
  │
  ├─ localEntity = dao.getByUuid(remoteUuid)
  │
  ├─ LWW 冲突裁决：
  │   └─ if localUpdatedAt >= remoteUpdatedAt → 跳过（忽略远程变更）
  │
  ├─ 写入本地：
  │   │
  │   ├─ localEntity != null（记录已存在）
  │   │   └─ dao.update(entity.copy(id = existing.id))  ← 保留本地 id，覆盖字段
  │   │       └─ **关键修复**：2026-06-29 前此处为 dao.insert()，导致重复行
  │   │
  │   └─ localEntity == null（首次出现的记录）
  │       └─ dao.insert(entity)
  │
  └─ syncMeta.insert(SyncMetadataEntity(
        tableName, localId, remoteUuid, syncStatus='synced', ...))
```

### 5.3 增量 vs 全量

| 场景 | lastSyncAt | 行为 |
|---|---|---|
| 正常启动 | 上次同步时间戳 | 增量：`updatedAt >= lastSyncAt` |
| 首次安装 | NULL | 全量：无 `gte` 过滤 |
| 首次加入家庭 | `resetLastSync()` → NULL | 全量：拉取家庭所有历史数据 |
| `fullSync()` | 上次同步时间戳 | 先 push 再增量 pull |

### 5.4 LWW 策略说明

- **裁决条件**: `localUpdatedAt >= remoteUpdatedAt` → 保留本地，忽略远程
- **注意**：`>=` 而非 `>`，意味着**相同时刻本地优先**
- **局限**：不处理真正的语义冲突（如两人同时修改同一字段），仅靠时间戳
- **合理性**：家庭场景下同一记录通常由一个用户操作，并发修改极少

---

## 六、实时同步（Realtime）

### 6.1 订阅机制

```
RealtimeManager.subscribeAll()
  │
  ├─ disconnect()  ← 先断开旧频道
  │
  ├─ supabase.channel("db-changes")
  │
  ├─ for each table in [10 业务表 + family_members]:
  │   └─ channel.postgresChangeFlow<PostgresAction>(
  │         schema = "public",
  │         filter = { table = tableName }
  │      )
  │      └─ onEach { handleRealtimeChange(tableName, action) }
  │
  └─ channel.subscribe(blockUntilSubscribed = true)
```

### 6.2 事件分发

| Supabase 事件 | 处理方式 | 备注 |
|---|---|---|
| `INSERT` | `applyRemoteChange(tableName, record)` | 同 pull 路径 |
| `UPDATE` | `applyRemoteChange(tableName, record)` | 同 pull 路径，包含软删除更新 |
| `DELETE` | `softDeleteLocal(tableName, uuid, now)` | 直接调用 DAO 软删除 |
| `SELECT` | 忽略 | — |

### 6.3 Realtime 软删除

```
PostgresAction.Delete
  │
  ├─ oldRecord['uuid'] → uuid
  └─ softDeleteLocal(tableName, uuid, now)
      └─ dao.softDeleteByUuid(uuid, deletedAt, updatedAt)
          └─ UPDATE $table SET deletedAt = ?, updatedAt = ? WHERE uuid = ?
```

**注意**：当前系统使用**软删除**（upsert with deletedAt），Supabase 上不会真正 DELETE 行，因此 `PostgresAction.Delete` 仅在云端执行物理删除时才触发（目前不常用）。

### 6.4 家庭隔离

- 所有 pull 查询带 `family_id = fid` 过滤
- Realtime 订阅通过 Supabase RLS，由服务端根据 token 中的 family 信息自动过滤
- `family_members` 表变更仅通知 UI 刷新，不写入 Room

---

## 七、同步触发时机

```
应用启动
  └─ ensureFamily()              ← 多层回退获取家庭 ID
       │
       ├─ ① 内存 currentFamily
       ├─ ② Supabase loadMyFamilies()
       └─ ③ SharedPreferences offline fallback
       │
       └─ fullSync()              ← 先 push 后 pull
            │
            ├─ push()             ← 推送本地 pending 变更
            └─ pull()             ← 拉取云端增量（或全量）

Realtime 连接
  └─ subscribeAll()              ← 建立 WebSocket，监听实时变更

用户操作（增删改）
  └─ Repository → syncMeta.pendingChange  ← 自动标记
       │
       └─ 由定时任务 或 下一次 fullSync 触发 push

家族切换/首次加入
  └─ resetLastSync()              ← 清空 lastSyncAt
       └─ markExistingPending()   ← 存量数据标记为 pending
            └─ fullSync()         ← 全量双向同步
```

---

## 八、关键约束与已知局限

### 8.1 已修复

| 问题 | 修复日期 | 修复内容 |
|---|---|---|
| `applyRemoteChange` 不停 insert 重复行 | 2026-06-29 | upsert 改为按 uuid 查找本地，存在则 update |
| `cascadeSoftDelete` 无 sync_metadata | 2026-06-29 | 增加子记录 id/uuid 收集 + pendingChange |
| 离线重启后家庭 ID 丢失 | 2026-06-29 | ensureFamily() 新增 SharedPreferences 回退 |

### 8.2 已知局限

| 局限 | 影响 | 备注 |
|---|---|---|
| LWW ≥ 判断（不是 >） | 相同时刻本地优先，可能丢失远程变更 | 低概率场景 |
| 无真正冲突解决 | 两人同时修改同一条记录会丢失一方 | 家庭场景概率极低 |
| `markDone`/`setEnabled`/`markRead` 无 sync_metadata | 提醒/消息的状态变更不会自动同步 | 需手动调用 update() 或增加 pendingChange |
| sync_metadata 行不清理 | 长期使用会产生大量历史记录 | 不影响功能，可后续加清理 |
| `applyRemoteChange` 每次都 insert 新 sync_metadata 行 | sync_metadata 会有重复记录 | id 不同，查询不受影响 |

### 8.3 约束

- **不物理删除**：所有删除操作使用软删除（`deletedAt`），不会在 Supabase 上 DELETE 行
- **家庭隔离**：依赖 `family_id` + Supabase RLS，无家庭上下文时 pull 跳过
- **互斥**：同一时刻只允许一个 push 或 pull 运行
- **UUID 为桥梁**：Room `id` 仅本地有效，跨设备通过 `uuid` 关联

---

## 九、文件索引

| 文件 | 职责 |
|---|---|
| `core/sync/SyncEngine.kt` | push/pull/fullSync 引擎，冲突裁决，Entity↔JSON 转换 |
| `core/sync/RealtimeManager.kt` | WebSocket 订阅，事件分发，Family 变更通知 |
| `core/data/repository/Repositories.kt` | CRUD + syncMeta.pendingChange 标记 |
| `core/database/dao/Daos.kt` | Room DAO（含 sync_metadata、softDeleteByUuid） |
| `core/database/Entities.kt` | Entity 定义（含 SyncMetadataEntity） |
| `core/database/AppDatabase.kt` | 数据库定义 + 迁移（含 sync_metadata 建表） |
| `feature/settings/SettingsViewModel.kt` | ensureFamily() 家庭 ID 获取 |
