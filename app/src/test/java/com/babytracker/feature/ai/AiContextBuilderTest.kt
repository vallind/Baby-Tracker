package com.babytracker.feature.ai

import com.babytracker.core.ai.settings.AiAssistantPreferences
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
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
        assertEquals("帮我分析最近的睡眠情况", analysisSuggestedQuestion(AiAnalysisSource.SLEEP))
        assertEquals("最近喂养记录有什么变化", analysisSuggestedQuestion(AiAnalysisSource.FEEDING))
        assertEquals("帮我整理近期健康情况", analysisSuggestedQuestion(AiAnalysisSource.HEALTH))
        assertEquals("综合解读宝宝的近期记录", analysisSuggestedQuestion(AiAnalysisSource.OVERVIEW))
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
}
