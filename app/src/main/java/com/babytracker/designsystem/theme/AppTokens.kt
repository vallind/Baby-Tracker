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
    val onTertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
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
    // —— 分档色板（对标 HeroUI semantic scale，取用约定见 AppColorScale.kt）——
    val primaryScale: AppColorScale,
    val secondaryScale: AppColorScale,
    val successScale: AppColorScale,
    val warningScale: AppColorScale,
    val dangerScale: AppColorScale,
    /** 中性阶梯（恒为 zinc），与主题品牌色无关 */
    val neutralScale: AppColorScale,
    /** 次级表面层级（对标 HeroUI content2）：卡片内的浅色块/代码块/徽章底 */
    val surfaceMuted: Color,
) {
    companion object {
        //  HeroUI 风格：中性色 zinc 化，语义五色采用官方锚点值
        fun light() = derive(
            primary = Color(0xFF006FEE),       // HeroUI blue-500
            surface = Color(0xFFFFFFFF),       // content1
            onSurface = Color(0xFF11181C),     // HeroUI foreground
            border = Color(0xFFE4E4E7),        // zinc-200
            onPrimary = Color.White,
            error = Color(0xFFF31260),         // HeroUI red-500（danger）
            onError = Color.White,
            success = Color(0xFF17C964),       // HeroUI green-500
            warning = Color(0xFFF5A524),       // HeroUI yellow-500
            secondary = Color(0xFF7828C8),     // HeroUI purple-500
            onSecondary = Color.White,
            tertiary = Color(0xFF06B7DB),      // HeroUI cyan-600
            primaryContainer = Color(0xFFCCE3FD),  // blue-100
            background = Color(0xFFF4F4F5),    // zinc-100 页面底，白卡浮于其上
            onBackground = Color(0xFF11181C),
            outline = Color(0xFFE4E4E7),       // border-input
            scrim = Color(0x52000000),
            semanticScales = HeroUiPalettes.officialSemantics,
        )

        fun dark() = derive(
            primary = Color(0xFF006FEE),       // HeroUI 暗色下主值不变，仅档位倒序
            surface = Color(0xFF18181B),       // zinc-900 = content1
            onSurface = Color(0xFFFAFAFA),
            border = Color(0xFF27272A),        // zinc-800
            onPrimary = Color.White,
            error = Color(0xFFF31260),
            onError = Color.White,
            success = Color(0xFF17C964),
            warning = Color(0xFFF5A524),
            secondary = Color(0xFF9353D3),     // HeroUI 暗色 secondary 取 purple-400
            onSecondary = Color.White,
            tertiary = Color(0xFF06B7DB),
            primaryContainer = Color(0xFF003B8A),  // 蓝暗底：介于 blue-700/800
            background = Color(0xFF09090B),    // zinc-950
            onBackground = Color(0xFFA1A1AA),  // zinc-400
            outline = Color(0xFF27272A),       // border-input
            scrim = Color(0x66000000),
            semanticScales = HeroUiPalettes.officialSemantics,
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

/** 按密度系数缩放全部间距（全局密度调整入口，零组件迁移成本） */
fun AppSpacing.scaled(factor: Float): AppSpacing = AppSpacing(
    none = none,
    xxs = xxs * factor,
    xs = xs * factor,
    sm = sm * factor,
    md = md * factor,
    lg = lg * factor,
    xl = xl * factor,
    xxl = xxl * factor,
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
    error: Color = Color(0xFFF31260),
    onError: Color = Color.White,
    success: Color = Color(0xFF17C964),
    onSuccess: Color = Color.White,
    warning: Color = Color(0xFFF5A524),
    onWarning: Color = Color.White,
    secondary: Color = Color(0xFFA78BFA),
    onSecondary: Color = Color.White,
    tertiary: Color = Color(0xFF06B7DB),
    onTertiary: Color = Color.White,
    primaryContainer: Color = Color.Unspecified,
    background: Color = surface,
    onBackground: Color = onSurface,
    outline: Color = border,
    scrim: Color = Color.Black.copy(alpha = 0.32f),
    /** 显式注入语义分档（旗舰主题传 HeroUI 官方表）；缺省从种子色生成同源色阶 */
    semanticScales: AppSemanticScales? = null,
): AppColors {
    val isDark = surface.luminance() < 0.5f

    // 分档优先级：显式注入 > 从种子色生成（保证自定义主题也有完整档位）
    val scales = semanticScales ?: AppSemanticScales(
        primary = AppColorScale.fromSeed(primary),
        secondary = AppColorScale.fromSeed(secondary),
        success = AppColorScale.fromSeed(success),
        warning = AppColorScale.fromSeed(warning),
        danger = AppColorScale.fromSeed(error),
    )

    return AppColors(
        primary = primary,
        onPrimary = onPrimary,
        // 未显式传入时取主色 shade100：不透明浅底，替代旧 alpha 叠加
        primaryContainer = if (primaryContainer == Color.Unspecified) scales.primary.shade100 else primaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        surface = surface,
        onSurface = onSurface,
        background = background,
        onBackground = onBackground,
        error = error,
        onError = onError,
        success = success,
        onSuccess = onSuccess,
        warning = warning,
        onWarning = onWarning,
        outline = outline,
        scrim = scrim,
        // 文本：基于 onSurface 递减 alpha
        textPrimary = onSurface,
        textSecondary = onSurface.copy(alpha = 0.64f),
        textTertiary = onSurface.copy(alpha = 0.45f),
        textDisabled = onSurface.copy(alpha = 0.38f),
        inverseOnSurface = surface,
        // 表面层级
        pageBackground = if (isDark) Color(0xFF09090B) else HeroUiPalettes.zinc.shade100,
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
        // 分档与次级表面
        primaryScale = scales.primary,
        secondaryScale = scales.secondary,
        successScale = scales.success,
        warningScale = scales.warning,
        dangerScale = scales.danger,
        neutralScale = HeroUiPalettes.zinc,
        surfaceMuted = if (isDark) HeroUiPalettes.zinc.shade800 else HeroUiPalettes.zinc.shade100,
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

/** 页面密度三档 */
enum class AppDensity(val key: String, val label: String) {
    Compact("compact", "紧凑"),
    Comfortable("comfortable", "舒适"),
    Large("large", "宽松"),
    ;
    companion object {
        fun fromKey(key: String): AppDensity =
            entries.firstOrNull { it.key == key } ?: Comfortable
    }
}

/** 密度档令牌：间距缩放系数 + 控件高度调整量 */
@Immutable
data class AppDensityTokens(
    val spacingScale: Float,
    val controlHeightDelta: Dp,
)

val AppDensity.tokens: AppDensityTokens
    get() = when (this) {
        AppDensity.Compact -> AppDensityTokens(spacingScale = 0.85f, controlHeightDelta = (-8).dp)
        AppDensity.Comfortable -> AppDensityTokens(spacingScale = 1.0f, controlHeightDelta = 0.dp)
        AppDensity.Large -> AppDensityTokens(spacingScale = 1.15f, controlHeightDelta = 8.dp)
    }

/** 密度调整控件基准（只动 medium 档，组件默认尺寸随密度变化） */
fun AppControlTokens.densityAdjusted(density: AppDensity): AppControlTokens {
    val delta = density.tokens.controlHeightDelta
    if (delta == 0.dp) return this
    return copy(medium = medium.copy(height = medium.height + delta))
}

/** 当前页面密度（默认舒适） */
val LocalAppDensity = staticCompositionLocalOf { AppDensity.Comfortable }
