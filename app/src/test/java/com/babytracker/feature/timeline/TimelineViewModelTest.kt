package com.babytracker.feature.timeline

import com.babytracker.core.domain.model.BreastSide
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 直接测试生产代码 toTimelineItems（TimelineViewModel.kt 顶层函数），禁止镜像复制。
 */
class TimelineViewModelTest {

    @Test
    fun `state items contain all record types after loading`() {
        val feedings = listOf(
            Feeding(babyId = 1, type = FeedingType.BREAST, breastSide = BreastSide.BOTH, durationMin = 25, timestamp = "2026-06-27T10:00:00"),
        )
        val sleeps = listOf(
            Sleep(babyId = 1, type = SleepType.NIGHT, startTime = "2026-06-26T22:00:00", endTime = "2026-06-27T06:00:00"),
        )
        val diapers = listOf(
            Diaper(babyId = 1, type = DiaperType.WET, timestamp = "2026-06-27T08:00:00"),
        )
        val growths = listOf(
            Growth(babyId = 1, type = GrowthType.HEIGHT, value = 52.0, measuredAt = "2026-06-25T10:00:00"),
        )
        val healths = listOf(
            HealthRecord(babyId = 1, category = "exam", description = "常规体检", recordDate = "2026-06-24T09:00:00"),
        )

        val items = toTimelineItems(feedings, sleeps, diapers, growths, healths)
        val types = items.map { it.recordType }.toSet()

        assertEquals(setOf("feeding", "sleep", "diaper", "growth", "health"), types)
    }

    @Test
    fun `items are sorted by timestamp descending`() {
        val feedings = listOf(
            Feeding(babyId = 1, type = FeedingType.BREAST, breastSide = BreastSide.BOTH, durationMin = 25, timestamp = "2026-06-27T10:00:00"),
            Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 120, timestamp = "2026-06-26T14:00:00"),
        )
        val items = toTimelineItems(feedings, emptyList(), emptyList(), emptyList(), emptyList())
        val dates = items.map { it.date }
        assertEquals(listOf("2026-06-27", "2026-06-26"), dates)
    }

    @Test
    fun `feeding entity maps to correct TimelineItem fields`() {
        val feeding = Feeding(babyId = 1, type = FeedingType.BREAST, breastSide = BreastSide.LEFT, durationMin = 20, timestamp = "2026-06-27T10:30:00")
        val items = toTimelineItems(listOf(feeding), emptyList(), emptyList(), emptyList(), emptyList())

        assertEquals(1, items.size)
        assertEquals("feeding", items[0].recordType)
        assertEquals("10:30", items[0].time)
        assertEquals("2026-06-27", items[0].date)
    }

    @Test
    fun `sleep entity subtitle contains duration`() {
        val sleep = Sleep(babyId = 1, type = SleepType.NIGHT, startTime = "2026-06-26T22:00:00", endTime = "2026-06-27T06:00:00")
        val items = toTimelineItems(emptyList(), listOf(sleep), emptyList(), emptyList(), emptyList())

        assertEquals("sleep", items[0].recordType)
        // Duration should be 8 hours
        assertTrue(items[0].subtitle.contains("8") || items[0].subtitle.contains("08"))
    }

    @Test
    fun `short or empty timestamps do not crash and produce empty time`() {
        // 同步/还原数据可能带短时间戳（回归：substring(11,16) 越界崩溃）
        val feeding = Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 100, timestamp = "2026-06-27")
        val sleep = Sleep(babyId = 1, type = SleepType.NAP, startTime = "", endTime = "")
        val health = HealthRecord(babyId = 1, category = "exam", description = "体检", recordDate = "2026-06-27T10:00:00")
        val growth = Growth(babyId = 1, type = GrowthType.HEIGHT, value = 50.0, measuredAt = "")

        val items = toTimelineItems(listOf(feeding), listOf(sleep), emptyList(), listOf(growth), listOf(health))

        assertEquals("", items.first { it.recordType == "feeding" }.time)
        assertEquals("-", items.first { it.recordType == "sleep" }.subtitle)
        assertEquals("", items.first { it.recordType == "growth" }.time)
        assertEquals("10:00", items.first { it.recordType == "health" }.time)
    }

    @Test
    fun `relativeDate for today returns today`() {
        val today = java.time.LocalDate.now().toString()
        assertEquals("今天", DateUtils.relativeDate(today))
    }

    @Test
    fun `relativeDate for yesterday returns yesterday`() {
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        assertEquals("昨天", DateUtils.relativeDate(yesterday))
    }
}
