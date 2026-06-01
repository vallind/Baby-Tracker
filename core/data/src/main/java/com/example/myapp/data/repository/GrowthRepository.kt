package com.example.myapp.data.repository

import com.example.myapp.data.room.GrowthDao
import com.example.myapp.data.room.GrowthEntity
import kotlinx.coroutines.flow.Flow

class GrowthRepository(private val dao: GrowthDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<GrowthEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: GrowthEntity) = dao.insert(entity)
    suspend fun delete(entity: GrowthEntity) = dao.delete(entity)
}
