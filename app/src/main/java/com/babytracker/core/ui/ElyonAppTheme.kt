package com.babytracker.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.elyon.kmp.theme.ElyonTheme
import io.elyon.kmp.theme.ThemeController as ElyonThemeController
import org.koin.compose.koinInject

/**
 * Elyon 驱动的应用主题根。
 *
 * 主题参数只保留主题名，由 ElyonThemeResolver 映射到固定色板
 * （旧 6 套主题配色，不再走 Monet 动态色板）。
 *
 * 界面密度通过 LocalDensity 全局缩放 dp：density 乘 spacingScale，
 * fontScale 反向除 spacingScale，保证文字 sp 实际像素不变、间距/控件随档位缩放。
 */
@Composable
fun BabyTrackerElyonTheme(
    themeName: String,
    content: @Composable () -> Unit,
) {
    val densityCtrl: DensityController = koinInject()
    val spec = remember(themeName) { ElyonThemeResolver.resolve(themeName) }
    val controller = remember(spec) {
        ElyonThemeController(
            colorSchemeMode = spec.mode,
            lightColors = spec.lightColors,
            darkColors = spec.darkColors,
        )
    }
    val baseDensity = LocalDensity.current
    val scale = densityCtrl.currentDensity.tokens.spacingScale
    val scaledDensity = remember(baseDensity, scale) {
        Density(
            density = baseDensity.density * scale,
            fontScale = baseDensity.fontScale / scale,
        )
    }
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        ElyonTheme(controller = controller) {
            content()
        }
    }
}
