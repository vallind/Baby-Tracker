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
//  每个主题直接持有 AppColors：AppColors 是唯一颜色来源，
//  亮色主题共用同一套派生规则，暗色主题只换 surface/边框/状态色
// ═══════════════════════════════════════════════════════════

data class AppTheme(
    val name: String,
    val colors: AppColors,
) {
    companion object {
        // 每个主题只需传入与默认值不同的种子，其余由 AppColors.derive() 自动派生
        // pure/night 为旗舰主题：primary 锚定 HeroUI blue-500 并直接注入官方分档表
        val pure = AppTheme("pure", lightThemeColors(
            primary = Color(0xFF006FEE),
            semanticScales = HeroUiPalettes.officialSemantics,
        ))

        val aurora = AppTheme("aurora", lightThemeColors(
            primary = Color(0xFF7C6CF0),
            warning = Color(0xFFFCD34D),
        ))

        val warm = AppTheme("warm", lightThemeColors(primary = Color(0xFFFF8A80)))

        val sunny = AppTheme("sunny", lightThemeColors(
            primary = Color(0xFFF5A623),
            warning = Color(0xFFE67A2E),
        ))

        val night = AppTheme("night", nightThemeColors(
            primary = Color(0xFF006FEE),
            semanticScales = HeroUiPalettes.officialSemantics,
        ))

        val morandi = AppTheme("morandi", lightThemeColors(
            primary = Color(0xFFB0BEC5),
            warning = Color(0xFFD0A878),
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}

/** 亮色主题统一派生：白卡片 + 中性边框，主色浅档作为容器色 */
private fun lightThemeColors(
    primary: Color,
    warning: Color = Color(0xFFF5A524),
    semanticScales: AppSemanticScales? = null,
): AppColors = AppColors.derive(
    primary = primary,
    surface = Color.White,
    onSurface = Color(0xFF11181C),
    // 边框收敛为中性 zinc-200：不再随品牌主色染色（HeroUI border/divider 均为中性）
    border = HeroUiPalettes.zinc.shade200,
    warning = warning,
    primaryContainer = primaryLight(primary),
    semanticScales = semanticScales,
)

/** 暗色主题统一派生：深色卡片 + 浅色文字，中性暗边框；状态色不柔和化，与亮色同源（HeroUI 策略） */
private fun nightThemeColors(
    primary: Color,
    semanticScales: AppSemanticScales? = null,
): AppColors = AppColors.derive(
    primary = primary,
    surface = Color(0xFF18181B),
    onSurface = Color.White,
    border = HeroUiPalettes.zinc.shade800,
    primaryContainer = primaryLight(primary),
    semanticScales = semanticScales,
)

private fun Color.mix(other: Color, weight: Float): Color = Color(
    red * (1 - weight) + other.red * weight,
    green * (1 - weight) + other.green * weight,
    blue * (1 - weight) + other.blue * weight,
    alpha = 1f,
)

private fun primaryLight(primary: Color): Color = primary.mix(Color.White, 0.88f)

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 声明 — LocalApp*：令牌体系（对标 Palette 的 11+ 个 Local）
// ═══════════════════════════════════════════════════════════

val LocalAppTypography = compositionLocalOf { AppTypography() }

// 内部 M3 Typography：数值与自建 AppTypography 保持一致，
// 仅提供给 MaterialTheme 内部使用，不对外暴露 M3 Typography 类型
private val internalMaterialTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
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
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primary,
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = c.secondary.copy(alpha = 0.12f),
        onSecondaryContainer = c.secondary,
        tertiary = c.tertiary,
        onTertiary = c.onTertiary,
        tertiaryContainer = c.tertiary.copy(alpha = 0.12f),
        onTertiaryContainer = c.tertiary,
        background = c.pageBackground,
        onBackground = c.onBackground,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.divider,
        error = c.error,
        onError = c.onError,
        errorContainer = c.error.copy(alpha = 0.12f),
        onErrorContainer = c.error,
        scrim = c.scrim,
    ) else lightColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primary,
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = c.secondary.copy(alpha = 0.12f),
        onSecondaryContainer = c.secondary,
        tertiary = c.tertiary,
        onTertiary = c.onTertiary,
        tertiaryContainer = c.tertiary.copy(alpha = 0.12f),
        onTertiaryContainer = c.tertiary,
        background = c.pageBackground,
        onBackground = c.onBackground,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceElevated,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.divider,
        error = c.error,
        onError = c.onError,
        errorContainer = c.error.copy(alpha = 0.12f),
        onErrorContainer = c.error,
        scrim = c.scrim,
    )
}

@Composable
fun BabyTrackerTheme(
    theme: AppTheme = AppTheme.pure,
    componentTokens: AppComponentTokens? = null,
    density: AppDensity = LocalAppDensity.current,
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
            // 状态栏融入页面背景（浅蓝），让顶部更柔和
            @Suppress("DEPRECATION")
            run { window.statusBarColor = if (darkTheme) colorScheme.surface.toArgb() else colorScheme.background.toArgb() }
            WindowCompat.getInsetsController(window, activity.window.decorView).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val densityTokens = density.tokens
    val tokensSpacing = AppSpacing().scaled(densityTokens.spacingScale)
    val tokensElevation = AppElevation()
    val tokensOpacity = AppOpacity()
    val tokensMotion = AppMotion()
    val tokensShapes = AppShapes()
    val tokensTypography = AppTypography()
    val tokensControl = AppControlTokens().densityAdjusted(density)

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
        LocalAppDensity provides density,
        LocalAppSpacing provides tokensSpacing,
        LocalAppElevation provides tokensElevation,
        LocalAppOpacity provides tokensOpacity,
        LocalAppMotion provides tokensMotion,
        LocalAppShapes provides tokensShapes,
        LocalAppControl provides tokensControl,
        LocalAppComponentTokens provides resolvedTokens,
        LocalAppTypography provides tokensTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = internalMaterialTypography,
            shapes = BabyTrackerShapes,
            content = content,
        )
    }
}
