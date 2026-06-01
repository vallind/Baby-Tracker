package com.example.myapp.data.repository

import com.example.myapp.data.room.VaccineDao
import com.example.myapp.data.room.VaccineEntity
import kotlinx.coroutines.flow.Flow

class VaccineRepository(private val dao: VaccineDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<VaccineEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: VaccineEntity) = dao.insert(entity)
    suspend fun markCompleted(id: Long, completedDate: Long) = dao.markCompleted(id, "COMPLETED", completedDate)
}
