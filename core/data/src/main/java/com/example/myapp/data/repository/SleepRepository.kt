package com.example.myapp.data.repository

import com.example.myapp.data.room.SleepDao
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val dao: SleepDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<SleepEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: SleepEntity) = dao.insert(entity)
    suspend fun delete(entity: SleepEntity) = dao.delete(entity)
}
