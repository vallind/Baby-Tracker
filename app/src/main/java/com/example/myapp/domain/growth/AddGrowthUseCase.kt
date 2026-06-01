package com.example.myapp.domain.growth

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.room.GrowthEntity

class AddGrowthUseCase(private val repository: GrowthRepository) {
    suspend operator fun invoke(height: Float, weight: Float, headCircumference: Float, date: Long) {
        try {
            repository.insert(
                GrowthEntity(height = height, weight = weight, headCircumference = headCircumference, recordDate = date)
            )
            GlobalEventBus.emit(UiEvent.Success("生长记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
