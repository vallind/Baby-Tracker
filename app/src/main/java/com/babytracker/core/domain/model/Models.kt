package com.babytracker.core.domain.model

import java.time.LocalDateTime
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain Model 层 — 与持久化层（Room Entity）解耦的纯 Kotlin 数据类。
 *
 * 已包含 uuid/updatedAt/deletedAt 同步字段（与 Entity v6 对齐）。
 */

data class Baby(
    val id: Int = 0,
    val name: String,
    val gender: String,
    val birthDate: String,
    val birthWeight: Double? = null,
    val birthHeight: Double? = null,
    val avatarPath: String? = null,
    val createdAt: String = "",
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

data class Feeding(
    val id: Int = 0,
    val babyId: Int,
    val type: FeedingType,
    val amountMl: Int? = null,
    val durationMin: Int? = null,
    val breastSide: BreastSide? = null,
    val foodName: String? = null,
    val amountG: Int? = null,
    val brand: String? = null,
    val note: String? = null,
    val timestamp: String,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class FeedingType { BREAST, FORMULA, FOOD, WATER;
    companion object {
        fun fromRaw(s: String): FeedingType = when (s.lowercase()) {
            "breast" -> BREAST
            "formula" -> FORMULA
            "food" -> FOOD
            "water" -> WATER
            else -> WATER
        }
        fun raw(value: FeedingType): String = value.name.lowercase()
    }
}

enum class BreastSide { LEFT, RIGHT, BOTH;
    companion object {
        fun fromRaw(s: String?): BreastSide? = when (s?.lowercase()) {
            "左侧" -> LEFT
            "右侧" -> RIGHT
            "双侧" -> BOTH
            else -> null
        }
        fun raw(value: BreastSide?): String? = value?.let {
            when (it) { LEFT -> "左侧"; RIGHT -> "右侧"; BOTH -> "双侧" }
        }
    }
}

data class Sleep(
    val id: Int = 0,
    val babyId: Int,
    val type: SleepType,
    val startTime: String,
    val endTime: String,
    val note: String? = null,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class SleepType { NIGHT, NAP;
    companion object {
        fun fromRaw(s: String): SleepType = if (s.lowercase() == "night") NIGHT else NAP
        fun raw(value: SleepType): String = if (value == NIGHT) "night" else "nap"
    }
}

data class Growth(
    val id: Int = 0,
    val babyId: Int,
    val type: GrowthType,
    val value: Double,
    val measuredAt: String,
    val note: String? = null,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class GrowthType { WEIGHT, HEIGHT, HEAD;
    companion object {
        fun fromRaw(s: String): GrowthType = when (s.lowercase()) {
            "weight" -> WEIGHT
            "height" -> HEIGHT
            "head" -> HEAD
            else -> WEIGHT
        }
        fun raw(value: GrowthType): String = value.name.lowercase()
    }
}

data class Vaccination(
    val id: Int = 0,
    val babyId: Int,
    val name: String,
    val dose: String? = null,
    val scheduledDate: String? = null,
    val administeredDate: String? = null,
    val status: VaccinationStatus = VaccinationStatus.PENDING,
    val note: String? = null,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class VaccinationStatus { PENDING, DONE, SKIPPED;
    companion object {
        fun fromRaw(s: String): VaccinationStatus = when (s.lowercase()) {
            "pending" -> PENDING
            "done" -> DONE
            "skipped" -> SKIPPED
            else -> PENDING
        }
        fun raw(value: VaccinationStatus): String = value.name.lowercase()
    }
}

data class HealthRecord(
    val id: Int = 0,
    val babyId: Int,
    val category: String,
    val description: String,
    val doctorName: String? = null,
    val recordDate: String,
    val attachments: String? = null,
    val note: String? = null,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

data class Diaper(
    val id: Int = 0,
    val babyId: Int,
    val type: DiaperType,
    val timestamp: String,
    val note: String? = null,
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class DiaperType { WET, POOP, BOTH;
    companion object {
        fun fromRaw(s: String): DiaperType = when (s.lowercase()) {
            "wet" -> WET
            "poop" -> POOP
            "both" -> BOTH
            else -> WET
        }
        fun raw(value: DiaperType): String = value.name.lowercase()
    }
}

data class BackupConfig(
    val id: Int = 0,
    val webdavUrl: String? = null,
    val webdavUser: String? = null,
    val webdavPass: String? = null,
    val autoBackup: Boolean = false,
    val lastBackupAt: String? = null,
)

/**
 * 提醒中心 domain 模型 —— 待办提醒 + 历史提醒。
 *
 * 类型覆盖：疫苗 / 体检 / 用药 / 发育评估 / 其他。
 * - 一次性提醒（VACCINE / CHECKUP / ASSESSMENT）：到期日触发，完成后归入历史。
 * - 重复提醒（MEDICATION）：每日 / 每周，可用 isEnabled 开关暂停。
 *
 * 注：id/babyId 用 Int 与 BabyEntity 对齐（FK 类型必须一致）。
 *     dueDate / doneDate 用 LocalDateTime（domain 层），Entity 层存 epoch milli。
 */
data class Reminder(
    val id: Int = 0,
    val babyId: Int,
    val type: ReminderType,
    val title: String,              // "13价肺炎疫苗 第4剂"
    val description: String = "",   // 详细描述
    val dueDate: LocalDateTime,     // 到期日期
    val isDone: Boolean = false,    // 是否已完成（历史提醒）
    val doneDate: LocalDateTime? = null,
    val isEnabled: Boolean = true,  // 是否启用（用药类每日提醒可开关）
    val repeatRule: String = "",    // "每日" / "每周一" / ""（一次性）
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

enum class ReminderType { VACCINE, CHECKUP, MEDICATION, ASSESSMENT, OTHER;
    companion object {
        fun fromRaw(s: String): ReminderType = when (s.uppercase()) {
            "VACCINE" -> VACCINE
            "CHECKUP" -> CHECKUP
            "MEDICATION" -> MEDICATION
            "ASSESSMENT" -> ASSESSMENT
            "OTHER" -> OTHER
            else -> OTHER
        }
        fun raw(value: ReminderType): String = value.name
    }
}

// —— 发育评估领域模型 ——
// 注：id/babyId 用 Int 与 BabyEntity.id 及其它领域模型保持一致（Room ForeignKey 类型对齐）。

/**
 * 发育评估领域模型 — 评估宝宝 5 项能力（大运动/精细动作/语言/社交/认知）。
 *
 * 评分含义：0=未观察 1=落后 2=正常 3=超前
 */
data class DevelopmentAssessment(
    val id: Int = 0,
    val babyId: Int,
    val assessDate: LocalDateTime,   // 评估日期
    val babyAgeMonths: Int,          // 评估时月龄
    // 5 项能力评估
    val grossMotor: Int,             // 大运动
    val fineMotor: Int,              // 精细动作
    val language: Int,               // 语言能力
    val social: Int,                 // 社交能力
    val cognitive: Int,              // 认知能力
    val note: String = "",           // 备注
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)

/**
 * 评估项元数据（UI 展示用） — 由 Screen 将 [DevelopmentAssessment] 的 5 个分项映射为此模型。
 */
data class AssessmentItem(
    val title: String,               // "大运动"
    val icon: ImageVector,           // 对应图标
    val score: Int,                  // 0-3
    val description: String,         // 评估描述文本
)

// —— 消息中心 —— 消息为 App 级（无 babyId 外键）

enum class MessageType { INTERACTION, SYSTEM, SERVICE;
    companion object {
        fun fromRaw(s: String): MessageType = when (s.uppercase()) {
            "INTERACTION" -> INTERACTION
            "SYSTEM" -> SYSTEM
            "SERVICE" -> SERVICE
            else -> SYSTEM
        }
        fun raw(value: MessageType): String = value.name
    }
}

/**
 * 消息中心统一消息体。
 * - [MessageType.INTERACTION] 互动消息（其他用户点赞/评论，[senderAvatar] 必填）
 * - [MessageType.SYSTEM] 系统通知（月度报告生成等，[senderAvatar] 可空）
 * - [MessageType.SERVICE] 服务通知（体检套餐优惠等，[senderAvatar] 可空）
 *
 * [extraData] 为附加 JSON 字符串，可用于携带跳转目标等扩展信息。
 */
data class AppMessage(
    val id: Long = 0,
    val type: MessageType,
    val title: String,
    val content: String,
    val senderAvatar: String? = null,
    val createTime: LocalDateTime,
    val isRead: Boolean = false,
    val extraData: String = "",
    val uuid: String? = null,
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
)
