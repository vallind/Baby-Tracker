package com.babytracker.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.database.entity.*
import com.babytracker.core.util.DateUtils
import com.babytracker.data.repository.*
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

private data class EntityBundle(
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growths: List<GrowthEntity>,
    val healths: List<HealthRecordEntity>,
    val vaccinations: List<VaccinationEntity>,
)

class TimelineViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val diaperRepo: DiaperRepository,
    private val growthRepo: GrowthRepository,
    private val healthRepo: HealthRepository,
    private val vaccinationRepo: VaccinationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TimelineUiState())
    val state: StateFlow<TimelineUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    private var cachedFeedings: List<FeedingEntity> = emptyList()
    private var cachedSleeps: List<SleepEntity> = emptyList()
    private var cachedDiapers: List<DiaperEntity> = emptyList()
    private var cachedGrowths: List<GrowthEntity> = emptyList()
    private var cachedHealths: List<HealthRecordEntity> = emptyList()
    private var cachedVaccinations: List<VaccinationEntity> = emptyList()

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                val firstFive = combine(
                    feedingRepo.watchByBaby(babyId),
                    sleepRepo.watchByBaby(babyId),
                    diaperRepo.watchByBaby(babyId),
                    growthRepo.watchByBaby(babyId),
                    healthRepo.watchByBaby(babyId),
                ) { f, s, d, g, h ->
                    EntityBundle(feedings = f, sleeps = s, diapers = d, growths = g, healths = h, vaccinations = emptyList())
                }
                combine(firstFive, vaccinationRepo.watchByBaby(babyId)) { bundle, vaccinations ->
                    bundle.copy(vaccinations = vaccinations)
                }
            }
            .map { bundle ->
                cachedFeedings = bundle.feedings
                cachedSleeps = bundle.sleeps
                cachedDiapers = bundle.diapers
                cachedGrowths = bundle.growths
                cachedHealths = bundle.healths
                cachedVaccinations = bundle.vaccinations

                val items = mutableListOf<TimelineItem>()
                var accent = false

                bundle.feedings.forEach { f ->
                    items.add(
                        TimelineItem(
                            id = f.id,
                            recordType = "feeding",
                            emoji = when (f.type) { "breast" -> "🤱"; "formula" -> "💧"; "food" -> "🥣"; else -> "🥤" },
                            title = DateUtils.feedingTypeLabel(f.type),
                            subtitle = when (f.type) {
                                "breast" -> "${f.breastSide ?: "双侧"} · ${f.durationMin}分钟"
                                "formula" -> "${f.amountMl}ml${if (f.brand != null) " · ${f.brand}" else ""}"
                                "food" -> "${f.foodName} ${f.amountG}g"
                                else -> "${f.amountMl}ml"
                            },
                            time = f.timestamp.substring(11, 16),
                            date = f.timestamp.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = f.timestamp,
                        )
                    )
                }

                bundle.sleeps.forEach { s ->
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
                            emoji = if (s.type == "night") "🌙" else "☀️",
                            title = if (s.type == "night") "夜间睡眠" else "小睡",
                            subtitle = buildString {
                                append("${s.startTime.substring(11, 16)}-${s.endTime.substring(11, 16)}")
                                if (secs > 0) append(" · ${DateUtils.durationFullText(secs)}")
                            },
                            time = s.startTime.substring(11, 16),
                            date = s.startTime.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = s.startTime,
                        )
                    )
                }

                bundle.diapers.forEach { d ->
                    items.add(
                        TimelineItem(
                            id = d.id,
                            recordType = "diaper",
                            emoji = "🧷",
                            title = "换尿布",
                            subtitle = DateUtils.diaperTypeLabel(d.type),
                            time = d.timestamp.substring(11, 16),
                            date = d.timestamp.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = d.timestamp,
                        )
                    )
                }

                bundle.growths.forEach { g ->
                    val unit = when (g.type) { "weight" -> "kg"; "height" -> "cm"; else -> "cm" }
                    items.add(
                        TimelineItem(
                            id = g.id,
                            recordType = "growth",
                            emoji = "📏",
                            title = DateUtils.growthTypeLabel(g.type),
                            subtitle = "${g.value}$unit",
                            time = g.measuredAt.substring(11, 16),
                            date = g.measuredAt.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = g.measuredAt,
                        )
                    )
                }

                bundle.healths.forEach { rec ->
                    items.add(
                        TimelineItem(
                            id = rec.id,
                            recordType = "health",
                            emoji = "❤️",
                            title = rec.description.take(30),
                            subtitle = rec.category,
                            time = if (rec.recordDate.length >= 16) rec.recordDate.substring(11, 16) else "",
                            date = rec.recordDate.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = rec.recordDate,
                        )
                    )
                }

                bundle.vaccinations.forEach { v ->
                    val sk = v.administeredDate ?: v.scheduledDate ?: ""
                    items.add(
                        TimelineItem(
                            id = v.id,
                            recordType = "vaccine",
                            emoji = "💉",
                            title = v.name,
                            subtitle = when {
                                v.status == "done" -> "已接种"
                                v.administeredDate != null -> v.administeredDate.take(10)
                                v.scheduledDate != null -> "计划 ${v.scheduledDate.take(10)}"
                                else -> "待安排"
                            },
                            time = "",
                            date = sk.take(10),
                            accent = accent.also { accent = !accent },
                            sortKey = sk,
                        )
                    )
                }

                items.sortByDescending { it.sortKey }

                TimelineUiState(items = items, loading = false)
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun load(babyId: Int) {
        _trigger.value = babyId
    }

    fun delete(item: TimelineItem) {
        viewModelScope.launch {
            when (item.recordType) {
                "feeding" -> cachedFeedings.find { it.id == item.id }?.let { feedingRepo.delete(it) }
                "sleep" -> cachedSleeps.find { it.id == item.id }?.let { sleepRepo.delete(it) }
                "diaper" -> cachedDiapers.find { it.id == item.id }?.let { diaperRepo.delete(it) }
                "growth" -> cachedGrowths.find { it.id == item.id }?.let { growthRepo.delete(it) }
                "health" -> cachedHealths.find { it.id == item.id }?.let { healthRepo.delete(it) }
                "vaccine" -> cachedVaccinations.find { it.id == item.id }?.let { vaccinationRepo.delete(it) }
            }
        }
    }
}
