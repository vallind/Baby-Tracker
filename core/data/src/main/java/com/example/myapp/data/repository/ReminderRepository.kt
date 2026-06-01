package com.example.myapp.data.repository

import com.example.myapp.data.room.ReminderDao
import com.example.myapp.data.room.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<ReminderEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: ReminderEntity) = dao.insert(entity)
    suspend fun toggleEnabled(id: Long, enabled: Boolean) = dao.toggleEnabled(id, enabled)
}
