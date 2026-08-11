package com.babytracker.core.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Card
import io.elyon.kmp.basic.CardDefaults as ElyonCardDefaults
import io.elyon.kmp.theme.ElyonTheme

/**
 * 统一卡片组件 — 对标 Palette Card 组件，消费 AppComponentTokens.card
 *
 * 优先级模型：
 *   显式参数 > CardDefaults（令牌） > M3 默认值
 *
 * 对齐 Elyon 示例：卡片默认扁平无阴影（elevation 参数保留兼容，但不再应用 shadow）。
 *
 * 用法：
 *   AppCard { Text("内容") }
 *   AppCard(cornerRadius = 12.dp, containerColor = Color.Red) { ... }
 */
@Composable
fun AppCard(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 0.dp,
    containerColor: Color = ElyonTheme.colorScheme.surfaceContainer,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Card(
        modifier = modifier
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, shape)
                } else {
                    Modifier
                },
            ),
        cornerRadius = cornerRadius,
        colors = ElyonCardDefaults.defaultColors(color = containerColor),
        content = content,
    )
}
