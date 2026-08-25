package com.babytracker.designsystem.components.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 迷你图表默认值 — 从组件令牌读取。
 */
object MiniChartDefaults {
    @Composable fun barGapRatio(): Float = LocalAppComponentTokens.current.miniChart.barGapRatio
    @Composable fun barDimmedAlpha(): Float = LocalAppComponentTokens.current.miniChart.barDimmedAlpha
    @Composable fun lineStrokeWidth(): Dp = LocalAppComponentTokens.current.miniChart.lineStrokeWidth
    @Composable fun pointRadius(): Dp = LocalAppComponentTokens.current.miniChart.pointRadius
    @Composable fun areaFillAlpha(): Float = LocalAppComponentTokens.current.miniChart.areaFillAlpha
    @Composable fun lineColor(): Color = LocalAppComponentTokens.current.miniChart.lineColor
}
