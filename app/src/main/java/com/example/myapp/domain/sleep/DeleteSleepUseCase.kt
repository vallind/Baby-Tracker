package com.example.myapp.domain.sleep

import com.example.myapp.data.room.SleepEntity
import com.example.myapp.data.repository.SleepRepository

class DeleteSleepUseCase(private val repository: SleepRepository) {
    suspend operator fun invoke(entity: SleepEntity) = repository.delete(entity)
}
