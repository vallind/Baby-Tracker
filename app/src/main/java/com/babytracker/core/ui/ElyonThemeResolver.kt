package com.babytracker.core.ui

import androidx.compose.ui.graphics.Color
import io.elyon.kmp.theme.ColorSchemeMode

/**
 * 旧 6 套主题 → Elyon ThemeController 参数的纯映射。
 *
 * 保持原行为：只有 night 强制暗色，其余主题固定亮色；
 * 暗色判定只读主题名，禁止读系统暗色（AGENTS 红线）。
 */
data class ElyonThemeSpec(
    val mode: ColorSchemeMode,
    val keyColor: Color,
)

object ElyonThemeResolver {
    private const val DEFAULT_THEME = "pure"

    private val specs: Map<String, ElyonThemeSpec> = mapOf(
        "pure" to ElyonThemeSpec(ColorSchemeMode.MonetLight, Color(0xFF4285F4)),
        "aurora" to ElyonThemeSpec(ColorSchemeMode.MonetLight, Color(0xFF7C6CF0)),
        "warm" to ElyonThemeSpec(ColorSchemeMode.MonetLight, Color(0xFFFF8A80)),
        "sunny" to ElyonThemeSpec(ColorSchemeMode.MonetLight, Color(0xFFF5A623)),
        "night" to ElyonThemeSpec(ColorSchemeMode.MonetDark, Color(0xFF5C6BC0)),
        "morandi" to ElyonThemeSpec(ColorSchemeMode.MonetLight, Color(0xFFB0BEC5)),
    )

    fun resolve(themeName: String): ElyonThemeSpec =
        specs[themeName] ?: specs.getValue(DEFAULT_THEME)

    fun isDark(themeName: String): Boolean = themeName == "night"
}
