# BabyTracker 前后端架构

> 最后更新：2026-08-11 · 对应版本：1.7.11

## Android App (前端)

### UI Layer (Compose)

路由定义在 `navigation/AppNavigation.kt`（Navigation 2.9 类型安全路由：25 个 `@Serializable data object`，无动画 instantComposable 跳转）：

| 页面 | 类型安全路由对象 |
|------|------|
| Home | `Home` |
| Timeline | `Timeline` |
| Feeding / Sleep / Growth / Diaper | `Feeding` `Sleep` `Growth` `Diaper` |
| Vaccination / Health / Stats | `Vaccination` `Health` `Stats` |
| Message | `Message` |
| DevelopmentAssessment / Reminder | `DevelopmentAssessment` `Reminder` |
| Settings 主 / 使用偏好 / 数据与同步 / 帮助与关于 | `Settings` `PreferenceSettings` `DataSettings` `SupportSettings` |
| BabyManagement / BabyProfile / Backup / LogViewer / SyncSettings / Family | `BabyManagement` `BabyProfile` `Backup` `LogViewer` `SyncSettings` `Family` |
| Login | `Login` |
| AiAssistant / AiSettings | `AiAssistant` `AiSettings` |

### ViewModel Layer（全部 `viewModel { }` 注册，Screen 用 `koinViewModel()`）

| ViewModel | 依赖 |
|-----------|------|
| `HomeViewModel` / `TimelineViewModel` / `StatsViewModel` | 业务 Repository 组合 |
| `MessageViewModel` / `ReminderViewModel` / `DevelopmentAssessmentViewModel` | 对应 Repository |
| `SettingsViewModel` | SettingsStore（DataStore 设置聚合，无家庭/同步逻辑） |
| `SyncViewModel` | SyncEngine + RealtimeManager + AuthService + FamilyService + NetworkMonitor |
| `FamilyViewModel` | FamilyService |
| `LoginViewModel` | AuthService |
| `AiChatViewModel` / `AiSettingsViewModel` | AI 配置协调 + 供应商适配 + 会话历史 |

按 ID 加载数据的 ViewModel 统一使用 `_trigger` + `flatMapLatest` 模式（红线 5）。

### Repository Layer

10 个 Repository，每个在 insert/update/delete 后自动写入 sync_metadata pending：

```
Room DAO insert/update/delete → syncMeta.pendingChange(table, id, uuid, updatedAt, familyId)
```

删除为软删除（`dao.update(entity.copy(deletedAt = now))`），撤销恢复用 `update()` 清除 deletedAt（1.7.9 修复主键冲突崩溃）。

### Storage

| 存储 | 内容 |
|------|------|
| RoomDB (`babytracker.db`) | version 8，15 张 @Entity（9 张同步业务表 + messages/development_assessments/reminders/backup_config/sync_metadata/sync_cursors + AI 会话/消息 2 张，AI 表仅本机） |
| DataStore | AppSettings（主题/同步/AI/日志偏好，JSON） |
| SharedPreferences | 登录缓存、当前家庭 ID、同步一次性标记等 |

### Sync Layer

```
push() 流程（pushPullMutex 互斥）:
  getPendingChanges(familyId, now)   // nextRetryAt <= now
  → entityToJson + injectFamilyId
  → 按 uuid 查远端，远端 updatedAt >= 本地 → applyRemoteChange(remote) 拉回（LWW 预检）
  → 否则 postgrest.upsert(payload) { onConflict="uuid" }
  → markSynced()；失败 → markRetry 指数退避（5 次后置 conflict）

pull() 流程（pushPullMutex 互斥）:
  sync_cursors 按 (familyId, tableName) 读游标 pageCursor
  → postgrest.select { eq(family_id) + gt(sync_version, pageCursor) } order ASC limit 500
  → 整页全部落库成功才推进游标（applyRemoteChange 先查后改，保留 sync_metadata 行 id）
  → 失败整页不推进，指数退避重试

fullSync()（fullSyncMutex）: push → pull（顺序固定，1.5.1 修复）

RealtimeManager:
  9 张业务表 + family_members（messages 不订阅）
  频道 db-changes-$familyId，显式 filter("family_id", EQ, familyId)
  INSERT/UPDATE → applyRemoteChange()；DELETE → softDeleteLocal()（按 uuid 软删）
  family_members 仅 SharedFlow 通知 UI，不写 Room

markExistingPending()（一次性，按家庭 sync_pending_marked_$fid 标记）:
  DELETE FROM sync_metadata WHERE tableName='messages'（清理已摘除表）
  → 遍历 9 张同步表，为存量记录生成 uuid 并插入 pending（部分失败不置标记，可重试）

触发源（SyncTrigger.kt）: 家庭切换/登录/网络恢复/PendingChangeNotifier 事件（防抖后 push，推送后复查补推）/ 退后台（syncOnExit）/ SyncWorker（周期后台）
```

### Auth Layer

```
AuthService
  signUp/signIn → toEmail(account) → account@baby-tracker.app（无 @ 自动拼接）
  autoLoadFromStorage = true + alwaysAutoRefresh = true
  SharedPreferences 缓存 userId 兜底断网登录态（1.5.10）
  同步/Realtime 要求 verifiedUserId + verifiedFamilyForSync（1.5.18 门禁）
```

### Family Layer

```
FamilyService → families / family_members (Supabase API)
  createFamily(name)    → UUID 生成 familyId → INSERT families + family_members(owner) → refreshForUser
  joinFamily(code)      → postgrest.rpc("join_family", {invite_code})（SECURITY DEFINER 绕过 RLS）→ refreshForUser → 大小写不敏感匹配
  selectFamily(family)  → 全局当前家庭状态（UI 与 SyncTrigger 桥接）
  refreshForUser(userId) → 拉取该用户家庭列表（按用户隔离，1.5.18）
  getFamilyMembers(fid) → family_members 查询
```

---

## Supabase (后端)

### Database (PostgreSQL)

同步业务表（9 张）均含 `family_id UUID → families`，RLS 策略 `is_family_member(family_id)`：

| 表 | 主键 | RLS |
|----|------|-----|
| babies / feedings / sleeps / growths | uuid (TEXT) | is_family_member(family_id) |
| vaccinations / health_records / diapers | uuid (TEXT) | is_family_member(family_id) |
| development_assessments / reminders | uuid (TEXT) | is_family_member(family_id) |

其他表：

| 表 | 说明 |
|----|------|
| messages | **用户私有，不参与同步**（RLS `user_id = auth.uid()`，1.5.11 退出云同步） |
| families / family_members / profiles | 家庭/成员/资料 |
| sync_version 递增列 | 9 张同步表均有单调递增 sync_version + (family_id) 复合索引，供增量拉取 |

> 云端无 AI 会话表：AI 历史只存本地 Room，不上行。

### 辅助函数

```sql
is_family_member(family_id UUID) → BOOLEAN
join_family(invite_code TEXT) → UUID   -- SECURITY DEFINER，绕过 RLS 查找+加入
```

### Auth

| 配置 | 值 |
|------|-----|
| Provider | Email |
| Confirm email | OFF |
| autoLoadFromStorage | true |
| alwaysAutoRefresh | true |

### Realtime

9 张业务表 + family_members 加入 `supabase_realtime` publication，INSERT/UPDATE/DELETE 事件通过 WebSocket 推送；客户端按 `family_id` 显式过滤。

---

## 数据流

### 上行 (push)

```
Room insert/update/delete → sync_metadata(pending) → push() → LWW 预检 → Supabase upsert
```

### 下行 (pull)

```
Supabase → pull() { sync_version 游标分页 } → applyRemoteChange → Room upsert
```

### 实时 (realtime)

```
Supabase INSERT/UPDATE/DELETE → WebSocket → RealtimeManager（频道 db-changes-$familyId）→ Room
```

### 首次同步 / 家庭切换

```
markExistingPending() → 批量 INSERT pending → push()
切换家庭 → resetLastSync()（清游标）→ markExistingPending() → fullSync → 重订阅 Realtime
```

### 离线

```
本地 Room 正常读写 → 联网后自动 push pending（NetworkMonitor 在线门禁）
```

---

## DI 模块 (Koin)

| 模块 | 注册内容 |
|------|----------|
| `appModule` | SharedPreferences, SettingsStore, BackupManager, 10 Repositories, 12 ViewModels |
| `databaseModule` | AppDatabase (version 8), 14 DAOs |
| `syncModule` | SupabaseClient(SupabaseProvider), AuthService, SyncEngine, SyncTrigger, SyncWorker, RealtimeManager, FamilyService, NetworkMonitor |

---

## 安全意识

| 优先级 | 项 | 状态 |
|--------|-----|------|
| ✅ | RLS 家庭隔离 (is_family_member) | 已部署 |
| ✅ | 登录态持久化 (autoLoadFromStorage + alwaysAutoRefresh + SharedPreferences) | 已部署 |
| ✅ | 本地优先 (离线正常使用) | 已实现 |
| ✅ | Soft delete (deletedAt) | 所有表支持 |
| ⚠️ | Supabase anon key 硬编码 | 仍在 `core/sync/SupabaseProvider.kt`（legacy anon key，可公开，未迁移 BuildConfig） |
| ⚠️ | 无 HTTPS 证书固定 | 依赖系统 CA |
| ✅ | AI 凭据安全 | 设备 RSA 密钥 + 公钥加密密文缓存（Android Keystore，Android 15 MGF1 授权） |
