# Room + Supabase 架构实现文档

> 版本：v1.0 / 更新日期：2026-06-29  
> 项目：BabyTracker Android App

---

## 目录

1. [架构总览](#架构总览)
2. [Room 本地数据库](#room-本地数据库)
3. [Supabase 云端集成](#supabase-云端集成)
4. [双向同步引擎](#双向同步引擎)
5. [Realtime 实时监听](#realtime-实时监听)
6. [认证模块](#认证模块)
7. [家庭共享](#家庭共享)
8. [数据流全景图](#数据流全景图)

---

## 架构总览

```
┌──────────────────────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose Screen + ViewModel + Domain)      │
├──────────────────────────────────────────────────────────────┤
│  Repository Layer (Entity ↔ Domain 映射 + SyncMeta 写入)    │
├──────────────────────┬───────────────────────────────────────┤
│   Room (SQLite)      │   Supabase (PostgreSQL)               │
│   ┌─────────────┐    │   ┌──────────────────────────────┐    │
│   │ 12 Entities │    │   │ Postgrest REST API            │    │
│   │ 12 DAOs     │◄──►│   │ Auth (Email/Pwd)             │    │
│   │ AppDatabase │    │   │ Realtime (WebSocket)          │    │
│   │ v6, 5 Migr. │    │   │ Storage (avatars/attachments) │    │
│   └─────────────┘    │   └──────────────────────────────┘    │
├──────────────────────┴───────────────────────────────────────┤
│  SyncEngine: push() + pull() + fullSync()                    │
│  RealtimeManager: WebSocket 变更监听 → 写入 Room             │
│  AuthService: 登录/注册/登出，本地优先不强制登录               │
│  FamilyService: 家庭创建/加入/成员管理（纯 Supabase）          │
└──────────────────────────────────────────────────────────────┘
```

**核心设计原则**：
- **Local-First（本地优先）**：所有读写首先经过 Room SQLite，UI 不直连 Supabase
- **登录可选**：未登录时所有数据存本地，登录后开启云同步和家庭协作
- **软删除**：所有业务表用 `deletedAt` 标记删除，不做物理删除
- **LWW 冲突解决**：比较 `updatedAt`（epoch milli），时间戳较新的覆盖

---

## Room 本地数据库

### 数据库文件

| 属性 | 值 |
|------|-----|
| 文件路径 | `core/database/AppDatabase.kt` |
| 文件名 | `babytracker.db` |
| 当前版本 | **6** |
| Schema 导出 | 关闭 |
| 单例模式 | `@Volatile` + `synchronized` 双重检查锁 |

### 12 张实体表

| # | 表名 | 实体类 | 主键 | 外键 | 说明 |
|---|------|--------|------|------|------|
| 1 | `babies` | `BabyEntity` | `id: Int` auto | — | 宝宝基本信息 |
| 2 | `feedings` | `FeedingEntity` | `id: Int` auto | `baby_id` | 喂养记录 |
| 3 | `sleeps` | `SleepEntity` | `id: Int` auto | `baby_id` | 睡眠记录 |
| 4 | `growths` | `GrowthEntity` | `id: Int` auto | `baby_id` | 生长记录 |
| 5 | `vaccinations` | `VaccinationEntity` | `id: Int` auto | `baby_id` | 疫苗记录 |
| 6 | `health_records` | `HealthRecordEntity` | `id: Int` auto | `baby_id` | 健康记录 |
| 7 | `diapers` | `DiaperEntity` | `id: Int` auto | `baby_id` | 尿布记录 |
| 8 | `messages` | `MessageEntity` | `id: Long` auto | — | 消息中心（App 级） |
| 9 | `development_assessments` | `DevelopmentAssessmentEntity` | `id: Int` auto | `baby_id` (CASCADE) | 发育评估 |
| 10 | `reminders` | `ReminderEntity` | `id: Int` auto | `baby_id` (CASCADE) | 提醒中心 |
| 11 | `backup_config` | `BackupConfigEntity` | `id: Int` auto | — | WebDAV 备份配置 |
| 12 | `sync_metadata` | `SyncMetadataEntity` | `id: Int` auto | — | 同步元数据 |

### 同步三件套（Supabase 兼容字段）

除 `BackupConfigEntity` 外，所有业务实体都包含：

```kotlin
val uuid: String? = null       // Supabase 云端 UUID
val updatedAt: Long = 0L       // 最后更新时间戳（epoch milli）
val deletedAt: Long? = null    // 软删除时间戳，非空 = 已删除
```

### 字段类型约定

| 数据类型 | Room 类型 | 说明 |
|---------|----------|------|
| 主键（业务表） | `Int` autoGenerate | 自增整数 |
| 主键（messages） | `Long` autoGenerate | 长整型 |
| 时间戳 | `Long` | epoch millisecond |
| 日期字符串 | `String` | ISO 格式 |
| 布尔值 | `Boolean` | Room 映射为 INTEGER 0/1 |
| 外键 | `Int` | 与 `BabyEntity.id` 类型一致 |

**无 TypeConverter**：所有字段使用 Room 原生支持的类型。

### 迁移历史

| 迁移 | 版本 | 变更 |
|------|------|------|
| `MIGRATION_1_2` | 1→2 | 创建 `diapers` 表 |
| `MIGRATION_2_3` | 2→3 | 创建 `messages` 表 |
| `MIGRATION_3_4` | 3→4 | 创建 `development_assessments` 表，含外键 + 索引 |
| `MIGRATION_4_5` | 4→5 | 创建 `reminders` 表，含外键 + 索引 |
| `MIGRATION_5_6` | 5→6 | **Supabase 同步改造**：为 10 张表添加 `uuid`/`updatedAt`/`deletedAt` 列；创建 `sync_metadata` 表 |

### 12 个 DAO 接口

所有 DAO 定义在 `core/database/dao/Daos.kt`（单文件）。

**统一接口模式**（10 张业务表 DAO）：

| 方法 | 返回 | 说明 |
|------|------|------|
| `watchByBaby(babyId)` | `Flow<List<Entity>>` | 按宝宝 ID 监听列表 |
| `getById(id)` | `Entity?` | 按本地 ID 查单条 |
| `getByUuid(uuid)` | `Entity?` | 按 Supabase UUID 查单条 |
| `softDeleteByUuid(uuid, deletedAt, updatedAt)` | — | 按 UUID 软删除 |
| `insert(entity)` | `Long` | 插入，返回 rowId |
| `update(entity)` | — | 更新 |
| `delete(entity)` | — | 物理删除 |

**特殊 DAO**：
- `MessageDao`：使用 `Long` 主键，提供 `markRead`/`markAllRead`
- `VaccinationDao`：查询按 `status = 'pending'` 优先排序
- `ReminderDao`：区分 `watchPending`/`watchHistory`
- `BackupConfigDao`：单条记录，`REPLACE` 策略
- `SyncMetadataDao`：同步状态管理（见下方）

### SyncMetadataDao

```kotlin
@Dao
interface SyncMetadataDao {
    suspend fun getPendingChanges(): List<SyncMetadataEntity>    // 获取所有 pending 变更
    suspend fun getByTableAndId(tableName, localId): Entity?     // 按表+ID 查同步状态
    suspend fun getLastSyncAt(): Long?                           // 获取上次同步时间（增量锚点）
    suspend fun insert(entity): Unit                             // REPLACE 策略
    suspend fun markSynced(id, remoteUuid, updatedAt): Unit      // 标记已同步
    suspend fun markConflict(id, updatedAt): Unit                // 标记冲突
    suspend fun updateLastSyncAt(lastSyncAt): Unit               // 更新全局同步时间戳
    suspend fun clearLastSyncAt(): Unit                          // 清除（全量拉取用）
    suspend fun getByRemoteUuid(tableName, remoteUuid): Entity?  // 按云端 UUID 反查
}
```

### SyncMetadataEntity 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Int` auto | 主键 |
| `tableName` | `String` | 表名："feedings", "sleeps", ... |
| `localId` | `Int` | Room 表的本地 id |
| `remoteUuid` | `String?` | Supabase 的 UUID |
| `syncStatus` | `String` | `pending` / `synced` / `conflict` |
| `updatedAt` | `Long` | 记录更新时间戳 |
| `lastSyncAt` | `Long?` | 全局上次同步时间戳 |

---

## Supabase 云端集成

### 客户端初始化

**文件**：`core/sync/SupabaseProvider.kt`

```kotlin
object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
            install(Postgrest)       // REST API
            install(Auth) {
                autoLoadFromStorage = true      // 启动自动恢复登录态
                alwaysAutoRefresh = true        // token 过期自动刷新
            }
            install(Realtime)        // WebSocket
            install(Storage)         // 文件存储（头像/附件）
        }
    }
}
```

**关键设计**：
- `lazy` 延迟初始化，全局单例
- Auth 配置 `autoLoadFromStorage = true`：App 重启自动恢复登录态
- Auth 配置 `alwaysAutoRefresh = true`：access token 过期自动刷新
- 安装了 **4 个模块**：Postgrest、Auth、Realtime、Storage

### 依赖版本

| 依赖 | 版本 |
|------|------|
| Supabase BOM | `3.6.0`（`io.github.jan-tennert.supabase:bom`） |
| Ktor Client | `3.5.1`（Supabase 底层 HTTP 引擎） |

**build.gradle.kts**：
```kotlin
implementation(platform(libs.supabase.bom))
implementation(libs.supabase.postgrest)
implementation(libs.supabase.realtime)
implementation(libs.supabase.auth)
implementation(libs.supabase.storage)
implementation(libs.ktor.client.android)
implementation(libs.ktor.client.okhttp)
```

### Koin DI 模块

**文件**：`core/di/Modules.kt`

```kotlin
val syncModule = module {
    single { SupabaseProvider.client }
    single { AuthService(get(), get()) }
    single { SyncEngine(get(), get()) }
    single { RealtimeManager(get(), get(), get()) }
    single { FamilyService(get()) }
}

val databaseModule = module {
    single { AppDatabase.get(androidContext()) }
    single { get<AppDatabase>().babyDao() }
    // ... 所有 12 个 DAO
}

val appModule = module {
    single<SharedPreferences> { ... }
    single<BabyRepository> { BabyRepositoryImpl(get(), get(), get()) }
    // ... 10 个 Repository + 10 个 ViewModel
}

// Application 入口
class BabyTrackerApp : Application() {
    override fun onCreate() {
        startKoin { modules(appModule, databaseModule, syncModule) }
    }
}
```

---

## 双向同步引擎

**文件**：`core/sync/SyncEngine.kt`

### 同步策略

| 策略 | 说明 |
|------|------|
| **上行 Push** | 扫描 `sync_metadata.syncStatus = 'pending'` → Entity→JSON → Postgrest `upsert`（onConflict=uuid） |
| **下行 Pull** | 根据 `lastSyncAt` 增量拉取 → `applyRemoteChange()` 写入 Room |
| **冲突解决** | **LWW**（Last-Write-Wins）：比较 `updatedAt`，新的覆盖旧的 |
| **家庭隔离** | pull 时添加 `family_id` 过滤，push 时自动注入 `family_id` |

### Push 流程

```
1. 读取 sync_metadata 中 syncStatus = 'pending' 的记录
2. 对每条 pending 记录：
   a. 通过 tableName 获取 EntityDao
   b. 按 localId 加载 Room Entity
   c. entityToJson() 序列化为 JSON
   d. injectFamilyId() 注入 family_id
   e. Postgrest upsert（onConflict = "uuid"）
   f. markSynced() 更新 sync_metadata
3. 异常时 markConflict() 标记冲突
4. 全部完成后 updateLastSyncAt()
```

### Pull 流程

```
1. 读取 lastSyncAt 增量锚点
2. 对每张业务表执行：
   a. Postgrest SELECT WHERE family_id = currentFamilyId AND updatedAt >= lastSyncAt
   b. 对每行远程数据调用 applyRemoteChange()
3. updateLastSyncAt()
```

### applyRemoteChange() — 远程变更写入本地

```
1. 按 remoteUuid 查找本地记录
2. 比较 updatedAt（LWW）：
   - 本地更新 → 忽略远程
   - 远程更新或本地不存在 → 写入 Room
3. 插入 sync_metadata（syncStatus = "synced"）
```

### Entity ↔ JSON 转换

SyncEngine 内部维护 10 张表的双向转换逻辑：

```
entityToJson():  BabyEntity → { "uuid": "...", "name": "...", ... }
                注意：babyId(Int) → babyUuid(String) 映射

parseXxx():      JsonObject → XxxEntity (id=0, babyId 通过 uuid 反查)
```

### 状态管理

```kotlin
enum class SyncState { IDLE, SYNCING, PUSHING, PULLING }
val syncState: StateFlow<SyncState>  // 供 UI 展示同步状态
```

### 内部辅助类

```kotlin
private class EntityDao<T>(
    val getById: suspend (Int) -> T?,        // 按本地 ID 查
    val getByUuid: suspend (String) -> T?,    // 按 UUID 查
    val upsert: suspend (JsonObject) -> Long, // 写入 Room
    val updateLocal: suspend (T) -> Unit,     // 更新本地记录
)
```

---

## Repository 层（数据仓库）

**文件**：`core/data/repository/Repositories.kt`

### 统一操作模式

每个 Repository 的 CRUD 操作都遵循同一模式：

```kotlin
// 插入
override suspend fun insert(entity: Domain): Long {
    val e = entity.copy(uuid = entity.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
    val id = dao.insert(e)
    syncMeta.insert(SyncMetadataEntity(
        tableName = "table_name",
        localId = id.toInt(),
        remoteUuid = e.uuid,
        syncStatus = "pending",
        updatedAt = e.updatedAt,
    ))
    return id
}

// 更新
override suspend fun update(entity: Domain) {
    val e = entity.copy(updatedAt = nowEpoch).toEntity()
    dao.update(e)
    syncMeta.insert(SyncMetadataEntity(/* ... */))
}

// 删除（软删除）
override suspend fun delete(entity: Domain) {
    val e = entity.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
    dao.update(e)
    syncMeta.insert(SyncMetadataEntity(/* ... */))
}
```

### 关键设计决策

1. **每次写操作同时更新 `sync_metadata`**：自动标记为 `pending`，同步引擎稍后推送
2. **UUID 在客户端生成**：`UUID.randomUUID().toString()`，不上线也能有全局唯一键
3. **软删除不物理删除**：`deletedAt` 标记，Room DAO 用 `update()` 而非 `delete()`
4. **宝宝级联软删除**：`BabyRepository.delete()` 同时软删除该宝宝下的 8 张子表（feedings/sleeps/growths/...）

### Entity ↔ Domain 映射

**文件**：`core/data/mapper/Mappers.kt`

```
Room Entity  ←→  Domain Model
  (Long)           (LocalDateTime)   ← 时间类型转换
  (String)         (Enum)            ← 枚举类型转换
```

例如：
- `MessageEntity.createTime: Long` ↔ `AppMessage.createTime: LocalDateTime`
- `FeedingEntity.type: String` ↔ `Feeding.type: FeedingType`（枚举）

---

## Realtime 实时监听

**文件**：`core/sync/RealtimeManager.kt`

### 工作原理

```
Supabase PostgreSQL ──WebSocket──> RealtimeManager
                                       │
                            ┌──────────┼──────────┐
                            │          │          │
                         INSERT     UPDATE     DELETE
                            │          │          │
                            ▼          ▼          ▼
                      applyRemote    applyRemote   softDelete
                      Change()       Change()      Local()
                            │          │          │
                            └──────────┼──────────┘
                                       ▼
                                  Room SQLite
                                       │
                                       ▼
                                  Flow 触发 UI 刷新
```

### 订阅范围

监听 11 张表（10 张业务表 + 1 张 `family_members`）：

```kotlin
val tables = listOf(
    "babies", "feedings", "sleeps", "growths", "vaccinations",
    "health_records", "diapers", "messages", "development_assessments", "reminders",
    "family_members",  // 仅通知 ViewModel 刷新，不写入 Room
)
```

### 事件处理

| 事件 | 处理方式 |
|------|---------|
| `INSERT` / `UPDATE` | `syncEngine.applyRemoteChange(tableName, record)` → LWW 写入 Room |
| `DELETE` | `softDeleteLocal(tableName, uuid, now)` → 按 UUID 软删除 |
| `SELECT` | 忽略 |
| `family_members` 变更 | 发射 `familyMembersChanged` SharedFlow 通知 ViewModel |

### 状态管理

```kotlin
enum class RealtimeState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }
val connectionState: StateFlow<RealtimeState>
val familyMembersChanged: SharedFlow<Unit>  // 家庭成员变更事件
```

### 频道管理

- **切换家庭时**：先 `disconnect()` 断开旧频道，再 `subscribeAll()` 建立新频道
- **频道名称**：`"db-changes"`
- **连接方式**：`blockUntilSubscribed = true`（阻塞直到建立连接）

---

## 认证模块

**文件**：`core/auth/AuthService.kt`

### 设计原则

```
登录为可选操作，本地优先：
  - 未登录 → App 完全本地使用，数据存 Room
  - 已登录 → 开启 Supabase 云同步 + 家庭共享
```

### 账户体系

```
用户输入账户名 "zhangsan"
        │
        ▼
  toEmail() 拼接虚拟域名
        │
        ▼
  "zhangsan@baby-tracker.app"
        │
        ▼
  Supabase Email Auth
        │
        ▼
  signUpWith(Email) / signInWith(Email)
```

### API

| 方法 | 说明 |
|------|------|
| `signUp(account, password)` | 注册（自动拼接虚拟域名） |
| `signIn(account, password)` | 登录 |
| `signOut()` | 登出（清除 SharedPreferences + Supabase 会话） |
| `isLoggedIn()` | 是否已登录 |
| `currentUserId()` | 当前用户 ID |
| `setNickname(name)` | 设置用户昵称（本地 SP） |
| `observeAuthState()` | 监听 Supabase Auth 状态流 |

### 状态持久化

| 存储层 | 内容 | 用途 |
|--------|------|------|
| Supabase Auth 内置 | `access_token` / `refresh_token` | `autoLoadFromStorage` + `alwaysAutoRefresh` |
| SharedPreferences | `auth_logged_in` / `auth_display_account` / `auth_nickname` | 双重保障 + UI 展示 |

### 关键配置

> 需在 Supabase Dashboard → Authentication → Settings 关闭 **"Confirm email"**，否则注册后需邮箱确认才能登录。

---

## 家庭共享

**文件**：`core/data/FamilyService.kt`

### 数据模型

```kotlin
@Serializable
data class Family(
    val id: String,           // UUID
    val name: String,         // 家庭名称
    val inviteCode: String,   // 6 位邀请码（客户端生成）
)

@Serializable
data class FamilyMember(
    val familyId: String,
    val userId: String,
    val role: String,         // "owner" / "admin" / "member"
    val joinedAt: String?,
)
```

### 设计要点

- **数据仅存于 Supabase**，不在 Room 本地存储
- **邀请码客户端生成**：`SecureRandom` 生成 6 位大写字母+数字
- **加入家庭**通过 `SECURITY DEFINER` 函数绕过 RLS：
  ```kotlin
  client.postgrest.rpc("join_family", parameters = mapOf("invite_code" to code))
  ```

### API

| 方法 | 说明 |
|------|------|
| `createFamily(name)` | 创建家庭 + 将自己设为 owner |
| `joinFamily(inviteCode)` | 通过邀请码加入 |
| `loadMyFamilies()` | 获取当前用户的所有家庭 |
| `selectFamily(family)` | 切换当前家庭 |
| `getFamilyMembers(familyId)` | 获取家庭成员列表 |

### 状态

```kotlin
val myFamilies: StateFlow<List<Family>>      // 我的家庭列表
val currentFamily: StateFlow<Family?>         // 当前选中的家庭
```

---

## 数据流全景图

### 完整数据写入链路

```
用户操作 (Screen)
    │
    ▼
ViewModel.CRUD
    │
    ▼
Repository.insert/update/delete()
    ├── 1. 生成 UUID（如有需要）
    ├── 2. 设置 updatedAt = System.currentTimeMillis()
    ├── 3. Domain → Entity 转换（Mapper）
    ├── 4. Room DAO insert/update
    └── 5. syncMeta.insert(status = "pending")
            │
            ▼
      SyncEngine.push()
            │
            ▼
      Supabase upsert
            │
            ▼
      syncMeta.markSynced()
```

### 云端变更接收链路

```
另一设备写入 Supabase
    │
    ├─── 方式 1：Realtime WebSocket 实时推送 ───┐
    │                                           │
    │    RealtimeManager.handleRealtimeChange() │
    │              │                             │
    │              ▼                             │
    │    SyncEngine.applyRemoteChange()         │
    │              │                             │
    │              ▼                             │
    │         Room SQLite                       │
    │              │                             │
    │              ▼                             │
    │         Flow → UI                         │
    │                                           │
    ├─── 方式 2：定时 SyncEngine.pull() ────────┘
```

### 现有同步数据映射

| Room 表 | 同步字段 | 同步方向 | 家庭隔离 |
|---------|---------|---------|---------|
| `babies` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `feedings` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `sleeps` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `growths` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `vaccinations` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `health_records` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `diapers` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `messages` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `development_assessments` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `reminders` | uuid, updatedAt, deletedAt | 双向 | family_id |
| `backup_config` | — | 不同步 | — |
| `sync_metadata` | — | 不同步 | — |

### 文件索引

| 模块 | 文件路径 |
|------|---------|
| **Entities** | `core/database/entity/Entities.kt` |
| **DAOs** | `core/database/dao/Daos.kt` |
| **AppDatabase** | `core/database/AppDatabase.kt` |
| **Mappers** | `core/data/mapper/Mappers.kt` |
| **Repositories** | `core/data/repository/Repositories.kt` |
| **DI Modules** | `core/di/Modules.kt` |
| **SupabaseProvider** | `core/sync/SupabaseProvider.kt` |
| **SyncEngine** | `core/sync/SyncEngine.kt` |
| **RealtimeManager** | `core/sync/RealtimeManager.kt` |
| **AuthService** | `core/auth/AuthService.kt` |
| **FamilyService** | `core/data/FamilyService.kt` |
