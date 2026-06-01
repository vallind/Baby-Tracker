package com.example.myapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.BabyRepository
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.room.BabyEntity
import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.data.room.GrowthEntity
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val baby: BabyEntity? = null,
    val todayFeedingCount: Int = 0,
    val todaySleepHours: Float = 0f,
    val recentFeedings: List<FeedingEntity> = emptyList(),
    val recentSleeps: List<SleepEntity> = emptyList(),
    val recentGrowths: List<GrowthEntity> = emptyList()
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
                _uiState.value = _uiState.value.copy(baby = baby)
            }
        }
        viewModelScope.launch {
            feedingRepo.getAllByBaby().collect { records ->
                val todayStart = todayStartMillis()
                val todayRecords = records.filter { it.createdAt >= todayStart }
                _uiState.value = _uiState.value.copy(
                    todayFeedingCount = todayRecords.size,
                    recentFeedings = records.take(3)
                )
            }
        }
        viewModelScope.launch {
            sleepRepo.getAllByBaby().collect { records ->
                val todayStart = todayStartMillis()
                val todayRecords = records.filter { it.endTime >= todayStart }
                val totalHours = todayRecords.sumOf {
                    (it.endTime - it.startTime) / 3_600_000.0
                }.toFloat()
                _uiState.value = _uiState.value.copy(
                    todaySleepHours = totalHours,
                    recentSleeps = records.take(3)
                )
            }
        }
        viewModelScope.launch {
            growthRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(
                    recentGrowths = records.take(3)
                )
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> {}
        }
    }

    private fun todayStartMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
