# 数据架构图

> 最后更新：2026-08-12 · 对应版本：1.8.0

---

## 当前架构（已实现）

### 层级总览

```
┌──────────────────────────────────────────────────────────────────┐
│                      ☁  Supabase 云端                             │
│                                                                  │
│  ┌─────────────────┐   ┌──────────────┐   ┌──────────────────┐  │
│  │ auth.users       │   │ families      │   │ family_members   │  │
│  │ (Supabase Auth)  │   │ id, name,     │   │ family_id,       │  │
│  │                  │   │ invite_code   │   │ user_id, role    │  │
│  └─────────────────┘   └──────────────┘   └──────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────────┐│
│  │ 同步数据表 (9 张)                           ★ 云端均有 family_id ││
│  │                                                              ││
│  │  babies  ──┐                                                 ││
│  │            ├── feedings                                      ││
│  │            ├── sleeps                                        ││
│  │            ├── growths           ★ 通过 babyId(uuid) 关联     ││
│  │            ├── vaccinations                                   ││
│  │            ├── health_records                                 ││
│  │            ├── diapers                                        ││
│  │            ├── development_assessments (FK CASCADE)           ││
│  │            └── reminders (FK CASCADE)                         ││
│  └──────────────────────────────────────────────────────────────┘│
└────────────────────────────┬─────────────────────────────────────┘
                             │  SyncEngine (push/pull, LWW)
                             │  sync_metadata 维护 id↔uuid 映射
                             │  sync_cursors 按 (familyId, tableName) 记账
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│                     📱 Room 本地数据库 (version 8)                 │
│                                                                  │
│  babies              ← 唯一容器，记录通过 babyId 挂载              │
│  ├── id(PK)  name  gender  birthDate  birthWeight  birthHeight   │
│  ├── avatarPath  createdAt                                        │
│  └── uuid  updatedAt  deletedAt  familyId  (同步字段)             │
│                                                                  │
│  ┌──────────────────────┬──────────────────────────────────────┐ │
│  │ 记录表 (8 张，含 babyId)    │ 无 babyId / 纯本地               │ │
│  │                      │                                     │ │
│  │ feedings      sleeps │ messages (App 级消息，不参与同步)      │ │
│  │ growths  vaccinations│ ai_conversations (AI 会话，仅本机)     │ │
│  │ health_records diapers│ ai_messages (AI 消息，仅本机)         │ │
│  │ development_assessments ※FK baby_id                         │ │
│  │ reminders ※FK baby_id │ backup_config (备份配置，纯本地)     │ │
│  │                      │                                     │ │
│  │ ★ 均含 uuid/updatedAt/deletedAt；无 familyId 列，             │ │
│  │   归属靠 JOIN babies 推导                                   │ │
│  └──────────────────────┴──────────────────────────────────────┘ │
│                                                                  │
│  ┌──────────────┐    ┌──────────────────────────────────────┐    │
│  │ sync_metadata │   │ sync_cursors                          │    │
│  │ id, tableName,│   │ PK(familyId, tableName)              │    │
│  │ localId,      │   │ lastVersion ← 服务端 sync_version     │    │
│  │ remoteUuid,   │   └──────────────────────────────────────┘    │
│  │ syncStatus... │   (唯一索引 (tableName, localId))              │
│  └──────────────┘                                               │
└──────────────────────────────────────────────────────────────────┘
```

### 15 张表清单（Room version 8 基线）

| # | 表名 | PK | 含 babyId | 有 FK | 同步字段 | 说明 |
|---|------|----|-----------|-------|----------|------|
| 1 | `babies` | Int auto | — | — | uuid/updatedAt/deletedAt/familyId | 宝宝容器，本地唯一含 familyId 的表 |
| 2 | `feedings` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 喂养 |
| 3 | `sleeps` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 睡眠 |
| 4 | `growths` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 生长 |
| 5 | `vaccinations` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 疫苗 |
| 6 | `health_records` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 健康 |
| 7 | `diapers` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 尿布 |
| 8 | `development_assessments` | Int auto | ✅ | ✅ → babies.id | uuid/updatedAt/deletedAt | 发育评估（5 项能力 0-3 分） |
| 9 | `reminders` | Int auto | ✅ | ✅ → babies.id | uuid/updatedAt/deletedAt | 提醒 |
| 10 | `messages` | Long auto | ❌ | — | 有字段但不参与同步 | 消息中心，type 存 MessageType.name |
| 11 | `backup_config` | Int auto | — | — | ❌ 纯本地 | WebDAV 备份配置 |
| 12 | `sync_metadata` | Int auto | — | — | ❌ 同步元数据 | id↔uuid 映射 + dirty 队列 |
| 13 | `sync_cursors` | (familyId, tableName) 复合 | — | — | ❌ 同步元数据 | 每家庭每表服务端 sync_version 游标 |
| 14 | `ai_conversations` | Long auto | ✅ | ✅ → babies.id | ❌ 仅本机 | AI 会话（v8 新增，绑定家庭+宝宝） |
| 15 | `ai_messages` | Long auto | 间接（经会话） | ✅ → ai_conversations.id | ❌ 仅本机 | AI 消息（v8 新增，含思考/安全字段） |

### 关键表字段

**sync_metadata**（除自增 PK 外 10 个业务字段，唯一索引 `(tableName, localId)` 防重复映射）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Int PK | 自增 |
| tableName | String | 业务表名："feedings", "sleeps", ... |
| localId | Int | Room 表本地自增 id |
| remoteUuid | String? | 云端 UUID |
| syncStatus | String | `pending` / `synced` / `conflict`，即 dirty 队列标记 |
| updatedAt | Long | epoch milli |
| lastSyncAt | Long? | 上次同步时间戳 |
| familyId | String? | 归属家庭（离线未选家庭时可为空） |
| retryCount | Int | 推送失败重试次数 |
| nextRetryAt | Long | 下次重试时间戳 |
| lastError | String? | 最近一次失败原因（截断 500 字符） |

**sync_cursors**（复合主键，替代旧文档的全局 lastSyncAt 时间戳）：

| 字段 | 类型 | 说明 |
|------|------|------|
| familyId | String | PK 一部分 |
| tableName | String | PK 一部分 |
| lastVersion | Long | 已应用的最大服务端 `sync_version` |

### 服务层与数据归属

```
┌─────────────────┐             Supabase 侧
│  AuthService     │  ─────────  auth.users (Supabase Auth 管理)
│  (登录/用户信息)   │             无 Room 本地 User 表
└─────────────────┘             昵称存 SharedPreferences

┌─────────────────┐             Supabase 侧
│  FamilyService   │  ─────────  families 表
│  (家庭/成员管理)   │              family_members 表
└─────────────────┘             无 Room 本地表，数据仅供云端

┌─────────────────┐             Room 本地 + Supabase 云端
│  SyncEngine      │  ─────────  9 张同步表双向 sync
│  (双向增量同步)    │             LWW 冲突策略
└─────────────────┘             sync_metadata / sync_cursors 跟踪状态
              ▲
              │ push 时 injectFamilyId() 注入 family_id，
              │ pull 时按 family_id 过滤、按 sync_version 增量
              │
┌─────────────┴───┐
│  currentFamilyId  │  ← FamilyService.currentFamily 传入
└─────────────────┘
```

### 同步数据流

```
┌──────────────────────────────────────────────────────────────┐
│                    sync_metadata 状态机                         │
│                                                               │
│  本地 id=5 ──── uuid="abc-123" ──── syncStatus="synced"      │
│  本地 id=7 ──── uuid="xyz-789" ──── syncStatus="pending"     │
│                                                               │
│  id 是本地自增，永不传递到云端                                   │
│  uuid 是全局唯一标识，连接本地与云端                              │
└──────────────────────────────────────────────────────────────┘

上行 (push):
  查询 syncStatus=pending 的 metadata → 查本地 Entity
  → entityToJson → injectFamilyId → 按 uuid 查云端：
    云端更新且较新 → applyRemoteChange 拉回本地
    否则 upsert (onConflict=uuid) 或 insert
  → markSynced
  失败 → markRetry (retryCount++ / nextRetryAt / lastError)

下行 (pull):
  按表读取 sync_cursors 游标
  → filter eq("family_id", fid) + gt("sync_version", lastVersion)
    order sync_version ASC, limit 500 分页
  → applyRemoteChange（本地 LWW 比较后决定是否覆盖）
  → 整页全部落库成功才推进 sync_cursors 游标

存量标记 (markExistingPending):
  为同步前已存在的本地数据补 uuid → 插入 sync_metadata (pending)

无归属数据认领 (claimUnscopedData):
  用户确认后，把 familyId IS NULL 的宝宝及子记录
  归入目标家庭并标记 pending（同步元数据同事务修正）
```

### 同步核心原则（Local-First）

```
┌─────────────────────────────────────────────────────────────┐
│                    同步五原则                                 │
│                                                              │
│  ① 写优先落本地      ② 离线队列 + 有网上传                   │
│    所有 CUD 先写         syncStatus=pending 排队             │
│    Room，再标记         push() 顺序 upsert                  │
│    pending               → markSynced 清除标记               │
│                                                              │
│  ③ 增量拉取           ④ 软删除                              │
│    sync_version > 游标    deletedAt 时间戳                   │
│    按表分页 500 条        真删会导致同步丢失删除指令          │
│                                                              │
│  ⑤ 实时推送                                               │
│     RealtimeManager 监听 Supabase 变更                      │
│     → applyRemoteChange() 写本地 Room                       │
│                                                              │
│  ★ 无需部署额外同步服务，全部在 App 内实现                    │
└─────────────────────────────────────────────────────────────┘
```

### 数据一致性设计

| 机制 | 实现 |
|------|------|
| 冲突解决（LWW） | 每次变更写入 `updatedAt`（epoch milli），pull/applyRemoteChange 比较本地与云端 updatedAt，旧者让位；push 时若云端较新则拉回本地而非盲目覆盖 |
| 软删除 | 删除只写 `deletedAt` 时间戳，永不物理删除；真删会丢失删除指令导致删除无法同步 |
| uuid 防重复 | 业务表 `uuid` 作为云端唯一键（onConflict=uuid upsert）；本地 sync_metadata 唯一索引 `(tableName, localId)` 保证一条记录只有一条同步元数据 |
| uuid 生成 | 新增记录在 SyncEngine 内用 `uuidgen()`；存量数据 markExistingPending 用 SQL `lower(hex(randomblob(16)))` 补 uuid（⚠️ 非标准 UUID 格式，仅保证唯一，不参与格式校验） |
| 上游一致性 | pull 整页（500 条）全部落库成功才推进 sync_cursors，部分失败不丢游标，下轮重试 |
| 幂等 upsert | 按 uuid 查本地：存在则保留原本地 id 只 update 字段（避免外键断裂），不存在才 insert |

---

## 多家庭 + 多账户（已落地）

原"待实施"改造已全部完成：

| 落地项 | 实现 | 版本 |
|--------|------|------|
| babies 增加归属字段 | `BabyEntity.familyId: String?`（列名用 `familyId`，**未采用** ownerId/groupId 命名） | v7 |
| sync_metadata 重构 | 重建表，新增 familyId/retryCount/nextRetryAt/lastError，加唯一索引 `(tableName, localId)`，清空 messages 的同步元数据 | v7 |
| 家庭级游标 | 新增 sync_cursors 表，按 `(familyId, tableName)` 独立记录服务端 sync_version 游标（替代全局 lastSyncAt 时间戳） | v7 |
| 多账户家庭会话隔离 | AI 会话表含 familyId，`ai_conversations` 查询按当前家庭+宝宝过滤，禁止跨作用域读取 | 1.5.18 |
| 无归属数据显式认领 | `claimUnscopedData(familyId)`（SyncEngine.kt:398-435）：用户确认后把 `familyId IS NULL` 的宝宝及子记录归入目标家庭，同步元数据同事务修正并重置重试计数 | — |
| 家庭切换全量重拉 | `resetLastSync()` 清除对应家庭游标，下次 pull 全量拉取 | — |

**未实现（如实记录）：**

- `watchVisible(userId, groupIds)` 可见性查询：**未实现**。多账户共享依赖 Supabase RLS 的 `is_family_member(family_id)` 策略与 `family_id` 过滤，客户端不做角色级可见性筛选。
- 家庭切换的云端数据清理策略未做（旧家庭数据保留在本地，靠 family_id 隔离查询）。

### 家庭归属推导规则

- 本地仅 `babies` 表有 `familyId` 列；其余 8 张记录表**没有** familyId 列，归属靠 `JOIN babies ON babies.id = t.baby_id` 推导。
- push 时 `injectFamilyId()` 把 `currentFamilyId` 写入 JSON payload 的 `family_id` 字段，满足 RLS 家庭隔离策略（否则触发 42501）。
- pull / Realtime 时以 `eq("family_id", fid)` 过滤云端行，落库后写入 sync_metadata.familyId。
- 离线未选家庭时创建的宝宝 `familyId = null`，不参与同步，等待 `claimUnscopedData` 认领。

---

## Migration 历史（version 6 → 8）

| 迁移 | 内容 | 状态 |
|------|------|------|
| MIGRATION_1_2 | 建 diapers | ✅ |
| MIGRATION_2_3 | 建 messages | ✅ |
| MIGRATION_3_4 | 建 development_assessments | ✅ |
| MIGRATION_4_5 | 建 reminders | ✅ |
| MIGRATION_5_6 | 10 表加 uuid/updatedAt/deletedAt + 建 sync_metadata（旧版 6 字段） | ✅ |
| MIGRATION_6_7 | babies +familyId；重建 sync_metadata（10 业务字段 + 唯一索引）；建 sync_cursors；清除 messages 同步元数据 | ✅ |
| MIGRATION_7_8 | 建 ai_conversations / ai_messages（含复合索引与 FK 级联） | ✅ |

> `exportSchema = true`，schema JSON 导出在 `app/schemas/com.babytracker.core.database.AppDatabase/8.json`。

---

## 设计决策

| 决策 | 原因 |
|------|------|
| family/groups 不入 Room | 数据量小，FamilyService API 实时查即可 |
| 无本地 User 表 | Supabase Auth 管理身份，SharedPreferences 缓存昵称 |
| records 挂 babyId 而非 record_books | App 语义就是宝宝记录，无需额外抽象 |
| messages 不挂 babyId | App 级消息，不属于特定宝宝；**不参与同步**（RLS 是 user 级，推送必 42501） |
| ai 两表仅本机 | AI 会话历史含家庭语境与安全字段，不出设备、不参与同步 |
| ids 自增 + uuid 全局 | 多设备间 id 可能相同但不冲突，uuid 全局唯一 |
| DAO/Repository 合一文件 | 减少文件碎片，小团队够用 |
| syncStatus 做 dirty 标记 | 无需额外 `_dirty` 列，sync_metadata.syncStatus 即队列 |
| familyId 列名而非 ownerId/groupId | 实际落地采用的命名，与 Supabase RLS 的 family_id 策略对齐 |

---

## 数据模型维护约定

- 当前基线：**15 张表 / Room version 8**。新增 @Entity 必须同步提供 Migration（AppDatabase.kt 注册），并提升 version。
- 新增表进同步前，先确认该表在 Supabase 的 RLS 策略与同步机制一致（必须有 `family_id` 且策略为 `is_family_member(family_id)`），并把表名加入 SyncEngine 的 `syncedTables`、`getEntityDao()`、`markExistingPending()` 三处。
- 从同步摘除某表时，必须在 `markExistingPending()` 开头 `DELETE FROM sync_metadata WHERE tableName='<表名>'` 清理存量，避免 conflict→pending 死循环。
- 改动表结构/新增表后，同步更新本文件与 `docs/project-structure.md`。
