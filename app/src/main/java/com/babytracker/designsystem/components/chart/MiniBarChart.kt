package com.babytracker.designsystem.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.theme.LocalAppShapes

/**
 * 迷你柱状图 — 指标卡内嵌 sparkline（自 StatsScreen 收编，全站统一规格）。
 *
 * 峰值柱实色、其余降透明（透明度走 MiniChartTokens），圆角随 shapes.extraSmall；
 * 全零序列直接留白（空态文案由 AppMetricCard 的 hasChartData 机制承担）。
 *
 * 用法：
 *   MiniBarChart(points, Modifier.fillMaxWidth().height(52.dp), barColor = c.danger)
 */
@Composable
fun MiniBarChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MiniChartDefaults.lineColor(),
) {
    val shapes = LocalAppShapes.current
    val gapRatio = MiniChartDefaults.barGapRatio()
    val dimmedAlpha = MiniChartDefaults.barDimmedAlpha()
    Canvas(modifier) {
        if (points.none { it > 0f }) return@Canvas

        val maxVal = points.max().coerceAtLeast(1f)
        val barCount = points.size
        val totalBars = barCount + (barCount - 1) * gapRatio
        val barWidth = size.width / totalBars
        val gapWidth = barWidth * gapRatio
        val barCornerRadius = shapes.extraSmall.toPx()

        points.forEachIndexed { i, v ->
            if (v <= 0f) return@forEachIndexed
            val barHeight = ((v / maxVal).coerceIn(0f, 1f) * size.height)
            val x = i * (barWidth + gapWidth)
            val y = size.height - barHeight
            val radius = minOf(barCornerRadius, barWidth / 2f, barHeight / 2f)
            drawRoundRect(
                color = barColor.copy(alpha = if (v == maxVal) 1f else dimmedAlpha),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}
