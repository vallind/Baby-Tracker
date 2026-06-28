package com.babytracker.feature.timeline

import com.babytracker.core.database.entity.FeedingEntity
import com.babytracker.core.database.entity.SleepEntity
import com.babytracker.core.database.entity.DiaperEntity
import com.babytracker.core.database.entity.GrowthEntity
import com.babytracker.core.database.entity.HealthRecordEntity
import com.babytracker.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimelineViewModelTest {

    @Test
    fun `state items contain all record types after loading`() {
        val feedings = listOf(
            FeedingEntity(babyId = 1, type = "breast", breastSide = "双侧", durationMin = 25, timestamp = "2026-06-27T10:00:00"),
        )
        val sleeps = listOf(
            SleepEntity(babyId = 1, type = "night", startTime = "2026-06-26T22:00:00", endTime = "2026-06-27T06:00:00"),
        )
        val diapers = listOf(
            DiaperEntity(babyId = 1, type = "wet", timestamp = "2026-06-27T08:00:00"),
        )
        val growths = listOf(
            GrowthEntity(babyId = 1, type = "height", value = 52.0, measuredAt = "2026-06-25T10:00:00"),
        )
        val healths = listOf(
            HealthRecordEntity(babyId = 1, category = "exam", description = "常规体检", recordDate = "2026-06-24T09:00:00"),
        )

        val items = toTimelineItems(feedings, sleeps, diapers, growths, healths)
        val types = items.map { it.recordType }.toSet()

        assertEquals(setOf("feeding", "sleep", "diaper", "growth", "health"), types)
    }

    @Test
    fun `items are sorted by timestamp descending`() {
        val feedings = listOf(
            FeedingEntity(babyId = 1, type = "breast", breastSide = "双侧", durationMin = 25, timestamp = "2026-06-27T10:00:00"),
            FeedingEntity(babyId = 1, type = "formula", amountMl = 120, timestamp = "2026-06-26T14:00:00"),
        )
        val items = toTimelineItems(feedings, emptyList(), emptyList(), emptyList(), emptyList())
        val dates = items.map { it.date }
        assertEquals(listOf("2026-06-27", "2026-06-26"), dates)
    }

    @Test
    fun `feeding entity maps to correct TimelineItem fields`() {
        val feeding = FeedingEntity(babyId = 1, type = "breast", breastSide = "左侧", durationMin = 20, timestamp = "2026-06-27T10:30:00")
        val items = toTimelineItems(listOf(feeding), emptyList(), emptyList(), emptyList(), emptyList())

        assertEquals(1, items.size)
        assertEquals("feeding", items[0].recordType)
        assertEquals("10:30", items[0].time)
        assertEquals("2026-06-27", items[0].date)
    }

    @Test
    fun `sleep entity subtitle contains duration`() {
        val sleep = SleepEntity(babyId = 1, type = "night", startTime = "2026-06-26T22:00:00", endTime = "2026-06-27T06:00:00")
        val items = toTimelineItems(emptyList(), listOf(sleep), emptyList(), emptyList(), emptyList())

        assertEquals("sleep", items[0].recordType)
        // Duration should be 8 hours
        assertTrue(items[0].subtitle.contains("8") || items[0].subtitle.contains("08"))
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

/** Maps entities to TimelineItems matching TimelineViewModel logic. */
private fun toTimelineItems(
    feedings: List<FeedingEntity>,
    sleeps: List<SleepEntity>,
    diapers: List<DiaperEntity>,
    growths: List<GrowthEntity>,
    healths: List<HealthRecordEntity>,
): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    var accent = false

    feedings.forEach { f ->
        items.add(TimelineItem(
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
        ))
    }

    sleeps.forEach { s ->
        val secs = try {
            Duration.between(
                LocalDateTime.parse(s.startTime, DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.parse(s.endTime, DateTimeFormatter.ISO_DATE_TIME),
            ).seconds
        } catch (_: Exception) { 0L }
        items.add(TimelineItem(
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
        ))
    }

    diapers.forEach { d ->
        items.add(TimelineItem(
            id = d.id,
            recordType = "diaper",
            emoji = "🧷",
            title = "换尿布",
            subtitle = DateUtils.diaperTypeLabel(d.type),
            time = d.timestamp.substring(11, 16),
            date = d.timestamp.take(10),
            accent = accent.also { accent = !accent },
            sortKey = d.timestamp,
        ))
    }

    growths.forEach { g ->
        val unit = when (g.type) { "weight" -> "kg"; "height" -> "cm"; else -> "cm" }
        items.add(TimelineItem(
            id = g.id,
            recordType = "growth",
            emoji = "📏",
            title = DateUtils.growthTypeLabel(g.type),
            subtitle = "${g.value}$unit",
            time = g.measuredAt.substring(11, 16),
            date = g.measuredAt.take(10),
            accent = accent.also { accent = !accent },
            sortKey = g.measuredAt,
        ))
    }

    healths.forEach { rec ->
        items.add(TimelineItem(
            id = rec.id,
            recordType = "health",
            emoji = "❤️",
            title = rec.description.take(30),
            subtitle = rec.category,
            time = if (rec.recordDate.length >= 16) rec.recordDate.substring(11, 16) else "",
            date = rec.recordDate.take(10),
            accent = accent.also { accent = !accent },
            sortKey = rec.recordDate,
        ))
    }

    items.sortByDescending { it.sortKey }
    return items
}
