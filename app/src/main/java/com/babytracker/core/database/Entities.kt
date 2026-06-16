package com.babytracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "babies")
data class BabyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val gender: String,
    val birthDate: String,
    val birthWeight: Double? = null,
    val birthHeight: Double? = null,
    val avatarPath: String? = null,
    val createdAt: String = "",
)

@Entity(tableName = "feedings")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val type: String,
    val amountMl: Int? = null,
    val durationMin: Int? = null,
    val breastSide: String? = null,
    val foodName: String? = null,
    val amountG: Int? = null,
    val brand: String? = null,
    val note: String? = null,
    val timestamp: String,
)

@Entity(tableName = "sleeps")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val type: String,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    val note: String? = null,
)

@Entity(tableName = "growths")
data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val type: String,
    val value: Double,
    @ColumnInfo(name = "measured_at") val measuredAt: String,
    val note: String? = null,
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val name: String,
    val dose: String? = null,
    @ColumnInfo(name = "scheduled_date") val scheduledDate: String? = null,
    @ColumnInfo(name = "administered_date") val administeredDate: String? = null,
    val status: String = "pending",
    val note: String? = null,
)

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val category: String,
    val description: String,
    @ColumnInfo(name = "doctor_name") val doctorName: String? = null,
    @ColumnInfo(name = "record_date") val recordDate: String,
    val attachments: String? = null,
    val note: String? = null,
)

@Entity(tableName = "diapers")
data class DiaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val type: String,
    val timestamp: String,
    val note: String? = null,
)

@Entity(tableName = "backup_config")
data class BackupConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val webdavUrl: String? = null,
    val webdavUser: String? = null,
    val webdavPass: String? = null,
    val autoBackup: Boolean = false,
    @ColumnInfo(name = "last_backup_at") val lastBackupAt: String? = null,
)
