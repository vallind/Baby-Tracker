package com.babytracker.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
//  核心语义令牌 — 基于 Palette 令牌驱动架构
//  对标 PaletteColors / PaletteSpacing / PaletteElevation /
//       PaletteOpacity / PaletteMotion
// ═══════════════════════════════════════════════════════════

// —— 间距令牌（6 级） ——
data class AppSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
)

// —— 阴影令牌（4 级） ——
data class AppElevation(
    val none: Dp = 0.dp,
    val card: Dp = 2.dp,
    val fab: Dp = 6.dp,
    val dialog: Dp = 8.dp,
)

// —— 透明度令牌（5 级） ——
data class AppOpacity(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val hover: Float = 0.08f,
    val overlay: Float = 0.32f,
    val divider: Float = 0.12f,
)

// —— 动效令牌（3 级） ——
data class AppMotion(
    val fast: Int = 150,
    val normal: Int = 300,
    val slow: Int = 600,
)

// —— 圆角令牌（对标 AppShapes，与 M3 Shapes 互补） ——
data class AppShapes(
    val xs: Dp = 8.dp,     // 小元素（图标背景）
    val sm: Dp = 12.dp,    // 输入框
    val md: Dp = 16.dp,    // 卡片
    val lg: Dp = 24.dp,    // 大卡片
    val xl: Dp = 32.dp,    // 弹窗/底部面板
    val full: Dp = 9999.dp, // 胶囊按钮/药丸
)

// ═══════════════════════════════════════════════════════════
//  CompositionLocal 注入（对标 Palette 的 11 个 Local）
// ═══════════════════════════════════════════════════════════

val LocalAppSpacing = compositionLocalOf { AppSpacing() }
val LocalAppElevation = compositionLocalOf { AppElevation() }
val LocalAppOpacity = compositionLocalOf { AppOpacity() }
val LocalAppMotion = compositionLocalOf { AppMotion() }
val LocalAppShapes = compositionLocalOf { AppShapes() }
