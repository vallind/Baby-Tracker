package com.babytracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.*
import com.babytracker.core.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter

data class ActiveCareState(
    val type: com.babytracker.designsystem.theme.CareType,
    val label: String,
    val startedAt: Long,
    val elapsedSeconds: Int,
)

data class HomeUiState(
    val feedCount: Int = 0,
    val sleepHours: String = "0h",
    val diaperCount: Int = 0,
    val recentItems: List<Any> = emptyList(),
    val activeCare: ActiveCareState? = null,
    val upcomingReminder: Reminder? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val diaperRepo: DiaperRepository,
    private val reminderRepo: ReminderRepository,
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
                    reminderRepo.watchPending(babyId),
                ) { feedings, sleeps, diapers, reminders ->
                    val today = LocalDate.now().toString()
                    val todayFeedings = feedings.filter { it.timestamp.startsWith(today) }
                    val todaySleeps = sleeps.filter { it.type == SleepType.NIGHT && it.startTime.startsWith(today) }
                    val nightSleepMin = todaySleeps.sumOf {
                        try { Duration.between(LocalDateTime.parse(it.startTime, DateTimeFormatter.ISO_DATE_TIME), LocalDateTime.parse(it.endTime, DateTimeFormatter.ISO_DATE_TIME)).toMinutes() } catch (_: Exception) { 0L }
                    }.toInt()
                    val todayDiapers = diapers.filter { it.timestamp.startsWith(today) }

                    val allItems = (feedings + sleeps + diapers).sortedByDescending {
                        when (it) {
                            is Feeding -> it.timestamp
                            is Sleep -> it.startTime
                            is Diaper -> it.timestamp
                            else -> ""
                        }
                    }.take(8)

                    val upcomingReminder = reminders
                        .filter { !it.isDone && it.isEnabled }
                        .minByOrNull { it.dueDate }

                    HomeUiState(
                        feedCount = todayFeedings.size,
                        sleepHours = if (nightSleepMin > 0) "${nightSleepMin / 60}时${nightSleepMin % 60}分" else "0h",
                        diaperCount = todayDiapers.size,
                        recentItems = allItems,
                        upcomingReminder = upcomingReminder,
                        isLoading = false,
                    )
                }
            }
            .catch { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun loadData(babyId: Int) {
        _trigger.value = babyId
    }
}
