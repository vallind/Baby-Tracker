package com.babytracker.core.database.dao

import androidx.room.*
import com.babytracker.core.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    @Query("SELECT * FROM babies ORDER BY id ASC")
    fun watchAll(): Flow<List<BabyEntity>>
    @Query("SELECT * FROM babies WHERE familyId = :familyId ORDER BY id ASC")
    fun watchByFamily(familyId: String): Flow<List<BabyEntity>>
    @Query("SELECT * FROM babies WHERE familyId IS NULL ORDER BY id ASC")
    fun watchUnscoped(): Flow<List<BabyEntity>>
    @Query("SELECT COUNT(*) FROM babies WHERE familyId IS NULL AND deletedAt IS NULL")
    fun watchUnscopedCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM babies WHERE familyId IS NULL AND deletedAt IS NULL")
    suspend fun unscopedCount(): Int
    @Query("SELECT * FROM babies ORDER BY id ASC")
    suspend fun getAll(): List<BabyEntity>
    @Query("SELECT * FROM babies WHERE id = :id")
    suspend fun getById(id: Int): BabyEntity?
    @Query("SELECT * FROM babies WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): BabyEntity?
    @Query("UPDATE babies SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(baby: BabyEntity): Long
    @Update
    suspend fun update(baby: BabyEntity)
    @Delete
    suspend fun delete(baby: BabyEntity)
}

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feedings WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun watchByBaby(babyId: Int): Flow<List<FeedingEntity>>
    @Query("SELECT * FROM feedings WHERE id = :id")
    suspend fun getById(id: Int): FeedingEntity?
    @Query("SELECT * FROM feedings WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): FeedingEntity?
    @Query("UPDATE feedings SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(feeding: FeedingEntity): Long
    @Update
    suspend fun update(feeding: FeedingEntity)
    @Delete
    suspend fun delete(feeding: FeedingEntity)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleeps WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY start_time DESC")
    fun watchByBaby(babyId: Int): Flow<List<SleepEntity>>
    @Query("SELECT * FROM sleeps WHERE id = :id")
    suspend fun getById(id: Int): SleepEntity?
    @Query("SELECT * FROM sleeps WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): SleepEntity?
    @Query("UPDATE sleeps SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(sleep: SleepEntity): Long
    @Update
    suspend fun update(sleep: SleepEntity)
    @Delete
    suspend fun delete(sleep: SleepEntity)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growths WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY measured_at DESC")
    fun watchByBaby(babyId: Int): Flow<List<GrowthEntity>>
    @Query("SELECT * FROM growths WHERE id = :id")
    suspend fun getById(id: Int): GrowthEntity?
    @Query("SELECT * FROM growths WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): GrowthEntity?
    @Query("UPDATE growths SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(growth: GrowthEntity): Long
    @Update
    suspend fun update(growth: GrowthEntity)
    @Delete
    suspend fun delete(growth: GrowthEntity)
}

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY CASE WHEN status = 'pending' THEN 0 ELSE 1 END, scheduled_date ASC")
    fun watchByBaby(babyId: Int): Flow<List<VaccinationEntity>>
    @Query("SELECT * FROM vaccinations WHERE id = :id")
    suspend fun getById(id: Int): VaccinationEntity?
    @Query("SELECT * FROM vaccinations WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): VaccinationEntity?
    @Query("UPDATE vaccinations SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(vaccination: VaccinationEntity): Long
    @Update
    suspend fun update(vaccination: VaccinationEntity)
    @Delete
    suspend fun delete(vaccination: VaccinationEntity)
}

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY record_date DESC")
    fun watchByBaby(babyId: Int): Flow<List<HealthRecordEntity>>
    @Query("SELECT * FROM health_records WHERE id = :id")
    suspend fun getById(id: Int): HealthRecordEntity?
    @Query("SELECT * FROM health_records WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): HealthRecordEntity?
    @Query("UPDATE health_records SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
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
    @Query("SELECT * FROM diapers WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun watchByBaby(babyId: Int): Flow<List<DiaperEntity>>
    @Query("SELECT * FROM diapers WHERE id = :id")
    suspend fun getById(id: Int): DiaperEntity?
    @Query("SELECT * FROM diapers WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): DiaperEntity?
    @Query("UPDATE diapers SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(diaper: DiaperEntity): Long
    @Update
    suspend fun update(diaper: DiaperEntity)
    @Delete
    suspend fun delete(diaper: DiaperEntity)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE deletedAt IS NULL ORDER BY createTime DESC")
    fun watchAll(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0 AND deletedAt IS NULL")
    fun watchUnreadCount(): Flow<Int>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): MessageEntity?

    @Query("UPDATE messages SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)

    @Insert
    suspend fun insert(entity: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE messages SET isRead = 1")
    suspend fun markAllRead()

    @Update
    suspend fun update(entity: MessageEntity)
    @Delete
    suspend fun delete(entity: MessageEntity)
}

@Dao
interface DevelopmentAssessmentDao {
    @Query("SELECT * FROM development_assessments WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY assess_date DESC")
    fun watchByBaby(babyId: Int): Flow<List<DevelopmentAssessmentEntity>>
    @Query("SELECT * FROM development_assessments WHERE id = :id")
    suspend fun getById(id: Int): DevelopmentAssessmentEntity?
    @Query("SELECT * FROM development_assessments WHERE baby_id = :babyId AND deletedAt IS NULL ORDER BY assess_date DESC LIMIT 1")
    fun watchLatest(babyId: Int): Flow<DevelopmentAssessmentEntity?>
    @Query("SELECT * FROM development_assessments WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): DevelopmentAssessmentEntity?
    @Query("UPDATE development_assessments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(entity: DevelopmentAssessmentEntity): Long
    @Update
    suspend fun update(entity: DevelopmentAssessmentEntity)
    @Delete
    suspend fun delete(entity: DevelopmentAssessmentEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE baby_id = :babyId AND is_done = 0 AND deletedAt IS NULL ORDER BY due_date ASC")
    fun watchPending(babyId: Int): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE baby_id = :babyId AND is_done = 1 AND deletedAt IS NULL ORDER BY done_date DESC")
    fun watchHistory(babyId: Int): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Int): ReminderEntity?
    @Query("SELECT * FROM reminders WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ReminderEntity?
    @Query("UPDATE reminders SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE uuid = :uuid")
    suspend fun softDeleteByUuid(uuid: String, deletedAt: Long, updatedAt: Long)
    @Insert
    suspend fun insert(entity: ReminderEntity): Long
    @Update
    suspend fun update(entity: ReminderEntity)
    @Delete
    suspend fun delete(entity: ReminderEntity)
}

@Dao
interface AiHistoryDao {
    @Query(
        """
        SELECT c.*,
            (
                SELECT content
                FROM ai_messages
                WHERE conversation_id = c.id
                ORDER BY position DESC
                LIMIT 1
            ) AS preview
        FROM ai_conversations c
        WHERE c.family_id = :familyId AND c.baby_id = :babyId
        ORDER BY c.updated_at DESC
        """,
    )
    fun watchConversations(
        familyId: String,
        babyId: Int,
    ): Flow<List<AiConversationSummaryEntity>>

    @Query(
        """
        SELECT * FROM ai_conversations
        WHERE id = :conversationId
          AND family_id = :familyId
          AND baby_id = :babyId
        LIMIT 1
        """,
    )
    suspend fun getConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    ): AiConversationEntity?

    @Query(
        """
        SELECT * FROM ai_messages
        WHERE conversation_id = :conversationId
        ORDER BY position ASC
        """,
    )
    suspend fun getMessages(conversationId: Long): List<AiMessageEntity>

    @Insert
    suspend fun insertConversation(entity: AiConversationEntity): Long

    @Query(
        """
        UPDATE ai_conversations
        SET title = :title, updated_at = :updatedAt
        WHERE id = :conversationId
          AND family_id = :familyId
          AND baby_id = :babyId
        """,
    )
    suspend fun updateConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
        title: String,
        updatedAt: Long,
    ): Int

    @Insert
    suspend fun insertMessages(messages: List<AiMessageEntity>)

    @Query("DELETE FROM ai_messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessages(conversationId: Long)

    @Query(
        """
        DELETE FROM ai_conversations
        WHERE id = :conversationId
          AND family_id = :familyId
          AND baby_id = :babyId
        """,
    )
    suspend fun deleteConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    ): Int

    @Transaction
    suspend fun replaceMessages(
        conversationId: Long,
        messages: List<AiMessageEntity>,
    ) {
        deleteMessages(conversationId)
        if (messages.isNotEmpty()) insertMessages(messages)
    }
}

// ============================================================
// Supabase 同步元数据 Dao
// ============================================================

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE syncStatus = 'pending' AND familyId = :familyId AND nextRetryAt <= :now ORDER BY updatedAt ASC")
    suspend fun getPendingChanges(familyId: String, now: Long): List<SyncMetadataEntity>

    @Query("SELECT COUNT(*) FROM sync_metadata WHERE syncStatus = 'pending' AND familyId = :familyId")
    suspend fun pendingCount(familyId: String): Int

    @Query("SELECT * FROM sync_metadata WHERE tableName = :tableName AND localId = :localId LIMIT 1")
    suspend fun getByTableAndId(tableName: String, localId: Int): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SyncMetadataEntity)

    @Query("UPDATE sync_metadata SET syncStatus = 'synced', remoteUuid = :remoteUuid, updatedAt = :updatedAt, retryCount = 0, nextRetryAt = 0, lastError = NULL WHERE id = :id")
    suspend fun markSynced(id: Int, remoteUuid: String?, updatedAt: Long)

    @Query("""
        UPDATE sync_metadata
        SET retryCount = retryCount + 1,
            nextRetryAt = :nextRetryAt,
            lastError = :error,
            syncStatus = CASE WHEN retryCount + 1 >= 5 THEN 'conflict' ELSE 'pending' END
        WHERE id = :id
    """)
    suspend fun markRetry(id: Int, nextRetryAt: Long, error: String?)

    /** 按 remoteUuid 查找同步元数据 */
    @Query("SELECT * FROM sync_metadata WHERE remoteUuid = :remoteUuid AND tableName = :tableName LIMIT 1")
    suspend fun getByRemoteUuid(tableName: String, remoteUuid: String): SyncMetadataEntity?

    @Query("""
        UPDATE sync_metadata SET
            syncStatus = 'pending',
            remoteUuid = :remoteUuid,
            updatedAt = :updatedAt,
            familyId = :familyId,
            retryCount = 0,
            nextRetryAt = 0,
            lastError = NULL
        WHERE tableName = :tableName AND localId = :localId
    """)
    suspend fun updatePending(tableName: String, localId: Int, remoteUuid: String?, updatedAt: Long, familyId: String?): Int

    @Query("""
        UPDATE sync_metadata SET
            remoteUuid = :remoteUuid,
            syncStatus = :syncStatus,
            updatedAt = :updatedAt,
            lastSyncAt = :lastSyncAt,
            familyId = :familyId,
            retryCount = 0,
            nextRetryAt = 0,
            lastError = NULL
        WHERE tableName = :tableName AND localId = :localId
    """)
    suspend fun updateByTableAndId(
        tableName: String,
        localId: Int,
        remoteUuid: String?,
        syncStatus: String,
        updatedAt: Long,
        lastSyncAt: Long?,
        familyId: String?,
    )
}

@Dao
interface SyncCursorDao {
    @Query("SELECT lastVersion FROM sync_cursors WHERE familyId = :familyId AND tableName = :tableName")
    suspend fun get(familyId: String, tableName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(cursor: SyncCursorEntity)

    @Query("DELETE FROM sync_cursors WHERE familyId = :familyId")
    suspend fun clear(familyId: String)
}
