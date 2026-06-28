package com.babytracker.core.data.mapper

import com.babytracker.core.database.entity.*
import com.babytracker.core.domain.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun BabyEntity.toDomain() = Baby(
    id = id, name = name, gender = gender,
    birthDate = birthDate,
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = createdAt,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Baby.toEntity() = BabyEntity(
    id = id, name = name, gender = gender,
    birthDate = birthDate,
    birthWeight = birthWeight, birthHeight = birthHeight, avatarPath = avatarPath,
    createdAt = createdAt,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun FeedingEntity.toDomain() = Feeding(
    id = id, babyId = babyId, type = FeedingType.fromRaw(type),
    amountMl = amountMl, durationMin = durationMin,
    breastSide = BreastSide.fromRaw(breastSide), foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = timestamp,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Feeding.toEntity() = FeedingEntity(
    id = id, babyId = babyId, type = FeedingType.raw(type),
    amountMl = amountMl, durationMin = durationMin,
    breastSide = BreastSide.raw(breastSide), foodName = foodName, amountG = amountG,
    brand = brand, note = note,
    timestamp = timestamp,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun SleepEntity.toDomain() = Sleep(
    id = id, babyId = babyId, type = SleepType.fromRaw(type),
    startTime = startTime,
    endTime = endTime,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Sleep.toEntity() = SleepEntity(
    id = id, babyId = babyId, type = SleepType.raw(type),
    startTime = startTime,
    endTime = endTime,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun DiaperEntity.toDomain() = Diaper(
    id = id, babyId = babyId, type = DiaperType.fromRaw(type),
    timestamp = timestamp,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Diaper.toEntity() = DiaperEntity(
    id = id, babyId = babyId, type = DiaperType.raw(type),
    timestamp = timestamp,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun GrowthEntity.toDomain() = Growth(
    id = id, babyId = babyId, type = GrowthType.fromRaw(type), value = value,
    measuredAt = measuredAt,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Growth.toEntity() = GrowthEntity(
    id = id, babyId = babyId, type = GrowthType.raw(type), value = value,
    measuredAt = measuredAt,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun VaccinationEntity.toDomain() = Vaccination(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = scheduledDate,
    administeredDate = administeredDate,
    status = VaccinationStatus.fromRaw(status), note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Vaccination.toEntity() = VaccinationEntity(
    id = id, babyId = babyId, name = name, dose = dose,
    scheduledDate = scheduledDate,
    administeredDate = administeredDate,
    status = VaccinationStatus.raw(status), note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun HealthRecordEntity.toDomain() = HealthRecord(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = recordDate,
    attachments = attachments, note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun HealthRecord.toEntity() = HealthRecordEntity(
    id = id, babyId = babyId, category = category,
    description = description, doctorName = doctorName,
    recordDate = recordDate,
    attachments = attachments, note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
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

// —— 消息中心 —— Entity ↔ Domain 双向映射

fun MessageEntity.toDomain(): AppMessage = AppMessage(
    id = id,
    type = MessageType.fromRaw(type),
    title = title,
    content = content,
    senderAvatar = senderAvatar,
    createTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(createTime),
        ZoneId.systemDefault(),
    ),
    isRead = isRead,
    extraData = extraData,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun AppMessage.toEntity(): MessageEntity = MessageEntity(
    id = id,
    type = MessageType.raw(type),
    title = title,
    content = content,
    senderAvatar = senderAvatar,
    createTime = createTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    isRead = isRead,
    extraData = extraData,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

// —— 发育评估 —— Entity ↔ Domain 双向映射（assessDate: Long(epoch milli) ↔ LocalDateTime）

fun DevelopmentAssessmentEntity.toDomain(): DevelopmentAssessment = DevelopmentAssessment(
    id = id,
    babyId = babyId,
    assessDate = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(assessDate),
        ZoneId.systemDefault(),
    ),
    babyAgeMonths = babyAgeMonths,
    grossMotor = grossMotor,
    fineMotor = fineMotor,
    language = language,
    social = social,
    cognitive = cognitive,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun DevelopmentAssessment.toEntity(): DevelopmentAssessmentEntity = DevelopmentAssessmentEntity(
    id = id,
    babyId = babyId,
    assessDate = assessDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    babyAgeMonths = babyAgeMonths,
    grossMotor = grossMotor,
    fineMotor = fineMotor,
    language = language,
    social = social,
    cognitive = cognitive,
    note = note,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

// —— 提醒中心 —— Entity ↔ Domain 双向映射
//   ReminderType enum ↔ String（type.name）
//   LocalDateTime ↔ epoch milli（dueDate / doneDate）

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    babyId = babyId,
    type = ReminderType.fromRaw(type),
    title = title,
    description = description,
    dueDate = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(dueDate),
        ZoneId.systemDefault(),
    ),
    isDone = isDone,
    doneDate = doneDate?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    },
    isEnabled = isEnabled,
    repeatRule = repeatRule,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    babyId = babyId,
    type = ReminderType.raw(type),
    title = title,
    description = description,
    dueDate = dueDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    isDone = isDone,
    doneDate = doneDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    isEnabled = isEnabled,
    repeatRule = repeatRule,
    uuid = uuid, updatedAt = updatedAt, deletedAt = deletedAt,
)
