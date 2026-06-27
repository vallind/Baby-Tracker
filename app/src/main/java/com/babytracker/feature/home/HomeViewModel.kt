package com.babytracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.database.entity.*
import com.babytracker.core.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val diaperRepo: DiaperRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                combine(
                    feedingRepo.watchByBaby(babyId),
                    sleepRepo.watchByBaby(babyId),
                    diaperRepo.watchByBaby(babyId),
                ) { feedings, sleeps, diapers ->
                    val today = LocalDate.now().toString()
                    val todayFeedings = feedings.filter { it.timestamp.startsWith(today) }
                    val todaySleeps = sleeps.filter { it.type == "night" && it.startTime.startsWith(today) }
                    val nightSleepMin = todaySleeps.sumOf {
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

                    HomeUiState(
                        feedCount = todayFeedings.size,
                        sleepHours = if (nightSleepMin > 0) "${nightSleepMin / 60}h${nightSleepMin % 60}min" else "--",
                        diaperCount = todayDiapers.size,
                        recentItems = allItems,
                        loading = false,
                    )
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun loadData(babyId: Int) {
        _trigger.value = babyId
    }
}
