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
        DiaperEntity::class, BackupConfigEntity::class],
    version = 2, exportSchema = false,
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

        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "babytracker.db")
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }
    }
}
