package com.babytracker.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun `relativeDate returns today for current date`() {
        val today = LocalDate.now().toString()
        assertEquals("今天", DateUtils.relativeDate(today))
    }

    @Test
    fun `relativeDate returns yesterday for yesterday date`() {
        val yesterday = LocalDate.now().minusDays(1).toString()
        assertEquals("昨天", DateUtils.relativeDate(yesterday))
    }

    @Test
    fun `relativeDate returns formatted date for older dates`() {
        val date = LocalDate.now().minusDays(5).toString()
        val result = DateUtils.relativeDate(date)
        assertTrue(result.matches(Regex("\\d+月\\d+日")))
    }

    @Test
    fun `relativeDate returns original string for invalid date`() {
        val bad = "not-a-date"
        assertEquals(bad, DateUtils.relativeDate(bad))
    }
}
