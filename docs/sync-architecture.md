# 同步架构全链路文档

> 最后更新：2026-08-12 · 对应版本：1.8.0
>
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
│          pendingChange 通知  sync_metadata 表 (syncStatus=pending)   │
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
│                   Room (dao.update)    Room (dao.upsert)            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

本地写入路径由 `PendingChangeNotifier` 发事件通知 `SyncTrigger` 自动触发 push（不依赖 Room Flow），pull 由 SyncTrigger/SyncWorker 显式驱动。

---

## 二、核心数据结构

### 2.1 业务表通用字段

所有参与同步的业务表均有以下同步相关字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | INT (PK) | Room 自增主键 |
| `uuid` | TEXT | 全局唯一标识，跨设备关联记录 |
| `updatedAt` | LONG (epoch) | 最后修改时间，LWW 冲突裁决依据 |
| `deletedAt` | LONG (epoch) | 软删除时间戳，NULL = 未删除 |
| `familyId` | TEXT | 本地端的家庭归属（宝宝表的列名就是 `familyId`） |

### 2.2 sync_metadata 表（10 字段 + 唯一索引）

```sql
sync_metadata (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tableName   TEXT NOT NULL,        -- 业务表名
    localId     INTEGER NOT NULL,     -- 业务记录 Room id
    remoteUuid  TEXT,                 -- Supabase uuid
    syncStatus  TEXT NOT NULL,        -- pending | synced | conflict
    updatedAt   INTEGER NOT NULL,     -- 业务记录 updatedAt（排序用）
    lastSyncAt  INTEGER,              -- 该行最近一次成功同步时间
    familyId    TEXT,                 -- 家庭归属（push 按此过滤）
    retryCount  INTEGER NOT NULL,     -- 连续失败次数（重试退避计数）
    nextRetryAt INTEGER NOT NULL,     -- 下次可重试时间戳（指数退避）
    lastError   TEXT                  -- 最近一次失败原因
)
-- 唯一索引：UNIQUE (tableName, localId)
```

`sync_metadata.insert` 使用 `IGNORE`（禁 REPLACE），`pendingChange` 先 `updatePending` 更新已有行、无匹配才 INSERT，防止行 id 被静默变更导致 push 的 `markSynced` 空匹配。

### 2.3 sync_cursors 表（增量拉取游标）

```sql
sync_cursors (
    familyId    TEXT NOT NULL,
    tableName   TEXT NOT NULL,
    lastVersion INTEGER NOT NULL,     -- 服务端 sync_version 单调递增
    PRIMARY KEY(familyId, tableName)
)
```

按「家庭 + 表」独立保存服务端递增版本，取代旧版全局 `lastSyncAt` 锚点（`getLastSyncAt`/`updateLastSyncAt`/`clearLastSyncAt` 已于 1.7.11 删除）。

### 2.4 涉及的表（9 张同步业务表 + 1 张 Realtime 监听表）

| 表名 | 说明 |
|---|---|
| `babies` | 宝宝信息（唯一含 `familyId` 的业务表，其他表经 baby_id 关联） |
| `feedings` | 喂养记录 |
| `sleeps` | 睡眠记录 |
| `growths` | 生长数据 |
| `vaccinations` | 疫苗接种 |
| `health_records` | 健康记录 |
| `diapers` | 尿布记录 |
| `development_assessments` | 发育评估 |
| `reminders` | 提醒事项 |
| `family_members` | 家庭成员（仅 Realtime 监听通知 UI，不存 Room、不走同步） |

⚠️ **`messages` 不参与同步**（1.5.11 摘除）。它是用户私有本地数据，RLS 策略是 `user_id = auth.uid()` 而非 family 级别，推送必触发 42501。`markExistingPending()` 开头显式执行 `DELETE FROM sync_metadata WHERE tableName='messages'` 清理存量。

---

## 三、本地写操作 → sync_metadata

### 3.1 Repository 三层模式

所有 CRUD 通过 Repository → DAO，每次都会调用 `syncMeta.pendingChange()`：

```
Repository.insert(domain)
  ├─ domain.copy(uuid = newUuid(), updatedAt = now) → entity
  ├─ dao.insert(entity) → localId
  └─ pendingChange(tableName, localId, uuid, updatedAt, familyId)
      └─ updatePending(已有行→pending) 或 INSERT IGNORE(pending) + PendingChangeNotifier.changed()

Repository.update(domain)
  ├─ domain.copy(updatedAt = now) → entity
  ├─ dao.update(entity)              ← id 保留
  └─ pendingChange(...)

Repository.delete(domain)
  ├─ domain.copy(deletedAt = now, updatedAt = now) → entity
  ├─ dao.update(entity)              ← 软删除，不是物理删除
  └─ pendingChange(...)
```

`ReminderRepository.markDone()` / `setEnabled()` 也已修复（1.5.19 起）走同一路径：读出当前行 → copy 修改 + 刷新 updatedAt → dao.update → pendingChange，状态变更可正常同步。

### 3.2 特殊：宝宝级联删除

`BabyRepository.delete()` 先级联软删除 8 张子表，再标记 sync_metadata：

```
BabyRepository.delete(baby)
  ├─ baby.copy(deletedAt = now, updatedAt = now) → dao.update()
  ├─ cascadeSoftDelete(baby.id)
  │   ├─ 查询各子表：SELECT id, uuid FROM $table WHERE baby_id = $id AND deletedAt IS NULL
  │   ├─ 批量 SQL：UPDATE $table SET deletedAt = now, updatedAt = now WHERE baby_id = $id
  │   └─ 逐条 pendingChange(table, id, uuid, now, familyId)
  └─ pendingChange("babies", baby.id, uuid, now, familyId)
```

### 3.3 同步元数据生命周期

```
         insert/update/delete
                │
                ▼
         syncStatus = 'pending'
                │
        ┌───────┴────────┐
        ▼                ▼
    push 成功         push 失败
        │                │
        ▼                ▼
   syncStatus =    markRetry：指数退避（5s→10s→20s→40s→80s）
   'synced'             │
        │                └─ retryCount ≥ 5 次 → 'conflict'（此后只靠
        │                   markExistingPending 显式重置，不再自动重试）
        ▼
   后续不再上传
```

---

## 四、上行同步（Push）

### 4.1 流程

```
SyncEngine.push()
  ├─ pushPullMutex.withLock（check-then-act 的 _syncState 守卫在并发下不可靠，1.7.10 起双互斥）
  │
  ├─ currentFamilyId == null → 跳过（无家庭不上行）
  │
  ├─ syncMeta.getPendingChanges(fid, now)
  │   └─ SELECT * FROM sync_metadata
  │      WHERE syncStatus='pending' AND familyId=:fid AND nextRetryAt <= :now
  │      ORDER BY updatedAt ASC        ← 未到重试时间的失败记录自动跳过
  │
  ├─ for each pending meta:
  │   ├─ getEntityDao(meta.tableName).getById(meta.localId) → entity（不存在则失败）
  │   ├─ entityToJson(tableName, entity) → payload
  │   │   ├─ babyId → babyUuid（本地 int → 全局 uuid）
  │   │   └─ 包含 deletedAt（软删除传播）
  │   ├─ injectFamilyId(payload) → payload + family_id（满足 RLS）
  │   │
  │   └─ remoteUuid != null（1.5.11 远端版本预检）：
  │       ├─ SELECT * FROM $table WHERE uuid = :remoteUuid AND family_id = :fid
  │       ├─ 远端存在 且 remoteUpdatedAt >= localUpdatedAt
  │       │   └─ applyRemoteChange(remote)  ← 远端较新，直接拉回本地覆盖
  │       └─ 否则 upsert(payload) { onConflict = "uuid" }
  │       └─ 成功：markSynced(id, uuid, max(local, remote))，重试计数清零
  │
  │   remoteUuid == null：
  │       ├─ insert(payload)
  │       └─ markSynced(id, payload.uuid, now)
  │
  └─ 任一步失败：markRetry(id, now + syncRetryDelay(retryCount), error)
      syncRetryDelay = (1L shl retryCount.coerceIn(0,5)) * 5000ms
```

### 4.2 关键细节

- **互斥**：`pushPullMutex` 保护 push/pull 单操作；`fullSyncMutex` 保护 fullSync；锁序固定（先 fullSync 后 push/pull），1.7.10 修复并发竞态且无死锁
- **防重复**：Supabase 端以 `uuid` 为冲突键，同一记录多次 push 自动覆盖（幂等）
- **失败重试**：指数退避（5s→10s→20s→40s→80s），连续失败 ≥ 5 次置 `conflict` 停止自动重试；`getPendingChanges` 的 `nextRetryAt <= now` 过滤保证退避期间不狂刷
- **撤销删除**：1.7.9 起撤销删除 = 清除 deletedAt 标记后 UPDATE（原 id 复用），不再用 id 重插（主键冲突必崩）

---

## 五、下行同步（Pull）

### 5.1 流程（1.5.11 起基于 sync_cursors 分页游标）

```
SyncEngine.pull()
  ├─ pushPullMutex.withLock
  ├─ currentFamilyId == null → 跳过（无家庭不下行）
  │
  ├─ for each tableName in [9 张同步表]:
  │   ├─ pageCursor = syncCursor.get(fid, tableName) ?: 0L   （无游标 = 全量）
  │   ├─ loop（分页）:
  │   │   SELECT * FROM $table
  │   │   WHERE family_id = :fid AND sync_version > :pageCursor
  │   │   ORDER BY sync_version ASC LIMIT 500
  │   │   │
  │   │   ├─ 整页逐条 applyRemoteChange(row)，任一条失败 → check 抛异常整页作废
  │   │   └─ 整页全部落库成功才推进：syncCursor.set(fid, table, maxVersion)
  │   ├─ hasMore = result.size >= 500 → 继续下一页
  │   └─ 单表 30s 超时保护（withTimeout），失败计入 failures 不影响其他表
  │
  └─ 整页提交保证：失败不漏数（游标不前进，下轮重拉）
```

### 5.2 applyRemoteChange 核心逻辑

```
applyRemoteChange(tableName, remoteRow)
  ├─ 提取 remoteUuid, remoteUpdatedAt（JsonPrimitive 安全读取，1.7.11）
  ├─ localEntity = dao.getByUuid(remoteUuid)
  ├─ LWW 裁决：localUpdatedAt >= remoteUpdatedAt → 跳过（本地优先，忽略远程）
  ├─ 写入本地：dao.upsert(remoteRow)  ← 按 uuid 查本地：存在则 update（保留原 id），无则 insert
  │
  └─ 同步元数据（1.7.7 修复，防行 id 漂移）：
      ├─ existingMeta = syncMeta.getByTableAndId(tableName, localId)
      ├─ 存在 → updateByTableAndId(保留原行 id)
      └─ 不存在 → insert（IGNORE）
```

### 5.3 增量 vs 全量

| 场景 | 游标 | 行为 |
|---|---|---|
| 正常增量 | `lastVersion = N` | `sync_version > N`，逐表独立 |
| 首次安装 / 首次加入家庭 | 无记录（= 0） | 全量：拉取家庭全部历史数据 |
| 切换家庭 `resetLastSync()` | 清空该家庭游标 | 全量重拉 |
| `fullSync()` | 不变 | 先 push 再 pull |

### 5.4 LWW 策略说明

- **裁决条件**：`localUpdatedAt >= remoteUpdatedAt` → 保留本地，忽略远程（`>=` 意味着相同时刻本地优先）
- **局限**：不处理真正的语义冲突（两人同时修改同一字段），仅靠时间戳
- **合理性**：家庭场景下同一记录通常由一个用户操作，并发修改极少

---

## 六、实时同步（Realtime）

### 6.1 订阅机制

```
RealtimeManager.subscribeAll()
  ├─ familyId = syncEngine.currentFamilyId（无家庭直接 return）
  ├─ disconnect()                    ← 先断开旧频道，避免订阅冲突和 RLS 上下文过期
  │   └─ 取消旧 changeJobs 协程 + channel.unsubscribe()（1.7.9 修泄漏）
  │
  ├─ supabase.channel("db-changes-$familyId")     ← 频道名按家庭隔离
  │
  ├─ for each table in [9 业务表 + family_members]（无 messages）:
  │   └─ postgresChangeFlow<PostgresAction>(
  │         schema = "public",
  │         filter = {
  │             table = tableName
  │             filter("family_id", EQ, familyId)   ← 显式家庭过滤
  │         })
  │       └─ onEach { handleRealtimeChange(tableName, action) }
  │
  └─ channel.subscribe(blockUntilSubscribed = true)  ← 10s 超时保护
```

### 6.2 事件分发

| Supabase 事件 | 处理方式 | 备注 |
|---|---|---|
| `INSERT` | `applyRemoteChange(tableName, record)` | 同 pull 路径 |
| `UPDATE` | `applyRemoteChange(tableName, record)` | 同 pull 路径，包含软删除更新 |
| `DELETE` | `softDeleteLocal(tableName, uuid, now)` | DAO 软删除（JsonPrimitive 读 uuid） |
| `SELECT` | 忽略 | — |
| `family_members` 任意事件 | 仅 `familyMembersChanged` 通知 UI 刷新 | 不写 Room |

入站记录先校验 `family_id` 与当前家庭一致，不一致丢弃（双保险）。

**云端孤儿行（外键违反）**：v9 起 Room 外键强制（`AppDatabase_Impl` 执行 `PRAGMA foreign_keys = ON`），入站行若 `babyId`（uuid）在本机 `babies` 不存在（`resolveLocalBabyId` 返回 0 → `baby_id=0`），插入/更新会被 `SQLiteConstraintException` 拒绝。`applyRemoteChange` 对该异常**特判跳过**：记 WARN 日志（Tag=Sync，含 table/uuid）、返回 true 推进游标——避免整页拉取永久失败；数据保留在云端不清除，宝宝 uuid 将来出现时全量拉取可恢复。pull 与 Realtime 共用此路径，行为一致。

### 6.3 说明

当前系统使用**软删除**（upsert with deletedAt），Supabase 上不会真正 DELETE 行，因此 `PostgresAction.Delete` 仅在云端执行物理删除时才触发（目前不常用）。

---

## 七、同步触发时机

### 7.1 家庭驱动链（取代旧 ensureFamily 三层回退）

家庭状态唯一来源是 `FamilyService.sessionState`，**只有经过会话校验的家庭才能驱动同步**：

```
FamilyService.refreshForUser()（登录/切号/网络恢复时）
  └─ sessionState 更新（sessionVerified = true, selectedFamily 生效）
      └─ SyncTrigger.observeFamily() 监听 verifiedFamilyForSync?.id
          ├─ syncEngine.currentFamilyId = newId
          ├─ 家庭切换（≠ 上次同步家庭）：
          │   ├─ syncEngine.resetLastSync()   ← 清空该家庭 sync_cursors（全量重拉）
          │   ├─ 记录 sync_last_family 到 SharedPreferences
          │   └─ markExistingPending()（一次性标记 sync_pending_marked_$fid：
          │       存量数据补标 pending；有表失败则不置位、保留重试机会）
          ├─ triggerSync()  ← fullSync（先 push 再 pull）
          └─ realtimeManager.subscribeAll()   ← Realtime 重订阅（新频道名）
```

- 无家庭（`verifiedFamilyForSync == null`，含本地模式）→ `currentFamilyId = null`，push/pull 直接跳过，Realtime unsubscribe
- `markExistingPending()`：补 uuid/updatedAt（缺失时）、`INSERT OR IGNORE` 补 pending 行、回填 familyId，全程单事务
- **备份还原后**（1.7.10）：`BackupManager` 清空 `sync_pending_marked_*` 一次性标记，让 markExistingPending 重新运行，还原数据才能自动上行

### 7.2 SyncTrigger 触发源

| 触发源 | 行为 |
|---|---|
| `observeAuth` | 登录/登出/账号切换 → 恢复缓存/刷新家庭会话 |
| `observeFamily` | 家庭变化 → resetLastSync + markExistingPending + fullSync + Realtime 重订阅 |
| `observeNetwork` | 断网→恢复在线 → refreshForUser + triggerSync |
| `observeAutoTrigger` | `PendingChangeNotifier` 事件（Repository 写入 pending 后发出，buffer 64）→ 按设置防抖 → `doPush()`；`doPush` 推送后复查 pending 余量自动补推（最多 3 次，1.7.10） |
| `observeBgInterval` | 设置周期同步 → WorkManager 唯一周期任务（SyncWorker） |
| `onAppBackgrounded` | 退后台且开启"退出时同步" → fullSync |

自动触发门禁：autoSync 开关、已登录、在线、wifiOnly 非计费网络、有 pending。

### 7.3 SyncWorker（WorkManager 周期后台同步）

`PeriodicWorkRequest<SyncWorker>`（唯一任务名 `bg_sync`，CANCEL_AND_REENQUEUE），网络约束跟随 wifiOnly 设置。执行：refreshForUser → 设置 currentFamilyId → fullSync；失败按 WorkManager 退避重试（最多 3 次 attempt）。

---

## 八、关键约束与已修复问题

### 8.1 已修复

| 问题 | 版本 | 修复内容 |
|---|---|---|
| `applyRemoteChange` 每次 insert 新 sync_metadata 行 | 1.7.7 | 先 `getByTableAndId`，有则 `updateByTableAndId` 保留行 id，无才 insert |
| `pendingChange` REPLACE 静默改行 id | 1.7.8 | 改 `updatePending` + INSERT IGNORE 双轨 |
| `markDone`/`setEnabled` 不同步 | 1.5.19 | 补 pendingChange 标记 |
| push/pull 并发守卫竞态 | 1.7.10 | `pushPullMutex` + `fullSyncMutex`，锁序固定无死锁 |
| 防抖期间事件缓冲溢出丢变更 | 1.7.10 | `doPush` 推送后复查 pending 补推（≤3 次） |
| 备份还原后存量数据永不自动上行 | 1.7.10 | 还原后清除 `sync_pending_marked_*` 标记 |
| markExistingPending 部分失败仍置一次性标记 | 1.7.10 | 有失败不置位，保留重试机会 |
| 撤销删除崩溃 | 1.7.9 | 撤销 = 清 deletedAt 后 UPDATE，不再原 id 重插 |
| Realtime 重订阅协程泄漏 | 1.7.9 | 统一取消 changeJobs，重抛 CancellationException |
| 远端返回字符串型 updatedAt 导致 LWW 退化 | 1.7.11 | 统一 JsonPrimitive 安全读取 |

### 8.2 已知局限

| 局限 | 影响 | 备注 |
|---|---|---|
| LWW ≥ 判断（不是 >） | 相同时刻本地优先，可能丢失远程变更 | 低概率场景 |
| 无真正冲突解决 | 两人同时修改同一条记录会丢失一方 | 家庭场景概率极低 |
| sync_metadata 行不清理 | 长期使用会产生大量历史记录 | 不影响功能，可后续加清理 |
| 游标仅整页成功才推进 | 失败页重拉，进度慢 | 一致性优先的正确取舍 |

### 8.3 约束

- **不物理删除**：所有删除操作使用软删除（`deletedAt`），不会在 Supabase 上 DELETE 行
- **家庭隔离**：依赖 `family_id` + Supabase RLS，无家庭上下文时 push/pull 跳过
- **双互斥**：`pushPullMutex`（push/pull 单操作）+ `fullSyncMutex`（完整双向）
- **UUID 为桥梁**：Room `id` 仅本地有效，跨设备通过 `uuid` 关联
- **messages 永远不同步**：`getEntityDao`/pull 表列表/markExistingPending 三处均无 `"messages"`（见 lessons.md 第 1 条）

---

## 九、文件索引

| 文件 | 职责 |
|---|---|
| `core/sync/SyncEngine.kt` | push/pull/fullSync 引擎，LWW 冲突裁决，Entity↔JSON 转换，markExistingPending/claimUnscopedData |
| `core/sync/SyncTrigger.kt` | 触发源编排（auth/family/network/autoTrigger/bgInterval/退后台），`PendingChangeNotifier` 事件通知（buffer 64） |
| `core/sync/SyncWorker.kt` | WorkManager 周期后台同步执行体 |
| `core/sync/RealtimeManager.kt` | 按家庭订阅 WebSocket 频道，事件分发，family_members 变更通知 |
| `core/sync/SyncConfig.kt` | 同步设置模型（autoSync/wifiOnly/syncDelay/bgInterval/syncOnExit） |
| `core/sync/SupabaseProvider.kt` | SupabaseClient 单例 |
| `core/data/FamilyService.kt` | `sessionState`/`verifiedFamilyForSync` 家庭会话唯一来源，同步只认已验证家庭 |
| `core/data/repository/Repositories.kt` | CRUD + pendingChange 标记 + PendingChangeNotifier 通知 |
| `core/database/dao/Daos.kt` | Room DAO（sync_metadata 10 字段、sync_cursors、softDeleteByUuid） |
| `core/database/Entities.kt` | Entity 定义（SyncMetadataEntity / SyncCursorEntity） |
| `core/database/AppDatabase.kt` | 数据库定义 + 迁移（v8；v6 建 sync_metadata，v7 换代游标+去 messages） |
| `core/backup/BackupManager.kt` | 备份还原；还原后清除 `sync_pending_marked_*` 触发重新标记 |
| `feature/settings/SyncViewModel.kt` | 手动同步按钮 + 同步结果展示 |
| `feature/settings/SettingsViewModel.kt` | 纯设置读写（updateSettings），不再包含家庭获取逻辑 |
