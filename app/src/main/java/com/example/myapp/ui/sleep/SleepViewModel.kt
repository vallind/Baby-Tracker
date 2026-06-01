package com.example.myapp.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.domain.sleep.AddSleepUseCase
import com.example.myapp.domain.sleep.DeleteSleepUseCase
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SleepEvent {
    data class Add(val startTime: Long, val endTime: Long, val type: String) : SleepEvent
    data class Delete(val entity: SleepEntity) : SleepEvent
    data object Refresh : SleepEvent
}

class SleepViewModel(
    private val repository: SleepRepository,
    private val addUseCase: AddSleepUseCase,
    private val deleteUseCase: DeleteSleepUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState(loading = true))
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SleepEvent>()
    val events: SharedFlow<SleepEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = SleepUiState(records = records)
            }
        }
    }

    fun onEvent(event: SleepEvent) {
        when (event) {
            is SleepEvent.Add -> {
                viewModelScope.launch { addUseCase(event.startTime, event.endTime, event.type) }
            }
            is SleepEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            SleepEvent.Refresh -> {}
        }
    }
}
