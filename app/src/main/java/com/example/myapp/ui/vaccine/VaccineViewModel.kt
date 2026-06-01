package com.example.myapp.ui.vaccine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.VaccineRepository
import com.example.myapp.data.room.VaccineEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VaccineEvent {
    data class Add(val name: String, val dose: Int, val plannedDate: Long) : VaccineEvent
    data class MarkCompleted(val id: Long) : VaccineEvent
}

class VaccineViewModel(
    private val repository: VaccineRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VaccineUiState(loading = true))
    val uiState: StateFlow<VaccineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VaccineEvent>()
    val events: SharedFlow<VaccineEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { vaccines ->
                _uiState.value = VaccineUiState(vaccines = vaccines)
            }
        }
    }

    fun onEvent(event: VaccineEvent) {
        when (event) {
            is VaccineEvent.Add -> {
                viewModelScope.launch {
                    repository.insert(VaccineEntity(name = event.name, dose = event.dose, plannedDate = event.plannedDate))
                }
            }
            is VaccineEvent.MarkCompleted -> {
                viewModelScope.launch {
                    repository.markCompleted(event.id, System.currentTimeMillis())
                }
            }
        }
    }
}
