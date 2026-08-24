package com.babytracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.babytracker.core.database.dao.*
import com.babytracker.core.database.entity.*

@Database(
    entities = [BabyEntity::class, FeedingEntity::class, SleepEntity::class,
        GrowthEntity::class, VaccinationEntity::class, HealthRecordEntity::class,
        DiaperEntity::class, BackupConfigEntity::class, MessageEntity::class,
        DevelopmentAssessmentEntity::class, ReminderEntity::class,
        SyncMetadataEntity::class, SyncCursorEntity::class,
        AiConversationEntity::class, AiMessageEntity::class],
    version = 9, exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun feedingDao(): FeedingDao
    abstract fun sleepDao(): SleepDao
    abstract fun growthDao(): GrowthDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun diaperDao(): DiaperDao
    abstract fun backupConfigDao(): BackupConfigDao
    abstract fun messageDao(): MessageDao
    abstract fun developmentAssessmentDao(): DevelopmentAssessmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun syncCursorDao(): SyncCursorDao
    abstract fun aiHistoryDao(): AiHistoryDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS diapers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        baby_id INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        timestamp TEXT NOT NULL,
                        note TEXT
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        senderAvatar TEXT,
                        createTime INTEGER NOT NULL,
                        isRead INTEGER NOT NULL,
                        extraData TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // 发育评估表 —— 字段类型/列名须与 DevelopmentAssessmentEntity 严格一致（Room schema 校验）：
        //  - id Int PK autoGenerate
        //  - baby_id / assess_date / baby_age_months: @ColumnInfo 改名 snake_case
        //  - grossMotor / fineMotor / language / social / cognitive: 未改名 → 列名同属性名 camelCase
        //  - note String (Kotlin 默认值 "" 由 Room 在构造层处理，SQL 列仍为 NOT NULL 无 DEFAULT)
        //  - LocalDateTime → epoch milli (Long)
        //  - 外键 babies(id) ON DELETE CASCADE；索引名遵循 Room 自动生成命名 index_<table>_<column>
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS development_assessments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        baby_id INTEGER NOT NULL,
                        assess_date INTEGER NOT NULL,
                        baby_age_months INTEGER NOT NULL,
                        grossMotor INTEGER NOT NULL,
                        fineMotor INTEGER NOT NULL,
                        language INTEGER NOT NULL,
                        social INTEGER NOT NULL,
                        cognitive INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        FOREIGN KEY(baby_id) REFERENCES babies(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_development_assessments_baby_id ON development_assessments(baby_id)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 提醒中心表 —— 字段类型/可空性须与 ReminderEntity 严格一致（Room schema 校验）
                // Boolean → INTEGER NOT NULL；String(默认值) → TEXT NOT NULL（Kotlin 默认值不写入 SQL）
                // LocalDateTime → epoch milli (Long)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        baby_id INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        due_date INTEGER NOT NULL,
                        is_done INTEGER NOT NULL,
                        done_date INTEGER,
                        is_enabled INTEGER NOT NULL,
                        repeat_rule TEXT NOT NULL,
                        FOREIGN KEY(baby_id) REFERENCES babies(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                // 索引名须与 Room 自动生成的命名一致：index_<table>_<column>
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_baby_id ON reminders(baby_id)")
            }
        }

        // Supabase 同步：为 10 张业务表添加 uuid / updatedAt / deletedAt，并创建 sync_metadata 表
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val tables = listOf(
                    "babies", "feedings", "sleeps", "growths", "diapers",
                    "vaccinations", "health_records", "reminders",
                    "development_assessments", "messages"
                )
                for (table in tables) {
                    db.execSQL("ALTER TABLE $table ADD COLUMN uuid TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE $table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE $table ADD COLUMN deletedAt INTEGER DEFAULT NULL")
                }
                // 同步元数据表
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
                """.trimIndent())
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE babies ADD COLUMN familyId TEXT")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_metadata_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tableName TEXT NOT NULL,
                        localId INTEGER NOT NULL,
                        remoteUuid TEXT,
                        syncStatus TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        lastSyncAt INTEGER,
                        familyId TEXT,
                        retryCount INTEGER NOT NULL,
                        nextRetryAt INTEGER NOT NULL,
                        lastError TEXT
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO sync_metadata_new
                        (id, tableName, localId, remoteUuid, syncStatus, updatedAt, lastSyncAt, retryCount, nextRetryAt)
                    SELECT id, tableName, localId, remoteUuid, syncStatus, updatedAt, lastSyncAt, 0, 0
                    FROM sync_metadata
                    WHERE id IN (
                        SELECT MAX(id) FROM sync_metadata GROUP BY tableName, localId
                    )
                """.trimIndent())
                db.execSQL("DROP TABLE sync_metadata")
                db.execSQL("ALTER TABLE sync_metadata_new RENAME TO sync_metadata")
                db.execSQL("CREATE UNIQUE INDEX index_sync_metadata_tableName_localId ON sync_metadata(tableName, localId)")
                db.execSQL("DELETE FROM sync_metadata WHERE tableName = 'messages'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_cursors (
                        familyId TEXT NOT NULL,
                        tableName TEXT NOT NULL,
                        lastVersion INTEGER NOT NULL,
                        PRIMARY KEY(familyId, tableName)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_conversations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        family_id TEXT NOT NULL,
                        baby_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(baby_id) REFERENCES babies(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_ai_conversations_family_id_baby_id_updated_at
                    ON ai_conversations(family_id, baby_id, updated_at)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_ai_conversations_baby_id
                    ON ai_conversations(baby_id)
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conversation_id INTEGER NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        reasoning_content TEXT NOT NULL,
                        provider_id TEXT,
                        model TEXT,
                        references_json TEXT NOT NULL,
                        risk_level TEXT,
                        safety_status TEXT,
                        position INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(conversation_id)
                            REFERENCES ai_conversations(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_ai_messages_conversation_id
                    ON ai_messages(conversation_id)
                    """.trimIndent(),
                )
            }
        }

        // Batch 6：六张业务表补 baby_id 外键（不带 CASCADE）+ baby_id 索引；babies 补 familyId 索引。
        // 迁移原则（评审确认）：迁移默认不做不可逆数据删除 —— 第一步六表孤儿预检，
        // 任一表存在孤儿（baby_id 不在 babies）即抛异常失败，绝不在迁移内自动 DELETE；
        // 孤儿可能来自旧版 bug/同步顺序/备份恢复/家庭数据迁移，是否清理由人工决定。
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ── 0) 孤儿预检（任何 DDL 之前）──
                val orphanTables = listOf(
                    "feedings", "sleeps", "growths", "vaccinations", "health_records", "diapers",
                )
                val orphanCounts = orphanTables.mapNotNull { table ->
                    val count = db.query("SELECT COUNT(*) AS c FROM $table WHERE baby_id NOT IN (SELECT id FROM babies)").use { cursor ->
                        if (cursor.moveToFirst()) cursor.getLong(0) else 0L
                    }
                    if (count > 0) "$table=$count" else null
                }
                if (orphanCounts.isNotEmpty()) {
                    throw IllegalStateException(
                        "迁移 8→9 孤儿预检失败（存在 baby_id 不在 babies 的记录）：${orphanCounts.joinToString(", ")}。" +
                            "请人工决定：恢复对应宝宝或确认清理后重试，迁移不会自动删除任何数据。",
                    )
                }

                // ── 1) babies.familyId 索引（纯增索引，无重建）──
                db.execSQL("CREATE INDEX IF NOT EXISTS index_babies_familyId ON babies(familyId)")

                // ── 2) 六张表 12 步重建：建新表（含 FK）→ INSERT SELECT → DROP → RENAME → CREATE INDEX ──
                // 列顺序/类型与 Entity 声明严格一致（Room v9 schema 校验）：
                // id INTEGER PK AUTOINC / baby_id INTEGER NOT NULL + FK / 业务列 / 同步列 TEXT/INTEGER
                rebuildWithBabyFk(
                    db, table = "feedings",
                    columns = "id, baby_id, type, amountMl, durationMin, breastSide, foodName, amountG, brand, note, timestamp, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE feedings_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            type TEXT NOT NULL,
                            amountMl INTEGER,
                            durationMin INTEGER,
                            breastSide TEXT,
                            foodName TEXT,
                            amountG INTEGER,
                            brand TEXT,
                            note TEXT,
                            timestamp TEXT NOT NULL,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
                rebuildWithBabyFk(
                    db, table = "sleeps",
                    columns = "id, baby_id, type, start_time, end_time, note, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE sleeps_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            type TEXT NOT NULL,
                            start_time TEXT NOT NULL,
                            end_time TEXT NOT NULL,
                            note TEXT,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
                rebuildWithBabyFk(
                    db, table = "growths",
                    columns = "id, baby_id, type, value, measured_at, note, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE growths_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            type TEXT NOT NULL,
                            value REAL NOT NULL,
                            measured_at TEXT NOT NULL,
                            note TEXT,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
                rebuildWithBabyFk(
                    db, table = "vaccinations",
                    columns = "id, baby_id, name, dose, scheduled_date, administered_date, status, note, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE vaccinations_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            name TEXT NOT NULL,
                            dose TEXT,
                            scheduled_date TEXT,
                            administered_date TEXT,
                            status TEXT NOT NULL,
                            note TEXT,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
                rebuildWithBabyFk(
                    db, table = "health_records",
                    columns = "id, baby_id, category, description, doctor_name, record_date, attachments, note, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE health_records_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            category TEXT NOT NULL,
                            description TEXT NOT NULL,
                            doctor_name TEXT,
                            record_date TEXT NOT NULL,
                            attachments TEXT,
                            note TEXT,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
                rebuildWithBabyFk(
                    db, table = "diapers",
                    columns = "id, baby_id, type, timestamp, note, uuid, updatedAt, deletedAt",
                    createSql = """
                        CREATE TABLE diapers_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            baby_id INTEGER NOT NULL,
                            type TEXT NOT NULL,
                            timestamp TEXT NOT NULL,
                            note TEXT,
                            uuid TEXT,
                            updatedAt INTEGER NOT NULL,
                            deletedAt INTEGER,
                            FOREIGN KEY(baby_id) REFERENCES babies(id)
                        )
                    """.trimIndent(),
                )
            }
        }

        /** 12 步重建辅助：建新表（含 FK）→ 拷贝 → 换名 → 补索引（SQLite 无法 ALTER 加 FK） */
        private fun rebuildWithBabyFk(db: SupportSQLiteDatabase, table: String, columns: String, createSql: String) {
            db.execSQL(createSql)
            db.execSQL("INSERT INTO ${table}_new ($columns) SELECT $columns FROM $table")
            db.execSQL("DROP TABLE $table")
            db.execSQL("ALTER TABLE ${table}_new RENAME TO $table")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_${table}_baby_id ON $table(baby_id)")
        }

        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "babytracker.db")
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                    )
                    .build().also { instance = it }
            }
        }
    }
}
