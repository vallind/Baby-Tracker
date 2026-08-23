package com.babytracker.designsystem.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
//  AppDefaults 快照 — 对标 Palette 默认令牌
//  为无需 @Composable 上下文的代码提供主题 Token 访问
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
    val level1: Dp = 2.dp,
    val level2: Dp = 4.dp,
    val level3: Dp = 8.dp,
    val level4: Dp = 12.dp,
    val level5: Dp = 20.dp,
)

data class OpacitySnapshot(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val pressed: Float = 0.16f,
    val scrim: Float = 0.48f,
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

data class ShapesSnapshot(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 8.dp,
    val small: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 20.dp,
    val full: Dp = 9999.dp,
)

object AppDefaults {
    val spacing = SpacingSnapshot()
    val elevation = ElevationSnapshot()
    val opacity = OpacitySnapshot()
    val motion = MotionSnapshot()
    val shapes = ShapesSnapshot()
}
