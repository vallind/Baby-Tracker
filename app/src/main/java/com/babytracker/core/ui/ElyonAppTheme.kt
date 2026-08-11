package com.babytracker.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.elyon.kmp.theme.ElyonTheme
import io.elyon.kmp.theme.ThemeController as ElyonThemeController

/**
 * Elyon 驱动的应用主题根。
 *
 * 主题参数只保留主题名，由 ElyonThemeResolver 映射到固定色板
 * （旧 6 套主题配色，不再走 Monet 动态色板）。界面密度暂不参与缩放（TODO：
 * 后续在 Elyon 排版/间距体系上重新实现密度档位）。
 */
@Composable
fun BabyTrackerElyonTheme(
    themeName: String,
    content: @Composable () -> Unit,
) {
    val spec = remember(themeName) { ElyonThemeResolver.resolve(themeName) }
    val controller = remember(spec) {
        ElyonThemeController(
            colorSchemeMode = spec.mode,
            lightColors = spec.lightColors,
            darkColors = spec.darkColors,
        )
    }
    ElyonTheme(controller = controller) {
        content()
    }
}
