package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records WHERE babyId = :babyId ORDER BY recordDate DESC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<GrowthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GrowthEntity)

    @Delete
    suspend fun delete(entity: GrowthEntity)
}
