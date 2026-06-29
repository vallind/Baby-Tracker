# 数据架构图

> 最后更新：2026-06-29 · Room DB version 6

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
│  │ 同步数据表 (10 张)                            ★ 均含 family_id ││
│  │                                                              ││
│  │  babies  ──┐                                                 ││
│  │            ├── feedings                                      ││
│  │            ├── sleeps                                        ││
│  │            ├── growths           ★ 通过 babyId(uuid) 关联     ││
│  │            ├── vaccinations                                   ││
│  │            ├── health_records                                 ││
│  │            ├── diapers                                        ││
│  │            ├── development_assessments (FK CASCADE)           ││
│  │            ├── reminders (FK CASCADE)                         ││
│  │            └── messages (无 babyId，App 级)                   ││
│  └──────────────────────────────────────────────────────────────┘│
└────────────────────────────┬─────────────────────────────────────┘
                             │  SyncEngine (push/pull, LWW)
                             │  sync_metadata 维护 id↔uuid 映射
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│                     📱 Room 本地数据库                             │
│                                                                  │
│  babies              ← 唯一容器，记录通过 babyId 挂载              │
│  ├── id(PK)  name  gender  birthDate  birthWeight  birthHeight   │
│  ├── avatarPath  createdAt                                        │
│  └── uuid  updatedAt  deletedAt          (同步字段)               │
│                                                                  │
│  ┌──────────────────────┬──────────────────────────────────────┐ │
│  │ 记录表 (8 张，含 babyId)              │ 无 babyId             │ │
│  │                                     │                      │ │
│  │ feedings          sleeps            │ messages             │ │
│  │ growths           vaccinations      │ (App 级消息中心)       │ │
│  │ health_records    diapers           │                      │ │
│  │ development_assessments ※FK baby_id │                      │ │
│  │ reminders ※FK baby_id             │                      │ │
│  │                                     │                      │ │
│  │ ★ 均含 uuid / updatedAt / deletedAt │                      │ │
│  └──────────────────────────────────────┴──────────────────────┘ │
│                                                                  │
│  ┌──────────────┐    ┌─────────────────┐                         │
│  │ backup_config│    │ sync_metadata    │                         │
│  │ (纯本地)      │    │ tableName        │                         │
│  │ webdavUrl... │    │ localId          │                         │
│  └──────────────┘    │ remoteUuid       │                         │
│                      │ syncStatus       │                         │
│                      │ updatedAt        │                         │
│                      │ lastSyncAt       │                         │
│                      └─────────────────┘                         │
└──────────────────────────────────────────────────────────────────┘
```

### 12 张表清单

| # | 表名 | PK类型 | 含 babyId | 有 FK | 同步字段 | 说明 |
|---|------|--------|-----------|-------|----------|------|
| 1 | `babies` | Int auto | — | — | uuid/updatedAt/deletedAt | 宝宝容器 |
| 2 | `feedings` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 喂养 |
| 3 | `sleeps` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 睡眠 |
| 4 | `growths` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 生长 |
| 5 | `vaccinations` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 疫苗 |
| 6 | `health_records` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 健康 |
| 7 | `diapers` | Int auto | ✅ | — | uuid/updatedAt/deletedAt | 尿布 |
| 8 | `development_assessments` | Int auto | ✅ | ✅ → babies.id | uuid/updatedAt/deletedAt | 发育评估 |
| 9 | `reminders` | Int auto | ✅ | ✅ → babies.id | uuid/updatedAt/deletedAt | 提醒 |
| 10 | `messages` | Long auto | ❌ | — | uuid/updatedAt/deletedAt | 消息中心 |
| 11 | `backup_config` | Int auto | — | — | ❌ 纯本地 | 备份配置 |
| 12 | `sync_metadata` | Int auto | — | — | ❌ 同步元数据 | id↔uuid 映射 |

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
│  SyncEngine      │  ─────────  10 张同步表双向 sync
│  (双向增量同步)    │             LWW 冲突策略
└─────────────────┘             sync_metadata 跟踪状态
              ▲
              │ injectFamilyId() 在 push 时注入，pull 时按 family_id 过滤
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
  查询 syncStatus=pending → 查本地 Entity → entityToJson → injectFamilyId
  → upsert to Supabase (onConflict=uuid) → markSynced

下行 (pull):
  filter eq("family_id", currentFamilyId) → gte("updatedAt", lastSyncAt)
  → 比较 localUpdatedAt vs remoteUpdatedAt (LWW) → upsert to Room
  → insert sync_metadata

存量标记 (markExistingPending):
  为同步前已存在的本地数据生成 uuid → 插入 sync_metadata
```

### 同步核心原则（Local-First）

```
┌─────────────────────────────────────────────────────────────┐
│                    同步五原则                                 │
│                                                              │
│  ① 写优先落本地      ② 离线队列 + 有网上传                   │
│    所有 CUD 先写         syncStatus=pending 排队             │
│    Room，再标记          push() 顺序 upsert                  │
│    pending               → markSynced 清除标记               │
│                                                              │
│  ③ 增量拉取           ④ 软删除                              │
│     WHERE updatedAt       deletedAt 时间戳                   │
│     >= lastSyncAt         真删会导致同步丢失删除指令          │
│                                                              │
│  ⑤ 实时推送                                               │
│     RealtimeManager 监听 Supabase 变更                      │
│     → applyRemoteChange() 写本地 Room                       │
│                                                              │
│  ★ 无需部署额外同步服务，全部在 App 内实现                    │
└─────────────────────────────────────────────────────────────┘
```

### 设计决策

| 决策 | 原因 |
|------|------|
| family/groups 不入 Room | 数据量小，FamilyService API 实时查即可 |
| 无本地 User 表 | Supabase Auth 管理身份，SharedPreferences 缓存昵称 |
| records 挂 babyId 而非 record_books | App 语义就是宝宝记录，无需额外抽象 |
| messages 不挂 babyId | App 级消息，不属于特定宝宝 |
| ids 自增 + uuid 全局 | 多设备间 id 可能相同但不冲突，uuid 全局唯一 |
| DAO/Repository 合一文件 | 减少文件碎片，小团队够用 |
| syncStatus 做 dirty 标记 | 无需额外 `_dirty` 列，sync_metadata.syncStatus 即队列 |

---

## 待实施：多家庭 + 多账户改造

### 目标

支持"多用户 → 多家庭 → 多宝宝"共享，核心改动：**babies 表增加归属字段**。

### 改动清单

| 层级 | 文件 | 改动 |
|------|------|------|
| Room `BabyEntity` | Entities.kt | +`ownerId: String` (创建者 user UUID) |
| | | +`groupId: String?` (null=私有, 非null=家庭共享) |
| Room BabysDao | Daos.kt | +`watchVisible(userId, groupIds)` 可见性查询 |
| Room BabysRepository | Repositories.kt | +`watchVisible()` 整合 FamilyService |
| Room AppDatabase | AppDatabase.kt | 版本 6→7 Migration |
| Supabase `babies` | SQL | +`owner_id UUID`, +`group_id UUID` |
| SyncEngine | SyncEngine.kt | entityToJson/parseBaby 增加二字段 |
| FamilyService | FamilyService.kt | +`myGroupIds(): List<String>` 便利方法 |

### 不需要改的

| 文件 | 原因 |
|------|------|
| 8 张记录 Entity | 继续挂 babyId，零改动 |
| 各 Screen/ViewModel | babyId 不变，DAOs 不变 |
| `record_books` 新表 | ❌ 不引入，babies 即容器 |
