package com.babytracker.designsystem.foundation.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 水平布局原语 —— 子项间距统一走 spacing 令牌，替代各页手写 Arrangement.spacedBy(…) 样板。
 *
 * @param wrap true 时自动换行（FlowRow），适合标签组/筛选组等不定长内容
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppRow(
    modifier: Modifier = Modifier,
    spacing: Dp = LocalAppSpacing.current.md,
    wrap: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    if (wrap) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
        ) { content() }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
            verticalAlignment = verticalAlignment,
        ) { content() }
    }
}

/**
 * 垂直布局原语 —— 子项间距统一走 spacing 令牌（与 AppRow 对偶）。
 */
@Composable
fun AppColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = LocalAppSpacing.current.md,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing),
        horizontalAlignment = horizontalAlignment,
    ) { content() }
}
