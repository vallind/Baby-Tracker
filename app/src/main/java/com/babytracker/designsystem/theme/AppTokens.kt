package com.babytracker.designsystem.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  核心语义令牌 — 基于 Palette 令牌驱动架构
//  对标 PaiColors / PaiSpacing / PaiElevation /
//       PaiOpacity / PaiMotion / PaiTypography / PaiControl
// ═══════════════════════════════════════════════════════════

// —— 调色板：原子色 + 语义状态字段 ——
@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val warning: Color,
    val outline: Color,
    val scrim: Color,
    // 文本
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val inverseOnSurface: Color,
    // 表面
    val pageBackground: Color,
    val surfaceElevated: Color,
    val surfaceOverlay: Color,
    val inverseSurface: Color,
    // 状态
    val borderHover: Color,
    val borderFocus: Color,
    val borderDisabled: Color,
    val bgDisabled: Color,
    val bgHover: Color,
    val bgPressed: Color,
    val bgSelected: Color,
    // 结构
    val divider: Color,
    val overlay: Color,
    val shadow: Color,
    val shadowFocus: Color,
    val shadowError: Color,
    val info: Color,
    val danger: Color,
) {
    companion object {
        fun light() = AppColors(
            primary = Color(0xFF4285F4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE6F0FF),
            secondary = Color(0xFFA78BFA),
            onSecondary = Color.White,
            tertiary = Color(0xFF4DD0E1),
            surface = Color.White,
            onSurface = Color(0xFF333333),
            background = Color(0xFFE6F0FF),
            onBackground = Color(0xFF333333),
            error = Color(0xFFEF4444),
            onError = Color.White,
            success = Color(0xFF4CAF50),
            warning = Color(0xFFFFA500),
            outline = Color(0xFFE0EAF5),
            scrim = Color(0x52000000),
            textPrimary = Color(0xFF333333),
            textSecondary = Color(0xAA666666),
            textTertiary = Color(0x99666666),
            textDisabled = Color(0x61C7C7CC),
            inverseOnSurface = Color(0xFFF5F5F5),
            pageBackground = Color(0xFFE6F0FF),
            surfaceElevated = Color.White,
            surfaceOverlay = Color(0x0D000000),
            inverseSurface = Color(0xFF1E1E32),
            borderHover = Color(0xFFD0D8E6),
            borderFocus = Color(0xFF4285F4),
            borderDisabled = Color(0x1A000000),
            bgDisabled = Color(0x1A000000),
            bgHover = Color(0x14FFFFFF),
            bgPressed = Color(0x1FFFFFFF),
            bgSelected = Color(0x244285F4),
            divider = Color(0x80E0EAF5),
            overlay = Color(0x52000000),
            shadow = Color(0x0A000000),
            shadowFocus = Color(0x1A4285F4),
            shadowError = Color(0x1AEF4444),
            info = Color(0xFF4285F4),
            danger = Color(0xFFEF4444),
        )

        fun dark() = AppColors(
            primary = Color(0xFF5C6BC0),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF1A2744),
            secondary = Color(0xFF9575CD),
            onSecondary = Color.White,
            tertiary = Color(0xFF4DD0E1),
            surface = Color(0xFF1E1E32),
            onSurface = Color.White,
            background = Color(0xFF12121F),
            onBackground = Color(0xFF8E8E93),
            error = Color(0xFFE57373),
            onError = Color.White,
            success = Color(0xFF4DB6AC),
            warning = Color(0xFFFFB74D),
            outline = Color(0xFF2A2A3E),
            scrim = Color(0x66000000),
            textPrimary = Color.White,
            textSecondary = Color(0xAA8E8E93),
            textTertiary = Color(0x998E8E93),
            textDisabled = Color(0x61555570),
            inverseOnSurface = Color(0xFF333333),
            pageBackground = Color(0xFF12121F),
            surfaceElevated = Color(0xFF252540),
            surfaceOverlay = Color(0x1AFFFFFF),
            inverseSurface = Color.White,
            borderHover = Color(0xFF3A3A50),
            borderFocus = Color(0xFF5C6BC0),
            borderDisabled = Color(0x33FFFFFF),
            bgDisabled = Color(0x33FFFFFF),
            bgHover = Color(0x14FFFFFF),
            bgPressed = Color(0x1FFFFFFF),
            bgSelected = Color(0x245C6BC0),
            divider = Color(0x802A2A3E),
            overlay = Color(0x66000000),
            shadow = Color(0x33000000),
            shadowFocus = Color(0x1A5C6BC0),
            shadowError = Color(0x1AE57373),
            info = Color(0xFF5C6BC0),
            danger = Color(0xFFE57373),
        )
    }
}

// —— 间距令牌（8 级 4 点网格） ——
@Immutable
data class AppSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
)

// —— 阴影令牌（6 级） ——
@Immutable
data class AppElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 2.dp,
    val level3: Dp = 4.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
)

// —— 透明度令牌（7 级） ——
@Immutable
data class AppOpacity(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val pressed: Float = 0.16f,
    val scrim: Float = 0.40f,
    val overlay: Float = 0.32f,
    val divider: Float = 0.12f,
)

// —— 动效令牌（时长 + 缓动） ——
@Immutable
data class AppMotionDuration(
    val fast: Int = 150,
    val medium: Int = 300,
    val long: Int = 600,
)

@Immutable
data class AppMotionEasing(
    val standard: Easing = FastOutSlowInEasing,
    val decelerate: Easing = LinearOutSlowInEasing,
    val accelerate: Easing = LinearEasing,
)

@Immutable
data class AppMotion(
    val duration: AppMotionDuration = AppMotionDuration(),
    val easing: AppMotionEasing = AppMotionEasing(),
)

// —— 圆角令牌（6 级） ——
@Immutable
data class AppShapes(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val full: Dp = 9999.dp,
)

// —— 排版令牌（5 组 7 级） ——
@Immutable
data class AppTypography(
    val display: TextStyle = TextStyle(
        fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold,
    ),
    val headline: TextStyle = TextStyle(
        fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal,
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
    ),
    val label: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
)

// —— 控件尺寸令牌 ——
@Immutable
data class ControlSizeTokens(
    val height: Dp = 48.dp,
    val fontSize: TextUnit = 15.sp,
    val iconSize: Dp = 20.dp,
    val horizontalPadding: Dp = 16.dp,
    val verticalPadding: Dp = 12.dp,
    val cornerRadius: Dp = 12.dp,
)

@Immutable
data class AppControlTokens(
    val small: ControlSizeTokens = ControlSizeTokens(
        height = 32.dp, fontSize = 13.sp, iconSize = 16.dp,
        horizontalPadding = 12.dp, verticalPadding = 4.dp, cornerRadius = 8.dp,
    ),
    val medium: ControlSizeTokens = ControlSizeTokens(),
    val large: ControlSizeTokens = ControlSizeTokens(
        height = 56.dp, fontSize = 16.sp, iconSize = 24.dp,
        horizontalPadding = 24.dp, verticalPadding = 16.dp, cornerRadius = 16.dp,
    ),
)

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 注入（对标 Palette 的 11 个 Local）
// ═══════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════
//  颜色派生缓存（TT-015 LRU）
// ═══════════════════════════════════════════════════════════

private val deriveCache = object : LinkedHashMap<Int, AppColors>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, AppColors>): Boolean =
        size > 12
}

private data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

private fun Color.toHsl(): Hsl {
    val r = red; val g = green; val b = blue
    val max = maxOf(r, g, b); val min = minOf(r, g, b)
    val delta = max - min
    val l = (max + min) / 2f
    if (delta == 0f) return Hsl(0f, 0f, l)
    val s = if (l <= 0.5f) delta / (max + min) else delta / (2f - max - min)
    val h = when (max) {
        r -> ((g - b) / delta) % 6f
        g -> (b - r) / delta + 2f
        else -> (r - g) / delta + 4f
    }
    return Hsl(h * 60f, s, l)
}

private fun hueShiftColor(color: Color, delta: Float): Color {
    val hsl = color.toHsl()
    return Color.hsl(
        hue = (hsl.hue + delta).mod(360f),
        saturation = hsl.saturation.coerceIn(0f, 1f),
        lightness = hsl.lightness.coerceIn(0f, 1f),
        alpha = color.alpha,
    )
}

fun AppColors.Companion.derive(
    primary: Color,
    isDark: Boolean = false,
    accent: Color = Color(0xFFFFA500),
    accentLight: Color = Color(0xFFFFF3E0),
): AppColors {
    val key = primary.hashCode() xor (if (isDark) 1 else 0) xor accent.hashCode()
    return deriveCache.getOrPut(key) {
        val base = if (isDark) AppColors.dark() else AppColors.light()
        val basePrimHsl = base.primary.toHsl()
        val targetHsl = primary.toHsl()
        val hueDelta = targetHsl.hue - basePrimHsl.hue

        fun shift(c: Color) = hueShiftColor(c, hueDelta)

        base.copy(
            primary = primary,
            onPrimary = shift(base.onPrimary),
            primaryContainer = shift(base.primaryContainer),
            secondary = shift(base.secondary),
            onSecondary = shift(base.onSecondary),
            tertiary = shift(base.tertiary),
            error = shift(base.error),
            success = shift(base.success),
            warning = shift(base.warning),
            outline = shift(base.outline),
            textPrimary = shift(base.textPrimary),
            textSecondary = shift(base.textSecondary),
            textTertiary = shift(base.textTertiary),
            textDisabled = shift(base.textDisabled),
            inverseOnSurface = shift(base.inverseOnSurface),
            pageBackground = shift(base.pageBackground),
            surfaceElevated = shift(base.surfaceElevated),
            surfaceOverlay = shift(base.surfaceOverlay),
            inverseSurface = shift(base.inverseSurface),
            borderHover = shift(base.borderHover),
            borderFocus = primary,
            borderDisabled = shift(base.borderDisabled),
            bgDisabled = shift(base.bgDisabled),
            bgHover = primary.copy(alpha = 0.08f),
            bgPressed = primary.copy(alpha = 0.12f),
            bgSelected = primary.copy(alpha = 0.14f),
            divider = shift(base.divider),
            overlay = shift(base.overlay),
            shadow = shift(base.shadow),
            shadowFocus = primary.copy(alpha = 0.10f),
            shadowError = shift(base.shadowError),
            info = primary,
            danger = shift(base.danger),
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 注入
// ═══════════════════════════════════════════════════════════

val LocalAppColors = compositionLocalOf { AppColors.light() }
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
val LocalAppElevation = staticCompositionLocalOf { AppElevation() }
val LocalAppOpacity = staticCompositionLocalOf { AppOpacity() }
val LocalAppMotion = staticCompositionLocalOf { AppMotion() }
val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
val LocalAppTypographyStyle = staticCompositionLocalOf { AppTypography() }
val LocalAppControl = staticCompositionLocalOf { AppControlTokens() }
