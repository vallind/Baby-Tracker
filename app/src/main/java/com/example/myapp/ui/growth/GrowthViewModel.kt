package com.example.myapp.ui.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.domain.growth.AddGrowthUseCase
import com.example.myapp.domain.growth.DeleteGrowthUseCase
import com.example.myapp.data.room.GrowthEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GrowthEvent {
    data class Add(val height: Float, val weight: Float, val headCircumference: Float, val date: Long) : GrowthEvent
    data class Delete(val entity: GrowthEntity) : GrowthEvent
    data object Refresh : GrowthEvent
}

class GrowthViewModel(
    private val repository: GrowthRepository,
    private val addUseCase: AddGrowthUseCase,
    private val deleteUseCase: DeleteGrowthUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(GrowthUiState(loading = true))
    val uiState: StateFlow<GrowthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GrowthEvent>()
    val events: SharedFlow<GrowthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = GrowthUiState(records = records)
            }
        }
    }

    fun onEvent(event: GrowthEvent) {
        when (event) {
            is GrowthEvent.Add -> {
                viewModelScope.launch { addUseCase(event.height, event.weight, event.headCircumference, event.date) }
            }
            is GrowthEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            GrowthEvent.Refresh -> {}
        }
    }
}
