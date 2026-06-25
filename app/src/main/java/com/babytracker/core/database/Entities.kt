package com.babytracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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

/**
 * 消息中心实体。App 级消息，无 babyId 外键。
 * type 存 MessageType.name（INTERACTION / SYSTEM / SERVICE）。
 * createTime 存 epoch milli。
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,           // MessageType.name
    val title: String,
    val content: String,
    val senderAvatar: String? = null,
    val createTime: Long,       // epoch milli
    val isRead: Boolean = false,
    val extraData: String = "",
)

/**
 * 发育评估实体 — 评估宝宝 5 项能力（大运动/精细动作/语言/社交/认知）。
 *
 * 评分含义：0=未观察 1=落后 2=正常 3=超前
 * assessDate 存 epoch milli。
 *
 * 注：babyId 类型为 Int，与 [BabyEntity.id] 对齐（Room ForeignKey 要求父子列类型一致）。
 */
@Entity(
    tableName = "development_assessments",
    foreignKeys = [
        ForeignKey(
            entity = BabyEntity::class,
            parentColumns = ["id"],
            childColumns = ["baby_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("baby_id")],
)
data class DevelopmentAssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    @ColumnInfo(name = "assess_date") val assessDate: Long,   // epoch milli
    @ColumnInfo(name = "baby_age_months") val babyAgeMonths: Int,
    val grossMotor: Int,          // 大运动 0-3
    val fineMotor: Int,           // 精细动作 0-3
    val language: Int,            // 语言能力 0-3
    val social: Int,              // 社交能力 0-3
    val cognitive: Int,           // 认知能力 0-3
    val note: String = "",
)

/**
 * 提醒中心实体 —— 待办提醒 + 历史提醒。
 *
 * 类型覆盖：疫苗 / 体检 / 用药 / 发育评估 / 其他（type 存 ReminderType.name）。
 * - dueDate / doneDate 存 epoch milli。
 * - isDone = 0 → 待办；isDone = 1 → 历史。
 * - isEnabled 用于用药类每日提醒开关。
 * - repeatRule: "每日" / "每周一" / ""（一次性）。
 *
 * 注：babyId 类型为 Int，与 [BabyEntity.id] 对齐（Room ForeignKey 要求父子列类型一致）。
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = BabyEntity::class,
            parentColumns = ["id"],
            childColumns = ["baby_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("baby_id")],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "baby_id") val babyId: Int,
    val type: String,           // ReminderType.name
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "due_date") val dueDate: Long,          // epoch milli
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
    @ColumnInfo(name = "done_date") val doneDate: Long? = null,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "repeat_rule") val repeatRule: String = "",
)
