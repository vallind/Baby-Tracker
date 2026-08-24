package com.babytracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.*
import com.babytracker.core.data.repository.*
import com.babytracker.core.util.BabyController
import com.babytracker.designsystem.i18n.AppStrings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val feedCount: Int = 0,
    val breastFeedCount: Int = 0,
    val formulaCount: Int = 0,
    val formulaTotalMl: Int = 0,
    val sleepHours: String = "--",
    val diaperCount: Int = 0,
    val recentItems: List<Any> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val diaperRepo: DiaperRepository,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    /**
     * 当前宝宝（Batch 2 自 Screen 迁入）。
     * babyCtrl.currentBabyId 是 Compose 状态（VM 侧不可观察），因此以 watchAll 列表派生：
     * 列表变化会重新求值，与原本 Screen 内 `find ?: firstOrNull` 语义一致。
     */
    val baby: StateFlow<Baby?> = babyRepo.watchAll()
        .map { babies -> babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
                    val todaySleeps = sleeps.filter {
                        (it.type == SleepType.NIGHT || it.type == SleepType.NAP) &&
                        (it.startTime.startsWith(today) || it.endTime.startsWith(today))
                    }
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

                    HomeUiState(
                        feedCount = todayFeedings.size,
                        breastFeedCount = todayFeedings.count { it.type == FeedingType.BREAST },
                        formulaCount = todayFeedings.count { it.type == FeedingType.FORMULA },
                        formulaTotalMl = todayFeedings.filter { it.type == FeedingType.FORMULA }.sumOf { it.amountMl ?: 0 },
                        sleepHours = if (nightSleepMin > 0) "${String.format(Locale.US, AppStrings.hoursMinutesFormat, nightSleepMin / 60, nightSleepMin % 60)}" else "--",
                        diaperCount = todayDiapers.size,
                        recentItems = allItems,
                        loading = false,
                    )
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)

        // 当前宝宝变化时：同步选中 + 加载今日数据（原 HomeScreen 内 LaunchedEffect 迁入）
        viewModelScope.launch {
            baby.filterNotNull().collectLatest { b ->
                if (babyCtrl.currentBabyId != b.id) babyCtrl.selectBaby(b.id)
                _trigger.value = b.id
            }
        }
    }

    fun loadData(babyId: Int) {
        _trigger.value = babyId
    }
}