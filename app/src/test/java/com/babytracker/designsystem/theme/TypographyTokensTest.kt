package com.babytracker.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTokensTest {

    @Test
    fun `AppTypography 应包含全部 15 个字段`() {
        val t = AppTypography()
        val fields = listOf(
            t.displayLarge, t.display, t.headlineLarge, t.headline, t.headlineMedium,
            t.headlineSmall, t.titleLarge, t.titleMedium, t.titleSmall,
            t.bodyLarge, t.bodyMedium, t.bodySmall,
            t.label, t.labelMedium, t.labelSmall,
        )
        assertEquals(15, fields.size)
    }

    @Test
    fun `新增层级 fontSize 应有序且无越界`() {
        val t = AppTypography()
        val ordered = listOf(
            t.displayLarge.fontSize.value, t.display.fontSize.value,
            t.headlineLarge.fontSize.value, t.headline.fontSize.value,
            t.headlineMedium.fontSize.value, t.headlineSmall.fontSize.value,
            t.titleLarge.fontSize.value, t.titleMedium.fontSize.value,
            t.bodyLarge.fontSize.value, t.titleSmall.fontSize.value,
            t.bodyMedium.fontSize.value, t.bodySmall.fontSize.value,
            t.label.fontSize.value, t.labelMedium.fontSize.value,
            t.labelSmall.fontSize.value,
        )
        // M3 允许不同层级同字号（bodySmall 与 labelMedium 均为 12sp），只约束单调不增与两端极值
        assertTrue(ordered.zipWithNext().all { (a, b) -> a >= b })
        assertEquals(40f, ordered.first())
        assertEquals(11f, ordered.last())
    }
}
