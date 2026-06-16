package com.babytracker.core.database.dao

import androidx.room.*
import com.babytracker.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    @Query("SELECT * FROM babies ORDER BY id ASC")
    fun watchAll(): Flow<List<BabyEntity>>
    @Query("SELECT * FROM babies WHERE id = :id")
    suspend fun getById(id: Int): BabyEntity?
    @Insert
    suspend fun insert(baby: BabyEntity): Long
    @Update
    suspend fun update(baby: BabyEntity)
    @Delete
    suspend fun delete(baby: BabyEntity)
}

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feedings WHERE baby_id = :babyId ORDER BY timestamp DESC")
    fun watchByBaby(babyId: Int): Flow<List<FeedingEntity>>
    @Query("SELECT * FROM feedings WHERE id = :id")
    suspend fun getById(id: Int): FeedingEntity?
    @Insert
    suspend fun insert(feeding: FeedingEntity): Long
    @Update
    suspend fun update(feeding: FeedingEntity)
    @Delete
    suspend fun delete(feeding: FeedingEntity)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleeps WHERE baby_id = :babyId ORDER BY start_time DESC")
    fun watchByBaby(babyId: Int): Flow<List<SleepEntity>>
    @Insert
    suspend fun insert(sleep: SleepEntity): Long
    @Update
    suspend fun update(sleep: SleepEntity)
    @Delete
    suspend fun delete(sleep: SleepEntity)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growths WHERE baby_id = :babyId ORDER BY measured_at DESC")
    fun watchByBaby(babyId: Int): Flow<List<GrowthEntity>>
    @Insert
    suspend fun insert(growth: GrowthEntity): Long
    @Update
    suspend fun update(growth: GrowthEntity)
    @Delete
    suspend fun delete(growth: GrowthEntity)
}

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE baby_id = :babyId ORDER BY CASE WHEN status = 'pending' THEN 0 ELSE 1 END, scheduled_date ASC")
    fun watchByBaby(babyId: Int): Flow<List<VaccinationEntity>>
    @Insert
    suspend fun insert(vaccination: VaccinationEntity): Long
    @Update
    suspend fun update(vaccination: VaccinationEntity)
    @Delete
    suspend fun delete(vaccination: VaccinationEntity)
}

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE baby_id = :babyId ORDER BY record_date DESC")
    fun watchByBaby(babyId: Int): Flow<List<HealthRecordEntity>>
    @Insert
    suspend fun insert(record: HealthRecordEntity): Long
    @Update
    suspend fun update(record: HealthRecordEntity)
    @Delete
    suspend fun delete(record: HealthRecordEntity)
}

@Dao
interface BackupConfigDao {
    @Query("SELECT * FROM backup_config LIMIT 1")
    suspend fun get(): BackupConfigEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: BackupConfigEntity)
}

@Dao
interface DiaperDao {
    @Query("SELECT * FROM diapers WHERE baby_id = :babyId ORDER BY timestamp DESC")
    fun watchByBaby(babyId: Int): Flow<List<DiaperEntity>>
    @Insert
    suspend fun insert(diaper: DiaperEntity): Long
    @Update
    suspend fun update(diaper: DiaperEntity)
    @Delete
    suspend fun delete(diaper: DiaperEntity)
}
