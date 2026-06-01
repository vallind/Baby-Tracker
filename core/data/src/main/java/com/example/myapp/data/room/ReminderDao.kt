package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE babyId = :babyId ORDER BY scheduledAt ASC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, enabled: Boolean)
}
