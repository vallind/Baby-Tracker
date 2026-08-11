package com.babytracker.core.ui

import androidx.compose.ui.graphics.Color
import io.elyon.kmp.theme.ColorSchemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 旧 6 套主题 → Elyon ThemeController 参数的映射契约。
 * 保持原行为：只有 night 强制暗色，其余主题固定亮色。
 * 固定色板替代 Monet：亮色主题用 lightColors，night 用 darkColors。
 */
class ElyonThemeResolverTest {

    @Test
    fun `pure 映射为亮色且主色为品牌蓝`() {
        val spec = ElyonThemeResolver.resolve("pure")

        assertEquals(ColorSchemeMode.Light, spec.mode)
        assertEquals(Color(0xFF4285F4), spec.lightColors.primary)
    }

    @Test
    fun `aurora 映射为亮色且主色为极光紫`() {
        val spec = ElyonThemeResolver.resolve("aurora")

        assertEquals(ColorSchemeMode.Light, spec.mode)
        assertEquals(Color(0xFF7C6CF0), spec.lightColors.primary)
    }

    @Test
    fun `warm 映射为亮色且主色为暖阳粉`() {
        val spec = ElyonThemeResolver.resolve("warm")

        assertEquals(ColorSchemeMode.Light, spec.mode)
        assertEquals(Color(0xFFFF8A80), spec.lightColors.primary)
    }

    @Test
    fun `sunny 映射为亮色且主色为阳光黄`() {
        val spec = ElyonThemeResolver.resolve("sunny")

        assertEquals(ColorSchemeMode.Light, spec.mode)
        assertEquals(Color(0xFFF5A623), spec.lightColors.primary)
    }

    @Test
    fun `night 映射为强制暗色且主色为靛蓝`() {
        val spec = ElyonThemeResolver.resolve("night")

        assertEquals(ColorSchemeMode.Dark, spec.mode)
        assertEquals(Color(0xFF5C6BC0), spec.darkColors.primary)
    }

    @Test
    fun `morandi 映射为亮色且主色为莫兰迪灰`() {
        val spec = ElyonThemeResolver.resolve("morandi")

        assertEquals(ColorSchemeMode.Light, spec.mode)
        assertEquals(Color(0xFFB0BEC5), spec.lightColors.primary)
    }

    @Test
    fun `未知主题兜底为 pure`() {
        assertEquals(
            ElyonThemeResolver.resolve("pure"),
            ElyonThemeResolver.resolve("unknown-theme"),
        )
    }

    @Test
    fun `只有 night 判定为暗色主题`() {
        assertTrue(ElyonThemeResolver.isDark("night"))
        assertFalse(ElyonThemeResolver.isDark("pure"))
        assertFalse(ElyonThemeResolver.isDark("aurora"))
    }
}
