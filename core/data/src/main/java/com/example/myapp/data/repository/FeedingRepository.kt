package com.example.myapp.data.repository

import com.example.myapp.data.room.FeedingDao
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.Flow

class FeedingRepository(private val dao: FeedingDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<FeedingEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: FeedingEntity) = dao.insert(entity)
    suspend fun delete(entity: FeedingEntity) = dao.delete(entity)
}
