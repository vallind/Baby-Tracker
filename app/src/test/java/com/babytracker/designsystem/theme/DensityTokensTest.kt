package com.babytracker.designsystem.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DensityTokensTest {

    @Test
    fun `AppSpacing scaled 应按系数缩放且 1 倍不变`() {
        val base = AppSpacing()
        assertEquals(0.dp, base.scaled(0.85f).none)
        assertEquals(base.xs.value * 0.85f, base.scaled(0.85f).xs.value, 0.01f)
        assertEquals(base.md, base.scaled(1.0f).md)
    }

    @Test
    fun `AppDensity 三档数值应唯一且有序`() {
        val compact = AppDensity.Compact.tokens
        val comfortable = AppDensity.Comfortable.tokens
        val large = AppDensity.Large.tokens
        assertTrue(compact.spacingScale < comfortable.spacingScale)
        assertTrue(comfortable.spacingScale < large.spacingScale)
        assertTrue(compact.controlHeightDelta < comfortable.controlHeightDelta)
        assertTrue(comfortable.controlHeightDelta < large.controlHeightDelta)
    }

    @Test
    fun `fromKey 应解析已知键并回退 Comfortable`() {
        assertEquals(AppDensity.Compact, AppDensity.fromKey("compact"))
        assertEquals(AppDensity.Comfortable, AppDensity.fromKey("comfortable"))
        assertEquals(AppDensity.Large, AppDensity.fromKey("large"))
        assertEquals(AppDensity.Comfortable, AppDensity.fromKey("unknown-key"))
    }

    @Test
    fun `densityAdjusted 应只调整 medium 高度`() {
        val base = AppControlTokens()
        assertEquals(base.medium.height - 8.dp, base.densityAdjusted(AppDensity.Compact).medium.height)
        assertEquals(base.medium.height, base.densityAdjusted(AppDensity.Comfortable).medium.height)
        assertEquals(base.medium.height + 8.dp, base.densityAdjusted(AppDensity.Large).medium.height)
        assertEquals(base.small.height, base.densityAdjusted(AppDensity.Compact).small.height)
    }
}
