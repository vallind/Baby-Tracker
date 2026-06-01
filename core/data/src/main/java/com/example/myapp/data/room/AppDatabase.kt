package com.example.myapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BabyEntity::class,
        FeedingEntity::class,
        SleepEntity::class,
        GrowthEntity::class,
        VaccineEntity::class,
        HealthProfileEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun feedingDao(): FeedingDao
    abstract fun sleepDao(): SleepDao
    abstract fun growthDao(): GrowthDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun healthProfileDao(): HealthProfileDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "baby_tracker_v2.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
