package com.example.myapp.domain.feeding

import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.data.repository.FeedingRepository

class DeleteFeedingUseCase(private val repository: FeedingRepository) {
    suspend operator fun invoke(entity: FeedingEntity) = repository.delete(entity)
}
