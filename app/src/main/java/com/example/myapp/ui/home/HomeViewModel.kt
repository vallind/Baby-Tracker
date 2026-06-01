package com.example.myapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.BabyRepository
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

data class HomeUiState(
    val babyName: String = "",
    val recentFeedingCount: Int = 0,
    val recentSleepHours: Float = 0f
)

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}

class HomeViewModel(
    private val babyRepo: BabyRepository,
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            babyRepo.firstBaby.collect { baby ->
                _uiState.value = _uiState.value.copy(babyName = baby?.name ?: "")
            }
        }
        viewModelScope.launch {
            feedingRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(recentFeedingCount = records.size)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> {}
        }
    }
}
