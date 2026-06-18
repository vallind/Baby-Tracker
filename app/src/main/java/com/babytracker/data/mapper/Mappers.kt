package com.babytracker.data.mapper

import com.babytracker.core.database.entity.*
import com.babytracker.domain.model.*

fun BabyEntity.toDomain() = Baby(
    id = id, name = name, gender = gender,
    birthDate = birthDate,
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = createdAt,
)

fun Baby.toEntity() = BabyEntity(
    id = id, name = name, gender = gender,
    birthDate = birthDate,
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = createdAt,
)

fun FeedingEntity.toDomain() = Feeding(
    id = id, babyId = babyId, type = FeedingType.fromRaw(type),
    amountMl = amountMl, durationMin = durationMin,
    breastSide = BreastSide.fromRaw(breastSide), foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = timestamp,
)

fun Feeding.toEntity() = FeedingEntity(
    id = id, babyId = babyId, type = FeedingType.raw(type),
    amountMl = amountMl, durationMin = durationMin,
    breastSide = BreastSide.raw(breastSide), foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = timestamp,
)

fun SleepEntity.toDomain() = Sleep(
    id = id, babyId = babyId, type = SleepType.fromRaw(type),
    startTime = startTime,
    endTime = endTime,
    note = note,
)

fun Sleep.toEntity() = SleepEntity(
    id = id, babyId = babyId, type = SleepType.raw(type),
    startTime = startTime,
    endTime = endTime,
    note = note,
)

fun DiaperEntity.toDomain() = Diaper(
    id = id, babyId = babyId, type = DiaperType.fromRaw(type),
    timestamp = timestamp,
    note = note,
)

fun Diaper.toEntity() = DiaperEntity(
    id = id, babyId = babyId, type = DiaperType.raw(type),
    timestamp = timestamp,
    note = note,
)

fun GrowthEntity.toDomain() = Growth(
    id = id, babyId = babyId, type = GrowthType.fromRaw(type), value = value,
    measuredAt = measuredAt,
    note = note,
)

fun Growth.toEntity() = GrowthEntity(
    id = id, babyId = babyId, type = GrowthType.raw(type), value = value,
    measuredAt = measuredAt,
    note = note,
)

fun VaccinationEntity.toDomain() = Vaccination(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = scheduledDate,
    administeredDate = administeredDate,
    status = VaccinationStatus.fromRaw(status), note = note,
)

fun Vaccination.toEntity() = VaccinationEntity(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = scheduledDate,
    administeredDate = administeredDate,
    status = VaccinationStatus.raw(status), note = note,
)

fun HealthRecordEntity.toDomain() = HealthRecord(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = recordDate,
    attachments = attachments, note = note,
)

fun HealthRecord.toEntity() = HealthRecordEntity(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = recordDate,
    attachments = attachments, note = note,
)

fun BackupConfigEntity.toDomain() = BackupConfig(
    id = id, webdavUrl = webdavUrl, webdavUser = webdavUser,
    webdavPass = webdavPass, autoBackup = autoBackup,
    lastBackupAt = lastBackupAt,
)

fun BackupConfig.toEntity() = BackupConfigEntity(
    id = id, webdavUrl = webdavUrl, webdavUser = webdavUser,
    webdavPass = webdavPass, autoBackup = autoBackup,
    lastBackupAt = lastBackupAt,
)
