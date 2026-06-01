package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthProfileDao {
    @Query("SELECT * FROM health_profiles WHERE babyId = :babyId LIMIT 1")
    fun getByBabyFlow(babyId: Long = 1): Flow<HealthProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HealthProfileEntity)
}
