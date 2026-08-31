package com.babytracker.ui.patterns.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.components.card.CardVariant
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.tintContainer

/**
 * 指标卡 —— 数据展示复合组件（G 批）：「图标+标题 | 数值列 | 图表」的标准骨架。
 *
 * 参数全部领域无关（String/Color/slot），宝宝睡眠、喂养、生长或任何未来领域都只是传值方；
 * 数值区是自由槽而非属性堆积——一行大数、多行明细、带趋势对比均由调用方组合。
 * 内壳复用 AppCard（variant 可透传），图表空态走统一占位文案。
 *
 * 用法：
 *   AppMetricCard(
 *       emoji = "🍼", title = "喂养", accentColor = c.danger,
 *       valueContent = { Text("12 次", style = typography.titleLarge, fontWeight = FontWeight.Bold) },
 *       chart = { MiniBarChart(points) }, hasChartData = points.any { it > 0f },
 *       chartEmptyText = "本周期暂无喂养记录",
 *   )
 */
@Composable
fun AppMetricCard(
    emoji: String,
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Elevated,
    valueContent: (@Composable ColumnScope.() -> Unit)? = null,
    chart: (@Composable () -> Unit)? = null,
    hasChartData: Boolean = true,
    chartHeight: Dp = 52.dp,
    chartEmptyText: String? = null,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppCard(modifier = modifier.fillMaxWidth(), variant = variant) {
        Column(Modifier.padding(spacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Box(
                        Modifier
                            .size(spacing.xl)
                            .clip(CircleShape)
                            // 小尺寸圆形徽章：底色取分档浅底（40dp 标准形态请用 AppEmojiBadge）
                            .background(AppColorScale.fromSeed(accentColor).tintContainer(c)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, style = typography.titleMedium)
                    }
                    Spacer(Modifier.height(spacing.sm))
                    Text(title, style = typography.labelMedium, color = c.textTertiary)
                }
                if (valueContent != null) {
                    Column(horizontalAlignment = Alignment.End) { valueContent() }
                }
            }
            if (chart != null) {
                Spacer(Modifier.height(12.dp))
                if (hasChartData) {
                    chart()
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            chartEmptyText ?: AppStrings.noData,
                            style = typography.labelMedium,
                            color = c.textTertiary,
                        )
                    }
                }
            }
        }
    }
}