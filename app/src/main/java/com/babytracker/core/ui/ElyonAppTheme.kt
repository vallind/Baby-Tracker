package com.babytracker.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.AppComponentTokens
import com.babytracker.designsystem.theme.AppControlTokens
import com.babytracker.designsystem.theme.AppDensity
import com.babytracker.designsystem.theme.AppElevation
import com.babytracker.designsystem.theme.AppMotion
import com.babytracker.designsystem.theme.AppOpacity
import com.babytracker.designsystem.theme.AppShapes
import com.babytracker.designsystem.theme.AppSpacing
import com.babytracker.designsystem.theme.AppTypography
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppComponentTokens
import com.babytracker.designsystem.theme.LocalAppControl
import com.babytracker.designsystem.theme.LocalAppDensity
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppMotion
import com.babytracker.designsystem.theme.LocalAppOpacity
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.densityAdjusted
import com.babytracker.designsystem.theme.derive
import com.babytracker.designsystem.theme.scaled
import com.babytracker.designsystem.theme.tokens
import io.elyon.kmp.theme.ElyonTheme
import io.elyon.kmp.theme.ThemeController as ElyonThemeController

/**
 * Elyon 驱动的应用主题根。
 *
 * 过渡期同时把 Elyon 色板映射为旧 DS 组件消费的 CompositionLocal，
 * 保证屏幕层在逐个迁移到 Elyon 组件期间保持可编译可运行；
 * 旧 DS 组件全部迁移完成后删除本文件的兼容映射。
 */
@Composable
fun BabyTrackerElyonTheme(
    themeName: String,
    density: AppDensity = LocalAppDensity.current,
    content: @Composable () -> Unit,
) {
    val spec = remember(themeName) { ElyonThemeResolver.resolve(themeName) }
    val controller = remember(spec) {
        ElyonThemeController(colorSchemeMode = spec.mode, keyColor = spec.keyColor)
    }
    ElyonTheme(controller = controller) {
        val scheme = ElyonTheme.colorScheme
        val darkTheme = ElyonThemeResolver.isDark(themeName)
        val colors = remember(scheme, darkTheme) {
            AppColors.derive(
                primary = scheme.primary,
                onPrimary = scheme.onPrimary,
                primaryContainer = scheme.primaryContainer,
                secondary = scheme.secondary,
                onSecondary = scheme.onSecondary,
                tertiary = scheme.tertiaryContainer,
                surface = scheme.surface,
                onSurface = scheme.onSurface,
                background = scheme.background,
                onBackground = scheme.onBackground,
                error = scheme.error,
                onError = scheme.onError,
                outline = scheme.outline,
                scrim = scheme.windowDimming,
                success = if (darkTheme) Color(0xFF4ADE80) else Color(0xFF22C55E),
                warning = if (darkTheme) Color(0xFFFBBF24) else Color(0xFFF59E0B),
            )
        }
        val densityTokens = density.tokens
        val tokensSpacing = remember(densityTokens) { AppSpacing().scaled(densityTokens.spacingScale) }
        val tokensElevation = remember { AppElevation() }
        val tokensOpacity = remember { AppOpacity() }
        val tokensMotion = remember { AppMotion() }
        val tokensShapes = remember { AppShapes() }
        val tokensTypography = remember { AppTypography() }
        val tokensControl = remember(density) { AppControlTokens().densityAdjusted(density) }
        val componentTokens = remember(colors, tokensSpacing, density, darkTheme) {
            AppComponentTokens.default(
                colors = colors,
                spacing = tokensSpacing,
                shapes = tokensShapes,
                typography = tokensTypography,
                opacity = tokensOpacity,
                motion = tokensMotion,
                elevation = tokensElevation,
                control = tokensControl,
                darkTheme = darkTheme,
            )
        }
        CompositionLocalProvider(
            LocalAppColors provides colors,
            LocalAppDensity provides density,
            LocalAppSpacing provides tokensSpacing,
            LocalAppElevation provides tokensElevation,
            LocalAppOpacity provides tokensOpacity,
            LocalAppMotion provides tokensMotion,
            LocalAppShapes provides tokensShapes,
            LocalAppControl provides tokensControl,
            LocalAppComponentTokens provides componentTokens,
            LocalAppTypography provides tokensTypography,
        ) {
            content()
        }
    }
}
