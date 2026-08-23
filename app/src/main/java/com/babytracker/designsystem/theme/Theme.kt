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
        // pure/night 为旗舰主题：奶油底 + 品牌蓝，直接注入官方软调分档表
        val pure = AppTheme("pure", lightThemeColors(
            primary = SoftPalettes.blue.default,
            semanticScales = SoftPalettes.softSemantics,
        ))

        // 晨曦紫：柔和紫为主色，琥珀点缀
        val aurora = AppTheme("aurora", lightThemeColors(
            primary = SoftPalettes.violet.default,
            warning = SoftPalettes.amber.default,
            semanticScales = SoftPalettes.softSemantics,
        ))

        // 暖阳粉：珊瑚色为主色
        val warm = AppTheme("warm", lightThemeColors(
            primary = Color(0xFFEF7967),
            warning = SoftPalettes.amber.default,
            semanticScales = SoftPalettes.softSemantics,
        ))

        // 晴日橙：琥珀为主色
        val sunny = AppTheme("sunny", lightThemeColors(
            primary = Color(0xFFF0A43B),
            warning = Color(0xFFE8782E),
            semanticScales = SoftPalettes.softSemantics,
        ))

        // 深夜蓝：暖黑底 + 提亮品牌蓝
        val night = AppTheme("night", nightThemeColors(
            primary = Color(0xFF8FA7F9),
            semanticScales = SoftPalettes.softSemantics,
        ))

        // 莫兰迪：低饱和鼠尾草绿，安静高级
        val morandi = AppTheme("morandi", lightThemeColors(
            primary = Color(0xFF9AAE8F),
            warning = Color(0xFFC9A96E),
            semanticScales = SoftPalettes.softSemantics,
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}

/** 亮色主题统一派生：奶油底 + 白卡片 + 暖灰描边，主色浅档作为容器色 */
private fun lightThemeColors(
    primary: Color,
    warning: Color = SoftPalettes.amber.default,
    onSurface: Color = Color(0xFF2E2925),
    background: Color = Color(0xFFF8F6F3),
    border: Color = SoftPalettes.stone.shade200,
    primaryContainer: Color = Color.Unspecified,
    secondary: Color = SoftPalettes.violet.default,
    tertiary: Color = SoftPalettes.teal.default,
    success: Color = SoftPalettes.green.default,
    error: Color = SoftPalettes.coral.default,
    semanticScales: AppSemanticScales? = null,
): AppColors = AppColors.derive(
    primary = primary,
    surface = Color.White,
    onSurface = onSurface,
    // 边框收敛为暖灰 stone-200，不随品牌主色染色
    border = border,
    warning = warning,
    background = background,
    pageBackground = background,
    primaryContainer = if (primaryContainer == Color.Unspecified) primary.mix(Color.White, 0.85f) else primaryContainer,
    secondary = secondary,
    tertiary = tertiary,
    success = success,
    error = error,
    semanticScales = semanticScales,
)

/** 暗色主题统一派生：暖黑卡片 + 浅色文字，暖灰暗边框；状态色提亮一档（HeroUI 暗色策略） */
private fun nightThemeColors(
    primary: Color,
    warning: Color = SoftPalettes.amber.shade400,
    semanticScales: AppSemanticScales? = null,
): AppColors = AppColors.derive(
    primary = primary,
    surface = Color(0xFF211E1B),
    onSurface = Color(0xFFF2EFEA),
    border = SoftPalettes.stone.shade800,
    pageBackground = Color(0xFF141110),
    background = Color(0xFF141110),
    primaryContainer = primary.mix(Color(0xFF211E1B), 0.55f),
    warning = warning,
    semanticScales = semanticScales,
)

private fun Color.mix(other: Color, weight: Float): Color = Color(
    red * (1 - weight) + other.red * weight,
    green * (1 - weight) + other.green * weight,
    blue * (1 - weight) + other.blue * weight,
    alpha = 1f,
)

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 声明 — LocalApp*：令牌体系
// ═══════════════════════════════════════════════════════════

val LocalAppTypography = compositionLocalOf { AppTypography() }

// 内部 M3 Typography：数值与自建 AppTypography 保持一致，
// 仅提供给 MaterialTheme 内部使用，不对外暴露 M3 Typography 类型
private val internalMaterialTypography = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
)

val BabyTrackerShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),    // 输入框/小元素
    small = RoundedCornerShape(16.dp),         // 标准卡片
    medium = RoundedCornerShape(20.dp),        // 中等卡片
    large = RoundedCornerShape(24.dp),         // 大卡片/对话框
    extraLarge = RoundedCornerShape(32.dp),    // 底部弹层/超大圆角
)

fun AppTheme.toColorScheme(isDark: Boolean = false): androidx.compose.material3.ColorScheme {
    val c = colors
    // 容器色统一走分档浅底/暗底（亮色 shade100 / 暗色 shade800），杜绝 alpha 伪造
    val secondaryTint = c.secondaryScale.tintContainer(c)
    val secondaryContent = c.secondaryScale.accentContent(c)
    val tertiaryScale = AppColorScale.fromSeed(c.tertiary)
    val tertiaryTint = tertiaryScale.tintContainer(c)
    val tertiaryContent = tertiaryScale.accentContent(c)
    val dangerTint = c.dangerScale.tintContainer(c)
    return if (isDark) darkColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primaryScale.accentContent(c),
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = secondaryTint,
        onSecondaryContainer = secondaryContent,
        tertiary = c.tertiary,
        onTertiary = c.onTertiary,
        tertiaryContainer = tertiaryTint,
        onTertiaryContainer = tertiaryContent,
        background = c.pageBackground,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceMuted,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.divider,
        error = c.error,
        onError = c.onError,
        errorContainer = dangerTint,
        onErrorContainer = c.dangerScale.accentContent(c),
        scrim = c.scrim,
    ) else lightColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.primaryScale.accentContent(c),
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = secondaryTint,
        onSecondaryContainer = secondaryContent,
        tertiary = c.tertiary,
        onTertiary = c.onTertiary,
        tertiaryContainer = tertiaryTint,
        onTertiaryContainer = tertiaryContent,
        background = c.pageBackground,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surfaceMuted,
        onSurfaceVariant = c.textSecondary,
        outline = c.outline,
        outlineVariant = c.divider,
        error = c.error,
        onError = c.onError,
        errorContainer = dangerTint,
        onErrorContainer = c.dangerScale.accentContent(c),
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
            // 状态栏/导航栏融入奶油页面底（暗色为暖黑），淡化系统栏存在感
            @Suppress("DEPRECATION")
            run {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
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