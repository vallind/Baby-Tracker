package com.babytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.data.repository.*
import com.babytracker.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class StatsPeriod { WEEK, MONTH, YEAR }

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val feedingCount: Int = 0,
    val sleepHours: Float = 0f,
    val diaperCount: Int = 0,
    val height: String = "--",
    val weight: String = "--",
    val feedingPoints: List<Float> = emptyList(),
    val sleepPoints: List<Float> = emptyList(),
    val heightPoints: List<Float> = emptyList(),
    val weightPoints: List<Float> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository,
    private val diaperRepo: DiaperRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Pair<Int, StatsPeriod>?>(null)

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { (babyId, period) ->
                combine(
                    feedingRepo.watchByBaby(babyId),
                    sleepRepo.watchByBaby(babyId),
                    growthRepo.watchByBaby(babyId),
                    diaperRepo.watchByBaby(babyId),
                ) { feedings, sleeps, growths, diapers ->
                    aggregate(period, feedings, sleeps, growths, diapers)
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun loadData(babyId: Int, period: StatsPeriod = StatsPeriod.WEEK) {
        _trigger.value = babyId to period
    }

    private fun aggregate(
        period: StatsPeriod,
        feedings: List<Feeding>,
        sleeps: List<Sleep>,
        growths: List<Growth>,
        diapers: List<Diaper>,
    ): StatsUiState {
        val now = LocalDateTime.now()
        val start = when (period) {
            StatsPeriod.WEEK -> now.minusDays(7)
            StatsPeriod.MONTH -> now.minusDays(30)
            StatsPeriod.YEAR -> now.minusDays(365)
        }

        val feedingsInPeriod = feedings.filter { it.timestamp.isAfter(start) }
        val feedingCount = feedingsInPeriod.size
        val dayCount = ChronoUnit.DAYS.between(start.toLocalDate(), now.toLocalDate()).toInt()
        val feedingPoints = if (feedingsInPeriod.isEmpty()) emptyList() else {
            (0 until dayCount).map { offset ->
                val day = start.toLocalDate().plusDays(offset.toLong())
                feedingsInPeriod.count { it.timestamp.toLocalDate() == day }.toFloat()
            }
        }

        val sleepsInPeriod = sleeps.filter { it.startTime.isAfter(start) }
        val totalSleepMins = sleepsInPeriod.sumOf { s ->
            Duration.between(s.startTime, s.endTime).toMinutes().coerceAtLeast(0)
        }
        val sleepHours = ((totalSleepMins / 60f) * 10).toInt() / 10f
        val sleepPoints = if (sleepsInPeriod.isEmpty()) emptyList() else {
            (0 until dayCount).map { offset ->
                val day = start.toLocalDate().plusDays(offset.toLong())
                val mins = sleepsInPeriod.filter { it.startTime.toLocalDate() == day }
                    .sumOf { s -> Duration.between(s.startTime, s.endTime).toMinutes().coerceAtLeast(0) }
                ((mins / 60f) * 10).toInt() / 10f
            }
        }

        val diapersInPeriod = diapers.filter { it.timestamp.isAfter(start) }
        val diaperCount = diapersInPeriod.size

        val heights = growths
            .filter { it.type == "height" && it.measuredAt.isAfter(start) }
            .sortedBy { it.measuredAt }
        val height = if (heights.isEmpty()) "--"
        else "${(heights.last().value * 10).toInt() / 10.0}cm"
        val heightPoints = heights.map { it.value.toFloat() }

        val weights = growths
            .filter { it.type == "weight" && it.measuredAt.isAfter(start) }
            .sortedBy { it.measuredAt }
        val weight = if (weights.isEmpty()) "--"
        else "${(weights.last().value * 10).toInt() / 10.0}kg"
        val weightPoints = weights.map { it.value.toFloat() }

        return StatsUiState(
            period = period,
            feedingCount = feedingCount,
            sleepHours = sleepHours,
            diaperCount = diaperCount,
            height = height,
            weight = weight,
            feedingPoints = feedingPoints,
            sleepPoints = sleepPoints,
            heightPoints = heightPoints,
            weightPoints = weightPoints,
        )
    }
}
