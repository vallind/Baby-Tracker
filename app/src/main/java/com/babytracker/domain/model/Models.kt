package com.babytracker.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Baby(
    val id: Int = 0,
    val name: String,
    val gender: String,
    val birthDate: LocalDate,
    val birthWeight: Double? = null,
    val birthHeight: Double? = null,
    val avatarPath: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.MIN,
)

data class Feeding(
    val id: Int = 0,
    val babyId: Int,
    val type: String,
    val amountMl: Int? = null,
    val durationMin: Int? = null,
    val breastSide: String? = null,
    val foodName: String? = null,
    val amountG: Int? = null,
    val brand: String? = null,
    val note: String? = null,
    val timestamp: LocalDateTime,
)

data class Sleep(
    val id: Int = 0,
    val babyId: Int,
    val type: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val note: String? = null,
)

data class Diaper(
    val id: Int = 0,
    val babyId: Int,
    val type: String,
    val timestamp: LocalDateTime,
    val note: String? = null,
)

data class Growth(
    val id: Int = 0,
    val babyId: Int,
    val type: String,
    val value: Double,
    val measuredAt: LocalDateTime,
    val note: String? = null,
)

data class Vaccination(
    val id: Int = 0,
    val babyId: Int,
    val name: String,
    val dose: String? = null,
    val scheduledDate: LocalDate? = null,
    val administeredDate: LocalDate? = null,
    val status: String = "pending",
    val note: String? = null,
)

data class HealthRecord(
    val id: Int = 0,
    val babyId: Int,
    val category: String,
    val description: String,
    val doctorName: String? = null,
    val recordDate: LocalDate,
    val attachments: String? = null,
    val note: String? = null,
)

data class BackupConfig(
    val id: Int = 0,
    val webdavUrl: String? = null,
    val webdavUser: String? = null,
    val webdavPass: String? = null,
    val autoBackup: Boolean = false,
    val lastBackupAt: LocalDateTime? = null,
)
