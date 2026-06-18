package com.babytracker.data.mapper

import com.babytracker.core.database.entity.*
import com.babytracker.domain.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dtFmt = DateTimeFormatter.ISO_DATE_TIME
private val dFmt = DateTimeFormatter.ISO_DATE

fun BabyEntity.toDomain() = Baby(
    id = id, name = name, gender = gender,
    birthDate = try { LocalDate.parse(birthDate) } catch (_: Exception) { LocalDate.MIN },
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = try { LocalDateTime.parse(createdAt, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
)

fun Baby.toEntity() = BabyEntity(
    id = id, name = name, gender = gender,
    birthDate = birthDate.toString(),
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = createdAt.format(dtFmt),
)

fun FeedingEntity.toDomain() = Feeding(
    id = id, babyId = babyId, type = type,
    amountMl = amountMl, durationMin = durationMin,
    breastSide = breastSide, foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = try { LocalDateTime.parse(timestamp, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
)

fun Feeding.toEntity() = FeedingEntity(
    id = id, babyId = babyId, type = type,
    amountMl = amountMl, durationMin = durationMin,
    breastSide = breastSide, foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = timestamp.format(dtFmt),
)

fun SleepEntity.toDomain() = Sleep(
    id = id, babyId = babyId, type = type,
    startTime = try { LocalDateTime.parse(startTime, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
    endTime = try { LocalDateTime.parse(endTime, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
    note = note,
)

fun Sleep.toEntity() = SleepEntity(
    id = id, babyId = babyId, type = type,
    startTime = startTime.format(dtFmt),
    endTime = endTime.format(dtFmt),
    note = note,
)

fun DiaperEntity.toDomain() = Diaper(
    id = id, babyId = babyId, type = type,
    timestamp = try { LocalDateTime.parse(timestamp, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
    note = note,
)

fun Diaper.toEntity() = DiaperEntity(
    id = id, babyId = babyId, type = type,
    timestamp = timestamp.format(dtFmt),
    note = note,
)

fun GrowthEntity.toDomain() = Growth(
    id = id, babyId = babyId, type = type, value = value,
    measuredAt = try { LocalDateTime.parse(measuredAt, dtFmt) } catch (_: Exception) { LocalDateTime.MIN },
    note = note,
)

fun Growth.toEntity() = GrowthEntity(
    id = id, babyId = babyId, type = type, value = value,
    measuredAt = measuredAt.format(dtFmt),
    note = note,
)

fun VaccinationEntity.toDomain() = Vaccination(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = try { scheduledDate?.let { LocalDate.parse(it.take(10)) } } catch (_: Exception) { null },
    administeredDate = try { administeredDate?.let { LocalDate.parse(it.take(10)) } } catch (_: Exception) { null },
    status = status, note = note,
)

fun Vaccination.toEntity() = VaccinationEntity(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = scheduledDate?.toString()?.let { "${it}T00:00:00" },
    administeredDate = administeredDate?.toString()?.let { "${it}T00:00:00" },
    status = status, note = note,
)

fun HealthRecordEntity.toDomain() = HealthRecord(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = try { LocalDate.parse(recordDate.take(10)) } catch (_: Exception) { LocalDate.MIN },
    attachments = attachments, note = note,
)

fun HealthRecord.toEntity() = HealthRecordEntity(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = "${recordDate}T00:00:00",
    attachments = attachments, note = note,
)

fun BackupConfigEntity.toDomain() = BackupConfig(
    id = id, webdavUrl = webdavUrl, webdavUser = webdavUser,
    webdavPass = webdavPass, autoBackup = autoBackup,
    lastBackupAt = try { lastBackupAt?.let { LocalDateTime.parse(it, dtFmt) } } catch (_: Exception) { null },
)

fun BackupConfig.toEntity() = BackupConfigEntity(
    id = id, webdavUrl = webdavUrl, webdavUser = webdavUser,
    webdavPass = webdavPass, autoBackup = autoBackup,
    lastBackupAt = lastBackupAt?.format(dtFmt),
)
