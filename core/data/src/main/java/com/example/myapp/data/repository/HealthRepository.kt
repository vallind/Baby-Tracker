package com.example.myapp.data.repository

import com.example.myapp.data.room.HealthProfileDao
import com.example.myapp.data.room.HealthProfileEntity
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val dao: HealthProfileDao) {
    fun getByBaby(babyId: Long = 1): Flow<HealthProfileEntity?> = dao.getByBabyFlow(babyId)
    suspend fun save(profile: HealthProfileEntity) = dao.insert(profile)
}
