package com.babytracker.designsystem.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 迷你折线图 — 指标卡内嵌 sparkline（自 StatsScreen 收编，全站统一规格）。
 *
 * 线下渐变面积 + 折线 + 末点圆标记；单点退化为居中圆点；
 * 默认色走 MiniChartTokens.lineColor（品牌主色），可用参数覆盖。
 *
 * 用法：
 *   MiniLineChart(points, Modifier.fillMaxWidth().height(52.dp))
 */
@Composable
fun MiniLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MiniChartDefaults.lineColor(),
) {
    val strokeWidth = MiniChartDefaults.lineStrokeWidth()
    val pointRadius = MiniChartDefaults.pointRadius()
    val areaTopAlpha = MiniChartDefaults.areaFillAlpha()
    val areaBrush = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = areaTopAlpha), Color.Transparent),
    )
    Canvas(modifier) {
        if (points.size < 2) {
            if (points.size == 1) {
                drawCircle(color = lineColor, radius = pointRadius.toPx(), center = Offset(size.width / 2, size.height / 2))
            }
            return@Canvas
        }
        val max = points.max()
        val min = points.min()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (points.size - 1)
        val dotRadius = pointRadius.toPx()
        val chartHeight = (size.height - dotRadius * 2).coerceAtLeast(0f)
        val coords = points.mapIndexed { i, v ->
            val progress = ((v - min) / range).coerceIn(0f, 1f)
            Offset(i * stepX, dotRadius + chartHeight * (1f - progress))
        }
        val areaPath = Path().apply {
            moveTo(coords.first().x, size.height)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, size.height)
            close()
        }
        drawPath(areaPath, areaBrush)
        for (i in 0 until coords.size - 1) {
            drawLine(
                color = lineColor,
                start = coords[i],
                end = coords[i + 1],
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(lineColor, dotRadius, coords.last())
    }
}
