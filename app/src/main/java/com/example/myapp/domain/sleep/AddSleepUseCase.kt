package com.example.myapp.domain.sleep

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.room.SleepEntity

class AddSleepUseCase(private val repository: SleepRepository) {
    suspend operator fun invoke(startTime: Long, endTime: Long, type: String) {
        try {
            repository.insert(SleepEntity(startTime = startTime, endTime = endTime, type = type))
            GlobalEventBus.emit(UiEvent.Success("睡眠记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
