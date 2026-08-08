package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 暗色 / 状态令牌审计 — 守护 AppComponentTokens 对暗色模式与状态色的响应。
 *
 * 测试模型与 AppColors.derive 一致：暗色模式只改 surface（由此推断 isDark），
 * 品牌主色 primary 两模式保持一致。
 */
class ComponentTokensStateAuditTest {

    private fun tokens(darkTheme: Boolean): AppComponentTokens {
        val colors = AppColors.derive(
            primary = Color(0xFF4A7DFF),
            surface = if (darkTheme) Color(0xFF18181B) else Color(0xFFFFFFFF),
            onSurface = Color(0xFF333333),
        )
        return AppComponentTokens.default(
            colors = colors,
            spacing = AppSpacing(),
            shapes = AppShapes(),
            typography = AppTypography(),
            opacity = AppOpacity(),
            motion = AppMotion(),
            elevation = AppElevation(),
            control = AppControlTokens(),
            darkTheme = darkTheme,
        )
    }

    @Test
    fun `暗色与亮色下按钮禁用容器色应不同且主容器色恒定`() {
        val light = tokens(darkTheme = false)
        val dark = tokens(darkTheme = true)
        // 禁用容器色派生自 surface，暗色下应随 surface 变暗
        assertNotEquals(light.button.disabledContainerColor, dark.button.disabledContainerColor)
        // 主容器色跟随品牌主色 primary，两模式 primary 不变，故应保持一致
        assertEquals(light.button.containerColor, dark.button.containerColor)
    }

    @Test
    fun `暗色与亮色下对话框与输入框容器色应不同`() {
        val light = tokens(darkTheme = false)
        val dark = tokens(darkTheme = true)
        assertNotEquals(light.dialog.containerColor, dark.dialog.containerColor)
        assertNotEquals(light.input.containerColor, dark.input.containerColor)
    }

    @Test
    fun `含状态色字段的令牌组必须派生齐全`() {
        val t = tokens(darkTheme = false)
        // 按钮：禁用态双色；输入框：三个状态边框色（InputTokens 无 disabled 字段）；
        // 选择控件：禁用色
        val stateColors = listOf(
            t.button.disabledContainerColor,
            t.button.disabledContentColor,
            t.input.unfocusedBorderColor,
            t.input.focusedBorderColor,
            t.input.errorBorderColor,
            t.selectionControl.disabledColor,
        )
        assertTrue(
            "状态色不得为 Unspecified: $stateColors",
            stateColors.all { it != Color.Unspecified },
        )
    }
}
