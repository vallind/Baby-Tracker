package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records WHERE babyId = :babyId ORDER BY startTime DESC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<SleepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SleepEntity)

    @Delete
    suspend fun delete(entity: SleepEntity)
}
