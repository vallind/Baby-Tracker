package com.babytracker.designsystem.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  AppDefaults 快照 — 为无需 @Composable 上下文的代码提供 Token 默认值
//  注意：这些是静态默认值（纯主题），不感知当前运行时主题。
//  在 @Composable 上下文中请优先使用 LocalApp* CompositionLocal。
// ═══════════════════════════════════════════════════════════

data class SpacingSnapshot(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
)

data class ElevationSnapshot(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 2.dp,
    val level3: Dp = 4.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
)

data class OpacitySnapshot(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val pressed: Float = 0.16f,
    val scrim: Float = 0.40f,
    val overlay: Float = 0.32f,
    val divider: Float = 0.12f,
)

data class MotionDurationSnapshot(
    val fast: Int = 150,
    val medium: Int = 300,
    val long: Int = 600,
)

data class MotionEasingSnapshot(
    val standard: String = "FastOutSlowIn",
    val decelerate: String = "LinearOutSlowIn",
    val accelerate: String = "Linear",
)

data class MotionSnapshot(
    val duration: MotionDurationSnapshot = MotionDurationSnapshot(),
    val easing: MotionEasingSnapshot = MotionEasingSnapshot(),
)

/** 与 AppShapes 默认值保持同步 */
data class ShapesSnapshot(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 6.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 12.dp,
    val full: Dp = 9999.dp,
)

/** 与 AppTypography 默认值保持同步 */
data class TypographySnapshot(
    val display: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    val headline: TextStyle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    val titleLarge: TextStyle = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    val titleMedium: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    val bodyLarge: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    val bodyMedium: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    val label: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

/** 与 AppColors.light()（纯主题）默认值保持同步 */
data class ColorsSnapshot(
    val primary: Color = Color(0xFF4285F4),
    val onPrimary: Color = Color.White,
    val primaryContainer: Color = Color(0xFFDBEAFE),
    val surface: Color = Color.White,
    val onSurface: Color = Color(0xFF333333),
    val background: Color = Color(0xFFF8FAFC),
    val textPrimary: Color = Color(0xFF333333),
    val textSecondary: Color = Color(0xFF999999),
    val textTertiary: Color = Color(0xFFB3B3B3),
    val error: Color = Color(0xFFEF4444),
    val success: Color = Color(0xFF4CAF50),
    val warning: Color = Color(0xFFFFA500),
    val outline: Color = Color(0xFFD9D9D9),
    val divider: Color = Color(0xFFD9D9D9),
)

object AppDefaults {
    val spacing = SpacingSnapshot()
    val elevation = ElevationSnapshot()
    val opacity = OpacitySnapshot()
    val motion = MotionSnapshot()
    val shapes = ShapesSnapshot()
    val typography = TypographySnapshot()
    val colors = ColorsSnapshot()
}
