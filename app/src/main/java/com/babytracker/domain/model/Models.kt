package com.babytracker.domain.model

/**
 * Domain Model 层 — 与持久化层（Room Entity）解耦的纯 Kotlin 数据类。
 *
 * 当前阶段仅定义类型，未在 Repository/UI 层使用。
 * 后续重构步骤：
 *   1. Repository 接口改为返回 Flow<List<DomainModel>>
 *   2. RepositoryImpl 内部 Entity → DomainModel 映射
 *   3. UI 层 import 从 entity.* 改为 domain.model.*
 *
 * 当前 Repository 仍返回 Entity，UI 仍直接用 Entity。
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
)

data class Diaper(
    val id: Int = 0,
    val babyId: Int,
    val type: DiaperType,
    val timestamp: String,
    val note: String? = null,
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
