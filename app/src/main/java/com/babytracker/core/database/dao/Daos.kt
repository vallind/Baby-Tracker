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

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE type = :type ORDER BY createTime DESC")
    fun watchByType(type: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY createTime DESC")
    fun watchAll(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    fun watchUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE messages SET isRead = 1")
    suspend fun markAllRead()

    @Delete
    suspend fun delete(entity: MessageEntity)
}

@Dao
interface DevelopmentAssessmentDao {
    @Query("SELECT * FROM development_assessments WHERE baby_id = :babyId ORDER BY assess_date DESC")
    fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessmentEntity>>

    @Query("SELECT * FROM development_assessments WHERE baby_id = :babyId ORDER BY assess_date DESC LIMIT 1")
    fun watchLatest(babyId: Int): Flow<DevelopmentAssessmentEntity?>

    @Insert
    suspend fun insert(entity: DevelopmentAssessmentEntity): Long

    @Delete
    suspend fun delete(entity: DevelopmentAssessmentEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE baby_id = :babyId AND is_done = 0 ORDER BY due_date ASC")
    fun watchPending(babyId: Int): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE baby_id = :babyId AND is_done = 1 ORDER BY done_date DESC")
    fun watchHistory(babyId: Int): Flow<List<ReminderEntity>>

    @Insert
    suspend fun insert(entity: ReminderEntity): Long

    @Update
    suspend fun update(entity: ReminderEntity)

    @Query("UPDATE reminders SET is_done = 1, done_date = :doneDate WHERE id = :id")
    suspend fun markDone(id: Int, doneDate: Long)

    @Query("UPDATE reminders SET is_enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean)

    @Delete
    suspend fun delete(entity: ReminderEntity)
}
