package com.babytracker.data.repository

import com.babytracker.core.database.dao.*
import com.babytracker.core.database.entity.*
import com.babytracker.core.database.entity.FeedingEntity
import kotlinx.coroutines.flow.Flow

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
