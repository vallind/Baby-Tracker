package com.babytracker.core.data.repository

import com.babytracker.core.database.AppDatabase
import com.babytracker.core.database.dao.*
import com.babytracker.core.database.entity.SyncMetadataEntity
import com.babytracker.core.data.mapper.toDomain
import com.babytracker.core.data.mapper.toEntity
import com.babytracker.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID

private val nowEpoch get() = System.currentTimeMillis()
private fun newUuid() = UUID.randomUUID().toString()

/** 标记本地记录为待同步到 Supabase */
private suspend fun SyncMetadataDao.pendingChange(tableName: String, localId: Int, uuid: String?, updatedAt: Long) {
    insert(SyncMetadataEntity(tableName = tableName, localId = localId, remoteUuid = uuid, syncStatus = "pending", updatedAt = updatedAt))
}

// ── 宝宝 ──

interface BabyRepository {
    fun watchAll(): Flow<List<Baby>>
    suspend fun getById(id: Int): Baby?
    suspend fun insert(baby: Baby): Long
    suspend fun update(baby: Baby)
    suspend fun delete(baby: Baby)
    suspend fun restore(baby: Baby)
}

class BabyRepositoryImpl(
    private val dao: BabyDao,
    private val syncMeta: SyncMetadataDao,
    private val db: AppDatabase,
) : BabyRepository {
    override fun watchAll() = dao.watchAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(baby: Baby): Long {
        val entity = baby.copy(uuid = baby.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("babies", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(baby: Baby) {
        val entity = baby.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("babies", baby.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(baby: Baby) {
        val entity = baby.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        // 级联软删除子记录
        cascadeSoftDelete(baby.id)
        syncMeta.pendingChange("babies", baby.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun restore(baby: Baby) {
        val entity = baby.copy(deletedAt = null, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("babies", baby.id, entity.uuid, entity.updatedAt)
    }

    private suspend fun cascadeSoftDelete(babyId: Int) {
        val sql = db.openHelper.writableDatabase
        val tables = listOf("feedings", "sleeps", "growths", "vaccinations",
            "health_records", "diapers", "development_assessments", "reminders")
        val now = nowEpoch

        // 先收集所有待级联删除的子记录（id + uuid），用于后续 sync_metadata 标记
        val pendingSync = mutableListOf<Triple<String, Int, String?>>()
        for (table in tables) {
            val cursor = sql.query("SELECT id, uuid FROM $table WHERE baby_id = $babyId AND deletedAt IS NULL")
            while (cursor.moveToNext()) {
                pendingSync.add(Triple(table, cursor.getInt(0), cursor.getString(1)))
            }
            cursor.close()
        }

        sql.beginTransaction()
        try {
            for (table in tables) {
                sql.execSQL("UPDATE $table SET deletedAt = $now, updatedAt = $now WHERE baby_id = $babyId")
            }
            sql.setTransactionSuccessful()
        } finally {
            sql.endTransaction()
        }

        // 为所有级联删除的子记录标记 pending，确保它们能上行同步到 Supabase
        for ((table, id, uuid) in pendingSync) {
            syncMeta.pendingChange(table, id, uuid, now)
        }
    }
}

// ── 喂养 ──

interface FeedingRepository {
    fun watchByBaby(babyId: Int): Flow<List<Feeding>>
    suspend fun getById(id: Int): Feeding?
    suspend fun insert(feeding: Feeding): Long
    suspend fun update(feeding: Feeding)
    suspend fun delete(feeding: Feeding)
}

class FeedingRepositoryImpl(private val dao: FeedingDao, private val syncMeta: SyncMetadataDao) : FeedingRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(feeding: Feeding): Long {
        val entity = feeding.copy(uuid = feeding.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("feedings", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(feeding: Feeding) {
        val entity = feeding.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("feedings", feeding.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(feeding: Feeding) {
        val entity = feeding.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("feedings", feeding.id, entity.uuid, entity.updatedAt)
    }
}

// ── 睡眠 ──

interface SleepRepository {
    fun watchByBaby(babyId: Int): Flow<List<Sleep>>
    suspend fun getById(id: Int): Sleep?
    suspend fun insert(sleep: Sleep): Long
    suspend fun update(sleep: Sleep)
    suspend fun delete(sleep: Sleep)
}

class SleepRepositoryImpl(private val dao: SleepDao, private val syncMeta: SyncMetadataDao) : SleepRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(sleep: Sleep): Long {
        val entity = sleep.copy(uuid = sleep.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("sleeps", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(sleep: Sleep) {
        val entity = sleep.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("sleeps", sleep.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(sleep: Sleep) {
        val entity = sleep.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("sleeps", sleep.id, entity.uuid, entity.updatedAt)
    }
}

// ── 生长 ──

interface GrowthRepository {
    fun watchByBaby(babyId: Int): Flow<List<Growth>>
    suspend fun getById(id: Int): Growth?
    suspend fun insert(growth: Growth): Long
    suspend fun update(growth: Growth)
    suspend fun delete(growth: Growth)
}

class GrowthRepositoryImpl(private val dao: GrowthDao, private val syncMeta: SyncMetadataDao) : GrowthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(growth: Growth): Long {
        val entity = growth.copy(uuid = growth.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("growths", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(growth: Growth) {
        val entity = growth.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("growths", growth.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(growth: Growth) {
        val entity = growth.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("growths", growth.id, entity.uuid, entity.updatedAt)
    }
}

// ── 疫苗 ──

interface VaccinationRepository {
    fun watchByBaby(babyId: Int): Flow<List<Vaccination>>
    suspend fun getById(id: Int): Vaccination?
    suspend fun insert(vaccination: Vaccination): Long
    suspend fun update(vaccination: Vaccination)
    suspend fun delete(vaccination: Vaccination)
}

class VaccinationRepositoryImpl(private val dao: VaccinationDao, private val syncMeta: SyncMetadataDao) : VaccinationRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(vaccination: Vaccination): Long {
        val entity = vaccination.copy(uuid = vaccination.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("vaccinations", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(vaccination: Vaccination) {
        val entity = vaccination.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("vaccinations", vaccination.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(vaccination: Vaccination) {
        val entity = vaccination.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("vaccinations", vaccination.id, entity.uuid, entity.updatedAt)
    }
}

// ── 健康记录 ──

interface HealthRepository {
    fun watchByBaby(babyId: Int): Flow<List<HealthRecord>>
    suspend fun getById(id: Int): HealthRecord?
    suspend fun insert(record: HealthRecord): Long
    suspend fun update(record: HealthRecord)
    suspend fun delete(record: HealthRecord)
}

class HealthRepositoryImpl(private val dao: HealthRecordDao, private val syncMeta: SyncMetadataDao) : HealthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(record: HealthRecord): Long {
        val entity = record.copy(uuid = record.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("health_records", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(record: HealthRecord) {
        val entity = record.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("health_records", record.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(record: HealthRecord) {
        val entity = record.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("health_records", record.id, entity.uuid, entity.updatedAt)
    }
}

// ── 尿布 ──

interface DiaperRepository {
    fun watchByBaby(babyId: Int): Flow<List<Diaper>>
    suspend fun getById(id: Int): Diaper?
    suspend fun insert(diaper: Diaper): Long
    suspend fun update(diaper: Diaper)
    suspend fun delete(diaper: Diaper)
}

class DiaperRepositoryImpl(private val dao: DiaperDao, private val syncMeta: SyncMetadataDao) : DiaperRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(diaper: Diaper): Long {
        val entity = diaper.copy(uuid = diaper.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("diapers", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }
    override suspend fun update(diaper: Diaper) {
        val entity = diaper.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("diapers", diaper.id, entity.uuid, entity.updatedAt)
    }
    override suspend fun delete(diaper: Diaper) {
        val entity = diaper.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("diapers", diaper.id, entity.uuid, entity.updatedAt)
    }
}

// —— 消息中心 —— Repository 返回 Domain Model（AppMessage），内部做 Entity↔Domain 映射

interface MessageRepository {
    fun watchByType(type: MessageType): Flow<List<AppMessage>>
    fun watchAll(): Flow<List<AppMessage>>
    fun watchUnreadCount(): Flow<Int>
    suspend fun insert(message: AppMessage): Long
    suspend fun markRead(id: Long)
    suspend fun markAllRead()
    suspend fun delete(message: AppMessage)
}

class MessageRepositoryImpl(private val dao: MessageDao, private val syncMeta: SyncMetadataDao) : MessageRepository {
    override fun watchByType(type: MessageType): Flow<List<AppMessage>> =
        dao.watchByType(MessageType.raw(type)).map { list -> list.map { it.toDomain() } }

    override fun watchAll(): Flow<List<AppMessage>> =
        dao.watchAll().map { list -> list.map { it.toDomain() } }

    override fun watchUnreadCount(): Flow<Int> = dao.watchUnreadCount()

    override suspend fun insert(message: AppMessage): Long {
        val entity = message.copy(uuid = message.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("messages", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }

    override suspend fun markRead(id: Long) = dao.markRead(id)

    override suspend fun markAllRead() = dao.markAllRead()

    override suspend fun delete(message: AppMessage) {
        val entity = message.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("messages", message.id.toInt(), entity.uuid, entity.updatedAt)
    }
}

// —— 发育评估 —— Repository 返回 Domain Model（DevelopmentAssessment），内部做 Entity↔Domain 映射

interface DevelopmentAssessmentRepository {
    fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessment>>
    suspend fun getById(id: Int): DevelopmentAssessment?
    fun watchLatest(babyId: Int): Flow<DevelopmentAssessment?>
    suspend fun insert(assessment: DevelopmentAssessment): Long
    suspend fun update(assessment: DevelopmentAssessment)
    suspend fun delete(assessment: DevelopmentAssessment)
}

class DevelopmentAssessmentRepositoryImpl(
    private val dao: DevelopmentAssessmentDao,
    private val syncMeta: SyncMetadataDao,
) : DevelopmentAssessmentRepository {
    override fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessment>> =
        dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Int): DevelopmentAssessment? =
        dao.getById(id)?.toDomain()

    override fun watchLatest(babyId: Int): Flow<DevelopmentAssessment?> =
        dao.watchLatest(babyId).map { it?.toDomain() }

    override suspend fun insert(assessment: DevelopmentAssessment): Long {
        val entity = assessment.copy(uuid = assessment.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("development_assessments", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }

    override suspend fun update(assessment: DevelopmentAssessment) {
        val entity = assessment.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("development_assessments", assessment.id, entity.uuid, entity.updatedAt)
    }

    override suspend fun delete(assessment: DevelopmentAssessment) {
        val entity = assessment.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("development_assessments", assessment.id, entity.uuid, entity.updatedAt)
    }
}

// —— 提醒中心 —— Repository 返回 Domain Model（Reminder），内部做 Entity↔Domain 映射。

interface ReminderRepository {
    fun watchPending(babyId: Int): Flow<List<Reminder>>
    fun watchHistory(babyId: Int): Flow<List<Reminder>>
    suspend fun getById(id: Int): Reminder?
    suspend fun insert(reminder: Reminder): Long
    suspend fun update(reminder: Reminder)
    suspend fun delete(reminder: Reminder)
    suspend fun markDone(id: Int, doneDate: LocalDateTime)
    suspend fun setEnabled(id: Int, enabled: Boolean)
}

class ReminderRepositoryImpl(private val dao: ReminderDao, private val syncMeta: SyncMetadataDao) : ReminderRepository {
    override fun watchPending(babyId: Int): Flow<List<Reminder>> =
        dao.watchPending(babyId).map { list -> list.map { it.toDomain() } }

    override fun watchHistory(babyId: Int): Flow<List<Reminder>> =
        dao.watchHistory(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Int): Reminder? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(reminder: Reminder): Long {
        val entity = reminder.copy(uuid = reminder.uuid ?: newUuid(), updatedAt = nowEpoch).toEntity()
        val id = dao.insert(entity)
        syncMeta.pendingChange("reminders", id.toInt(), entity.uuid, entity.updatedAt)
        return id
    }

    override suspend fun update(reminder: Reminder) {
        val entity = reminder.copy(updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("reminders", reminder.id, entity.uuid, entity.updatedAt)
    }

    override suspend fun delete(reminder: Reminder) {
        val entity = reminder.copy(deletedAt = nowEpoch, updatedAt = nowEpoch).toEntity()
        dao.update(entity)
        syncMeta.pendingChange("reminders", reminder.id, entity.uuid, entity.updatedAt)
    }

    override suspend fun markDone(id: Int, doneDate: LocalDateTime) =
        dao.markDone(id, doneDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())

    override suspend fun setEnabled(id: Int, enabled: Boolean) =
        dao.setEnabled(id, enabled)
}
