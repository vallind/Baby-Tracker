package com.example.myapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.SleepRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StatsEvent {
    data object Refresh : StatsEvent
}

class StatsViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState(loading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<StatsEvent>()
    val events: SharedFlow<StatsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            feedingRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(feedingCount = records.size, loading = false)
            }
        }
        viewModelScope.launch {
            sleepRepo.getAllByBaby().collect { records ->
                val totalHours = records.sumOf { (it.endTime - it.startTime) / 3_600_000.0 }.toFloat()
                _uiState.value = _uiState.value.copy(sleepHours = totalHours)
            }
        }
        viewModelScope.launch {
            growthRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(growthTrend = records.sortedBy { it.recordDate }.map { it.height })
            }
        }
    }

    fun onEvent(event: StatsEvent) {
        when (event) { StatsEvent.Refresh -> {} }
    }
}
