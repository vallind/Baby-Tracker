package com.babytracker.core.ui

import io.elyon.kmp.theme.ColorSchemeMode
import io.elyon.kmp.theme.Colors
import io.elyon.kmp.theme.darkColorScheme
import io.elyon.kmp.theme.lightColorScheme

/**
 * 旧 6 套主题 → Elyon ThemeController 参数的纯映射。
 *
 * 固定色板替代 Monet 动态色板，恢复旧主题配色；只有 night 强制暗色；
 * 暗色判定只读主题名，禁止读系统暗色（AGENTS 红线）。
 */
data class ElyonThemeSpec(
    val mode: ColorSchemeMode,
    val lightColors: Colors = lightColorScheme(),
    val darkColors: Colors = darkColorScheme(),
)

object ElyonThemeResolver {
    private const val DEFAULT_THEME = "pure"

    private val specs: Map<String, ElyonThemeSpec> = mapOf(
        "pure" to ElyonThemeSpec(ColorSchemeMode.Light, lightColors = BabyTrackerPalettes.pure),
        "aurora" to ElyonThemeSpec(ColorSchemeMode.Light, lightColors = BabyTrackerPalettes.aurora),
        "warm" to ElyonThemeSpec(ColorSchemeMode.Light, lightColors = BabyTrackerPalettes.warm),
        "sunny" to ElyonThemeSpec(ColorSchemeMode.Light, lightColors = BabyTrackerPalettes.sunny),
        "night" to ElyonThemeSpec(ColorSchemeMode.Dark, darkColors = BabyTrackerPalettes.night),
        "morandi" to ElyonThemeSpec(ColorSchemeMode.Light, lightColors = BabyTrackerPalettes.morandi),
    )

    fun resolve(themeName: String): ElyonThemeSpec =
        specs[themeName] ?: specs.getValue(DEFAULT_THEME)

    fun isDark(themeName: String): Boolean = themeName == "night"
}
