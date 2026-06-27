package com.babytracker.core.data.repository

import com.babytracker.core.database.dao.*
import com.babytracker.core.database.entity.*
import com.babytracker.core.database.entity.FeedingEntity
import com.babytracker.core.data.mapper.toDomain
import com.babytracker.core.data.mapper.toEntity
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.DevelopmentAssessment
import com.babytracker.core.domain.model.MessageType
import com.babytracker.core.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

interface BabyRepository {
    fun watchAll(): Flow<List<BabyEntity>>
    suspend fun getById(id: Int): BabyEntity?
    suspend fun insert(baby: BabyEntity): Long
    suspend fun update(baby: BabyEntity)
    suspend fun delete(baby: BabyEntity)
}

class BabyRepositoryImpl(private val dao: BabyDao) : BabyRepository {
    override fun watchAll() = dao.watchAll()
    override suspend fun getById(id: Int) = dao.getById(id)
    override suspend fun insert(baby: BabyEntity) = dao.insert(baby)
    override suspend fun update(baby: BabyEntity) = dao.update(baby)
    override suspend fun delete(baby: BabyEntity) = dao.delete(baby)
}

interface FeedingRepository {
    fun watchByBaby(babyId: Int): Flow<List<FeedingEntity>>
    suspend fun insert(feeding: FeedingEntity): Long
    suspend fun update(feeding: FeedingEntity)
    suspend fun delete(feeding: FeedingEntity)
}

class FeedingRepositoryImpl(private val dao: FeedingDao) : FeedingRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(feeding: FeedingEntity) = dao.insert(feeding)
    override suspend fun update(feeding: FeedingEntity) = dao.update(feeding)
    override suspend fun delete(feeding: FeedingEntity) = dao.delete(feeding)
}

interface SleepRepository {
    fun watchByBaby(babyId: Int): Flow<List<SleepEntity>>
    suspend fun insert(sleep: SleepEntity): Long
    suspend fun update(sleep: SleepEntity)
    suspend fun delete(sleep: SleepEntity)
}

class SleepRepositoryImpl(private val dao: SleepDao) : SleepRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(sleep: SleepEntity) = dao.insert(sleep)
    override suspend fun update(sleep: SleepEntity) = dao.update(sleep)
    override suspend fun delete(sleep: SleepEntity) = dao.delete(sleep)
}

interface GrowthRepository {
    fun watchByBaby(babyId: Int): Flow<List<GrowthEntity>>
    suspend fun insert(growth: GrowthEntity): Long
    suspend fun update(growth: GrowthEntity)
    suspend fun delete(growth: GrowthEntity)
}

class GrowthRepositoryImpl(private val dao: GrowthDao) : GrowthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(growth: GrowthEntity) = dao.insert(growth)
    override suspend fun update(growth: GrowthEntity) = dao.update(growth)
    override suspend fun delete(growth: GrowthEntity) = dao.delete(growth)
}

interface VaccinationRepository {
    fun watchByBaby(babyId: Int): Flow<List<VaccinationEntity>>
    suspend fun insert(vaccination: VaccinationEntity): Long
    suspend fun update(vaccination: VaccinationEntity)
    suspend fun delete(vaccination: VaccinationEntity)
}

class VaccinationRepositoryImpl(private val dao: VaccinationDao) : VaccinationRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(vaccination: VaccinationEntity) = dao.insert(vaccination)
    override suspend fun update(vaccination: VaccinationEntity) = dao.update(vaccination)
    override suspend fun delete(vaccination: VaccinationEntity) = dao.delete(vaccination)
}

interface HealthRepository {
    fun watchByBaby(babyId: Int): Flow<List<HealthRecordEntity>>
    suspend fun insert(record: HealthRecordEntity): Long
    suspend fun update(record: HealthRecordEntity)
    suspend fun delete(record: HealthRecordEntity)
}

class HealthRepositoryImpl(private val dao: HealthRecordDao) : HealthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(record: HealthRecordEntity) = dao.insert(record)
    override suspend fun update(record: HealthRecordEntity) = dao.update(record)
    override suspend fun delete(record: HealthRecordEntity) = dao.delete(record)
}

interface DiaperRepository {
    fun watchByBaby(babyId: Int): Flow<List<DiaperEntity>>
    suspend fun insert(diaper: DiaperEntity): Long
    suspend fun update(diaper: DiaperEntity)
    suspend fun delete(diaper: DiaperEntity)
}

class DiaperRepositoryImpl(private val dao: DiaperDao) : DiaperRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId)
    override suspend fun insert(diaper: DiaperEntity) = dao.insert(diaper)
    override suspend fun update(diaper: DiaperEntity) = dao.update(diaper)
    override suspend fun delete(diaper: DiaperEntity) = dao.delete(diaper)
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
    fun watchLatest(babyId: Int): Flow<DevelopmentAssessment?>
    suspend fun insert(assessment: DevelopmentAssessment): Long
    suspend fun delete(assessment: DevelopmentAssessment)
}

class DevelopmentAssessmentRepositoryImpl(
    private val dao: DevelopmentAssessmentDao,
) : DevelopmentAssessmentRepository {
    override fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessment>> =
        dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }

    override fun watchLatest(babyId: Int): Flow<DevelopmentAssessment?> =
        dao.watchLatest(babyId).map { it?.toDomain() }

    override suspend fun insert(assessment: DevelopmentAssessment): Long =
        dao.insert(assessment.toEntity())

    override suspend fun delete(assessment: DevelopmentAssessment) =
        dao.delete(assessment.toEntity())
}

// —— 提醒中心 —— Repository 返回 Domain Model（Reminder），内部做 Entity↔Domain 映射。
//   注：markDone 接收 LocalDateTime（domain 层），内部转 epoch milli 调 DAO。
//   setEnabled 用于用药类每日提醒开关。

interface ReminderRepository {
    fun watchPending(babyId: Int): Flow<List<Reminder>>
    fun watchHistory(babyId: Int): Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder): Long
    suspend fun markDone(id: Int, doneDate: LocalDateTime)
    suspend fun setEnabled(id: Int, enabled: Boolean)
    suspend fun delete(reminder: Reminder)
}

class ReminderRepositoryImpl(private val dao: ReminderDao) : ReminderRepository {
    override fun watchPending(babyId: Int): Flow<List<Reminder>> =
        dao.watchPending(babyId).map { list -> list.map { it.toDomain() } }

    override fun watchHistory(babyId: Int): Flow<List<Reminder>> =
        dao.watchHistory(babyId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(reminder: Reminder): Long =
        dao.insert(reminder.toEntity())

    override suspend fun markDone(id: Int, doneDate: LocalDateTime) =
        dao.markDone(id, doneDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())

    override suspend fun setEnabled(id: Int, enabled: Boolean) =
        dao.setEnabled(id, enabled)

    override suspend fun delete(reminder: Reminder) =
        dao.delete(reminder.toEntity())
}
