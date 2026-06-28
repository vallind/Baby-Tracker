package com.babytracker.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.database.entity.*
import com.babytracker.core.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
            .distinctUntilChanged()
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun loadData(babyId: Int, period: StatsPeriod = StatsPeriod.WEEK) {
        _trigger.value = babyId to period
    }

    private fun aggregate(
        period: StatsPeriod,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        growths: List<GrowthEntity>,
        diapers: List<DiaperEntity>,
    ): StatsUiState {
        val now = LocalDateTime.now()
        val start = when (period) {
            StatsPeriod.WEEK -> now.minusDays(7)
            StatsPeriod.MONTH -> now.minusDays(30)
            StatsPeriod.YEAR -> now.minusDays(365)
        }

        val fmt = DateTimeFormatter.ISO_DATE_TIME
        fun safeParse(dt: String) = try { LocalDateTime.parse(dt, fmt) } catch (_: Exception) { null }

        val feedingsInPeriod = feedings.filter { safeParse(it.timestamp)?.isAfter(start) == true }
        val feedingCount = feedingsInPeriod.size
        val dayCount = ChronoUnit.DAYS.between(start.toLocalDate(), now.toLocalDate()).toInt()
        val feedingPoints = if (feedingsInPeriod.isEmpty()) emptyList() else {
            val byDay = feedingsInPeriod.groupBy { safeParse(it.timestamp)?.toLocalDate() }
            (0 until dayCount).map { offset ->
                val day = start.toLocalDate().plusDays(offset.toLong())
                (byDay[day]?.size ?: 0).toFloat()
            }
        }

        val sleepsInPeriod = sleeps.filter { safeParse(it.startTime)?.isAfter(start) == true }
        val totalSleepMins = sleepsInPeriod.sumOf { s ->
            val st = safeParse(s.startTime) ?: return@sumOf 0L
            val et = safeParse(s.endTime) ?: return@sumOf 0L
            Duration.between(st, et).toMinutes().coerceAtLeast(0)
        }
        val sleepHours = ((totalSleepMins / 60f) * 10).toInt() / 10f
        val sleepPoints = if (sleepsInPeriod.isEmpty()) emptyList() else {
            val byDay: Map<java.time.LocalDate, Long> = sleepsInPeriod.groupBy(
                { safeParse(it.startTime)?.toLocalDate() ?: java.time.LocalDate.MIN },
            ) { s ->
                val st = safeParse(s.startTime) ?: return@groupBy 0L
                val et = safeParse(s.endTime) ?: return@groupBy 0L
                Duration.between(st, et).toMinutes().coerceAtLeast(0)
            }.mapValues { it.value.sum() }
            (0 until dayCount).map { offset ->
                val day = start.toLocalDate().plusDays(offset.toLong())
                ((byDay[day]?.toFloat() ?: 0f) / 60f * 10).toInt() / 10f
            }
        }

        val diapersInPeriod = diapers.filter { safeParse(it.timestamp)?.isAfter(start) == true }
        val diaperCount = diapersInPeriod.size

        val heights = growths
            .filter { it.type == "height" && safeParse(it.measuredAt)?.isAfter(start) == true }
            .sortedBy { it.measuredAt }
        val height = if (heights.isEmpty()) "--"
        else "${(heights.last().value * 10).toInt() / 10.0}cm"
        val heightPoints = heights.map { it.value.toFloat() }

        val weights = growths
            .filter { it.type == "weight" && safeParse(it.measuredAt)?.isAfter(start) == true }
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