package com.babytracker.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════
//  AppTheme — 6 套主题定义
//  每个主题直接持有 AppColors，由 AppColors.derive() 派生
// ═══════════════════════════════════════════════════════════

data class AppTheme(
    val name: String,
    val colors: AppColors,
) {
    companion object {
        // 每个主题只需传入与默认值不同的种子，其余由 AppColors.derive() 自动派生
        val pure = AppTheme("pure", AppColors.derive(
            primary = Color(0xFF4285F4),
        ))

        val aurora = AppTheme("aurora", AppColors.derive(
            primary = Color(0xFF7C6CF0),
            background = Color(0xFFF8F5FF),
            warning = Color(0xFFFCD34D),
        ))

        val warm = AppTheme("warm", AppColors.derive(
            primary = Color(0xFFFF8A80),
            background = Color(0xFFFFFBF7),
        ))

        val sunny = AppTheme("sunny", AppColors.derive(
            primary = Color(0xFFF5A623),
            background = Color(0xFFFFFAF0),
            warning = Color(0xFFE67A2E),
        ))

        val night = AppTheme("night", AppColors.derive(
            primary = Color(0xFF5C6BC0),
            surface = Color(0xFF18181B),
            onSurface = Color(0xFFFAFAFA),
            background = Color(0xFF12121F),
            border = Color(0xFF2A2A3E),
            warning = Color(0xFFFFA500),
            error = Color(0xFFE57373),
            success = Color(0xFF4DB6AC),
            secondary = Color(0xFFA78BFA),
            tertiary = Color(0xFF4DD0E1),
            primaryContainer = Color(0xFF1E3A5F),
        ))

        val morandi = AppTheme("morandi", AppColors.derive(
            primary = Color(0xFFB0BEC5),
            background = Color(0xFFFAFAFA),
            warning = Color(0xFFD0A878),
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 声明
//  — LocalThemeColors：已废弃，保留以兼容旧引用（将移除）
//  — LocalApp*：令牌体系（对标 Palette 的 11+ 个 Local）
// ═══════════════════════════════════════════════════════════

@Deprecated("Use LocalAppColors instead", replaceWith = ReplaceWith("LocalAppColors"))
val LocalThemeColors = compositionLocalOf { AppTheme.pure.colors }

val LocalAppTypography = compositionLocalOf { BabyTrackerTypography }

val BabyTrackerTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

/**
 * 从 AppShapes 派生的 M3 Shapes，使 M3 原生组件圆角跟随 radiusScale。
 */
fun appShapesToM3Shapes(shapes: AppShapes): Shapes = Shapes(
    extraSmall = RoundedCornerShape(shapes.scaled(shapes.medium)),
    small = RoundedCornerShape(shapes.scaled(shapes.large)),
    medium = RoundedCornerShape(shapes.scaled(shapes.large * 2)),
    large = RoundedCornerShape(shapes.scaled(shapes.large * 3)),
    extraLarge = RoundedCornerShape(shapes.scaled(shapes.large * 4)),
)

fun AppTheme.toColorScheme(isDark: Boolean = false): androidx.compose.material3.ColorScheme {
    val c = colors
    val tagBg = c.warning.copy(alpha = 0.15f)
    return if (isDark) darkColorScheme(
        primary = c.primary,
        onPrimary = c.surface,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primary,
        secondary = c.secondary,
        onSecondary = c.surface,
        secondaryContainer = c.secondary.copy(alpha = 0.12f),
        tertiary = c.tertiary,
        tertiaryContainer = c.tertiary.copy(alpha = 0.12f),
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.outline.copy(alpha = 0.5f),
        error = c.error,
        onError = c.surface,
        errorContainer = tagBg,
        scrim = c.scrim,
    ) else lightColorScheme(
        primary = c.primary,
        onPrimary = c.surface,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primary,
        secondary = c.secondary,
        onSecondary = c.surface,
        secondaryContainer = c.secondary.copy(alpha = 0.12f),
        tertiary = c.tertiary,
        tertiaryContainer = c.tertiary.copy(alpha = 0.12f),
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.outline.copy(alpha = 0.5f),
        error = c.error,
        onError = c.surface,
        errorContainer = tagBg,
        scrim = c.scrim,
    )
}

@Composable
fun BabyTrackerTheme(
    theme: AppTheme = AppTheme.pure,
    dynamicColor: Boolean = false,
    componentTokens: AppComponentTokens? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme = theme.name == "night"
    val resolvedColors = theme.colors
    val colorScheme = theme.toColorScheme(isDark = darkTheme)

    SideEffect {
        val activity = context as? Activity
        if (activity != null && Build.VERSION.SDK_INT >= 21) {
            val window = activity.window
            @Suppress("DEPRECATION")
            run { window.statusBarColor = if (darkTheme) colorScheme.surface.toArgb() else colorScheme.background.toArgb() }
            WindowCompat.getInsetsController(window, activity.window.decorView).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val tokensSpacing = AppSpacing()
    val tokensElevation = AppElevation()
    val tokensOpacity = AppOpacity()
    val tokensMotion = AppMotion()
    val tokensShapes = AppShapes()
    val tokensTypography = AppTypography()
    val tokensControl = AppControlTokens()
    val m3Shapes = appShapesToM3Shapes(tokensShapes)

    val resolvedTokens = componentTokens ?: AppComponentTokens.default(
        colors = resolvedColors,
        spacing = tokensSpacing,
        shapes = tokensShapes,
        typography = tokensTypography,
        opacity = tokensOpacity,
        motion = tokensMotion,
        elevation = tokensElevation,
        control = tokensControl,
        darkTheme = darkTheme,
    )

    CompositionLocalProvider(
        LocalAppColors provides resolvedColors,
        LocalAppSpacing provides tokensSpacing,
        LocalAppElevation provides tokensElevation,
        LocalAppOpacity provides tokensOpacity,
        LocalAppMotion provides tokensMotion,
        LocalAppShapes provides tokensShapes,
        LocalAppTypographyStyle provides tokensTypography,
        LocalAppControl provides tokensControl,
        LocalAppComponentTokens provides resolvedTokens,
        LocalAppTypography provides BabyTrackerTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BabyTrackerTypography,
            shapes = m3Shapes,
            content = content,
        )
    }
}
