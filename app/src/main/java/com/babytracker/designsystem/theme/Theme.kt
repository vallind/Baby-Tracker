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
//  ThemeColors — 6 套主题的旧兼容颜色定义
//  被 AppTheme 持有，在 BabyTrackerTheme() 中映射到 AppColors
// ═══════════════════════════════════════════════════════════

data class ThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val bg: Color,
    val card: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pink: Color,
    val blue: Color,
    val green: Color,
    val yellow: Color,
    val purple: Color,
    val cyan: Color,
    val tagBg: Color,
    val tagText: Color,
    val accent: Color,
    val accentLight: Color,
    val pageBg: Color,
    val cardShadow: Color,
    val borderHover: Color,
    val borderFocus: Color,
    val textDisabled: Color,
    val bgHover: Color,
    val bgPressed: Color,
) {
    companion object {
        /**
         * 从主色自动派生全部 29 个颜色字段。各主题只需传入差异项。
         * @param primary 主色（品牌色）
         * @param isDark 是否为暗色主题
         * @param bg 背景色，默认亮色=primaryLight / 暗色=#12121F
         * @param pageBg 页面背景色，默认=bg
         * @param primaryLight 主色浅色变体，默认从 primary + white 混合
         * @param card 卡片背景色，默认亮色=white / 暗色=#1E1E32
         * @param accent 强调色（默认橙），同时影响 warning/tagBg/tagText/accentLight
         */
        fun derive(
            primary: Color,
            isDark: Boolean = false,
            bg: Color? = null,
            pageBg: Color? = null,
            primaryLight: Color? = null,
            card: Color? = null,
            accent: Color? = null,
        ): ThemeColors {
            val white = Color.White
            val black = Color.Black
            fun Color.mix(other: Color, weight: Float): Color = Color(
                red * (1 - weight) + other.red * weight,
                green * (1 - weight) + other.green * weight,
                blue * (1 - weight) + other.blue * weight,
                alpha = 1f,
            )
            fun Color.desaturate(factor: Float): Color {
                val avg = (red + green + blue) / 3f
                return Color(red * (1 - factor) + avg * factor, green * (1 - factor) + avg * factor, blue * (1 - factor) + avg * factor, alpha)
            }

            val resolvedPrimaryLight = primaryLight ?: primary.mix(white, 0.88f)
            val resolvedBg = bg ?: if (isDark) Color(0xFF12121F) else resolvedPrimaryLight
            val resolvedCard = card ?: if (isDark) Color(0xFF1E1E32) else white
            val resolvedPageBg = pageBg ?: resolvedBg
            val resolvedAccent = accent ?: Color(0xFFFFA500)
            val textPrimary = if (isDark) white else Color(0xFF09090B)
            val textSecondary = if (isDark) Color(0xFF8E8E93) else Color(0xFF71717A)

            // 边框/分隔线由主色派生 + 去饱和
            val borderBase = if (isDark) Color(0xFF2A2A3E) else primary.mix(white, 0.85f).desaturate(0.5f)

            return ThemeColors(
                primary = primary,
                primaryLight = resolvedPrimaryLight,
                bg = resolvedBg,
                card = resolvedCard,
                cardBorder = borderBase,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                textHint = Color(0xFFC7C7CC),
                divider = borderBase,
                success = if (isDark) Color(0xFF4DB6AC) else Color(0xFF4CAF50),
                warning = resolvedAccent,
                danger = if (isDark) Color(0xFFE57373) else Color(0xFFEF4444),
                pink = if (isDark) Color(0xFFE57373) else Color(0xFFFF8A9E),
                blue = primary,
                green = if (isDark) Color(0xFF4DB6AC) else Color(0xFF4CAF50),
                yellow = Color(0xFFFFD54F),
                purple = Color(0xFFA78BFA),
                cyan = Color(0xFF4DD0E1),
                tagBg = resolvedAccent.mix(if (isDark) black else white, 0.85f),
                tagText = resolvedAccent,
                accent = resolvedAccent,
                accentLight = resolvedAccent.mix(if (isDark) black else white, 0.85f),
                pageBg = resolvedPageBg,
                cardShadow = if (isDark) black else primary.mix(white, 0.7f),
                borderHover = if (isDark) Color(0xFF3A3A50) else primary.mix(white, 0.92f),
                borderFocus = primary,
                textDisabled = Color(0xFFC7C7CC),
                bgHover = if (isDark) Color(0xFF252540) else primary.mix(white, 0.92f),
                bgPressed = if (isDark) Color(0xFF2E2E50) else primary.mix(white, 0.85f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  AppTheme — 6 套主题定义
//  每个主题包含名称 + ThemeColors，在 BabyTrackerTheme() 中消费
// ═══════════════════════════════════════════════════════════

data class AppTheme(
    val name: String,
    val colors: ThemeColors,
) {
    companion object {
        // 每个主题只需传入与默认值不同的种子，其余由 ThemeColors.derive() 自动派生
        val pure = AppTheme("pure", ThemeColors.derive(primary = Color(0xFF4285F4)))

        val aurora = AppTheme("aurora", ThemeColors.derive(
            primary = Color(0xFF7C6CF0), bg = Color.White, pageBg = Color(0xFFF8F5FF),
            accent = Color(0xFFFCD34D),
        ))

        val warm = AppTheme("warm", ThemeColors.derive(
            primary = Color(0xFFFF8A80), bg = Color(0xFFFFFBF7),
        ))

        val sunny = AppTheme("sunny", ThemeColors.derive(
            primary = Color(0xFFF5A623), bg = Color(0xFFFFFAF0),
            accent = Color(0xFFE67A2E),
        ))

        val night = AppTheme("night", ThemeColors.derive(
            primary = Color(0xFF5C6BC0), isDark = true,
        ))

        val morandi = AppTheme("morandi", ThemeColors.derive(
            primary = Color(0xFFB0BEC5), bg = Color(0xFFFAFAFA),
            accent = Color(0xFFD0A878),
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 声明
//  — LocalThemeColors：保留原有，向后兼容现有代码
//  — LocalApp*：令牌体系（对标 Palette 的 11+ 个 Local）
// ═══════════════════════════════════════════════════════════

val LocalThemeColors = compositionLocalOf { AppTheme.pure.colors }
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
