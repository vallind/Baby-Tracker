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
        SyncMetadataEntity::class],
    version = 6, exportSchema = false,
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

        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "babytracker.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build().also { instance = it }
            }
        }
    }
}
