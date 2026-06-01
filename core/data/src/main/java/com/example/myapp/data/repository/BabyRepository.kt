package com.example.myapp.data.repository

import com.example.myapp.data.room.BabyDao
import com.example.myapp.data.room.BabyEntity
import kotlinx.coroutines.flow.Flow

class BabyRepository(private val dao: BabyDao) {
    val firstBaby: Flow<BabyEntity?> = dao.getFirstFlow()
    suspend fun insert(baby: BabyEntity) = dao.insert(baby)
}
