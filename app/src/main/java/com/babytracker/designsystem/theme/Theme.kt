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
//  CompositionLocal 声明
//  — LocalThemeColors：保留原有，向后兼容现有代码
//  — LocalApp*：令牌体系（对标 Palette 的 11+ 个 Local）
// ═══════════════════════════════════════════════════════════

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

val BabyTrackerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),    // 原 8 → 12（更圆润，输入框/小元素）
    small = RoundedCornerShape(16.dp),         // 原 16（不变，标准卡片）
    medium = RoundedCornerShape(20.dp),        // 原 20（不变，中等卡片）
    large = RoundedCornerShape(28.dp),         // 原 28（不变，大卡片/对话框）
    extraLarge = RoundedCornerShape(36.dp),    // 原 32 → 36（底部弹层/超大圆角）
)

fun AppTheme.toColorScheme(isDark: Boolean = false): androidx.compose.material3.ColorScheme {
    val c = colors
    return if (isDark) darkColorScheme(
        primary = c.primary,
        onPrimary = c.card,
        primaryContainer = c.primaryLight,
        onPrimaryContainer = c.primary,
        secondary = c.purple,
        onSecondary = c.card,
        secondaryContainer = c.purple.copy(alpha = 0.12f),
        tertiary = c.cyan,
        tertiaryContainer = c.cyan.copy(alpha = 0.12f),
        background = c.bg,
        onBackground = c.textPrimary,
        surface = c.card,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.cardBorder,
        outlineVariant = c.cardBorder.copy(alpha = 0.5f),
        error = c.danger,
        onError = c.card,
        errorContainer = c.tagBg,
        scrim = Color.Black.copy(alpha = 0.32f),
    ) else lightColorScheme(
        primary = c.primary,
        onPrimary = c.card,
        primaryContainer = c.primaryLight,
        onPrimaryContainer = c.primary,
        secondary = c.purple,
        onSecondary = c.card,
        secondaryContainer = c.purple.copy(alpha = 0.12f),
        tertiary = c.cyan,
        tertiaryContainer = c.cyan.copy(alpha = 0.12f),
        background = c.bg,
        onBackground = c.textPrimary,
        surface = c.card,
        onSurface = c.textPrimary,
        surfaceVariant = c.divider,
        onSurfaceVariant = c.textSecondary,
        outline = c.cardBorder,
        outlineVariant = c.cardBorder.copy(alpha = 0.5f),
        error = c.danger,
        onError = c.card,
        errorContainer = c.tagBg,
        scrim = Color.Black.copy(alpha = 0.32f),
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
    val tc = theme.colors
    val resolvedColors = AppColors.derive(
        primary = tc.primary,
        surface = if (darkTheme) Color(0xFF18181B) else tc.card,
        onSurface = tc.textPrimary,
        border = tc.cardBorder,
        secondary = tc.purple,
        tertiary = tc.cyan,
        success = tc.green,
        warning = tc.warning,
        error = tc.danger,
        primaryContainer = tc.primaryLight,
    )
    val colorScheme = theme.toColorScheme(isDark = darkTheme)

    SideEffect {
        val activity = context as? Activity
        if (activity != null && Build.VERSION.SDK_INT >= 21) {
            val window = activity.window
            // 状态栏融入页面背景（浅蓝），让顶部更柔和
            @Suppress("DEPRECATION")
            run { window.statusBarColor = if (darkTheme) colorScheme.surface.toArgb() else colorScheme.background.toArgb() }
            WindowCompat.getInsetsController(window, activity.window.decorView).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val mergedColors = theme.colors.copy(
        primary = colorScheme.primary,
        primaryLight = colorScheme.primaryContainer,
        bg = colorScheme.background,
        card = colorScheme.surface,
        cardBorder = colorScheme.outlineVariant,
        textPrimary = colorScheme.onSurface,
        textSecondary = colorScheme.onSurfaceVariant,
        textHint = colorScheme.outline,
        divider = colorScheme.surfaceVariant,
        success = colorScheme.tertiary,
        danger = colorScheme.error,
        blue = colorScheme.primary,
        green = colorScheme.tertiary,
        purple = colorScheme.secondary,
        cyan = colorScheme.tertiary,
        tagBg = colorScheme.errorContainer,
        tagText = colorScheme.onErrorContainer,
        // 宝宝追踪专属辅助色：不来自 colorScheme，直接从 theme.colors 透传
        accent = theme.colors.accent,
        accentLight = theme.colors.accentLight,
        pageBg = theme.colors.pageBg,
        cardShadow = theme.colors.cardShadow,
    )

    val tokensSpacing = AppSpacing()
    val tokensElevation = AppElevation()
    val tokensOpacity = AppOpacity()
    val tokensMotion = AppMotion()
    val tokensShapes = AppShapes()
    val tokensTypography = AppTypography()
    val tokensControl = AppControlTokens()

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
        LocalThemeColors provides mergedColors,
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
            shapes = BabyTrackerShapes,
            content = content,
        )
    }
}
