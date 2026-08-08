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
        //  shadcn 风格：中性色灰调化，主色保持温暖蓝
        fun light() = derive(
            primary = Color(0xFF3B82F6),       // shadcn blue-500
            surface = Color(0xFFFFFFFF),       // shadcn background
            onSurface = Color(0xFF1A1A2E),     // shadcn foreground（接近 zinc-900）
            border = Color(0xFFE4E4E7),        // shadcn border（zinc-200）
            onPrimary = Color(0xFFFFFFFF),
            error = Color(0xFFEF4444),         // red-500
            onError = Color.White,
            success = Color(0xFF22C55E),       // green-500（更清新的绿）
            warning = Color(0xFFF59E0B),       // amber-500
            secondary = Color(0xFF8B5CF6),     // violet-500
            onSecondary = Color.White,
            tertiary = Color(0xFF06B6D4),      // cyan-500
            primaryContainer = Color(0xFFDBEAFE),  // blue-100
            background = Color(0xFFF8FAFC),    // slate-50 偏灰白（shadcn 风格）
            onBackground = Color(0xFF1A1A2E),
            outline = Color(0xFFE4E4E7),       // border-input
            scrim = Color(0x52000000),
        )

        fun dark() = derive(
            primary = Color(0xFF60A5FA),       // blue-400（暗色下稍亮）
            surface = Color(0xFF18181B),       // zinc-900
            onSurface = Color(0xFFFAFAFA),     // zinc-50
            border = Color(0xFF27272A),        // zinc-800
            onPrimary = Color(0xFF18181B),
            error = Color(0xFFF87171),         // red-400
            onError = Color(0xFF18181B),
            success = Color(0xFF4ADE80),       // green-400
            warning = Color(0xFFFBBF24),       // amber-400
            secondary = Color(0xFFA78BFA),     // violet-400
            onSecondary = Color(0xFF18181B),
            tertiary = Color(0xFF22D3EE),      // cyan-400
            primaryContainer = Color(0xFF1E3A5F),  // blue-900/10
            background = Color(0xFF09090B),    // zinc-950
            onBackground = Color(0xFFA1A1AA),  // zinc-400
            outline = Color(0xFF27272A),       // border-input
            scrim = Color(0x66000000),
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

// —— 圆角令牌（10 级 + 全局缩放，参照 M3 shape scale） ——
@Immutable
data class AppShapes(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,               // 极小：标签/徽章/骨架屏
    val small: Dp = 8.dp,                    // 小：输入框/菜单项
    val medium: Dp = 12.dp,                  // 中等：按钮/对话框
    val large: Dp = 16.dp,                   // 大：底部弹层/大卡片
    val largeIncreased: Dp = 20.dp,          // 增大：大圆角卡片
    val extraLarge: Dp = 28.dp,              // 超大：底部弹层/大容器
    val extraLargeIncreased: Dp = 32.dp,     // 增大超大
    val extraExtraLarge: Dp = 48.dp,         // 特大
    val full: Dp = 9999.dp,                  // 胶囊：按钮/标签
    /** 全局圆角缩放倍率，影响所有组件 cornerRadius。1.0 = 默认，0.8 = 更方，1.2 = 更圆 */
    val radiusScale: Float = 1.0f,
) {
    /** 应用缩放后的圆角值 */
    fun scaled(base: Dp): Dp = base * radiusScale
}

// —— 排版令牌（自建 12 级） ——
@Immutable
data class AppTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold,
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headlineSmall: TextStyle = TextStyle(
        fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
    ),
    val titleSmall: TextStyle = TextStyle(
        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal,
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal,
    ),
    val labelMedium: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
    val labelSmall: TextStyle = TextStyle(
        fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
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
//  颜色派生（参照 PaletteColors.derive：语义派生，非 HSL 位移）
// ═══════════════════════════════════════════════════════════

/**
 * 从 4 个基础色自动派生所有语义颜色。
 * 参照 PaletteColors 的 derive 模式：传入 primary / surface / onSurface / border，
 * 其余字段按语义关系自动计算。
 */
fun AppColors.Companion.derive(
    primary: Color,
    surface: Color = Color.White,
    onSurface: Color = Color(0xFF333333),
    border: Color = Color(0xFFD9D9D9),
    onPrimary: Color = Color.White,
    error: Color = Color(0xFFEF4444),
    onError: Color = Color.White,
    success: Color = Color(0xFF4CAF50),
    warning: Color = Color(0xFFFFA500),
    secondary: Color = Color(0xFFA78BFA),
    onSecondary: Color = Color.White,
    tertiary: Color = Color(0xFF4DD0E1),
    primaryContainer: Color = primary.copy(alpha = 0.12f),
    background: Color = surface,
    onBackground: Color = onSurface,
    outline: Color = border,
    scrim: Color = Color.Black.copy(alpha = 0.32f),
): AppColors {
    val isDark = surface.luminance() < 0.5f

    return AppColors(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        surface = surface,
        onSurface = onSurface,
        background = background,
        onBackground = onBackground,
        error = error,
        onError = onError,
        success = success,
        warning = warning,
        outline = outline,
        scrim = scrim,
        // 文本：基于 onSurface 递减 alpha
        textPrimary = onSurface,
        textSecondary = onSurface.copy(alpha = 0.64f),
        textTertiary = onSurface.copy(alpha = 0.45f),
        textDisabled = onSurface.copy(alpha = 0.38f),
        inverseOnSurface = surface,
        // 表面层级
        pageBackground = if (isDark) Color(0xFF121212) else Color(0xFFF5F7FA),
        surfaceElevated = if (isDark) surface.copy(red = surface.red + 0.08f, green = surface.green + 0.08f, blue = surface.blue + 0.08f) else surface,
        surfaceOverlay = surface.copy(alpha = 0.95f),
        inverseSurface = onSurface,
        // 边框状态
        borderHover = primary.copy(alpha = 0.30f),
        borderFocus = primary.copy(alpha = 0.60f),
        borderDisabled = border.copy(alpha = 0.50f),
        // 背景状态
        bgDisabled = surface.copy(alpha = 0.05f),
        bgHover = primary.copy(alpha = 0.08f),
        bgPressed = primary.copy(alpha = 0.12f),
        bgSelected = primary.copy(alpha = 0.14f),
        // 结构
        divider = border.copy(alpha = 0.72f),
        overlay = Color.Black.copy(alpha = 0.45f),
        shadow = Color.Black.copy(alpha = 0.16f),
        shadowFocus = primary.copy(alpha = 0.20f),
        shadowError = error.copy(alpha = 0.20f),
        info = primary,
        danger = error,
    )
}

/** 估算颜色的感知亮度（0~1），用于判断亮/暗主题 */
private fun Color.luminance(): Float {
    val r = red; val g = green; val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
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
val LocalAppControl = staticCompositionLocalOf { AppControlTokens() }
