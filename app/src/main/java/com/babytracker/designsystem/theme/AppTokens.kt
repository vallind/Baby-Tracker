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
//  核心语义令牌 —「柔和奶油 + 多彩分区」设计语言
//  奶油暖底 + 粉彩分区色 + 大圆角 + 柔和暖阴影
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
    // —— 分档色板（取用约定见 AppColorScale.kt）——
    val primaryScale: AppColorScale,
    val secondaryScale: AppColorScale,
    val successScale: AppColorScale,
    val warningScale: AppColorScale,
    val dangerScale: AppColorScale,
    /** 中性阶梯（恒为 stone 暖灰），与主题品牌色无关 */
    val neutralScale: AppColorScale,
    /** 次级表面层级：卡片内的浅色块/代码块/徽章底/填充输入框底 */
    val surfaceMuted: Color,
) {
    companion object {
        fun light() = derive(
            primary = SoftPalettes.blue.default,      // 品牌蓝 indigo
            surface = Color(0xFFFFFFFF),              // 卡片纯白
            onSurface = Color(0xFF2E2925),            // 暖近黑文字
            border = SoftPalettes.stone.shade200,     // 暖灰描边
            onPrimary = Color.White,
            error = SoftPalettes.coral.default,       // 珊瑚红（喂养/删除）
            onError = Color.White,
            success = SoftPalettes.green.default,
            warning = SoftPalettes.amber.default,
            secondary = SoftPalettes.violet.default,  // 睡眠紫
            onSecondary = Color.White,
            tertiary = SoftPalettes.teal.default,     // 尿布青
            onTertiary = Color.White,
            primaryContainer = SoftPalettes.blue.shade100,
            background = Color(0xFFF8F6F3),           // 奶油页面底
            onBackground = Color(0xFF2E2925),
            outline = SoftPalettes.stone.shade200,
            scrim = Color(0x80211B12),
            semanticScales = SoftPalettes.softSemantics,
        )

        fun dark() = derive(
            primary = Color(0xFF8FA7F9),              // 暗色下主色提亮（浅青蓝）
            surface = Color(0xFF211E1B),              // 暖黑卡片
            onSurface = Color(0xFFF2EFEA),
            border = SoftPalettes.stone.shade800,     // 暖灰描边
            onPrimary = Color(0xFF101B3C),            // 深海军蓝文字压浅色主按钮
            error = SoftPalettes.coral.shade400,
            onError = Color(0xFF3D1010),
            success = SoftPalettes.green.shade400,
            onSuccess = Color(0xFF0B2E1D),
            warning = SoftPalettes.amber.shade400,
            onWarning = Color(0xFF3A2503),
            secondary = SoftPalettes.violet.shade400,
            onSecondary = Color(0xFF221848),
            tertiary = SoftPalettes.teal.shade400,
            onTertiary = Color(0xFF062E33),
            primaryContainer = Color(0xFF1B2A55),     // 深靛蓝容器
            background = Color(0xFF141110),           // 暖黑页面底
            onBackground = SoftPalettes.stone.shade300,
            outline = SoftPalettes.stone.shade800,
            scrim = Color(0x8C000000),
            semanticScales = SoftPalettes.softSemantics,
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

// —— 阴影令牌（6 级，暖棕色调阴影，配合奶油底更柔和） ——
@Immutable
data class AppElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 2.dp,
    val level2: Dp = 4.dp,
    val level3: Dp = 8.dp,
    val level4: Dp = 12.dp,
    val level5: Dp = 20.dp,
)

// —— 透明度令牌（7 级） ——
@Immutable
data class AppOpacity(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val pressed: Float = 0.16f,
    val scrim: Float = 0.48f,
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

// —— 圆角令牌（10 级 + 全局缩放） ——
//  整体放大一档：卡片 24dp、弹层 32dp、控件 16dp（现代大圆角趋势）
@Immutable
data class AppShapes(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 8.dp,               // 极小：标签/徽章/骨架屏
    val small: Dp = 12.dp,                    // 小：输入框/菜单项
    val medium: Dp = 16.dp,                   // 中等：按钮/对话框
    val large: Dp = 20.dp,                    // 大：底部弹层/大卡片
    val largeIncreased: Dp = 24.dp,           // 增大：大圆角卡片
    val extraLarge: Dp = 32.dp,               // 超大：底部弹层/大容器
    val extraLargeIncreased: Dp = 36.dp,      // 增大超大
    val extraExtraLarge: Dp = 48.dp,          // 特大
    val full: Dp = 9999.dp,                   // 胶囊：按钮/标签
    /** 全局圆角缩放倍率，影响所有组件 cornerRadius。1.0 = 默认，0.8 = 更方，1.2 = 更圆 */
    val radiusScale: Float = 1.0f,
) {
    /** 应用缩放后的圆角值 */
    fun scaled(base: Dp): Dp = base * radiusScale
}

// —— 排版令牌（自建 12 级，现代大排版：层级分明、行高宽松） ——
@Immutable
data class AppTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold,
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold,
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headlineSmall: TextStyle = TextStyle(
        fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
    ),
    val titleSmall: TextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal,
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
    ),
    val labelMedium: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
    val labelSmall: TextStyle = TextStyle(
        fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium,
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
    val cornerRadius: Dp = 16.dp,
)

@Immutable
data class AppControlTokens(
    val small: ControlSizeTokens = ControlSizeTokens(
        height = 32.dp, fontSize = 13.sp, iconSize = 16.dp,
        horizontalPadding = 12.dp, verticalPadding = 4.dp, cornerRadius = 10.dp,
    ),
    val medium: ControlSizeTokens = ControlSizeTokens(),
    val large: ControlSizeTokens = ControlSizeTokens(
        height = 56.dp, fontSize = 16.sp, iconSize = 24.dp,
        horizontalPadding = 24.dp, verticalPadding = 16.dp, cornerRadius = 18.dp,
    ),
)

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 注入
// ═══════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════
//  颜色派生（语义派生，非 HSL 位移）
// ═══════════════════════════════════════════════════════════

/**
 * 从基础色自动派生所有语义颜色。
 * 传入 primary / surface / onSurface / border 等，其余字段按语义关系自动计算。
 */
fun AppColors.Companion.derive(
    primary: Color,
    surface: Color = Color.White,
    onSurface: Color = Color(0xFF2E2925),
    border: Color = SoftPalettes.stone.shade200,
    onPrimary: Color = Color.White,
    error: Color = SoftPalettes.coral.default,
    onError: Color = Color.White,
    success: Color = SoftPalettes.green.default,
    onSuccess: Color = Color.White,
    warning: Color = SoftPalettes.amber.default,
    onWarning: Color = Color.White,
    secondary: Color = SoftPalettes.violet.default,
    onSecondary: Color = Color.White,
    tertiary: Color = SoftPalettes.teal.default,
    onTertiary: Color = Color.White,
    primaryContainer: Color = Color.Unspecified,
    background: Color = surface,
    onBackground: Color = onSurface,
    outline: Color = border,
    scrim: Color = Color(0x80211B12),
    /** 页面底（奶油色）：默认亮色 #F8F6F3 / 暗色 #141110 */
    pageBackground: Color = Color.Unspecified,
    /** 显式注入语义分档；缺省从种子色生成同源色阶 */
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
        pageBackground = when {
            pageBackground != Color.Unspecified -> pageBackground
            isDark -> Color(0xFF141110)
            else -> Color(0xFFF8F6F3)
        },
        surfaceElevated = if (isDark) surface.copy(red = surface.red + 0.06f, green = surface.green + 0.06f, blue = surface.blue + 0.06f) else surface,
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
        // 结构：暖棕调阴影，配合奶油底更柔和
        divider = border.copy(alpha = 0.72f),
        overlay = Color(0x66211B12),
        shadow = Color(red = 0.16f, green = 0.12f, blue = 0.07f, alpha = 0.18f),
        shadowFocus = primary.copy(alpha = 0.25f),
        shadowError = error.copy(alpha = 0.25f),
        info = primary,
        danger = error,
        // 分档与次级表面
        primaryScale = scales.primary,
        secondaryScale = scales.secondary,
        successScale = scales.success,
        warningScale = scales.warning,
        dangerScale = scales.danger,
        neutralScale = SoftPalettes.stone,
        surfaceMuted = if (isDark) SoftPalettes.stone.shade800 else SoftPalettes.stone.shade100,
    )
}

/** 估算颜色的感知亮度（0~1），用于判断亮/暗主题；internal 供分档取色辅助函数复用 */
internal fun Color.luminance(): Float {
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