package com.babytracker.designsystem.composites.chartcontainer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 图表容器 —— 「标题行 + 图表区」的布局骨架（G 批），不含卡片外壳（外壳由调用方 AppCard 决定）。
 *
 * 空数据时以统一占位文案替代图表内容；最小高度由 minHeight 控制。
 *
 * 用法：
 *   AppChartContainer(title = "体重趋势", trailingContent = { DateNavCapsule(...) }) {
 *       LineChart(points)
 *   }
 */
@Composable
fun AppChartContainer(
    modifier: Modifier = Modifier,
    title: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    isEmpty: Boolean = false,
    emptyText: String = AppStrings.noData,
    minHeight: Dp = 120.dp,
    content: @Composable () -> Unit,
) {
    val c = LocalAppColors.current
    val typography = LocalAppTypography.current
    Column(modifier.fillMaxWidth()) {
        if (title != null || trailingContent != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (title != null) {
                    Text(title, style = typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.weight(1f))
                }
                trailingContent?.invoke()
            }
        }
        if (isEmpty) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(emptyText, style = typography.bodySmall, color = c.textTertiary)
            }
        } else {
            content()
        }
    }
}
