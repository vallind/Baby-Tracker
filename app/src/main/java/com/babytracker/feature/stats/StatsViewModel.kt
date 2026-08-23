package com.babytracker.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.*
import com.babytracker.core.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

data class StatsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val period: StatsPeriod = StatsPeriod.WEEK,
    /** 当前展示周期的偏移量（0 = 本周/今日, -1 = 上一期, 1 = 下一期），由导航箭头控制 */
    val periodOffset: Int = 0,
    /** 日期范围展示文本，如 "5.13 - 5.19" */
    val dateRangeText: String = "",
    // —— 当前周期数据 ——
    val feedingCount: Int = 0,
    val breastFeedCount: Int = 0,
    val formulaCount: Int = 0,
    val formulaTotalMl: Int = 0,
    val sleepMinutes: Long = 0,
    // —— 尿布（2.1 新增，H5） ——
    val diaperCount: Int = 0,
    val diaperWetCount: Int = 0,
    val diaperPoopCount: Int = 0,
    val diaperBothCount: Int = 0,
    val height: String = "--",
    val heightRaw: Float = 0f,
    val weight: String = "--",
    val weightRaw: Float = 0f,
    // —— 对比文案（与上一周期比较） ——
    val feedingCompare: String = "",
    val sleepCompare: String = "",
    val heightCompare: String = "",
    val weightCompare: String = "",
    // —— 图表数据点 ——
    val feedingPoints: List<Float> = emptyList(),
    val sleepPoints: List<Float> = emptyList(),
    val heightPoints: List<Float> = emptyList(),
    val weightPoints: List<Float> = emptyList(),
) {
    val hasAnyData: Boolean
        get() = feedingCount > 0 ||
            sleepMinutes > 0 ||
            heightPoints.isNotEmpty() ||
            weightPoints.isNotEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository,
    private val diaperRepo: DiaperRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    /** 触发器：babyId + period + offset 三元组 */
    private data class Trigger(
        val babyId: Int,
        val period: StatsPeriod,
        val offset: Int,
        val requestId: Int = 0,
    )

    private val _trigger = MutableStateFlow<Trigger?>(null)

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { (babyId, period, offset) ->
                combine(
                    feedingRepo.watchByBaby(babyId),
                    sleepRepo.watchByBaby(babyId),
                    growthRepo.watchByBaby(babyId),
                    diaperRepo.watchByBaby(babyId),
                ) { feedings, sleeps, growths, diapers ->
                    aggregateStats(period, offset, feedings, sleeps, growths, diapers)
                }
                    .onStart {
                        emit(
                            _state.value.copy(
                                isLoading = true,
                                errorMessage = null,
                                period = period,
                                periodOffset = offset,
                                dateRangeText = statsBuildDateRangeText(
                                    period,
                                    statsPeriodStart(period, offset),
                                    statsPeriodEnd(period, offset),
                                ),
                            ),
                        )
                    }
                    .catch {
                        emit(
                            _state.value.copy(
                                isLoading = false,
                                errorMessage = "统计数据加载失败，请稍后重试",
                                period = period,
                                periodOffset = offset,
                            ),
                        )
                    }
            }
            .distinctUntilChanged()
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun selectPeriod(babyId: Int, period: StatsPeriod) {
        _trigger.value = Trigger(babyId, period, 0)
    }

    fun goBack(babyId: Int) {
        val t = _trigger.value ?: return
        _trigger.value = t.copy(offset = t.offset - 1)
    }

    fun goForward(babyId: Int) {
        val t = _trigger.value ?: return
        if (t.offset < 0) _trigger.value = t.copy(offset = t.offset + 1)
    }

    fun loadData(babyId: Int, period: StatsPeriod = StatsPeriod.WEEK) {
        _trigger.value = Trigger(babyId, period, 0)
    }

    fun retry() {
        val trigger = _trigger.value ?: return
        _trigger.value = trigger.copy(requestId = trigger.requestId + 1)
    }
}

// ═══════════════════════════════════════════════════════════════
// 聚合核心（顶层纯函数，供 ViewModel 与单元测试共同调用）
// ═══════════════════════════════════════════════════════════════

internal fun aggregateStats(
    period: StatsPeriod,
    offset: Int,
    feedings: List<Feeding>,
    sleeps: List<Sleep>,
    growths: List<Growth>,
    diapers: List<Diaper> = emptyList(),
): StatsUiState {
    val start = statsPeriodStart(period, offset)
    val end = statsPeriodEnd(period, offset)
    val dateRangeText = statsBuildDateRangeText(period, start, end)

    val prevStart = statsPeriodStart(period, offset - 1)
    val prevEnd = statsPeriodEnd(period, offset - 1)

    // ── 喂养 ──
    val feedingInRange = feedings.filter { statsInRange(it.timestamp, start, end) }
    val feedingPrevInRange = feedings.filter { statsInRange(it.timestamp, prevStart, prevEnd) }
    val bucketCount = statsPeriodBucketCount(period)
    val feedingPoints = statsBucketByDay(period, feedingInRange, start, bucketCount) { 1f }
    // 计数以能落入图表的记录为准，避免时间戳解析失败的记录在数字与柱状图间不一致
    val feedingCount = feedingPoints.sum().toInt()
    val breastFeedCount = feedingInRange.count { it.type == FeedingType.BREAST }
    val formulaCount = feedingInRange.count { it.type == FeedingType.FORMULA }
    val formulaTotalMl = feedingInRange.filter { it.type == FeedingType.FORMULA }.sumOf { it.amountMl ?: 0 }
    val feedingCompare = statsBuildCompare((feedingCount - feedingPrevInRange.size).toLong(), "次")

    val sleepPoints = statsBucketByDay(
        period,
        sleeps.filter { statsInRange(it.startTime, start, end) },
        start,
        bucketCount,
    ) { s -> statsSleepDurationMinutes(s).toFloat() / 60f }

    // ── 睡眠 ──
    val sleepMinutes = sleeps.filter { statsInRange(it.startTime, start, end) }.sumOf { statsSleepDurationMinutes(it) }
    val sleepPrevMinutes = sleeps.filter { statsInRange(it.startTime, prevStart, prevEnd) }.sumOf { statsSleepDurationMinutes(it) }
    val sleepDiffHours = if (sleepMinutes == 0L && sleepPrevMinutes == 0L) 0L
    else (sleepMinutes - sleepPrevMinutes + 30) / 60
    val sleepCompare = statsBuildCompare(sleepDiffHours, "时")

    // ── 尿布（2.1 新增） ──
    val diaperInRange = diapers.filter { statsInRange(it.timestamp, start, end) }
    val diaperCount = diaperInRange.size
    val diaperWetCount = diaperInRange.count { it.type == DiaperType.WET }
    val diaperPoopCount = diaperInRange.count { it.type == DiaperType.POOP }
    val diaperBothCount = diaperInRange.count { it.type == DiaperType.BOTH }

    // ── 身高/体重 ──
    val (heightRaw, heightPoints) = statsLatestGrowth(growths, GrowthType.HEIGHT, start, end)
    val (heightPrevRaw, _) = statsLatestGrowth(growths, GrowthType.HEIGHT, prevStart, prevEnd)
    val height = if (heightRaw < 0) "--" else "${(heightRaw * 10).toInt() / 10.0}cm"
    val heightCompare = if (heightRaw < 0 || heightPrevRaw < 0) ""
    else statsBuildCompare(((heightRaw - heightPrevRaw) * 10).toInt() / 10f, "cm")

    val (weightRaw, weightPoints) = statsLatestGrowth(growths, GrowthType.WEIGHT, start, end)
    val (weightPrevRaw, _) = statsLatestGrowth(growths, GrowthType.WEIGHT, prevStart, prevEnd)
    val weight = if (weightRaw < 0) "--" else "${(weightRaw * 10).toInt() / 10.0}kg"
    val weightCompare = if (weightRaw < 0 || weightPrevRaw < 0) ""
    else statsBuildCompare(((weightRaw - weightPrevRaw) * 10).toInt() / 10f, "kg")

    return StatsUiState(
        isLoading = false,
        errorMessage = null,
        period = period,
        periodOffset = offset,
        dateRangeText = dateRangeText,
        diaperCount = diaperCount,
        diaperWetCount = diaperWetCount,
        diaperPoopCount = diaperPoopCount,
        diaperBothCount = diaperBothCount,
        feedingCount = feedingCount,
        breastFeedCount = breastFeedCount,
        formulaCount = formulaCount,
        formulaTotalMl = formulaTotalMl,
        sleepMinutes = sleepMinutes,
        height = height,
        heightRaw = heightRaw,
        weight = weight,
        weightRaw = weightRaw,
        feedingCompare = feedingCompare,
        sleepCompare = sleepCompare,
        heightCompare = heightCompare,
        weightCompare = weightCompare,
        feedingPoints = feedingPoints,
        sleepPoints = sleepPoints,
        heightPoints = heightPoints,
        weightPoints = weightPoints,
    )
}

// ── 时间窗口工具 ──

/** 周期的起始时间（以当前 offset 计算） */
internal fun statsPeriodStart(period: StatsPeriod, offset: Int): LocalDateTime {
    val now = LocalDate.now()
    return when (period) {
        StatsPeriod.DAY -> now.plusDays(offset.toLong()).atStartOfDay()
        StatsPeriod.WEEK -> {
            // 周日作为一周开始
            val dayOfWeek = now.dayOfWeek.value % 7 // 0=周日
            val thisWeekStart = now.minusDays(dayOfWeek.toLong())
            thisWeekStart.plusWeeks(offset.toLong()).atStartOfDay()
        }
        StatsPeriod.MONTH -> now.withDayOfMonth(1).plusMonths(offset.toLong()).atStartOfDay()
        StatsPeriod.YEAR -> now.withDayOfYear(1).plusYears(offset.toLong()).atStartOfDay()
    }
}

/** 周期的结束时间（不含） */
internal fun statsPeriodEnd(period: StatsPeriod, offset: Int): LocalDateTime {
    return when (period) {
        StatsPeriod.DAY -> statsPeriodStart(period, offset).plusDays(1)
        StatsPeriod.WEEK -> statsPeriodStart(period, offset).plusWeeks(1)
        StatsPeriod.MONTH -> statsPeriodStart(period, offset).plusMonths(1)
        StatsPeriod.YEAR -> statsPeriodStart(period, offset).plusYears(1)
    }
}

/** 用于图表分桶的天数 */
internal fun statsPeriodBucketCount(period: StatsPeriod): Int = when (period) {
    StatsPeriod.DAY -> 24  // 按小时
    StatsPeriod.WEEK -> 7
    StatsPeriod.MONTH -> 30
    StatsPeriod.YEAR -> 12 // 按月
}

// ── 格式化工具 ──

internal fun statsBuildDateRangeText(period: StatsPeriod, start: LocalDateTime, end: LocalDateTime): String {
    val fmt = when (period) {
        StatsPeriod.DAY -> DateTimeFormatter.ofPattern("M.d")
        StatsPeriod.WEEK, StatsPeriod.MONTH -> DateTimeFormatter.ofPattern("M.d")
        StatsPeriod.YEAR -> DateTimeFormatter.ofPattern("yyyy.M.d")
    }
    val endDisplay = end.minusDays(1)
    return "${start.format(fmt)} - ${endDisplay.format(fmt)}"
}

/** 构建同比对比文案：+6次 / -2时 */
internal fun statsBuildCompare(diff: Long, suffix: String): String = when {
    diff > 0 -> "+$diff$suffix"
    diff < 0 -> "$diff$suffix"
    else -> ""
}

/** Float 版对比文案 */
internal fun statsBuildCompare(diff: Float, suffix: String): String = when {
    diff > 0f -> "+$diff$suffix"
    diff < 0f -> "$diff$suffix"
    else -> ""
}

// ── 分桶辅助：按日分组数据 → points 列表 ──

internal fun <T> statsBucketByDay(
    period: StatsPeriod,
    items: List<T>,
    periodStart: LocalDateTime,
    bucketCount: Int,
    valueExtractor: (T) -> Float,
): List<Float> {
    if (period.let { it == StatsPeriod.DAY }) {
        // 日视图：按小时分 24 桶
        val byHour = items.groupBy { item ->
            try {
                val ts = when (item) {
                    is Feeding -> item.timestamp
                    is Sleep -> item.startTime
                    else -> ""
                }
                LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).hour
            } catch (_: Exception) { -1 }
        }
        return (0 until 24).map { hour -> byHour[hour]?.sumOf { valueExtractor(it).toDouble() }?.toFloat() ?: 0f }
    }
    val byDay: Map<LocalDate, Float> = items.groupBy { item ->
        val ts = when (item) {
            is Feeding -> item.timestamp
            is Sleep -> item.startTime
            else -> ""
        }
        try { LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() }
        catch (_: Exception) { LocalDate.MIN }
    }.mapValues { (_, list) -> list.sumOf { valueExtractor(it).toDouble() }.toFloat() }

    return (0 until bucketCount).map { offset ->
        val day = periodStart.toLocalDate().plusDays(offset.toLong())
        byDay[day] ?: 0f
    }
}

internal fun statsInRange(timestamp: String, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Boolean {
    val t = try { LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { return false }
    return !t.isBefore(rangeStart) && t.isBefore(rangeEnd)
}

internal fun statsSleepDurationMinutes(sleep: Sleep): Long {
    val st = try { LocalDateTime.parse(sleep.startTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { return 0L }
    val et = try { LocalDateTime.parse(sleep.endTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { return 0L }
    return Duration.between(st, et).toMinutes().coerceAtLeast(0)
}

internal fun statsLatestGrowth(growths: List<Growth>, type: GrowthType, rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Pair<Float, List<Float>> {
    val filtered = growths.filter { it.type == type && statsInRange(it.measuredAt, rangeStart, rangeEnd) }.sortedBy { it.measuredAt }
    val raw = filtered.lastOrNull()?.value?.toFloat() ?: -1f
    return raw to filtered.map { it.value.toFloat() }
}
