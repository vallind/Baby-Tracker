package com.babytracker.ui.patterns.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 指标趋势标签 —— 数值下方「较上周期 +x / -x」的着色对比文本
 * （自 StatsScreen.StatCompareLabel 收编，与 AppMetricCard 同族配套）。
 *
 * 约定：以 "+" 开头视为正向增长（success 色），其余（含负值/持平）为中性次级色。
 *
 * 用法：
 *   AppMetricCard(
 *       valueContent = {
 *           Text("8 次", ...)
 *           MetricTrendLabel("+2")
 *       },
 *   )
 */
@Composable
fun MetricTrendLabel(
    compare: String,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val isPositive = compare.startsWith("+")
    Text(
        compare,
        style = typography.labelMedium,
        color = if (isPositive) c.success else c.textSecondary,
        modifier = modifier.padding(top = spacing.xs),
    )
}