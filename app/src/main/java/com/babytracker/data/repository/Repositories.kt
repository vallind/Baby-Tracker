package com.babytracker.data.repository

import com.babytracker.core.database.dao.*
import com.babytracker.data.mapper.*
import com.babytracker.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

interface FeedingRepository {
    fun watchByBaby(babyId: Int): Flow<List<Feeding>>
    suspend fun insert(feeding: Feeding): Long
    suspend fun update(feeding: Feeding)
    suspend fun delete(feeding: Feeding)
}

class FeedingRepositoryImpl(private val dao: FeedingDao) : FeedingRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(feeding: Feeding) = dao.insert(feeding.toEntity())
    override suspend fun update(feeding: Feeding) = dao.update(feeding.toEntity())
    override suspend fun delete(feeding: Feeding) = dao.delete(feeding.toEntity())
}

interface SleepRepository {
    fun watchByBaby(babyId: Int): Flow<List<Sleep>>
    suspend fun insert(sleep: Sleep): Long
    suspend fun update(sleep: Sleep)
    suspend fun delete(sleep: Sleep)
}

class SleepRepositoryImpl(private val dao: SleepDao) : SleepRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(sleep: Sleep) = dao.insert(sleep.toEntity())
    override suspend fun update(sleep: Sleep) = dao.update(sleep.toEntity())
    override suspend fun delete(sleep: Sleep) = dao.delete(sleep.toEntity())
}

interface GrowthRepository {
    fun watchByBaby(babyId: Int): Flow<List<Growth>>
    suspend fun insert(growth: Growth): Long
    suspend fun update(growth: Growth)
    suspend fun delete(growth: Growth)
}

class GrowthRepositoryImpl(private val dao: GrowthDao) : GrowthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(growth: Growth) = dao.insert(growth.toEntity())
    override suspend fun update(growth: Growth) = dao.update(growth.toEntity())
    override suspend fun delete(growth: Growth) = dao.delete(growth.toEntity())
}

interface VaccinationRepository {
    fun watchByBaby(babyId: Int): Flow<List<Vaccination>>
    suspend fun insert(vaccination: Vaccination): Long
    suspend fun update(vaccination: Vaccination)
    suspend fun delete(vaccination: Vaccination)
}

class VaccinationRepositoryImpl(private val dao: VaccinationDao) : VaccinationRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(vaccination: Vaccination) = dao.insert(vaccination.toEntity())
    override suspend fun update(vaccination: Vaccination) = dao.update(vaccination.toEntity())
    override suspend fun delete(vaccination: Vaccination) = dao.delete(vaccination.toEntity())
}

interface HealthRepository {
    fun watchByBaby(babyId: Int): Flow<List<HealthRecord>>
    suspend fun insert(record: HealthRecord): Long
    suspend fun update(record: HealthRecord)
    suspend fun delete(record: HealthRecord)
}

class HealthRepositoryImpl(private val dao: HealthRecordDao) : HealthRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(record: HealthRecord) = dao.insert(record.toEntity())
    override suspend fun update(record: HealthRecord) = dao.update(record.toEntity())
    override suspend fun delete(record: HealthRecord) = dao.delete(record.toEntity())
}

interface DiaperRepository {
    fun watchByBaby(babyId: Int): Flow<List<Diaper>>
    suspend fun insert(diaper: Diaper): Long
    suspend fun update(diaper: Diaper)
    suspend fun delete(diaper: Diaper)
}

class DiaperRepositoryImpl(private val dao: DiaperDao) : DiaperRepository {
    override fun watchByBaby(babyId: Int) = dao.watchByBaby(babyId).map { list -> list.map { it.toDomain() } }
    override suspend fun insert(diaper: Diaper) = dao.insert(diaper.toEntity())
    override suspend fun update(diaper: Diaper) = dao.update(diaper.toEntity())
    override suspend fun delete(diaper: Diaper) = dao.delete(diaper.toEntity())
}
