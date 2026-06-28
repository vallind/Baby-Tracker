# BabyTracker 前后端架构

## Android App (前端)

### UI Layer (Compose)

| 页面 | 路由 |
|------|------|
| Home | `/` |
| Timeline | `/timeline` |
| Feeding | `/feeding` |
| Sleep | `/sleep` |
| Growth | `/growth` |
| Vaccination | `/vaccination` |
| Health | `/health` |
| Diaper | `/diaper` |
| Stats | `/stats` |
| Settings | `/settings` |
| BabyManagement | `/settings/babies` |
| Backup | `/settings/backup` |
| Family | `/settings/family` |
| Message | `/message` |
| DevelopmentAssessment | `/development_assessment` |
| Reminder | `/reminder` |
| Login | `/login` |

### ViewModel Layer

| ViewModel | 依赖 |
|-----------|------|
| `HomeViewModel` | FeedingRepository, SleepRepository, DiaperRepository |
| `SettingsViewModel` | SyncEngine, RealtimeManager, AuthService, FamilyService, Context |
| `LoginViewModel` | AuthService |
| `FamilyViewModel` | FamilyService |
| `StatsViewModel` | 4 Repositories |
| `TimelineViewModel` | 5 Repositories |

### Repository Layer

10个 Repository，每个在 insert/update/delete 后自动写入 sync_metadata pending：

```
Room DAO insert → syncMeta.insert(SyncMetadataEntity(status='pending'))
```

### Storage

| 存储 | 内容 |
|------|------|
| RoomDB (`babytracker.db`) | 10业务表 + sync_metadata + backup_config (version 6) |
| SharedPreferences | KEY_LOGGED_IN, theme 等 |

### Sync Layer

```
push() 流程:
  syncMeta.getPendingChanges()
  → entityToJson(table, entity)
  → injectFamilyId(json)     // 注入 currentFamilyId
  → postgrest.upsert(payload) { onConflict="uuid" }
  → syncMeta.markSynced()

pull() 流程:
  lastSyncAt = syncMeta.getLastSyncAt()
  → postgrest.select { filter { gte("updatedAt", lastSyncAt) } }
  → parseEntity(json) → dao.insert/update
  → syncMeta.insert(synced)

RealtimeManager:
  10张表 postgresChangeFlow → INSERT/UPDATE/DELETE
  → applyRemoteChange() / softDeleteLocal()

markExistingPending():
  INSERT INTO sync_metadata SELECT * FROM {table} WHERE deletedAt IS NULL
    AND id NOT IN (SELECT localId FROM sync_metadata WHERE tableName='{table}')
```

### Auth Layer

```
AuthService
  signUp/signIn → toEmail(account) → account@baby-tracker.app
  autoLoadFromStorage = true
  alwaysAutoRefresh = true
  SharedPreferences KEY_LOGGED_IN 双重保障
```

### Family Layer

```
FamilyService → families / family_members (Supabase API)
  createFamily(name)    → UUID.randomUUID() → INSERT familys → INSERT family_members(owner) → SELECT
  joinFamily(code)      → SELECT families WHERE invite_code → INSERT family_members(member)
  loadMyFamilies()      → SELECT family_members WHERE user_id → SELECT families
  getFamilyMembers(fid) → SELECT family_members WHERE family_id
```

---

## Supabase (后端)

### Database (PostgreSQL)

| 表 | 主键 | 外键 | RLS |
|----|------|------|-----|
| babies | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| feedings | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| sleeps | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| growths | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| vaccinations | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| health_records | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| diapers | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| messages | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| development_assessments | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| reminders | uuid (TEXT) | family_id → families | is_family_member(family_id) |
| families | id (UUID) | - | is_family_member(id) |
| family_members | (family_id, user_id) | families, auth.users | is_family_member(family_id) |
| profiles | id (UUID) | auth.users | id = auth.uid() |

### 辅助函数

```sql
is_family_member(family_id UUID) → BOOLEAN
join_family(invite_code TEXT) → UUID
```

### Auth

| 配置 | 值 |
|------|-----|
| Provider | Email |
| Confirm email | OFF |
| autoLoadFromStorage | true |
| alwaysAutoRefresh | true |

### Realtime

10张业务表加入 `supabase_realtime` publication，INSERT/UPDATE/DELETE 事件通过 WebSocket 推送。

---

## 数据流

### 上行 (push)

```
Room insert → sync_metadata(pending) → push() → Supabase upsert
```

### 下行 (pull)

```
Supabase → pull() { gte(updatedAt) } → Room insert/update
```

### 实时 (realtime)

```
Supabase INSERT/UPDATE/DELETE → WebSocket → RealtimeManager → Room
```

### 首次同步

```
markExistingPending() → 批量 INSERT pending → push()
```

### 离线

```
本地 Room 正常读写 → 联网后自动 push pending
```

---

## DI 模块 (Koin)

| 模块 | 注册内容 |
|------|----------|
| `appModule` | SharedPreferences, ThemeController, BabyController, BackupManager, 10 Repositories, 8 ViewModels |
| `databaseModule` | AppDatabase (version 6), 12 DAOs (含 SyncMetadataDao) |
| `syncModule` | SupabaseClient, AuthService, SyncEngine, RealtimeManager, FamilyService |

---

## 安全意识

| 优先级 | 项 | 状态 |
|--------|-----|------|
| ✅ | RLS 家庭隔离 (is_family_member) | 10表已部署 |
| ✅ | 登录态持久化 (autoLoadFromStorage + alwaysAutoRefresh + SharedPreferences) | 已部署 |
| ✅ | 本地优先 (离线正常使用) | 已实现 |
| ✅ | Soft delete (deletedAt) | 所有表支持 |
| ⚠️ | Supabase anon key 硬编码 | 待迁移至 BuildConfig |
| ⚠️ | 无 HTTPS 证书固定 | 依赖系统 CA |
