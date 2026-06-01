package com.example.myapp.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.HealthRepository
import com.example.myapp.data.room.HealthProfileEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HealthEvent {
    data class Save(val profile: HealthProfileEntity) : HealthEvent
}

class HealthViewModel(
    private val repository: HealthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HealthUiState(loading = true))
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<HealthEvent>()
    val events: SharedFlow<HealthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getByBaby().collect { profile ->
                _uiState.value = HealthUiState(profile = profile)
            }
        }
    }

    fun onEvent(event: HealthEvent) {
        when (event) {
            is HealthEvent.Save -> viewModelScope.launch { repository.save(event.profile) }
        }
    }
}
