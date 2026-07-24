package com.babytracker.feature.ai

import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiContextBuilderTest {
    private val now = LocalDateTime.parse("2026-07-23T12:00:00")

    @Test
    fun `问题分类只选择必要记录`() {
        assertEquals(AiQuestionCategory.SLEEP, classifyAiQuestion("宝宝最近总是夜醒怎么办"))
        assertEquals(AiQuestionCategory.SLEEP, classifyAiQuestion("如何建立规律的睡前流程？"))
        assertEquals(AiQuestionCategory.FEEDING, classifyAiQuestion("最近奶量有没有变化"))
        assertEquals(AiQuestionCategory.DIAPER, classifyAiQuestion("这几天排便正常吗"))
        assertEquals(AiQuestionCategory.GROWTH, classifyAiQuestion("看看体重增长趋势"))
        assertEquals(AiQuestionCategory.HEALTH, classifyAiQuestion("宝宝发烧了"))
        assertEquals(AiQuestionCategory.GENERAL, classifyAiQuestion("这个月龄要注意什么"))
    }

    @Test
    fun `近期记录总开关和分类开关共同生效`() {
        assertTrue(
            !isAiContextEnabled(
                AiQuestionCategory.SLEEP,
                AiAssistantPreferences(useRecentRecords = false),
            ),
        )
        assertTrue(
            !isAiContextEnabled(
                AiQuestionCategory.HEALTH,
                AiAssistantPreferences(useHealthRecords = false),
            ),
        )
        assertTrue(
            isAiContextEnabled(
                AiQuestionCategory.FEEDING,
                AiAssistantPreferences(),
            ),
        )
    }

    @Test
    fun `快捷分析遵循总开关和对应分类开关`() {
        assertTrue(
            !isAnalysisSourceEnabled(
                AiAnalysisSource.SLEEP,
                AiAssistantPreferences(useRecentRecords = false),
            ),
        )
        assertTrue(
            !isAnalysisSourceEnabled(
                AiAnalysisSource.HEALTH,
                AiAssistantPreferences(useHealthRecords = false),
            ),
        )
        assertTrue(
            isAnalysisSourceEnabled(
                AiAnalysisSource.OVERVIEW,
                AiAssistantPreferences(
                    useSleepRecords = false,
                    useFeedingRecords = true,
                    useDiaperRecords = false,
                    useGrowthRecords = false,
                    useHealthRecords = false,
                ),
            ),
        )
    }

    @Test
    fun `快捷分析提供可编辑的建议问题`() {
        assertEquals(
            "对比最近7天与前7天的睡眠变化",
            analysisSuggestedQuestion(AiAnalysisSource.SLEEP, AiAnalysisPeriod.DAYS_7),
        )
        assertEquals(
            "对比最近14天与前14天的喂养变化",
            analysisSuggestedQuestion(AiAnalysisSource.FEEDING, AiAnalysisPeriod.DAYS_14),
        )
        assertEquals(
            "对比最近30天与前30天的健康记录",
            analysisSuggestedQuestion(AiAnalysisSource.HEALTH, AiAnalysisPeriod.DAYS_30),
        )
        assertEquals(
            "综合对比宝宝最近7天的多类记录变化",
            analysisSuggestedQuestion(AiAnalysisSource.OVERVIEW, AiAnalysisPeriod.DAYS_7),
        )
    }

    @Test
    fun `喂养摘要排除时间范围外记录`() {
        val records = listOf(
            Feeding(
                babyId = 1,
                type = FeedingType.FORMULA,
                amountMl = 120,
                timestamp = "2026-07-23T08:00:00",
            ),
            Feeding(
                babyId = 1,
                type = FeedingType.FORMULA,
                amountMl = 90,
                timestamp = "2026-07-19T08:00:00",
            ),
        )

        assertEquals("最近3天喂养：共1次；配方奶1次、合计120毫升", summarizeFeeding(records, now))
    }

    @Test
    fun `睡眠和尿布摘要正确聚合`() {
        val sleep = summarizeSleep(
            listOf(
                Sleep(
                    babyId = 1,
                    type = SleepType.NIGHT,
                    startTime = "2026-07-22T22:00:00",
                    endTime = "2026-07-23T04:30:00",
                ),
            ),
            now,
        )
        val diaper = summarizeDiaper(
            listOf(
                Diaper(babyId = 1, type = DiaperType.WET, timestamp = "2026-07-23T08:00:00"),
                Diaper(babyId = 1, type = DiaperType.BOTH, timestamp = "2026-07-23T09:00:00"),
            ),
            now,
        )

        assertTrue(sleep.contains("6小时30分钟"))
        assertEquals("最近3天尿布：共2次；尿湿2次，排便1次", diaper)
    }

    @Test
    fun `生长摘要最多保留最近三条`() {
        val records = (1..4).map { day ->
            Growth(
                babyId = 1,
                type = GrowthType.WEIGHT,
                value = 4.0 + day,
                measuredAt = "2026-07-${18 + day}T08:00:00",
            )
        }

        val summary = summarizeGrowth(records, now)

        assertTrue(summary.contains("2026-07-22"))
        assertTrue(!summary.contains("2026-07-19"))
    }

    @Test
    fun `睡眠周期对比严格拆分本期和前期`() {
        val summary = summarizeSleepComparison(
            records = listOf(
                Sleep(
                    babyId = 1,
                    type = SleepType.NIGHT,
                    startTime = "2026-07-22T22:00:00",
                    endTime = "2026-07-23T04:00:00",
                ),
                Sleep(
                    babyId = 1,
                    type = SleepType.NAP,
                    startTime = "2026-07-15T10:00:00",
                    endTime = "2026-07-15T11:00:00",
                ),
                Sleep(
                    babyId = 1,
                    type = SleepType.NAP,
                    startTime = "2026-07-01T10:00:00",
                    endTime = "2026-07-01T13:00:00",
                ),
            ),
            now = now,
            days = 7,
        )

        assertTrue(summary.contains("本期共1次、合计6小时"))
        assertTrue(summary.contains("前期共1次、合计1小时"))
        assertTrue(!summary.contains("3小时"))
    }

    @Test
    fun `周期对比仅以前期记录不能激活分析`() {
        val summary = summarizeFeedingComparison(
            records = listOf(
                Feeding(
                    babyId = 1,
                    type = FeedingType.FORMULA,
                    amountMl = 120,
                    timestamp = "2026-07-12T08:00:00",
                ),
            ),
            now = now,
            days = 7,
        )

        assertTrue(summary.endsWith("：无记录"))
    }

    @Test
    fun `健康周期对比保留两期分类和记录`() {
        val summary = summarizeHealthComparison(
            records = listOf(
                HealthRecord(
                    babyId = 1,
                    category = "症状",
                    description = "轻微咳嗽",
                    recordDate = "2026-07-22T08:00:00",
                ),
                HealthRecord(
                    babyId = 1,
                    category = "体温",
                    description = "体温正常",
                    recordDate = "2026-07-12T08:00:00",
                ),
            ),
            now = now,
            days = 7,
        )

        assertTrue(summary.contains("本期共1条"))
        assertTrue(summary.contains("轻微咳嗽"))
        assertTrue(summary.contains("前期共1条"))
        assertTrue(summary.contains("体温正常"))
    }
}
