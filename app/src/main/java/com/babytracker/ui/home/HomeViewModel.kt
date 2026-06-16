package com.babytracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.database.entity.*
import com.babytracker.data.repository.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val feedCount: Int = 0,
    val sleepHours: String = "--",
    val diaperCount: Int = 0,
    val recentItems: List<Any> = emptyList(),
    val loading: Boolean = true,
)

class HomeViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository,
    private val diaperRepo: DiaperRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun loadData(babyId: Int) {
        feedingRepo.watchByBaby(babyId)
            .combine(sleepRepo.watchByBaby(babyId)) { feedings, sleeps -> feedings to sleeps }
            .combine(diaperRepo.watchByBaby(babyId)) { (feedings, sleeps), diapers ->
                val today = LocalDate.now().toString()
                val todayFeedings = feedings.filter { it.timestamp.startsWith(today) }
                val todaySleeps = sleeps.filter { it.startTime.startsWith(today) }
                val nightSleepMin = todaySleeps.filter { it.type == "night" }.sumOf {
                    try { Duration.between(LocalDateTime.parse(it.startTime, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(it.endTime, DateTimeFormatter.ISO_DATE_TIME)).toMinutes() } catch (_: Exception) { 0L }
                }.toInt()
                val todayDiapers = diapers.filter { it.timestamp.startsWith(today) }

                val allItems = (feedings + sleeps + diapers).sortedByDescending {
                    when (it) {
                        is FeedingEntity -> it.timestamp
                        is SleepEntity -> it.startTime
                        is DiaperEntity -> it.timestamp
                        else -> ""
                    }
                }.take(8)

                _state.update { HomeUiState(
                    feedCount = todayFeedings.size,
                    sleepHours = if (nightSleepMin > 0) "${nightSleepMin / 60}h${nightSleepMin % 60}min" else "--",
                    diaperCount = todayDiapers.size,
                    recentItems = allItems,
                    loading = false,
                )}
            }.launchIn(viewModelScope)
    }
}