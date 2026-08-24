package com.babytracker.feature.stats

import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 统计聚合纯逻辑测试（调用生产函数 aggregateStats，禁止镜像复制）。
 */
class StatsLogicTest {

    private val today = LocalDate.now()

    private fun at(day: LocalDate, hour: Int): String =
        LocalDateTime.of(day, java.time.LocalTime.of(hour, 0)).format(DateTimeFormatter.ISO_DATE_TIME)

    // ── 周期窗口 ──

    @Test
    fun `week period starts on sunday`() {
        val start = statsPeriodStart(StatsPeriod.WEEK, 0)
        assertEquals(0, start.dayOfWeek.value % 7)
        assertEquals(7, statsPeriodBucketCount(StatsPeriod.WEEK))
    }

    @Test
    fun `month starts at first day and day starts at midnight`() {
        assertEquals(today.withDayOfMonth(1).atStartOfDay(), statsPeriodStart(StatsPeriod.MONTH, 0))
        assertEquals(today.atStartOfDay(), statsPeriodStart(StatsPeriod.DAY, 0))
        assertEquals(today.plusDays(1), statsPeriodEnd(StatsPeriod.DAY, 0).toLocalDate())
    }

    @Test
    fun `negative offset moves to previous period`() {
        assertEquals(today.minusDays(1).atStartOfDay(), statsPeriodStart(StatsPeriod.DAY, -1))
    }

    @Test
    fun `year period starts at january first`() {
        assertEquals(today.withDayOfYear(1).atStartOfDay(), statsPeriodStart(StatsPeriod.YEAR, 0))
        assertEquals(12, statsPeriodBucketCount(StatsPeriod.YEAR))
    }

    // ── 睡眠 ──

    @Test
    fun `cross-midnight sleep counts full minutes`() {
        val sleep = Sleep(babyId = 1, type = SleepType.NIGHT, startTime = at(today.minusDays(1), 22), endTime = at(today, 6))
        assertEquals(480L, statsSleepDurationMinutes(sleep))
    }

    @Test
    fun `reversed sleep duration clamps to zero`() {
        val sleep = Sleep(babyId = 1, type = SleepType.NAP, startTime = at(today, 6), endTime = at(today, 4))
        assertEquals(0L, statsSleepDurationMinutes(sleep))
    }

    @Test
    fun `unparseable sleep timestamps produce zero minutes`() {
        val sleep = Sleep(babyId = 1, type = SleepType.NAP, startTime = "", endTime = "")
        val state = aggregateStats(StatsPeriod.WEEK, 0, emptyList(), listOf(sleep), emptyList())

        assertEquals(0L, state.sleepMinutes)
        assertEquals(7, state.sleepPoints.size)
    }

    // ── 喂养计数与分桶 ──

    @Test
    fun `feedingCount matches chartable records only`() {
        // 回归：1.7.10 之前解析失败的记录计入数字但柱状图不显示
        val good = Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 100, timestamp = at(today, 8))
        val bad = Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 100, timestamp = "")
        val state = aggregateStats(StatsPeriod.DAY, 0, listOf(good, bad), emptyList(), emptyList())

        assertEquals(1, state.feedingCount)
        assertEquals(24, state.feedingPoints.size)
        assertEquals(1f, state.feedingPoints[8], 0f)
    }

    @Test
    fun `week buckets place records on correct day`() {
        val start = statsPeriodStart(StatsPeriod.WEEK, 0)
        val day3 = start.toLocalDate().plusDays(3)
        val f = Feeding(babyId = 1, type = FeedingType.BREAST, durationMin = 10, timestamp = at(day3, 9))
        val state = aggregateStats(StatsPeriod.WEEK, 0, listOf(f), emptyList(), emptyList())

        assertEquals(7, state.feedingPoints.size)
        assertEquals(1f, state.feedingPoints[3], 0f)
        assertEquals(1, state.feedingCount)
    }

    @Test
    fun `feeding compare counts previous period`() {
        val prev = Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 90, timestamp = at(today.minusDays(1), 8))
        val state = aggregateStats(StatsPeriod.DAY, 0, listOf(prev), emptyList(), emptyList())

        assertEquals(0, state.feedingCount)
        assertEquals("-1次", state.feedingCompare)
    }

    @Test
    fun `compare text formats diff`() {
        assertEquals("+2次", statsBuildCompare(2L, "次"))
        assertEquals("-1时", statsBuildCompare(-1L, "时"))
        assertEquals("", statsBuildCompare(0L, "次"))
    }

    // ── 生长 ──

    @Test
    fun `latest growth takes last measured and ignores other types`() {
        val older = Growth(babyId = 1, type = GrowthType.HEIGHT, value = 50.0, measuredAt = at(today, 9))
        val newer = Growth(babyId = 1, type = GrowthType.HEIGHT, value = 52.0, measuredAt = at(today, 18))
        val weight = Growth(babyId = 1, type = GrowthType.WEIGHT, value = 4.0, measuredAt = at(today, 12))
        val state = aggregateStats(StatsPeriod.DAY, 0, emptyList(), emptyList(), listOf(older, newer, weight))

        assertEquals("52.0cm", state.height)
        assertEquals("4.0kg", state.weight)
        assertEquals(2, state.heightPoints.size)
    }

    @Test
    fun `no growth in range shows placeholder`() {
        val state = aggregateStats(StatsPeriod.DAY, 0, emptyList(), emptyList(), emptyList())

        assertEquals("--", state.height)
        assertEquals("--", state.weight)
        assertTrue(state.heightPoints.isEmpty())
    }

    // ── 空数据 ──

    @Test
    fun `empty data produces empty state`() {
        val state = aggregateStats(StatsPeriod.WEEK, 0, emptyList(), emptyList(), emptyList())

        assertEquals(0, state.feedingCount)
        assertEquals(0L, state.sleepMinutes)
        assertEquals("", state.feedingCompare)
        assertFalse(state.hasAnyData)
        assertEquals(7, state.feedingPoints.size)
        assertEquals(7, state.sleepPoints.size)
    }
}
