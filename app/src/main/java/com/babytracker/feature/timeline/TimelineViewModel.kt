package com.babytracker.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.*
import com.babytracker.core.util.DateUtils
import com.babytracker.core.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TimelineItem(
    val id: Int,
    val recordType: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val date: String,
    val accent: Boolean,
    internal val sortKey: String = "",
)

data class TimelineUiState(
    val items: List<TimelineItem> = emptyList(),
    val loading: Boolean = true,
)

/** 提取 "yyyy-MM-ddTHH:mm" 中的 HH:mm；短串/空串（同步或还原数据）返回空串，避免下标越界崩溃 */
private fun timePart(s: String): String =
    if (s.length >= 16) s.substring(11, 16) else ""

private data class EntityBundle(
    val feedings: List<Feeding>,
    val sleeps: List<Sleep>,
    val diapers: List<Diaper>,
    val growths: List<Growth>,
    val healths: List<HealthRecord>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val diaperRepo: DiaperRepository,
    private val growthRepo: GrowthRepository,
    private val healthRepo: HealthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TimelineUiState())
    val state: StateFlow<TimelineUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    private var cachedFeedings: List<Feeding> = emptyList()
    private var cachedSleeps: List<Sleep> = emptyList()
    private var cachedDiapers: List<Diaper> = emptyList()
    private var cachedGrowths: List<Growth> = emptyList()
    private var cachedHealths: List<HealthRecord> = emptyList()

    // 撤销删除：暂存最近一次删除的实体
    private var lastDeletedEntity: Any? = null
    private var lastDeletedType: String? = null

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                combine(
                    feedingRepo.watchByBaby(babyId),
                    sleepRepo.watchByBaby(babyId),
                    diaperRepo.watchByBaby(babyId),
                    growthRepo.watchByBaby(babyId),
                    healthRepo.watchByBaby(babyId),
                ) { f, s, d, g, h ->
                    EntityBundle(feedings = f, sleeps = s, diapers = d, growths = g, healths = h)
                }
            }
            .map { bundle ->
                cachedFeedings = bundle.feedings
                cachedSleeps = bundle.sleeps
                cachedDiapers = bundle.diapers
                cachedGrowths = bundle.growths
                cachedHealths = bundle.healths

                TimelineUiState(
                    items = toTimelineItems(
                        bundle.feedings, bundle.sleeps, bundle.diapers, bundle.growths, bundle.healths,
                    ),
                    loading = false,
                )
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun load(babyId: Int) {
        _trigger.value = babyId
    }

    /** 删除记录，并在内部暂存实体副本用于可能的撤销操作 */
    fun delete(item: TimelineItem) {
        viewModelScope.launch {
            when (item.recordType) {
                "feeding" -> cachedFeedings.find { it.id == item.id }?.let {
                    lastDeletedEntity = it; lastDeletedType = "feeding"; feedingRepo.delete(it)
                }
                "sleep" -> cachedSleeps.find { it.id == item.id }?.let {
                    lastDeletedEntity = it; lastDeletedType = "sleep"; sleepRepo.delete(it)
                }
                "diaper" -> cachedDiapers.find { it.id == item.id }?.let {
                    lastDeletedEntity = it; lastDeletedType = "diaper"; diaperRepo.delete(it)
                }
                "growth" -> cachedGrowths.find { it.id == item.id }?.let {
                    lastDeletedEntity = it; lastDeletedType = "growth"; growthRepo.delete(it)
                }
                "health" -> cachedHealths.find { it.id == item.id }?.let {
                    lastDeletedEntity = it; lastDeletedType = "health"; healthRepo.delete(it)
                }
            }
        }
    }

    fun findFeeding(id: Int): Feeding? = cachedFeedings.find { it.id == id }
    fun findSleep(id: Int): Sleep? = cachedSleeps.find { it.id == id }
    fun findDiaper(id: Int): Diaper? = cachedDiapers.find { it.id == id }
    fun findGrowth(id: Int): Growth? = cachedGrowths.find { it.id == id }
    fun findHealth(id: Int): HealthRecord? = cachedHealths.find { it.id == id }

    fun addFeeding(e: Feeding) { viewModelScope.launch { feedingRepo.insert(e) } }
    fun addSleep(e: Sleep) { viewModelScope.launch { sleepRepo.insert(e) } }
    fun addDiaper(e: Diaper) { viewModelScope.launch { diaperRepo.insert(e) } }

    fun updateFeeding(e: Feeding) { viewModelScope.launch { feedingRepo.update(e) } }
    fun updateSleep(e: Sleep) { viewModelScope.launch { sleepRepo.update(e) } }
    fun updateDiaper(e: Diaper) { viewModelScope.launch { diaperRepo.update(e) } }
    fun updateGrowth(e: Growth) { viewModelScope.launch { growthRepo.update(e) } }
    fun updateHealth(e: HealthRecord) { viewModelScope.launch { healthRepo.update(e) } }

    /** 撤销最近一次删除 */
    fun undoLastDelete() {
        viewModelScope.launch {
            when (lastDeletedType) {
                "feeding" -> (lastDeletedEntity as? Feeding)?.let { feedingRepo.update(it) }
                "sleep" -> (lastDeletedEntity as? Sleep)?.let { sleepRepo.update(it) }
                "diaper" -> (lastDeletedEntity as? Diaper)?.let { diaperRepo.update(it) }
                "growth" -> (lastDeletedEntity as? Growth)?.let { growthRepo.update(it) }
                "health" -> (lastDeletedEntity as? HealthRecord)?.let { healthRepo.update(it) }
            }
            lastDeletedEntity = null
            lastDeletedType = null
        }
    }
}

/**
 * 五类记录 → TimelineItem 列表（按时间倒序）。
 * 顶层纯函数，供 ViewModel 与单元测试共同调用（禁止测试镜像复制）。
 */
internal fun toTimelineItems(
    feedings: List<Feeding>,
    sleeps: List<Sleep>,
    diapers: List<Diaper>,
    growths: List<Growth>,
    healths: List<HealthRecord>,
): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    var accent = false

    feedings.forEach { f ->
        items.add(
            TimelineItem(
                id = f.id,
                recordType = "feeding",
                emoji = when (f.type) { FeedingType.BREAST -> "🤱"; FeedingType.FORMULA -> "💧"; FeedingType.FOOD -> "🥣"; else -> "🥤" },
                title = DateUtils.feedingTypeLabel(FeedingType.raw(f.type)),
                subtitle = when (f.type) {
                    FeedingType.BREAST -> "${f.breastSide?.let { BreastSide.raw(it) } ?: "双侧"} · ${f.durationMin}分钟"
                    FeedingType.FORMULA -> "${f.amountMl}ml${if (f.brand != null) " · ${f.brand}" else ""}"
                    FeedingType.FOOD -> "${f.foodName} ${f.amountG}g"
                    else -> "${f.amountMl}ml"
                },
                time = timePart(f.timestamp),
                date = f.timestamp.take(10),
                accent = accent.also { accent = !accent },
                sortKey = f.timestamp,
            )
        )
    }

    sleeps.forEach { s ->
        val secs = try {
            Duration.between(
                LocalDateTime.parse(s.startTime, DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse(s.endTime, DateTimeFormatter.ISO_DATE_TIME),
            ).seconds
        } catch (_: Exception) { 0L }
        items.add(
            TimelineItem(
                id = s.id,
                recordType = "sleep",
                emoji = if (s.type == SleepType.NIGHT) "🌙" else "☀️",
                title = if (s.type == SleepType.NIGHT) "夜间睡眠" else "小睡",
                subtitle = buildString {
                    append("${timePart(s.startTime)}-${timePart(s.endTime)}")
                    if (secs > 0) append(" · ${DateUtils.durationFullText(secs)}")
                },
                time = timePart(s.startTime),
                date = s.startTime.take(10),
                accent = accent.also { accent = !accent },
                sortKey = s.startTime,
            )
        )
    }

    diapers.forEach { d ->
        items.add(
            TimelineItem(
                id = d.id,
                recordType = "diaper",
                emoji = "🧷",
                title = "换尿布",
                subtitle = DateUtils.diaperTypeLabel(DiaperType.raw(d.type)),
                time = timePart(d.timestamp),
                date = d.timestamp.take(10),
                accent = accent.also { accent = !accent },
                sortKey = d.timestamp,
            )
        )
    }

    growths.forEach { g ->
        val unit = when (g.type) { GrowthType.WEIGHT -> "kg"; GrowthType.HEIGHT -> "cm"; else -> "cm" }
        items.add(
            TimelineItem(
                id = g.id,
                recordType = "growth",
                emoji = "📏",
                title = DateUtils.growthTypeLabel(GrowthType.raw(g.type)),
                subtitle = "${g.value}$unit",
                time = timePart(g.measuredAt),
                date = g.measuredAt.take(10),
                accent = accent.also { accent = !accent },
                sortKey = g.measuredAt,
            )
        )
    }

    healths.forEach { rec ->
        items.add(
            TimelineItem(
                id = rec.id,
                recordType = "health",
                emoji = "❤️",
                title = rec.description.take(30),
                subtitle = rec.category,
                time = timePart(rec.recordDate),
                date = rec.recordDate.take(10),
                accent = accent.also { accent = !accent },
                sortKey = rec.recordDate,
            )
        )
    }

    items.sortByDescending { it.sortKey }
    return items
}
