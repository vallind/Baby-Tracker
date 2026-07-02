package com.babytracker.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.CardDefaults as AppCardDefaults

/**
 * 统一卡片组件 — 对标 Palette Card 组件，消费 AppComponentTokens.card
 *
 * 优先级模型：
 *   显式参数 > CardDefaults（令牌） > M3 默认值
 *
 * 用法：
 *   AppCard { Text("内容") }
 *   AppCard(cornerRadius = 12.dp, containerColor = Color.Red) { ... }
 */
@Composable
fun AppCard(
    cornerRadius: Dp = AppCardDefaults.cornerRadius(),
    elevation: Dp = AppCardDefaults.elevation(),
    containerColor: Color = AppCardDefaults.containerColor(),
    borderColor: Color = AppCardDefaults.borderColor(),
    borderWidth: Dp = AppCardDefaults.borderWidth(),
    innerPadding: Dp = AppCardDefaults.innerPadding(),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)

    Card(
        modifier = modifier.shadow(elevation = elevation, shape = shape),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        content = { androidx.compose.foundation.layout.Column(Modifier.padding(innerPadding), content = content) },
    )
}
