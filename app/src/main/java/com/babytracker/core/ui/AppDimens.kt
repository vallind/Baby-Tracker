package com.babytracker.core.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 应用级间距/圆角常量（替代旧设计系统令牌，直接以 dp 定义）。
 * 屏幕迁移到 Elyon 后继续使用，不属于设计系统。
 */
object AppSpacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
}

object AppShapes {
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val largeIncreased: Dp = 20.dp
    val extraLarge: Dp = 28.dp
    val extraLargeIncreased: Dp = 32.dp
    val extraExtraLarge: Dp = 48.dp
    val full: Dp = 50.dp
}
