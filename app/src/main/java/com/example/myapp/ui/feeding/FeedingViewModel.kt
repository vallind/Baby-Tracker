package com.example.myapp.ui.feeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FeedingEvent {
    data class Add(val type: String, val amount: Int, val unit: String, val note: String?) : FeedingEvent
    data class Delete(val entity: FeedingEntity) : FeedingEvent
    data object Refresh : FeedingEvent
}

class FeedingViewModel(
    private val repository: FeedingRepository,
    private val addUseCase: AddFeedingUseCase,
    private val deleteUseCase: DeleteFeedingUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedingUiState(loading = true))
    val uiState: StateFlow<FeedingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedingEvent>()
    val events: SharedFlow<FeedingEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = FeedingUiState(records = records)
            }
        }
    }

    fun onEvent(event: FeedingEvent) {
        when (event) {
            is FeedingEvent.Add -> {
                viewModelScope.launch {
                    addUseCase(event.type, event.amount, event.unit, event.note)
                }
            }
            is FeedingEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            FeedingEvent.Refresh -> {}
        }
    }
}
