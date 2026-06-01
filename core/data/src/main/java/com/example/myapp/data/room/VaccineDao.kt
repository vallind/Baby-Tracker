package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccines WHERE babyId = :babyId ORDER BY plannedDate ASC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<VaccineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VaccineEntity)

    @Query("UPDATE vaccines SET status = :status, completedDate = :completedDate WHERE id = :id")
    suspend fun markCompleted(id: Long, status: String, completedDate: Long)
}
