package com.babytracker.core.data.repository

import com.babytracker.core.database.dao.*
import com.babytracker.core.data.mapper.toDomain
import com.babytracker.core.data.mapper.toEntity
import com.babytracker.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

// ── 宝宝 ──

interface BabyRepository {
    fun watchAll(): Flow<List<Baby>>
    suspend fun getById(id: Int): Baby?
    suspend fun insert(baby: Baby): Long
    suspend fun update(baby: Baby)
    suspend fun delete(baby: Baby)
}

class BabyRepositoryImpl(private val dao: BabyDao) : BabyRepository {
    override fun watchAll() = dao.watchAll().map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(baby: Baby) = dao.insert(baby.toEntity())
    override suspend fun update(baby: Baby) = dao.update(baby.toEntity())
    override suspend fun delete(baby: Baby) = dao.delete(baby.toEntity())
}

// ── 喂养 ──

interface FeedingRepository {
    fun watchByBaby(babyId: Int): Flow<List<Feeding>>
    suspend fun getById(id: Int): Feeding?
    suspend fun insert(feeding: Feeding): Long
    suspend fun update(feeding: Feeding)
    suspend fun delete(feeding: Feeding)
}

class FeedingRepositoryImpl(private val dao: FeedingDao) : FeedingRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(feeding: Feeding) = dao.insert(feeding.toEntity())
    override suspend fun update(feeding: Feeding) = dao.update(feeding.toEntity())
    override suspend fun delete(feeding: Feeding) = dao.delete(feeding.toEntity())
}

// ── 睡眠 ──

interface SleepRepository {
    fun watchByBaby(babyId: Int): Flow<List<Sleep>>
    suspend fun getById(id: Int): Sleep?
    suspend fun insert(sleep: Sleep): Long
    suspend fun update(sleep: Sleep)
    suspend fun delete(sleep: Sleep)
}

class SleepRepositoryImpl(private val dao: SleepDao) : SleepRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(sleep: Sleep) = dao.insert(sleep.toEntity())
    override suspend fun update(sleep: Sleep) = dao.update(sleep.toEntity())
    override suspend fun delete(sleep: Sleep) = dao.delete(sleep.toEntity())
}

// ── 生长 ──

interface GrowthRepository {
    fun watchByBaby(babyId: Int): Flow<List<Growth>>
    suspend fun getById(id: Int): Growth?
    suspend fun insert(growth: Growth): Long
    suspend fun update(growth: Growth)
    suspend fun delete(growth: Growth)
}

class GrowthRepositoryImpl(private val dao: GrowthDao) : GrowthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(growth: Growth) = dao.insert(growth.toEntity())
    override suspend fun update(growth: Growth) = dao.update(growth.toEntity())
    override suspend fun delete(growth: Growth) = dao.delete(growth.toEntity())
}

// ── 疫苗 ──

interface VaccinationRepository {
    fun watchByBaby(babyId: Int): Flow<List<Vaccination>>
    suspend fun getById(id: Int): Vaccination?
    suspend fun insert(vaccination: Vaccination): Long
    suspend fun update(vaccination: Vaccination)
    suspend fun delete(vaccination: Vaccination)
}

class VaccinationRepositoryImpl(private val dao: VaccinationDao) : VaccinationRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(vaccination: Vaccination) = dao.insert(vaccination.toEntity())
    override suspend fun update(vaccination: Vaccination) = dao.update(vaccination.toEntity())
    override suspend fun delete(vaccination: Vaccination) = dao.delete(vaccination.toEntity())
}

// ── 健康记录 ──

interface HealthRepository {
    fun watchByBaby(babyId: Int): Flow<List<HealthRecord>>
    suspend fun getById(id: Int): HealthRecord?
    suspend fun insert(record: HealthRecord): Long
    suspend fun update(record: HealthRecord)
    suspend fun delete(record: HealthRecord)
}

class HealthRepositoryImpl(private val dao: HealthRecordDao) : HealthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(record: HealthRecord) = dao.insert(record.toEntity())
    override suspend fun update(record: HealthRecord) = dao.update(record.toEntity())
    override suspend fun delete(record: HealthRecord) = dao.delete(record.toEntity())
}

// ── 尿布 ──

interface DiaperRepository {
    fun watchByBaby(babyId: Int): Flow<List<Diaper>>
    suspend fun getById(id: Int): Diaper?
    suspend fun insert(diaper: Diaper): Long
    suspend fun update(diaper: Diaper)
    suspend fun delete(diaper: Diaper)
}

class DiaperRepositoryImpl(private val dao: DiaperDao) : DiaperRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun getById(id: Int) = dao.getById(id)?.toDomain()
    override suspend fun insert(diaper: Diaper) = dao.insert(diaper.toEntity())
    override suspend fun update(diaper: Diaper) = dao.update(diaper.toEntity())
    override suspend fun delete(diaper: Diaper) = dao.delete(diaper.toEntity())
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

class MessageRepositoryImpl(private val dao: MessageDao) : MessageRepository {
    override fun watchByType(type: MessageType): Flow<List<AppMessage>> =
        dao.watchByType(MessageType.raw(type)).map { list -> list.map { it.toDomain() } }

    override fun watchAll(): Flow<List<AppMessage>> =
        dao.watchAll().map { list -> list.map { it.toDomain() } }

    override fun watchUnreadCount(): Flow<Int> = dao.watchUnreadCount()

    override suspend fun insert(message: AppMessage): Long = dao.insert(message.toEntity())

    override suspend fun markRead(id: Long) = dao.markRead(id)

    override suspend fun markAllRead() = dao.markAllRead()

    override suspend fun delete(message: AppMessage) = dao.delete(message.toEntity())
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
) : DevelopmentAssessmentRepository {
    override fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessment>> =
        dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Int): DevelopmentAssessment? =
        dao.getById(id)?.toDomain()

    override fun watchLatest(babyId: Int): Flow<DevelopmentAssessment?> =
        dao.watchLatest(babyId).map { it?.toDomain() }

    override suspend fun insert(assessment: DevelopmentAssessment): Long =
        dao.insert(assessment.toEntity())

    override suspend fun update(assessment: DevelopmentAssessment) =
        dao.update(assessment.toEntity())

    override suspend fun delete(assessment: DevelopmentAssessment) =
        dao.delete(assessment.toEntity())
}

// —— 提醒中心 —— Repository 返回 Domain Model（Reminder），内部做 Entity↔Domain 映射。
//   注：markDone 接收 LocalDateTime（domain 层），内部转 epoch milli 调 DAO。
//   setEnabled 用于用药类每日提醒开关。

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

class ReminderRepositoryImpl(private val dao: ReminderDao) : ReminderRepository {
    override fun watchPending(babyId: Int): Flow<List<Reminder>> =
        dao.watchPending(babyId).map { list -> list.map { it.toDomain() } }

    override fun watchHistory(babyId: Int): Flow<List<Reminder>> =
        dao.watchHistory(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Int): Reminder? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(reminder: Reminder): Long =
        dao.insert(reminder.toEntity())

    override suspend fun update(reminder: Reminder) =
        dao.update(reminder.toEntity())

    override suspend fun delete(reminder: Reminder) =
        dao.delete(reminder.toEntity())

    override suspend fun markDone(id: Int, doneDate: LocalDateTime) =
        dao.markDone(id, doneDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())

    override suspend fun setEnabled(id: Int, enabled: Boolean) =
        dao.setEnabled(id, enabled)
}
