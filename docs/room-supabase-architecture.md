# Room + Supabase 架构实现文档

> 最后更新：2026-08-12 · 对应版本：1.8.0 · 项目：BabyTracker Android App

---

## 架构总览

UI（Compose + ViewModel）→ Repository（Entity↔Domain 映射 + SyncMeta 写入）→ Room(SQLite) ◄──► Supabase（Postgrest / Auth / Realtime / Storage）。同步链路：`SyncEngine`(push/pull) + `RealtimeManager` + `AuthService` + `FamilyService`。

**核心设计原则**：
- **Local-First（本地优先）**：所有读写首先经过 Room SQLite，UI 不直连 Supabase
- **登录可选、同步受限**：未登录时纯本地使用；云同步/Realtime 从 1.5.18 起要求**已验证用户 + 家庭成员关系**（`verifiedFamilyForSync`），比"登录即可同步"更严
- **软删除**：所有业务表用 `deletedAt` 标记删除，不做物理删除
- **LWW 冲突解决**：比较 `updatedAt`（epoch milli），时间戳较新的覆盖
- **messages 不参与同步**（1.5.11）：站内信为 App 级私有数据，仅存本机

---

## Room 本地数据库

### 数据库文件（`core/database/AppDatabase.kt`）

| 属性 | 值 |
|------|-----|
| 文件名 | `babytracker.db`；当前版本 **9**，Schema 导出 **开启**（`exportSchema = true`） |
| 单例模式 | `@Volatile` + `synchronized` 双重检查锁；迁移 `MIGRATION_1_2` ~ `MIGRATION_8_9` 共 **8 个**（AppDatabase.kt） |
### 15 张实体表

| # | 表名 | 主键 | 说明 |
|---|------|------|------|
| 1 | `babies` | `id: Int` auto | 宝宝基本信息，含 `familyId` 家庭隔离字段 |
| 2-7 | `feedings` / `sleeps` / `growths` / `vaccinations` / `health_records` / `diapers` | `id: Int` auto | 六张宝宝记录表 |
| 8 | `backup_config` | `id: Int` auto | WebDAV 备份配置（单条，REPLACE） |
| 9 | `messages` | `id: Long` auto | 消息中心（App 级，**不参与同步**） |
| 10 | `development_assessments` | `id: Int` auto | 发育评估，FK `baby_id` (CASCADE) |
| 11 | `reminders` | `id: Int` auto | 提醒中心，FK `baby_id` (CASCADE) |
| 12 | `sync_metadata` | `id: Int` auto | 同步元数据（不参与同步） |
| 13 | `sync_cursors` | 复合 PK `(familyId, tableName)` | 每家庭每表同步游标（v7 起） |
| 14 | `ai_conversations` | `id: Long` auto | AI 会话（v8 起，**仅本机不同步**），FK `baby_id` |
| 15 | `ai_messages` | `id: Long` auto | AI 消息（v8 起，**仅本机不同步**），FK `conversation_id` |

### 同步三件套（Supabase 兼容字段）

除 `backup_config`、`sync_metadata`、`sync_cursors`、`ai_*` 外，9 张参与同步的业务实体都包含：`uuid: String?`（客户端生成）、`updatedAt: Long`（epoch milli）、`deletedAt: Long?`（非空 = 已软删除）。`BabyEntity` 额外含 `familyId: String?`（离线未选家庭时可为空）。

**字段类型约定**：业务表主键 `Int` auto；`messages` / `ai_*` 主键 `Long` auto；时间戳一律 `Long`（epoch milli）；日期字符串 `String`（ISO）；布尔 `Boolean`；外键 `Int` 与 `BabyEntity.id` 一致。**无 TypeConverter**，全部使用 Room 原生类型。

### 迁移历史

| 迁移 | 版本 | 变更 |
|------|------|------|
| `MIGRATION_1_2` | 1→2 | 创建 `diapers` 表 |
| `MIGRATION_2_3` | 2→3 | 创建 `messages` 表 |
| `MIGRATION_3_4` / `MIGRATION_4_5` | 3→4 / 4→5 | 创建 `development_assessments` / `reminders`（含外键 + 索引） |
| `MIGRATION_5_6` | 5→6 | **Supabase 同步改造**：10 张表加 `uuid`/`updatedAt`/`deletedAt`；创建旧版 `sync_metadata`（6 字段） |
| `MIGRATION_6_7` | 6→7 | `babies` 加 `familyId`；重建 `sync_metadata`（新列 `familyId`/`retryCount`/`nextRetryAt`/`lastError`，按 `(tableName, localId)` 去重迁移 + 唯一索引）；**删除 messages 存量同步记录**；创建 `sync_cursors` |
| `MIGRATION_7_8` | 7→8 | 创建 `ai_conversations` / `ai_messages` 两表（含索引），仅本机 |
| `MIGRATION_8_9` | 8→9 | 六表补 `FOREIGN KEY(baby_id) REFERENCES babies(id)`（NO ACTION，无 CASCADE）+ `Index(baby_id)`；babies 补 `Index(familyId)`；**孤儿清理**（无归属宝宝记录物理删除 + WARN 日志；曾为抛异常导致真机启动闪退，已改自动清理）；sync_metadata / sync_cursors 零改动 |

### 14 个 DAO 接口（`core/database/dao/Daos.kt`）

**统一接口模式**（9 张同步业务表 DAO）：`watchByBaby(babyId)`（过滤 `deletedAt IS NULL`）/ `getById` / `getByUuid` / `softDeleteByUuid(uuid, deletedAt, updatedAt)` / `insert` / `update` / `delete`。

**特殊 DAO**：
- `MessageDao`：`Long` 主键，`markRead(id)` / `markAllRead()`
- `VaccinationDao`：查询按 `CASE WHEN status = 'pending'` 优先排序；`ReminderDao`：`watchPending` / `watchHistory`
- `BackupConfigDao`：单条记录，`@Insert(onConflict = REPLACE)`
- `BabyDao`：额外 `watchByFamily(familyId)` / `watchUnscoped()`（家庭隔离）
- `AiHistoryDao`：AI 会话/消息 CRUD，`replaceMessages` 事务内先删后插
- `SyncMetadataDao` / `SyncCursorDao`：见下

### SyncMetadataDao（1.7.8 重构后）

```kotlin
getPendingChanges(familyId, now)              // pending 且 nextRetryAt <= now，按 updatedAt 升序
pendingCount(familyId) / getByTableAndId(tableName, localId)
insert(entity)                                // @Insert(onConflict = IGNORE)
markSynced(id, remoteUuid, updatedAt)         // 重置 retryCount/nextRetryAt/lastError
markRetry(id, nextRetryAt, error)             // retryCount+1，指数退避；≥5 次置 conflict
getByRemoteUuid(tableName, remoteUuid) / updatePending(...)   // 反查 / 复用既有行
updateByTableAndId(tableName, localId, remoteUuid, syncStatus, updatedAt, lastSyncAt, familyId)
```

> 已删除（1.7.11）：`getLastSyncAt` / `updateLastSyncAt` / `clearLastSyncAt`（增量锚点改为 `sync_cursors`）；已删除（1.7.8）：`markConflict`（改为 `markRetry` 指数退避）。

**SyncMetadataEntity（11 字段 + 唯一索引 `(tableName, localId)`）**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Int` auto | 主键 |
| `tableName` / `localId` | `String` / `Int` | 表名 + Room 本地 id |
| `remoteUuid` | `String?` | Supabase 的 UUID |
| `syncStatus` | `String` | `pending` / `synced` / `conflict` |
| `updatedAt` / `lastSyncAt` | `Long` / `Long?` | 记录更新时间戳 / 上次落库时间戳 |
| `familyId` / `retryCount` / `nextRetryAt` / `lastError` | — | 家庭隔离 / 失败次数（≥5 → conflict）/ 退避时间窗 / 失败原因 |

**SyncCursorDao**：`get(familyId, tableName)` / `set(SyncCursorEntity)`（REPLACE）/ `clear(familyId)`。

---

## Supabase 云端集成

### 客户端初始化（`core/sync/SupabaseProvider.kt`）

```kotlin
val client: SupabaseClient by lazy {
    createSupabaseClient(PROJECT_URL, PUBLIC_API_KEY) {
        install(Postgrest)                     // REST API
        install(Auth) { autoLoadFromStorage = true; alwaysAutoRefresh = true }
        install(Realtime)                      // WebSocket
        install(Storage)                       // 文件存储（头像/附件）
    }
}
```

`lazy` 延迟初始化，全局单例；安装 **4 个模块**：Postgrest、Auth、Realtime、Storage。只存放客户端可公开的 anon key，严禁放置 service_role / secret key。

### 依赖版本（gradle/libs.versions.toml）

| 依赖 | 版本 |
|------|------|
| Supabase BOM | `3.6.0`（`io.github.jan-tennert.supabase:bom`）；依赖：`postgrest` / `realtime` / `auth` / `storage` |
| Ktor Client | `3.5.1`（`ktor-client-android` / `ktor-client-okhttp`，Supabase 底层 HTTP 引擎） |

### Koin DI 模块（`core/di/Modules.kt`）

```kotlin
val syncModule = module {
    single { SupabaseProvider.client }
    single { AuthService(get(), get()) }
    single { FamilyService(get(), get()) }          // SupabaseClient + SharedPreferences（Modules.kt:97）
    single { SyncEngine(get(), get()) }
    single { RealtimeManager(get(), get(), get()) }
    // + SyncTrigger、OkHttpClient、AI 配置链路
}
// databaseModule：AppDatabase.get(androidContext()) + 14 个 DAO 全部注册（babyDao ~ aiHistoryDao）
```

---

## 双向同步引擎

**文件**：`core/sync/SyncEngine.kt`

### 同步策略

| 策略 | 说明 |
|------|------|
| **上行 Push** | 扫描 pending 且已过退避期的记录 → Entity→JSON → **先远端 LWW 预检**，远端较新则拉回，否则 `upsert`（onConflict=uuid） |
| **下行 Pull** | **`sync_version` 游标分页**（每家庭每表），500 行/页，**整页成功才推进游标** |
| **冲突解决** | **LWW**：比较 `updatedAt`，新的覆盖旧的 |
| **失败退避** | `markRetry`：`(1L shl retryCount) * 5s` 指数退避，连续失败 ≥5 次置 `conflict` |
| **家庭隔离** | pull 按 `family_id` 过滤，push 时 `injectFamilyId()` 注入 |

**同步启动门禁**（SyncTrigger / SyncWorker）：`authService.verifiedUserId() != null` 且 `sessionState.verifiedFamilyForSync?.id != null`，否则直接跳过。

### Push 流程

```
1. fid = currentFamilyId，为空直接返回
2. getPendingChanges(fid, now) 取未过退避期的 pending 记录
3. 每条记录：a. 取 EntityDao + 按 localId 加载 Room Entity
             b. entityToJson() + injectFamilyId() 注入 family_id
             c. 有 remoteUuid 时先 SELECT 预检（eq uuid + eq family_id）：
                远端存在且 remoteUpdatedAt >= localUpdatedAt → applyRemoteChange() 拉回，不覆盖
                否则 → upsert（onConflict = "uuid"）；无 remoteUuid → insert
             d. markSynced()（重置 retryCount/nextRetryAt/lastError）
4. 单条异常 → markRetry(退避时间窗, 错误信息)，继续下一条
```

### Pull 流程

```
1. 逐表处理（syncedTables，9 张）：
   a. pageCursor = syncCursor.get(fid, tableName) ?: 0
   b. SELECT WHERE family_id = fid AND sync_version > pageCursor，ORDER BY sync_version ASC，LIMIT 500
   c. 逐行 applyRemoteChange()；整页全部成功才 committedSyncCursor() 推进游标
   d. 拉满 500 行继续下一页；超时(30s)/异常记入 failures
```

> 全量重拉：`resetLastSync()` = `syncCursor.clear(fid)`（旧版 `clearLastSyncAt` 已删除）。

### applyRemoteChange() — 远程变更写入本地

```
1. 按 remoteUuid 查本地记录，比较 updatedAt（LWW）：本地更新 → 忽略
2. 远程更新或本地不存在 → dao.upsert(json)（按 uuid 查本地：存在则 update 保留原 id，否则 insert）
3. 写 sync_metadata：先 getByTableAndId 查既有行（1.7.7 起"先查后改"）：
   有 → updateByTableAndId（保留原 id，避免 push 持有旧 id 空匹配）；无 → insert("synced")
```
### Entity ↔ JSON 转换

- push 方向：`babyId(Int)` → `babyUuid(String)`（子表引用云端宝宝 UUID）；pull 方向：`resolveLocalBabyId(uuid)` 反查本地 `baby_id`
- 所有 JSON 解析统一用 `jsonStr()`（`(json[x] as? JsonPrimitive)?.content`），避免 `JsonNull.toString()` 产生字符串 "null"（lessons.md #6）

### 状态与辅助

```kotlin
enum class SyncState { IDLE, SYNCING, PUSHING, PULLING }
val syncState: StateFlow<SyncState>
suspend fun fullSync(): SyncRunResult          // push + pull（fullSyncMutex / pushPullMutex 互斥）
suspend fun markExistingPending(): List<SyncFailure>   // 存量数据首次同步标记
suspend fun claimUnscopedData(familyId: String): Int   // 无归属宝宝归入当前家庭
private class EntityDao<T>(val getById: suspend (Int) -> T?,  // 按本地 ID 查
    val getByUuid: suspend (String) -> T?,                    // 按 UUID 查
    val upsert: suspend (JsonObject) -> Long)                 // 写入 Room；1.7.11 已删 updateLocal
```

> `markExistingPending()` 开头会 `DELETE FROM sync_metadata WHERE tableName='messages'`，兜底清理 messages 同步痕迹（lessons.md #3）。

---

## Repository 层

**文件**：`core/data/repository/Repositories.kt`

### 统一操作模式

```kotlin
val e = entity.copy(uuid = entity.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
val id = dao.insert(e)
val updated = syncMeta.updatePending("feedings", id.toInt(), e.uuid, e.updatedAt, currentFamilyId)
if (updated == 0) syncMeta.insert(SyncMetadataEntity(/* status = pending */))  // 无既有行才插
```

### 关键设计决策

1. **每次写操作同时更新 `sync_metadata`**：优先 `updatePending` 复用既有行（IGNORE 策略防重复）
2. **UUID 在客户端生成**：`UUID.randomUUID()`，不上线也有全局唯一键
3. **软删除不物理删除**：`deletedAt` 标记，DAO 用 `update()` 而非 `delete()`；`BabyRepository.delete()` 级联软删除 8 张子表
4. **messages 例外**：`MessageRepositoryImpl(get())` 只注入 MessageDao，**不写 sync_metadata**（1.5.11 消息中心退出云同步）

### Entity ↔ Domain 映射（`core/data/mapper/Mappers.kt`）

```
Room Entity ←→ Domain Model：(Long) ↔ (LocalDateTime) 时间转换（如 MessageEntity.createTime）
                             (String) ↔ (Enum) 枚举转换（如 FeedingEntity.type ↔ FeedingType）
```

---

## Realtime 实时监听

**文件**：`core/sync/RealtimeManager.kt`

```
Supabase PostgreSQL ──WebSocket──> RealtimeManager
    INSERT/UPDATE → applyRemoteChange()（LWW 写入 Room）   DELETE → softDeleteLocal()（按 uuid 软删除）
    SELECT → 忽略   family_members → 发射 familyMembersChanged（不写 Room）
                        │ ▼ Room SQLite → Flow 触发 UI 刷新
```

### 订阅范围（RealtimeManager.kt:66-70）

监听 **9 张业务表 + 1 张 `family_members`**，**无 `messages`**：

```kotlin
val tables = listOf("babies", "feedings", "sleeps", "growths", "vaccinations",
    "health_records", "diapers", "development_assessments", "reminders",
    "family_members")  // 仅通知 ViewModel 刷新，不写入 Room
```

每张表都带 `filter("family_id", EQ, familyId)`，非当前家庭的事件会被丢弃。

### 状态与频道管理

```kotlin
enum class RealtimeState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }
val connectionState: StateFlow<RealtimeState>; val familyMembersChanged: SharedFlow<Unit>
```

- **切换家庭/重新订阅时**：`subscribeAll()` 内部**先 `disconnect()` 再建新频道**（先取消旧 changeJobs、unsubscribe，避免订阅冲突和 RLS 上下文过期）
- **频道名称**：`"db-changes-$familyId"`（按家庭隔离）
- **连接方式**：`ch.subscribe(blockUntilSubscribed = true)`，`withTimeout(10_000L)` 超时降级为 DISCONNECTED

---

## 认证模块

**文件**：`core/auth/AuthService.kt`

### 设计原则

```
登录可选、本地优先：未登录 → 纯本地；已登录 → 云同步 + 家庭共享
双轨登录态：currentUser（session 合并 SP 缓存，断网回退，UI 用）；verifiedUser（仅会话确认，云同步只能用该值，1.5.18 起）
```

### 账户体系

```
账户名 "zhangsan" → toEmail() 拼接虚拟域名 → "zhangsan@baby-tracker.app"
  → Supabase Email Auth → signUpWith(Email) / signInWith(Email)
```

### API

| 方法 | 说明 |
|------|------|
| `signUp(account, password)` / `signIn(account, password)` | 注册/登录（自动拼接虚拟域名，返回 `Result<UserInfo>`） |
| `signOut()` | 登出（清除 Supabase 会话 + SharedPreferences） |
| `isLoggedIn()` / `hasCachedSession()` | 是否已登录（缓存态）/ SP 是否有缓存记录（离线兜底） |
| `currentUserId()` / `verifiedUserId()` | 当前用户 ID（缓存态）/ 仅会话确认的用户 ID（**同步门禁用**） |
| `setNickname(name)` / `clearNickname()` | 设置/清除昵称（本地 SP） |
| `observeAuthState()` / `observeVerifiedAuthState()` | 监听登录状态（缓存合并流 / 已验证流） |

### 状态持久化

- Supabase Auth 内置：`access_token` / `refresh_token`（`autoLoadFromStorage` + `alwaysAutoRefresh`）
- SharedPreferences：`auth_logged_in` / `auth_user_id` / `auth_display_account` / `auth_nickname`（双重保障 + UI 展示）

### 关键配置

> 需在 Supabase Dashboard → Authentication → Settings 关闭 **"Confirm email"**，否则注册后需邮箱确认才能登录。

---

## 家庭共享

**文件**：`core/data/FamilyService.kt`

### 数据模型

```kotlin
data class Family(val id: String = "", val name: String = "",
    @SerialName("invite_code") val inviteCode: String = "",
    @SerialName("created_by") val createdBy: String? = null)
data class FamilyMember(@SerialName("family_id") val familyId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "member",               // "owner" / "member"
    @SerialName("joined_at") val joinedAt: String? = null)
```
### 设计要点

- **数据仅存于 Supabase**，不在 Room 本地存储；SP 缓存只负责离线展示，**不能驱动同步**
- **RLS**：业务表策略为 `is_family_member(family_id)`（服务端 SQL），所有请求必须携带合法 `family_id`
- **加入家庭**通过 `SECURITY DEFINER` 函数绕过 RLS：`client.postgrest.rpc("join_family", mapOf("invite_code" to code))`（FamilyService.kt:203）
- **邀请码客户端生成**：`SecureRandom` 生成 6 位大写字母+数字
- **同步门禁 `verifiedFamilyForSync`**：`sessionVerified && families 包含当前选中家庭` 才返回家庭 ID，否则同步/Realtime 不启动（SyncWorker.kt:26-32、SyncTrigger.kt:123）

### API 与状态

| 方法 | 说明 |
|------|------|
| `createFamily(name)` | 创建家庭 + 写入 `family_members`(owner) + 选中 |
| `joinFamily(inviteCode)` | RPC `join_family` 通过邀请码加入（忽略大小写匹配） |
| `refreshForUser(userId)` | 用已验证会话刷新家庭列表（1.7.11 起；旧 `loadMyFamilies()` 已删除），请求版本号防过期覆盖 |
| `selectFamily(family)` / `selectLocalMode()` | 切换家庭 / 本地模式 |
| `getFamilyMembers(familyId)` | 获取家庭成员（要求已验证成员关系） |
| `restoreCachedForUser(userId)` / `clearSession()` | 恢复账号专属缓存 / 清空会话状态 |
```kotlin
val sessionState: StateFlow<FamilySessionState>   // 唯一来源
val myFamilies: StateFlow<List<Family>>           // 派生
val currentFamily: StateFlow<Family?>             // activeFamily（本地模式下为 null）
```
---

## 数据流全景图

### 完整数据写入链路

```
用户操作 (Screen) → ViewModel.CRUD → Repository.insert/update/delete()
    ├── 1. 生成 UUID（如有需要）  2. updatedAt = now  3. Domain → Entity（Mapper）  4. Room DAO 写入
    └── 5. syncMeta.updatePending()/insert(status = "pending") → SyncEngine.push()（LWW 预检 → upsert）→ markSynced()
```

### 云端变更接收链路

```
另一设备写入 Supabase
    ├─── 方式 1：Realtime WebSocket 实时推送 ───┐
    │    RealtimeManager → SyncEngine.applyRemoteChange()（LWW） → Room → Flow → UI
    └─── 方式 2：定时/手动 SyncEngine.pull() ────┘（sync_version 游标分页，500 行/页，整页成功才推进）
```

### 同步数据映射

| Room 表 | 同步方向 | 家庭隔离 | 说明 |
|---------|---------|---------|------|
| `babies` | 双向 | family_id + 本地 familyId | 本地额外 `watchUnscoped` 支持 |
| 其余 8 张业务表（feedings ~ reminders） | 双向 | family_id | 经宝宝归属家庭 |
| `messages` | **不同步**（1.5.11） | — | 本地私有；不订阅 Realtime、不写 pending、迁移时清理存量 |
| `backup_config` / `sync_metadata` / `sync_cursors` | 不同步 | — | 本机配置 / 同步引擎内部状态 |
| `ai_conversations` / `ai_messages` | **不同步**（v8） | — | 仅本机；按 (family, baby) 隔离 |
### 文件索引

| 模块 | 文件路径 |
|------|---------|
| **Entities** | `core/database/Entities.kt`（无 entity/ 子目录） |
| **DAOs** | `core/database/dao/Daos.kt` |
| **AppDatabase** | `core/database/AppDatabase.kt` |
| **Mappers** | `core/data/mapper/Mappers.kt` |
| **Repositories** | `core/data/repository/Repositories.kt` |
| **DI Modules** | `core/di/Modules.kt` |
| **SupabaseProvider** | `core/sync/SupabaseProvider.kt` |
| **SyncEngine** | `core/sync/SyncEngine.kt` |
| **RealtimeManager** | `core/sync/RealtimeManager.kt` |
| **SyncTrigger / SyncWorker** | `core/sync/SyncTrigger.kt`、`core/sync/SyncWorker.kt` |
| **AuthService** | `core/auth/AuthService.kt` |
| **FamilyService** | `core/data/FamilyService.kt` |
