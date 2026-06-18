package com.babytracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.data.repository.*
import com.babytracker.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.LocalDate

sealed class RecentItem {
    data class Feeding(val entity: com.babytracker.domain.model.Feeding) : RecentItem()
    data class Sleep(val entity: com.babytracker.domain.model.Sleep) : RecentItem()
    data class Diaper(val entity: com.babytracker.domain.model.Diaper) : RecentItem()
}

data class HomeUiState(
    val feedCount: Int = 0,
    val sleepHours: String = "--",
    val diaperCount: Int = 0,
    val recentItems: List<RecentItem> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
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
                feedingRepo.watchByBaby(babyId)
                    .combine(sleepRepo.watchByBaby(babyId)) { feedings, sleeps -> feedings to sleeps }
                    .combine(diaperRepo.watchByBaby(babyId)) { (feedings, sleeps), diapers ->
                        val today = LocalDate.now()
                        val todayFeedings = feedings.filter { it.timestamp.toLocalDate() == today }
                        val todaySleeps = sleeps.filter { it.startTime.toLocalDate() == today }
                        val nightSleepMin = todaySleeps.filter { it.type == "night" }.sumOf {
                            Duration.between(it.startTime, it.endTime).toMinutes().coerceAtLeast(0)
                        }.toInt()
                        val todayDiapers = diapers.filter { it.timestamp.toLocalDate() == today }

                        val allItems = (feedings.map { RecentItem.Feeding(it) } + sleeps.map { RecentItem.Sleep(it) } + diapers.map { RecentItem.Diaper(it) }).sortedByDescending {
                            when (it) {
                                is RecentItem.Feeding -> it.entity.timestamp
                                is RecentItem.Sleep -> it.entity.startTime
                                is RecentItem.Diaper -> it.entity.timestamp
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
