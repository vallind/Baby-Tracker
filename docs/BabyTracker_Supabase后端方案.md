# BabyTracker Supabase 后端方案

> 用 Supabase 替代 WebDAV 备份，实现用户认证 + 实时云同步 + 多设备协作  
> 版本：v1.0 ｜ 日期：2026-06-25

---

## 一、现状与目标

### 1.1 现状问题

| 问题 | 说明 |
|------|------|
| **无用户系统** | 数据全部本地，换手机数据丢失 |
| **备份靠手动** | WebDAV 导出 ZIP → 手动上传，恢复也是全量覆盖 |
| **无实时同步** | 两个人带同一个宝宝，各自记各自，数据不互通 |
| **无冲突处理** | WebDAV 恢复是暴力覆盖，不比较时间戳 |
| **备份不含全部表** | Reminder、Message、DevelopmentAssessment 未纳入导出 |

### 1.2 Supabase 能解决什么

| 能力 | 对应 BabyTracker 场景 |
|------|----------------------|
| **Auth** | 账户登录（可选），家庭共享账号 |
| **PostgreSQL** | 11 张表从 Room 1:1 映射到云端 |
| **Realtime** | 妈妈记录喂养 → 爸爸手机即时看到 |
| **Row Level Security** | 每个家庭只能看自己的数据 |
| **Storage** | 宝宝头像、体检附件上传 |
| **Edge Functions** | 月度成长报告自动生成、疫苗提醒推送 |

---

## 二、数据库设计

### 2.1 Supabase 表结构

每张表比 Room Entity 多 4 个字段：`uuid`（云端主键）、`user_id`（归属）、`created_at`、`updated_at`（用于增量同步）。

```sql
-- ============================================
-- profiles：用户档案（Auth 注册后自动创建）
-- ============================================
CREATE TABLE profiles (
  id          UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  display_name TEXT,
  avatar_url   TEXT,
  created_at   TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- families：家庭（支持多宝宝共享）
-- ============================================
CREATE TABLE families (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name        TEXT NOT NULL,
  invite_code TEXT UNIQUE DEFAULT upper(substr(md5(random()::text), 1, 6)),
  created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE family_members (
  family_id UUID REFERENCES families(id) ON DELETE CASCADE,
  user_id   UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  role      TEXT DEFAULT 'member',  -- 'owner' | 'member'
  PRIMARY KEY (family_id, user_id)
);

-- ============================================
-- babies：宝宝信息
-- ============================================
CREATE TABLE babies (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  family_id   UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
  name        TEXT NOT NULL,
  gender      TEXT NOT NULL,
  birth_date  DATE NOT NULL,
  birth_weight NUMERIC,
  birth_height NUMERIC,
  avatar_path  TEXT,
  local_id     INTEGER,  -- 本地 Room 的 id，用于映射
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- feedings：喂养记录
-- ============================================
CREATE TABLE feedings (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id      UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  type         TEXT NOT NULL,
  amount_ml    INTEGER,
  duration_min INTEGER,
  breast_side  TEXT,
  food_name    TEXT,
  amount_g     INTEGER,
  brand        TEXT,
  note         TEXT,
  timestamp    TIMESTAMPTZ NOT NULL,
  local_id     INTEGER,
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- sleeps：睡眠记录
-- ============================================
CREATE TABLE sleeps (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id     UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  type        TEXT NOT NULL,
  start_time  TIMESTAMPTZ NOT NULL,
  end_time    TIMESTAMPTZ NOT NULL,
  note        TEXT,
  local_id    INTEGER,
  created_at  TIMESTAMPTZ DEFAULT now(),
  updated_at  TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- growths：生长记录
-- ============================================
CREATE TABLE growths (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id      UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  type         TEXT NOT NULL,
  value        NUMERIC NOT NULL,
  measured_at  TIMESTAMPTZ NOT NULL,
  note         TEXT,
  local_id     INTEGER,
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- diapers：换尿布记录
-- ============================================
CREATE TABLE diapers (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id     UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  type        TEXT NOT NULL,
  timestamp   TIMESTAMPTZ NOT NULL,
  note        TEXT,
  local_id    INTEGER,
  created_at  TIMESTAMPTZ DEFAULT now(),
  updated_at  TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- vaccinations：疫苗接种
-- ============================================
CREATE TABLE vaccinations (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id           UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  name              TEXT NOT NULL,
  dose              TEXT,
  scheduled_date    DATE,
  administered_date DATE,
  status            TEXT DEFAULT 'pending',
  note              TEXT,
  local_id          INTEGER,
  created_at        TIMESTAMPTZ DEFAULT now(),
  updated_at        TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- health_records：健康记录
-- ============================================
CREATE TABLE health_records (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id      UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  category     TEXT NOT NULL,
  description  TEXT NOT NULL,
  doctor_name  TEXT,
  record_date  DATE NOT NULL,
  attachments  TEXT,
  note         TEXT,
  local_id     INTEGER,
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- reminders：提醒
-- ============================================
CREATE TABLE reminders (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id     UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  type        TEXT NOT NULL,
  title       TEXT NOT NULL,
  description TEXT DEFAULT '',
  due_date    TIMESTAMPTZ NOT NULL,
  is_done     BOOLEAN DEFAULT false,
  done_date   TIMESTAMPTZ,
  is_enabled  BOOLEAN DEFAULT true,
  repeat_rule TEXT DEFAULT '',
  local_id    INTEGER,
  created_at  TIMESTAMPTZ DEFAULT now(),
  updated_at  TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- development_assessments：发育评估
-- ============================================
CREATE TABLE development_assessments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  baby_id         UUID NOT NULL REFERENCES babies(id) ON DELETE CASCADE,
  assess_date     TIMESTAMPTZ NOT NULL,
  baby_age_months INTEGER NOT NULL,
  gross_motor     INTEGER NOT NULL DEFAULT 0,
  fine_motor      INTEGER NOT NULL DEFAULT 0,
  language        INTEGER NOT NULL DEFAULT 0,
  social          INTEGER NOT NULL DEFAULT 0,
  cognitive       INTEGER NOT NULL DEFAULT 0,
  note            TEXT DEFAULT '',
  local_id        INTEGER,
  created_at      TIMESTAMPTZ DEFAULT now(),
  updated_at      TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- messages：消息中心（App 级，无 baby_id）
-- ============================================
CREATE TABLE messages (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  type         TEXT NOT NULL,
  title        TEXT NOT NULL,
  content      TEXT NOT NULL,
  sender_avatar TEXT,
  create_time  TIMESTAMPTZ NOT NULL,
  is_read      BOOLEAN DEFAULT false,
  extra_data   JSONB DEFAULT '{}',
  local_id     INTEGER,
  created_at   TIMESTAMPTZ DEFAULT now(),
  updated_at   TIMESTAMPTZ DEFAULT now()
);
```

### 2.2 索引

```sql
-- 高频查询索引
CREATE INDEX idx_feedings_baby    ON feedings(baby_id, timestamp DESC);
CREATE INDEX idx_sleeps_baby      ON sleeps(baby_id, start_time DESC);
CREATE INDEX idx_growths_baby     ON growths(baby_id, measured_at DESC);
CREATE INDEX idx_diapers_baby     ON diapers(baby_id, timestamp DESC);
CREATE INDEX idx_vaccinations_baby ON vaccinations(baby_id);
CREATE INDEX idx_health_baby      ON health_records(baby_id, record_date DESC);
CREATE INDEX idx_reminders_baby   ON reminders(baby_id, is_done, due_date);
CREATE INDEX idx_assessments_baby ON development_assessments(baby_id, assess_date DESC);
CREATE INDEX idx_messages_user    ON messages(user_id, is_read, create_time DESC);

-- 增量同步索引
CREATE INDEX idx_feedings_updated ON feedings(updated_at);
CREATE INDEX idx_sleeps_updated   ON sleeps(updated_at);
CREATE INDEX idx_growths_updated  ON growths(updated_at);
CREATE INDEX idx_diapers_updated  ON diapers(updated_at);
CREATE INDEX idx_babies_updated   ON babies(updated_at);
```

### 2.3 自动更新 updated_at

```sql
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为每张业务表创建触发器
DO $$ DECLARE t TEXT; BEGIN
  FOREACH t IN ARRAY ARRAY[
    'babies','feedings','sleeps','growths','diapers',
    'vaccinations','health_records','reminders',
    'development_assessments','messages'
  ] LOOP
    EXECUTE format(
      'CREATE TRIGGER set_updated_at BEFORE UPDATE ON %I
       FOR EACH ROW EXECUTE FUNCTION update_updated_at()', t);
  END LOOP;
END $$;
```

---

## 三、Row Level Security（数据隔离）

每个家庭只能访问自己的数据，核心安全机制：

```sql
-- 1. 启用 RLS
ALTER TABLE families ENABLE ROW LEVEL SECURITY;
ALTER TABLE family_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE babies ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedings ENABLE ROW LEVEL SECURITY;
ALTER TABLE sleeps ENABLE ROW LEVEL SECURITY;
ALTER TABLE growths ENABLE ROW LEVEL SECURITY;
ALTER TABLE diapers ENABLE ROW LEVEL SECURITY;
ALTER TABLE vaccinations ENABLE ROW LEVEL SECURITY;
ALTER TABLE health_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE reminders ENABLE ROW LEVEL SECURITY;
ALTER TABLE development_assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- 1.5 profiles / family_members 的 RLS（认证相关表）
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE family_members ENABLE ROW LEVEL SECURITY;

-- profiles：用户只能读写自己的档案
CREATE POLICY "user_read_own_profile" ON profiles
  FOR SELECT USING (id = auth.uid());

CREATE POLICY "user_update_own_profile" ON profiles
  FOR UPDATE USING (id = auth.uid());

-- family_members：家庭成员可读取，owner 可管理成员
CREATE POLICY "member_read" ON family_members
  FOR SELECT USING (is_family_member(family_id));

CREATE POLICY "owner_manage_members" ON family_members
  FOR ALL USING (
    EXISTS (
      SELECT 1 FROM family_members fm
      WHERE fm.family_id = family_members.family_id
        AND fm.user_id = auth.uid()
        AND fm.role = 'owner'
    )
  );

-- 2. 辅助函数：当前用户是否属于该记录的家庭
CREATE OR REPLACE FUNCTION is_family_member(family_id UUID)
RETURNS BOOLEAN AS $$
  SELECT EXISTS (
    SELECT 1 FROM family_members
    WHERE family_members.family_id = is_family_member.family_id
      AND family_members.user_id = auth.uid()
  );
$$ LANGUAGE sql SECURITY DEFINER STABLE;

-- 3. babies 表策略（其他表类似，通过 baby_id → babies.family_id 关联）
CREATE POLICY "family_read_babies" ON babies
  FOR SELECT USING (is_family_member(family_id));

CREATE POLICY "family_insert_babies" ON babies
  FOR INSERT WITH CHECK (is_family_member(family_id));

CREATE POLICY "family_update_babies" ON babies
  FOR UPDATE USING (is_family_member(family_id));

CREATE POLICY "family_delete_babies" ON babies
  FOR DELETE USING (is_family_member(family_id));

-- 4. 子表策略模板（feedings 为例，通过 JOIN babies 检查 family_id）
CREATE POLICY "family_read_feedings" ON feedings
  FOR SELECT USING (
    EXISTS (SELECT 1 FROM babies WHERE babies.id = feedings.baby_id AND is_family_member(babies.family_id))
  );

CREATE POLICY "family_write_feedings" ON feedings
  FOR ALL USING (
    EXISTS (SELECT 1 FROM babies WHERE babies.id = feedings.baby_id AND is_family_member(babies.family_id))
  );

-- sleeps, growths, diapers, vaccinations, health_records, reminders, development_assessments 同理

-- 5. messages 是用户级，不走家庭
CREATE POLICY "user_read_messages" ON messages
  FOR SELECT USING (user_id = auth.uid());

CREATE POLICY "user_write_messages" ON messages
  FOR ALL USING (user_id = auth.uid());

-- 6. 家庭成员管理
CREATE POLICY "member_read_family" ON families
  FOR SELECT USING (is_family_member(id));

CREATE POLICY "owner_manage_family" ON families
  FOR ALL USING (
    EXISTS (SELECT 1 FROM family_members WHERE family_id = id AND user_id = auth.uid() AND role = 'owner')
  );
```

---

## 四、认证方案

### 4.1 设计原则：不强制登录，本地优先

- **未登录状态**：App 完全本地使用，所有数据存在 Room，不触发任何网络请求
- **登录为可选操作**：用户在 Settings 中选择登录，登录后开启云同步和家庭共享
- **无登录页强跳**：`AppNavigation` 不判断登录状态，永远直接进入主页

### 4.2 Supabase Auth 配置

使用**自定义用户名（账户名）+ 密码**，不依赖邮箱验证，保持简单。

Supabase Auth 默认要求邮箱，但可以通过关闭"Confirm email"并允许自定义用户名来实现账户名登录：

```sql
-- Supabase Dashboard → Authentication → Settings
-- 关闭 "Enable email confirmations"
-- 这样注册后无需验证邮件，可直接登录
```

Android 端用邮箱字段存账户名（Supabase Auth 底层仍用 email 字段，但业务层显示为"账户名"）。

```kotlin
// 支持的登录方式：账户名 + 密码（底层用 email 字段存储账户名）
```

### 4.3 Android 端 Auth 集成

```kotlin
// core/auth/AuthService.kt
class AuthService(
    private val client: SupabaseClient,
) {
    val currentUser: StateFlow<User?> = MutableStateFlow(null)

    init {
        // 启动时尝试从本地存储恢复登录态
        client.auth.loadFromStorage()
        currentUser.value = client.auth.currentUser()
    }

    // 注册（account 作为 email 传入，Supabase 不验证邮箱格式）
    suspend fun signUp(account: String, password: String): Result<User> = runCatching {
        val result = client.auth.signUpWith(Email) {
            this.email = account   // 账户名存到 email 字段
            this.password = password
        }
        currentUser.value = result.user
        result.user
    }

    // 登录
    suspend fun signIn(account: String, password: String): Result<User> = runCatching {
        val result = client.auth.signInWith(Email) {
            this.email = account
            this.password = password
        }
        currentUser.value = result.user
        result.user
    }

    // 退出（退出后继续本地使用，只是停止同步）
    suspend fun signOut() {
        client.auth.signOut()
        currentUser.value = null
    }

    // 当前是否已登录
    fun isLoggedIn(): Boolean = client.auth.currentUser() != null

    // 监听登录状态变化
    fun observeAuthState(): Flow<User?> = client.auth.authStateChanges
        .map { it.session?.user }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)
}
```

---

## 五、同步架构

### 5.1 整体架构

```
┌─────────────────────────────────────────────┐
│                 Android 端                   │
│                                              │
│  ┌──────────┐    ┌──────────┐               │
│  │   Room   │    │  Sync    │               │
│  │ Database │◄──►│  Engine  │               │
│  │ (本地)   │    │ (调度)   │               │
│  └──────────┘    └─────┬────┘               │
│                        │                     │
│                   ┌────▼────┐                │
│                   │  Sync   │                │
│                   │  Queue  │                │
│                   │ (待同步) │                │
│                   └────┬────┘                │
└────────────────────────┼─────────────────────┘
                         │ HTTPS + Realtime
┌────────────────────────┼─────────────────────┐
│                   ┌────▼────┐                │
│                   │ Supabase│                │
│                   │   PG    │                │
│                   └─────────┘                │
└──────────────────────────────────────────────┘
```

### 5.2 同步策略：增量 + 双向 + Last-Write-Wins

| 策略 | 说明 |
|------|------|
| **增量同步** | 每次只拉取 `updated_at > 上次同步时间` 的记录 |
| **双向同步** | 本地新增 → 推到云端；云端新增 → 写入 Room |
| **冲突解决** | Last-Write-Wins（比较 `updated_at`，新的覆盖旧的） |
| **离线优先** | 本地 Room 为主，网络恢复后自动同步 |
| **实时推送** | 订阅 Supabase Realtime，其他设备变更即时到达 |

### 5.3 同步引擎核心代码

```kotlin
// core/sync/SyncEngine.kt
class SyncEngine(
    private val client: SupabaseClient,
    private val db: AppDatabase,
    private val syncDao: SyncMetadataDao,
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * 全量同步流程：
     * 1. 检查登录状态，未登录直接返回
     * 2. 推送本地待同步记录（pending 状态）
     * 3. 拉取云端增量（updated_at > lastSyncAt）
     * 4. 合并写入 Room
     * 5. 更新 lastSyncAt
     */
    suspend fun fullSync() {
        // 未登录时不触发同步，直接返回
        if (client.auth.currentUser() == null) {
            _syncState.value = SyncState.Idle
            return
        }
        _syncState.value = SyncState.Syncing
        try {
            pushLocalChanges()   // 本地 → 云端
            pullRemoteChanges()  // 云端 → 本地
            _syncState.value = SyncState.Synced
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "同步失败")
        }
    }

    // ─── 推送本地变更 ───
    private suspend fun pushLocalChanges() {
        val pending = syncDao.getPendingChanges()  // status = PENDING
        pending.forEach { change ->
            when (change.tableName) {
                "feedings" -> pushFeeding(change)
                "sleeps"   -> pushSleep(change)
                "growths"  -> pushGrowth(change)
                // ... 其他表
            }
            syncDao.markSynced(change.localId, change.tableName)
        }
    }

    // ─── 拉取云端增量 ───
    private suspend fun pullRemoteChanges() {
        val lastSync = syncDao.getLastSyncAt() ?: Instant.EPOCH

        pullBabies(lastSync)
        pullFeedings(lastSync)
        pullSleeps(lastSync)
        pullGrowths(lastSync)
        pullDiapers(lastSync)
        pullVaccinations(lastSync)
        pullHealthRecords(lastSync)
        pullReminders(lastSync)
        pullAssessments(lastSync)

        syncDao.updateLastSyncAt(Instant.now())
    }

    // ─── 以 feedings 为例 ───
    private suspend fun pullFeedings(since: Instant) {
        val remoteFeedings = client.from("feedings")
            .select {
                filter {
                    gt("updated_at", since.toString())
                    // RLS 自动过滤只返回当前家庭的数据
                }
            }
            .decodeList<RemoteFeeding>()

        remoteFeedings.forEach { remote ->
            val local = db.feedingDao().getByUuid(remote.id)
            if (local == null) {
                // 云端新增 → 写入 Room
                db.feedingDao().insert(remote.toEntity())
            } else if (remote.updatedAt > local.updatedAt) {
                // 云端更新且更新时间更新 → 覆盖 Room
                db.feedingDao().update(remote.toEntity(local.id))
            }
            // else: 本地更新时间更新，不覆盖（本地变更会在 push 阶段推送）
        }
    }

    private suspend fun pushFeeding(change: SyncChange) {
        val entity = db.feedingDao().getById(change.localId) ?: return
        val remote = entity.toRemote()
        client.from("feedings").upsert(remote) {
            onConflict("id")  // UUID 冲突时更新
        }
    }

    // ─── Realtime 订阅 ───
    fun subscribeRealtime(scope: CoroutineScope) {
        val channel = client.realtime.channel("babytracker")

        listOf("feedings", "sleeps", "growths", "diapers", "vaccinations",
               "health_records", "reminders", "development_assessments", "babies")
            .forEach { table ->
                // INSERT
                channel.postgresChangeFlow<PostgresAction.Insert>(table) {
                    schema = "public"
                }.onEach { action ->
                    handleRemoteInsert(table, action.record)
                }.launchIn(scope)

                // UPDATE
                channel.postgresChangeFlow<PostgresAction.Update>(table) {
                    schema = "public"
                }.onEach { action ->
                    handleRemoteUpdate(table, action.record)
                }.launchIn(scope)

                // DELETE
                channel.postgresChangeFlow<PostgresAction.Delete>(table) {
                    schema = "public"
                }.onEach { action ->
                    handleRemoteDelete(table, action.oldRecord)
                }.launchIn(scope)
            }

        channel.subscribe()
    }
}
```

### 5.4 同步元数据表

Room 新增一张同步元数据表，跟踪每条记录的同步状态：

```kotlin
@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableName: String,        // "feedings", "sleeps", ...
    val localId: Int,             // Room 表的 id
    val remoteUuid: String? = null,  // Supabase 的 UUID
    val syncStatus: String = "pending",  // pending | synced | conflict
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long? = null, // 全局上次同步时间戳
)
```

### 5.5 Entity 扩展字段

每张 Room Entity 表增加两个字段，用于云端映射：

```kotlin
@Entity(tableName = "feedings")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String? = null,          // 🔽 新增：Supabase UUID
    val updatedAt: Long = 0L,          // 🔽 新增：最后更新时间戳（epoch milli）
    // ... 原有字段不变
)
```

**数据库迁移：**

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 为每张业务表添加 uuid + updatedAt
        listOf("babies", "feedings", "sleeps", "growths", "diapers",
               "vaccinations", "health_records", "reminders",
               "development_assessments", "messages").forEach { table ->
            db.execSQL("ALTER TABLE $table ADD COLUMN uuid TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE $table ADD COLUMN updatedAt INTEGER DEFAULT 0")
        }
        // 创建 sync_metadata 表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS sync_metadata (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tableName TEXT NOT NULL,
                localId INTEGER NOT NULL,
                remoteUuid TEXT DEFAULT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'pending',
                updatedAt INTEGER NOT NULL,
                lastSyncAt INTEGER DEFAULT NULL
            )
        """)
    }
}
```

---

## 六、删除策略（Soft Delete）

### 6.1 为什么需要 Soft Delete

硬删除（直接从表删除记录）在多设备同步场景下有问题：
- 设备 A 删除了记录，但设备 B 在删除前已离线，恢复网络后无法感知该记录已被删除
- Last-Write-Wins 冲突解决无法处理"一方删除、一方更新"的情况

### 6.2 方案：所有业务表增加 `deleted_at` 字段

```sql
-- 每张业务表增加 deleted_at 字段
ALTER TABLE babies ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE feedings ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE sleeps ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE growths ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE diapers ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE vaccinations ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE health_records ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE reminders ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE development_assessments ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;
-- messages 同理
```

- `deleted_at IS NULL` → 有效记录
- `deleted_at IS NOT NULL` → 已删除，保留 30 天供同步传播，之后由定时任务硬删除

### 6.3 同步时的删除处理

| 场景 | 处理方式 |
|------|----------|
| 本地删除 | 标记 `deleted_at = now()`，推送到云端 |
| 云端删除到达 | 本地标记 `deleted_at`，不从 Room 物理删除 |
| 拉取时过滤 | 所有查询默认加 `WHERE deleted_at IS NULL` |
| 硬删除定时任务 | Supabase Cron 每天删除 `deleted_at < now() - 30d` 的记录 |

### 6.4 RLS 补充

已删除的记录（`deleted_at IS NOT NULL`）对所有用户不可见，无需额外 RLS 规则。

---

## 七、家庭共享机制

### 6.1 流程

```
妈妈（owner）                爸爸（member）
    │                           │
    │  1. 创建家庭              │
    │  2. 获得邀请码 "A3F7K2"   │
    │                           │
    │  ◄─── 3. 爸爸输入邀请码 ──►│
    │                           │
    │  4. 妈妈确认加入           │
    │                           │
    │  5. 数据通过 RLS 自动共享   │
    │     （is_family_member）   │
```

### 6.2 Edge Function：加入家庭

```sql
-- supabase/functions/join-family/index.ts
-- 用邀请码加入家庭
CREATE OR REPLACE FUNCTION join_family(invite_code TEXT)
RETURNS UUID AS $$
DECLARE
  fam_id UUID;
BEGIN
  SELECT id INTO fam_id FROM families WHERE families.invite_code = join_family.invite_code;
  IF fam_id IS NULL THEN RAISE EXCEPTION '邀请码无效';
  END IF;
  INSERT INTO family_members (family_id, user_id, role)
  VALUES (fam_id, auth.uid(), 'member')
  ON CONFLICT DO NOTHING;
  RETURN fam_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

---

## 八、Android 端集成

### 7.1 依赖

```kotlin
// build.gradle.kts
dependencies {
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:3.1.3")
    implementation("io.ktor:ktor-client-okhttp:3.1.3")
}
```

### 7.2 Supabase Client 初始化

```kotlin
// core/sync/SupabaseProvider.kt
object SupabaseProvider {
    private const val SUPABASE_URL = "https://xxx.supabase.co"
    private const val SUPABASE_KEY = "eyJ..."

    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
            install(Postgrest)
            install(Auth) {
                autoLoadFromStorage = true
            }
            install(Realtime)
            install(Storage)
        }
    }
}
```

### 7.3 Koin DI

```kotlin
val syncModule = module {
    single { SupabaseProvider.client }
    single { AuthService(get()) }
    single { SyncEngine(get(), get(), get()) }
    single { SyncScheduler(get(), get()) }
}
```

### 7.4 自动同步调度

```kotlin
// core/sync/SyncScheduler.kt
class SyncScheduler(
    private val syncEngine: SyncEngine,
    private val connectivityManager: ConnectivityManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        // 网络恢复时自动触发同步
        connectivityManager.observeConnectivity()
            .filter { it }
            .debounce(2000)
            .onEach { syncEngine.fullSync() }
            .launchIn(scope)

        // 定时同步（每 5 分钟）
        flow { while (true) { emit(Unit); delay(5.minutes) } }
            .onEach { syncEngine.fullSync() }
            .launchIn(scope)

        // Realtime 订阅
        syncEngine.subscribeRealtime()
    }
}
```

### 7.5 Repository 层改造

现有 Repository 不变，在内部追加同步逻辑：

```kotlin
class FeedingRepositoryImpl(
    private val dao: FeedingDao,
    private val syncDao: SyncMetadataDao,  // 🔽 新增
) : FeedingRepository {

    override fun watchByBaby(babyId: Int): Flow<List<Feeding>> =
        dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(feeding: Feeding): Long {
        val id = dao.insert(feeding.toEntity())
        // 🔽 标记待同步
        syncDao.insert(SyncMetadata(
            tableName = "feedings", localId = id.toInt(),
            syncStatus = "pending", updatedAt = System.currentTimeMillis()
        ))
        return id
    }

    override suspend fun delete(feeding: Feeding) {
        dao.delete(feeding.toEntity())
        // 🔽 标记待同步（删除也需要推到云端）
        syncDao.insert(SyncMetadata(
            tableName = "feedings", localId = feeding.id,
            syncStatus = "pending", updatedAt = System.currentTimeMillis()
        ))
    }
}
```

---

## 九、Storage（文件存储）

### 8.1 Bucket 设计

```sql
-- 头像存储
INSERT INTO storage.buckets (id, name, public) VALUES ('avatars', 'avatars', true);

-- 体检附件存储
INSERT INTO storage.buckets (id, name, public) VALUES ('attachments', 'attachments', false);
```

### 8.2 Storage RLS

```sql
-- avatars：公开读取，只有本人可写
CREATE POLICY "public_read_avatars" ON storage.objects
  FOR SELECT USING (bucket_id = 'avatars');

CREATE POLICY "authenticated_upload_avatars" ON storage.objects
  FOR INSERT WITH CHECK (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

-- attachments：家庭内可读可写
CREATE POLICY "family_read_attachments" ON storage.objects
  FOR SELECT USING (
    bucket_id = 'attachments'
    AND EXISTS (
      SELECT 1 FROM babies
      WHERE babies.id::text = (storage.foldername(name))[1]
        AND is_family_member(babies.family_id)
    )
  );
```

### 8.3 头像上传

```kotlin
// core/sync/AvatarUploader.kt
class AvatarUploader(private val client: SupabaseClient) {
    suspend fun upload(babyId: UUID, bytes: ByteArray, contentType: String = "image/jpeg"): String {
        val path = "$babyId/avatar_${System.currentTimeMillis()}.jpg"
        client.storage.from("avatars").upload(path, bytes) {
            upsert = true
            contentType = contentType
        }
        return client.storage.from("avatars").publicUrl(path)
    }
}
```

---

## 十、Edge Functions

### 9.1 月度成长报告

```typescript
// supabase/functions/monthly-report/index.ts
import { createClient } from '@supabase/supabase-js'

Deno.serve(async (req) => {
  const { baby_id, month } = await req.json()
  const supabase = createClient(Deno.env.get('SUPABASE_URL'), Deneno.env.get('SUPABASE_SERVICE_KEY'))

  // 查询该月所有数据
  const [feedings, sleeps, growths, diapers] = await Promise.all([
    supabase.from('feedings').select('*').eq('baby_id', baby_id)
      .gte('timestamp', `${month}-01`).lt('timestamp', `${month}-32`),
    supabase.from('sleeps').select('*').eq('baby_id', baby_id)
      .gte('start_time', `${month}-01`).lt('start_time', `${month}-32`),
    supabase.from('growths').select('*').eq('baby_id', baby_id)
      .gte('measured_at', `${month}-01`).lt('measured_at', `${month}-32`),
    supabase.from('diapers').select('*').eq('baby_id', baby_id)
      .gte('timestamp', `${month}-01`).lt('timestamp', `${month}-32`),
  ])

  // 生成报告消息
  const report = {
    type: 'SYSTEM',
    title: `${month} 月度报告`,
    content: `喂养 ${feedings.data?.length} 次，睡眠 ${sleeps.data?.length} 次，换尿布 ${diapers.data?.length} 次`,
    user_id: req.headers.get('x-user-id'),
  }

  await supabase.from('messages').insert(report)
  return new Response(JSON.stringify({ ok: true }), { headers: { 'Content-Type': 'application/json' } })
})
```

### 9.2 疫苗提醒推送

```typescript
// supabase/functions/vaccine-reminder/index.ts
// 由 Cron 触发（每天 8:00 检查）
Deno.serve(async () => {
  const supabase = createClient(...)
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0]

  const { data: pending } = await supabase.from('vaccinations')
    .select('*, babies(name, family_id)')
    .eq('status', 'pending')
    .eq('scheduled_date', tomorrow)

  // 为每个待接种的宝宝家庭发送提醒
  for (const v of pending ?? []) {
    const { data: members } = await supabase.from('family_members')
      .select('user_id').eq('family_id', v.babies.family_id)

    for (const m of members ?? []) {
      await supabase.from('reminders').insert({
        baby_id: v.baby_id, type: 'VACCINE',
        title: `${v.babies.name} 明天接种 ${v.name}`,
        due_date: v.scheduled_date,
      })
    }
  }
  return new Response('ok')
})
```

---

## 十一、UI 变更

### 11.1 新增页面

| 页面 | 功能 | 入口 |
|------|------|------|
| **LoginPage** | 账户登录 / 注册 | Settings → 账户 |
| **FamilyPage** | 创建/加入家庭，显示邀请码和成员列表 | Settings → 家庭（登录后可见） |
| **SyncStatusPage** | 同步状态、手动同步、冲突解决 | Settings → 同步（登录后可见） |

### 11.2 现有页面改造

| 改造点 | 说明 |
|--------|------|
| SettingsScreen | 增加「账户」「家庭」「同步」三个分区（未登录时只显示「账户」） |
| BabyManagementScreen | 增加头像上传（登录后可用，未登录时头像只存本地） |
| AppNavigation | **不判断登录状态**，永远直接进入主页 |
| 全局 | 同步状态指示器（登录后才显示，未登录时不显示） |

### 11.3 未登录时的行为

| 功能 | 行为 |
|------|------|
| 所有记录功能 | 正常可用，数据存 Room |
| 同步 | 不触发，SyncEngine 检测到未登录直接返回 |
| 家庭共享 | 不可用，入口隐藏 |
| 头像上传 | 只存本地路径，不上传 Storage |
| 设置页 | 显示「登录账户」入口，点击后跳转 LoginPage |

### 10.3 同步状态指示器

```kotlin
@Composable
fun SyncIndicator(syncState: SyncState) {
    val (color, icon) = when (syncState) {
        is SyncState.Synced  -> Color(0xFF4CAF50) to Icons.Default.CloudDone
        is SyncState.Syncing -> Color(0xFFFFA000) to Icons.Default.CloudSync
        is SyncState.Error   -> Color(0xFFEF4444) to Icons.Default.CloudOff
        SyncState.Idle       -> Color.Gray to Icons.Default.CloudQueue
    }
    Icon(icon, tint = color, modifier = Modifier.size(20.dp), contentDescription = "同步状态")
}
```

---

## 十二、迁移策略

### 12.1 渐进迁移（不丢数据）

```
阶段 1：基础架构（不影响现有功能）
  ├── 新增 Supabase SDK 依赖
  ├── 新增 sync_metadata 表 + Entity 扩展字段（uuid + updatedAt + deletedAt）
  ├── Room Migration 1→2
  └── AuthService + LoginPage（可选，不影响未登录使用）

阶段 2：双向同步上线
  ├── SyncEngine + SyncScheduler（登录后才激活）
  ├── Realtime 订阅
  ├── 家庭共享功能
  └── WebDAV 备份标记为 deprecated

阶段 3：完全切换
  ├── 去掉 WebDAV 备份代码
  ├── 备份配置迁移到 Supabase
  └── Edge Functions（月度报告、推送提醒）
```

### 11.2 首次登录数据迁移

```kotlin
// core/sync/InitialSync.kt
class InitialSync(private val syncEngine: SyncEngine, private val db: AppDatabase) {
    /**
     * 首次登录后，将所有本地数据推送到 Supabase
     * 只在 sync_metadata.lastSyncAt == null 时触发
     */
    suspend fun pushAllLocalData() {
        val babies = db.babyDao().watchAll().first()
        babies.forEach { baby ->
            // 1. 推送 baby，获取云端 UUID
            val remoteId = syncEngine.pushBaby(baby)
            // 2. 更新本地 Entity 的 uuid 字段
            db.babyDao().update(baby.copy(uuid = remoteId.toString()))
            // 3. 推送该 baby 下的所有子表数据
            db.feedingDao().watchByBaby(baby.id).first().forEach { syncEngine.pushFeeding(it, remoteId) }
            db.sleepDao().watchByBaby(baby.id).first().forEach { syncEngine.pushSleep(it, remoteId) }
            db.growthDao().watchByBaby(baby.id).first().forEach { syncEngine.pushGrowth(it, remoteId) }
            db.diaperDao().watchByBaby(baby.id).first().forEach { syncEngine.pushDiaper(it, remoteId) }
            db.vaccinationDao().watchByBaby(baby.id).first().forEach { syncEngine.pushVaccination(it, remoteId) }
            db.healthRecordDao().watchByBaby(baby.id).first().forEach { syncEngine.pushHealth(it, remoteId) }
        }
        // 标记首次同步完成
        syncDao.updateLastSyncAt(Instant.now())
    }
}
```

---

## 十三、Supabase 免费额度评估

| 资源 | 免费额度 | BabyTracker 预估用量 | 是否够用 |
|------|---------|--------------------|:------:|
| 数据库 | 500 MB | 每个家庭约 5 MB（1 年） | ✅ 100 个家庭 |
| Auth | 50,000 MAU | 预计初期 < 1,000 | ✅ |
| Storage | 1 GB | 头像 + 附件约 200 KB/家庭 | ✅ 5,000 家庭 |
| Realtime | 200 并发 | 初期 < 50 | ✅ |
| Edge Functions | 500K 调用/月 | 月度报告 ~1K/月 | ✅ |
| 带宽 | 5 GB | 每次同步约 50 KB | ✅ |

**结论：免费额度足以支撑到数千用户，前期零成本。**

---

## 十四、实施路线图

| 阶段 | 内容 | 工时 |
|:----:|------|:----:|
| **Phase 1** | Supabase 项目创建 + SQL 建表 + RLS + 索引 | 1 天 |
| **Phase 2** | Android Auth 集成（登录/注册页面） | 1.5 天 |
| **Phase 3** | Room Migration + sync_metadata + Entity 扩展 | 1 天 |
| **Phase 4** | SyncEngine 核心（推/拉/冲突处理） | 3 天 |
| **Phase 5** | Realtime 订阅 + SyncScheduler | 1.5 天 |
| **Phase 6** | 家庭共享（邀请码 + 成员管理） | 2 天 |
| **Phase 7** | Storage（头像上传 + 附件） | 1 天 |
| **Phase 8** | UI 改造（同步状态 + 设置页） | 1.5 天 |
| **Phase 9** | Edge Functions（月度报告 + 疫苗提醒） | 1.5 天 |
| **Phase 10** | 测试 + 数据迁移验证 | 2 天 |
| | **合计** | **16 天** |

---

## 十五、风险与应对

| 风险 | 概率 | 影响 | 应对 |
|------|:----:|:----:|------|
| RLS 配置错误导致数据泄露 | 低 | 高 | 上线前安全审计，用不同用户测试越权访问 |
| 同步冲突数据丢失 | 中 | 高 | Last-Write-Wins + 保留 30 天删除记录的 soft-delete |
| Supabase 免费额度不够 | 低 | 中 | 监控用量，超限前升级 Pro（$25/月） |
| Realtime 连接不稳定 | 中 | 低 | 降级为定时轮询（5 分钟），Realtime 仅作加速 |
| 首次全量推送耗时过长 | 中 | 中 | 分批推送（每批 100 条），显示进度条 |
| 国内访问 Supabase 延迟 | 高 | 中 | 可选自建 PostgreSQL + PostgREST，或用 Supabase 亚太节点 |

---
*AI生成*
