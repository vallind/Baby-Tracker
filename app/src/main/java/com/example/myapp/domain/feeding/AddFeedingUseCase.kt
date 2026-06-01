package com.example.myapp.domain.feeding

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.room.FeedingEntity

class AddFeedingUseCase(private val repository: FeedingRepository) {
    suspend operator fun invoke(type: String, amount: Int, unit: String, note: String?) {
        try {
            repository.insert(FeedingEntity(type = type, amount = amount, unit = unit, note = note))
            GlobalEventBus.emit(UiEvent.Success("喂养记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
