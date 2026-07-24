package com.babytracker.feature.ai

import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.data.repository.DiaperRepository
import com.babytracker.core.data.repository.FeedingRepository
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.core.data.repository.HealthRepository
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

enum class AiQuestionCategory {
    GENERAL,
    SLEEP,
    FEEDING,
    DIAPER,
    GROWTH,
    HEALTH,
}

data class AiContextResult(
    val prompt: String,
    val references: List<String>,
    val hasRecords: Boolean = true,
)

class AiContextBuilder(
    private val feedingRepository: FeedingRepository,
    private val sleepRepository: SleepRepository,
    private val growthRepository: GrowthRepository,
    private val healthRepository: HealthRepository,
    private val diaperRepository: DiaperRepository,
) {
    suspend fun build(
        babyId: Int,
        question: String,
        preferences: AiAssistantPreferences,
        analysisSource: AiAnalysisSource? = null,
        analysisPeriod: AiAnalysisPeriod = AiAnalysisPeriod.DAYS_7,
    ): AiContextResult {
        if (analysisSource != null) {
            if (!isAnalysisSourceEnabled(analysisSource, preferences)) {
                return AiContextResult("", emptyList(), hasRecords = false)
            }
            return buildAnalysisContext(babyId, analysisSource, analysisPeriod, preferences)
        }
        val category = classifyAiQuestion(question)
        if (!isAiContextEnabled(category, preferences)) return AiContextResult("", emptyList())
        val now = LocalDateTime.now()
        return when (category) {
            AiQuestionCategory.GENERAL -> AiContextResult("", emptyList())
            AiQuestionCategory.SLEEP -> {
                val sleep = summarizeSleep(sleepRepository.watchByBaby(babyId).first(), now)
                val parts = mutableListOf(sleep to "最近7天睡眠")
                if (preferences.useFeedingRecords) {
                    parts += summarizeFeeding(feedingRepository.watchByBaby(babyId).first(), now) to
                        "最近3天喂养"
                }
                parts.toContextResult()
            }
            AiQuestionCategory.FEEDING -> {
                val feeding = summarizeFeeding(feedingRepository.watchByBaby(babyId).first(), now)
                val parts = mutableListOf(feeding to "最近3天喂养")
                if (preferences.useGrowthRecords) {
                    parts += summarizeGrowth(growthRepository.watchByBaby(babyId).first(), now) to
                        "最近90天生长"
                }
                parts.toContextResult()
            }
            AiQuestionCategory.DIAPER -> {
                val diaper = summarizeDiaper(diaperRepository.watchByBaby(babyId).first(), now)
                val parts = mutableListOf(diaper to "最近3天尿布")
                if (preferences.useFeedingRecords) {
                    parts += summarizeFeeding(feedingRepository.watchByBaby(babyId).first(), now) to
                        "最近3天喂养"
                }
                parts.toContextResult()
            }
            AiQuestionCategory.GROWTH -> listOf(
                summarizeGrowth(growthRepository.watchByBaby(babyId).first(), now) to
                    "最近90天生长",
            ).toContextResult()
            AiQuestionCategory.HEALTH -> listOf(
                summarizeHealth(healthRepository.watchByBaby(babyId).first(), now) to
                    "最近30天健康记录",
            ).toContextResult()
        }
    }

    suspend fun availableAnalyses(
        babyId: Int,
        preferences: AiAssistantPreferences,
        period: AiAnalysisPeriod,
    ): Set<AiAnalysisSource> = AiAnalysisSource.entries
        .filterTo(mutableSetOf()) { source ->
            isAnalysisSourceEnabled(source, preferences) &&
                buildAnalysisContext(babyId, source, period, preferences).hasRecords
        }

    private suspend fun buildAnalysisContext(
        babyId: Int,
        source: AiAnalysisSource,
        period: AiAnalysisPeriod,
        preferences: AiAssistantPreferences,
    ): AiContextResult {
        val now = LocalDateTime.now()
        val days = period.days
        return when (source) {
            AiAnalysisSource.SLEEP -> {
                val sleep = summarizeSleepComparison(
                    sleepRepository.watchByBaby(babyId).first(),
                    now,
                    days,
                )
                val parts = mutableListOf(
                    sleep to "最近${days}天与前${days}天睡眠",
                )
                if (preferences.useFeedingRecords) {
                    parts += summarizeFeedingComparison(
                        feedingRepository.watchByBaby(babyId).first(),
                        now,
                        days,
                    ) to "最近${days}天与前${days}天喂养"
                }
                parts.toContextResult(requiredSummary = sleep)
            }
            AiAnalysisSource.FEEDING -> {
                val feeding = summarizeFeedingComparison(
                    feedingRepository.watchByBaby(babyId).first(),
                    now,
                    days,
                )
                val parts = mutableListOf(
                    feeding to "最近${days}天与前${days}天喂养",
                )
                if (preferences.useGrowthRecords) {
                    parts += summarizeGrowthComparison(
                        growthRepository.watchByBaby(babyId).first(),
                        now,
                        days,
                    ) to "最近${days}天与前${days}天生长"
                }
                parts.toContextResult(requiredSummary = feeding)
            }
            AiAnalysisSource.HEALTH -> listOf(
                summarizeHealthComparison(
                    healthRepository.watchByBaby(babyId).first(),
                    now,
                    days,
                ) to "最近${days}天与前${days}天健康记录",
            ).toContextResult()
            AiAnalysisSource.OVERVIEW -> buildList {
                if (preferences.useSleepRecords) {
                    add(
                        summarizeSleepComparison(
                            sleepRepository.watchByBaby(babyId).first(),
                            now,
                            days,
                        ) to "最近${days}天与前${days}天睡眠",
                    )
                }
                if (preferences.useFeedingRecords) {
                    add(
                        summarizeFeedingComparison(
                            feedingRepository.watchByBaby(babyId).first(),
                            now,
                            days,
                        ) to "最近${days}天与前${days}天喂养",
                    )
                }
                if (preferences.useDiaperRecords) {
                    add(
                        summarizeDiaperComparison(
                            diaperRepository.watchByBaby(babyId).first(),
                            now,
                            days,
                        ) to "最近${days}天与前${days}天尿布",
                    )
                }
                if (preferences.useGrowthRecords) {
                    add(
                        summarizeGrowthComparison(
                            growthRepository.watchByBaby(babyId).first(),
                            now,
                            days,
                        ) to "最近${days}天与前${days}天生长",
                    )
                }
                if (preferences.useHealthRecords) {
                    add(
                        summarizeHealthComparison(
                            healthRepository.watchByBaby(babyId).first(),
                            now,
                            days,
                        ) to "最近${days}天与前${days}天健康记录",
                    )
                }
            }.toContextResult(
                suffix = "\n跨类别解读要求：只描述记录中同步出现的变化和可能关联，" +
                    "不得声称因果关系；数据不足时必须明确说明。",
            )
        }
    }
}

internal fun isAnalysisSourceEnabled(
    source: AiAnalysisSource,
    preferences: AiAssistantPreferences,
): Boolean = preferences.useRecentRecords && when (source) {
    AiAnalysisSource.SLEEP -> preferences.useSleepRecords
    AiAnalysisSource.FEEDING -> preferences.useFeedingRecords
    AiAnalysisSource.HEALTH -> preferences.useHealthRecords
    AiAnalysisSource.OVERVIEW -> preferences.useSleepRecords ||
        preferences.useFeedingRecords ||
        preferences.useDiaperRecords ||
        preferences.useGrowthRecords ||
        preferences.useHealthRecords
}

internal fun isAiContextEnabled(
    category: AiQuestionCategory,
    preferences: AiAssistantPreferences,
): Boolean = preferences.useRecentRecords && when (category) {
    AiQuestionCategory.GENERAL -> false
    AiQuestionCategory.SLEEP -> preferences.useSleepRecords
    AiQuestionCategory.FEEDING -> preferences.useFeedingRecords
    AiQuestionCategory.DIAPER -> preferences.useDiaperRecords
    AiQuestionCategory.GROWTH -> preferences.useGrowthRecords
    AiQuestionCategory.HEALTH -> preferences.useHealthRecords
}

private fun List<Pair<String, String>>.toContextResult(
    requiredSummary: String? = null,
    suffix: String = "",
) = AiContextResult(
    prompt = joinToString("\n") { it.first } + suffix,
    references = map { it.second },
    hasRecords = requiredSummary?.let { !it.endsWith("：无记录") }
        ?: any { !it.first.endsWith("：无记录") },
)

internal fun classifyAiQuestion(question: String): AiQuestionCategory {
    val normalized = question.lowercase()
    return when {
        HEALTH_KEYWORDS.any(normalized::contains) -> AiQuestionCategory.HEALTH
        DIAPER_KEYWORDS.any(normalized::contains) -> AiQuestionCategory.DIAPER
        SLEEP_KEYWORDS.any(normalized::contains) -> AiQuestionCategory.SLEEP
        FEEDING_KEYWORDS.any(normalized::contains) -> AiQuestionCategory.FEEDING
        GROWTH_KEYWORDS.any(normalized::contains) -> AiQuestionCategory.GROWTH
        else -> AiQuestionCategory.GENERAL
    }
}

internal fun summarizeFeeding(records: List<Feeding>, now: LocalDateTime): String {
    val recent = records.filterSince(now.minusDays(3)) { it.timestamp }
    if (recent.isEmpty()) return "最近3天喂养：无记录"
    val breast = recent.filter { it.type == FeedingType.BREAST }
    val formula = recent.filter { it.type == FeedingType.FORMULA }
    val food = recent.filter { it.type == FeedingType.FOOD }
    val water = recent.filter { it.type == FeedingType.WATER }
    val details = buildList {
        if (breast.isNotEmpty()) add("母乳${breast.size}次、合计${breast.sumOf { it.durationMin ?: 0 }}分钟")
        if (formula.isNotEmpty()) add("配方奶${formula.size}次、合计${formula.sumOf { it.amountMl ?: 0 }}毫升")
        if (food.isNotEmpty()) add("辅食${food.size}次")
        if (water.isNotEmpty()) add("饮水${water.size}次、合计${water.sumOf { it.amountMl ?: 0 }}毫升")
    }
    return "最近3天喂养：共${recent.size}次；${details.joinToString("；")}"
}

internal fun summarizeSleep(records: List<Sleep>, now: LocalDateTime): String {
    val recent = records.filterSince(now.minusDays(7)) { it.startTime }
    if (recent.isEmpty()) return "最近7天睡眠：无记录"
    val totalMinutes = recent.sumOf { record ->
        val start = parseDateTime(record.startTime)
        val end = parseDateTime(record.endTime)
        if (start == null || end == null) 0L else Duration.between(start, end).toMinutes().coerceAtLeast(0)
    }
    return "最近7天睡眠：共${recent.size}次，合计${formatDuration(totalMinutes)}；" +
        "夜间${recent.count { it.type == SleepType.NIGHT }}次，小睡${recent.count { it.type == SleepType.NAP }}次"
}

internal fun summarizeDiaper(records: List<Diaper>, now: LocalDateTime): String {
    val recent = records.filterSince(now.minusDays(3)) { it.timestamp }
    if (recent.isEmpty()) return "最近3天尿布：无记录"
    val wet = recent.count { it.type == DiaperType.WET || it.type == DiaperType.BOTH }
    val poop = recent.count { it.type == DiaperType.POOP || it.type == DiaperType.BOTH }
    return "最近3天尿布：共${recent.size}次；尿湿${wet}次，排便${poop}次"
}

internal fun summarizeGrowth(records: List<Growth>, now: LocalDateTime): String {
    val recent = records
        .filterSince(now.minusDays(90)) { it.measuredAt }
        .sortedByDescending { it.measuredAt }
        .take(3)
    if (recent.isEmpty()) return "最近90天生长：无记录"
    val values = recent.joinToString("；") { record ->
        val label = when (record.type) {
            GrowthType.WEIGHT -> "体重"
            GrowthType.HEIGHT -> "身高"
            GrowthType.HEAD -> "头围"
        }
        val unit = if (record.type == GrowthType.WEIGHT) "千克" else "厘米"
        "${record.measuredAt.take(10)} $label${formatDecimal(record.value)}$unit"
    }
    return "最近90天生长：$values"
}

internal fun summarizeHealth(records: List<HealthRecord>, now: LocalDateTime): String {
    val recent = records
        .filterSince(now.minusDays(30)) { it.recordDate }
        .sortedByDescending { it.recordDate }
        .take(10)
    if (recent.isEmpty()) return "最近30天健康记录：无记录"
    val values = recent.joinToString("；") { record ->
        "${record.recordDate.take(10)} ${sanitizeRecordText(record.category)}：" +
            sanitizeRecordText(record.description)
    }
    return "最近30天健康记录：$values"
}

internal fun summarizeSleepComparison(
    records: List<Sleep>,
    now: LocalDateTime,
    days: Long,
): String {
    val (current, previous) = records.comparisonPeriods(now, days) { it.startTime }
    val title = "睡眠周期对比（最近${days}天 / 前${days}天）"
    if (current.isEmpty()) return "$title：无记录"
    return "$title：本期${sleepPeriodMetrics(current)}；前期${sleepPeriodMetrics(previous)}"
}

internal fun summarizeFeedingComparison(
    records: List<Feeding>,
    now: LocalDateTime,
    days: Long,
): String {
    val (current, previous) = records.comparisonPeriods(now, days) { it.timestamp }
    val title = "喂养周期对比（最近${days}天 / 前${days}天）"
    if (current.isEmpty()) return "$title：无记录"
    return "$title：本期${feedingPeriodMetrics(current)}；前期${feedingPeriodMetrics(previous)}"
}

internal fun summarizeDiaperComparison(
    records: List<Diaper>,
    now: LocalDateTime,
    days: Long,
): String {
    val (current, previous) = records.comparisonPeriods(now, days) { it.timestamp }
    val title = "尿布周期对比（最近${days}天 / 前${days}天）"
    if (current.isEmpty()) return "$title：无记录"
    return "$title：本期${diaperPeriodMetrics(current)}；前期${diaperPeriodMetrics(previous)}"
}

internal fun summarizeGrowthComparison(
    records: List<Growth>,
    now: LocalDateTime,
    days: Long,
): String {
    val (current, previous) = records.comparisonPeriods(now, days) { it.measuredAt }
    val title = "生长周期对比（最近${days}天 / 前${days}天）"
    if (current.isEmpty()) return "$title：无记录"
    return "$title：本期${growthPeriodMetrics(current)}；前期${growthPeriodMetrics(previous)}"
}

internal fun summarizeHealthComparison(
    records: List<HealthRecord>,
    now: LocalDateTime,
    days: Long,
): String {
    val (current, previous) = records.comparisonPeriods(now, days) { it.recordDate }
    val title = "健康周期对比（最近${days}天 / 前${days}天）"
    if (current.isEmpty()) return "$title：无记录"
    return "$title：本期${healthPeriodMetrics(current)}；前期${healthPeriodMetrics(previous)}"
}

private fun sleepPeriodMetrics(records: List<Sleep>): String {
    if (records.isEmpty()) return "无记录"
    val totalMinutes = records.sumOf { record ->
        val start = parseDateTime(record.startTime)
        val end = parseDateTime(record.endTime)
        if (start == null || end == null) 0L else {
            Duration.between(start, end).toMinutes().coerceAtLeast(0)
        }
    }
    return "共${records.size}次、合计${formatDuration(totalMinutes)}、" +
        "夜间${records.count { it.type == SleepType.NIGHT }}次、" +
        "小睡${records.count { it.type == SleepType.NAP }}次"
}

private fun feedingPeriodMetrics(records: List<Feeding>): String {
    if (records.isEmpty()) return "无记录"
    val breast = records.filter { it.type == FeedingType.BREAST }
    val formula = records.filter { it.type == FeedingType.FORMULA }
    val food = records.filter { it.type == FeedingType.FOOD }
    val water = records.filter { it.type == FeedingType.WATER }
    return buildList {
        add("共${records.size}次")
        if (breast.isNotEmpty()) {
            add("母乳${breast.size}次、${breast.sumOf { it.durationMin ?: 0 }}分钟")
        }
        if (formula.isNotEmpty()) {
            add("配方奶${formula.size}次、${formula.sumOf { it.amountMl ?: 0 }}毫升")
        }
        if (food.isNotEmpty()) add("辅食${food.size}次")
        if (water.isNotEmpty()) {
            add("饮水${water.size}次、${water.sumOf { it.amountMl ?: 0 }}毫升")
        }
    }.joinToString("、")
}

private fun diaperPeriodMetrics(records: List<Diaper>): String {
    if (records.isEmpty()) return "无记录"
    val wet = records.count { it.type == DiaperType.WET || it.type == DiaperType.BOTH }
    val poop = records.count { it.type == DiaperType.POOP || it.type == DiaperType.BOTH }
    return "共${records.size}次、尿湿${wet}次、排便${poop}次"
}

private fun growthPeriodMetrics(records: List<Growth>): String {
    if (records.isEmpty()) return "无记录"
    return records
        .sortedByDescending { it.measuredAt }
        .take(3)
        .joinToString("、") { record ->
            val label = when (record.type) {
                GrowthType.WEIGHT -> "体重"
                GrowthType.HEIGHT -> "身高"
                GrowthType.HEAD -> "头围"
            }
            val unit = if (record.type == GrowthType.WEIGHT) "千克" else "厘米"
            "${record.measuredAt.take(10)} $label${formatDecimal(record.value)}$unit"
        }
}

private fun healthPeriodMetrics(records: List<HealthRecord>): String {
    if (records.isEmpty()) return "无记录"
    val categories = records
        .groupingBy { sanitizeRecordText(it.category) }
        .eachCount()
        .entries
        .joinToString("、") { "${it.key}${it.value}条" }
    val details = records
        .sortedByDescending { it.recordDate }
        .take(5)
        .joinToString("、") {
            "${it.recordDate.take(10)} ${sanitizeRecordText(it.description)}"
        }
    return "共${records.size}条；分类：$categories；记录：$details"
}

private fun <T> List<T>.comparisonPeriods(
    now: LocalDateTime,
    days: Long,
    timestamp: (T) -> String,
): Pair<List<T>, List<T>> {
    val currentStart = now.minusDays(days)
    val previousStart = currentStart.minusDays(days)
    val current = filterBetween(currentStart, now.plusNanos(1), timestamp)
    val previous = filterBetween(previousStart, currentStart, timestamp)
    return current to previous
}

private fun <T> List<T>.filterBetween(
    startInclusive: LocalDateTime,
    endExclusive: LocalDateTime,
    timestamp: (T) -> String,
): List<T> = filter { item ->
    parseDateTime(timestamp(item))?.let { time ->
        !time.isBefore(startInclusive) && time.isBefore(endExclusive)
    } == true
}

private fun <T> List<T>.filterSince(
    start: LocalDateTime,
    timestamp: (T) -> String,
): List<T> = filter { item ->
    parseDateTime(timestamp(item))?.let { !it.isBefore(start) } == true
}

private fun parseDateTime(value: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME) }.getOrNull()

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val remainder = minutes % 60
    return when {
        hours == 0L -> "${remainder}分钟"
        remainder == 0L -> "${hours}小时"
        else -> "${hours}小时${remainder}分钟"
    }
}

private fun formatDecimal(value: Double): String =
    if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.ROOT, "%.1f", value)
    }

private fun sanitizeRecordText(value: String): String =
    value.replace(Regex("[\\r\\n]+"), " ").take(120)

private val HEALTH_KEYWORDS = listOf(
    "发热", "发烧", "体温", "咳嗽", "呕吐", "腹泻", "皮疹", "症状", "生病", "用药", "药物",
)
private val DIAPER_KEYWORDS = listOf(
    "尿布", "尿不湿", "排便", "大便", "便秘", "腹泻", "尿量", "小便", "拉粑粑",
)
private val SLEEP_KEYWORDS = listOf("睡眠", "睡觉", "睡前", "夜醒", "小睡", "作息", "哄睡", "入睡")
private val FEEDING_KEYWORDS = listOf("喂养", "母乳", "奶粉", "配方奶", "吃奶", "奶量", "辅食", "饮水")
private val GROWTH_KEYWORDS = listOf("体重", "身高", "头围", "生长", "增长趋势")
