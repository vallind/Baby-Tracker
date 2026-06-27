package com.babytracker.designsystem.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════
//  AppDefaults 快照 — 对标 Palette PaiDefaults
//  为无需 @Composable 上下文的代码提供主题 Token 访问
// ═══════════════════════════════════════════════════════════

/**
 * 间距快照 — 6 级间距默认值
 */
data class SpacingSnapshot(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
)

/**
 * 阴影快照 — 4 级阴影默认值
 */
data class ElevationSnapshot(
    val none: Dp = 0.dp,
    val card: Dp = 2.dp,
    val fab: Dp = 6.dp,
    val dialog: Dp = 8.dp,
)

/**
 * 透明度快照 — 5 级默认值
 */
data class OpacitySnapshot(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val overlay: Float = 0.32f,
    val divider: Float = 0.12f,
)

/**
 * 动效快照 — 3 级时长默认值（毫秒）
 */
data class MotionSnapshot(
    val fast: Int = 150,
    val normal: Int = 300,
    val slow: Int = 600,
)

/**
 * 圆角快照 — 6 级默认值
 */
data class ShapesSnapshot(
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val full: Dp = 9999.dp,
)

/**
 * 所有 Token 快照的聚合体（非 Composable 使用）
 *
 * 用法：
 *   val space = AppDefaults.spacing.md  // 不可覆盖，固定 16.dp
 *   // Composable 中用 CardDefaults.cornerRadius() 可覆盖
 */
object AppDefaults {
    val spacing = SpacingSnapshot()
    val elevation = ElevationSnapshot()
    val opacity = OpacitySnapshot()
    val motion = MotionSnapshot()
    val shapes = ShapesSnapshot()
}
