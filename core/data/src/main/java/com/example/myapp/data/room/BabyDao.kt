package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    @Query("SELECT * FROM babies ORDER BY id LIMIT 1")
    fun getFirstFlow(): Flow<BabyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BabyEntity)

    @Query("DELETE FROM babies")
    suspend fun deleteAll()
}
