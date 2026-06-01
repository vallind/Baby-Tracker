package com.example.myapp.domain.growth

import com.example.myapp.data.room.GrowthEntity
import com.example.myapp.data.repository.GrowthRepository

class DeleteGrowthUseCase(private val repository: GrowthRepository) {
    suspend operator fun invoke(entity: GrowthEntity) = repository.delete(entity)
}
