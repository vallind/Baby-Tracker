package com.babytracker.designsystem.components.pagination

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AppPagination 页码折叠序列的纯逻辑测试。
 * 序列约定：首页/末页常驻，数字窗宽 = visibleCount-2 且 ≥3，空隙以 null（省略号）占位。
 */
class PageSequenceTest {

    @Test
    fun `short page count renders all pages without ellipsis`() {
        assertEquals(listOf(1, 2, 3, 4, 5), pageSequence(5, 1, 5))
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), pageSequence(7, 7, 5))
    }

    @Test
    fun `head page keeps leading window and trailing ellipsis`() {
        assertEquals(listOf(1, 2, 3, 4, null, 12), pageSequence(12, 1, 5))
    }

    @Test
    fun `second page has no leading ellipsis`() {
        assertEquals(listOf(1, 2, 3, 4, null, 12), pageSequence(12, 2, 5))
    }

    @Test
    fun `middle page folds both sides`() {
        assertEquals(listOf(1, null, 5, 6, 7, null, 12), pageSequence(12, 6, 5))
    }

    @Test
    fun `tail page shifts window against the end`() {
        assertEquals(listOf(1, null, 9, 10, 11, 12), pageSequence(12, 12, 5))
        assertEquals(listOf(1, null, 9, 10, 11, 12), pageSequence(12, 11, 5))
    }

    @Test
    fun `current is clamped into range`() {
        val seq = pageSequence(10, 99, 5)
        assertEquals(false, seq.contains(99))
        assertEquals(10, seq.filterNotNull().last())
    }
}
