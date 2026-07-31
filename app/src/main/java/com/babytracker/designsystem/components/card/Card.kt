package com.babytracker.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.CardDefaults as AppCardDefaults
import com.babytracker.designsystem.components.pressScale

/**
 * 统一卡片组件 — 对标 Palette Card 组件，消费 AppComponentTokens.card
 *
 * 优先级模型：
 *   显式参数 > CardDefaults（令牌） > M3 默认值
 *
 * 用法：
 *   AppCard { Text("内容") }
 *   AppCard(cornerRadius = 12.dp, containerColor = Color.Red) { ... }
 *   AppCard(onClick = { ... }) { ... }   // 可点击卡片（内置按压反馈）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    cornerRadius: Dp = AppCardDefaults.cornerRadius(),
    elevation: Dp = AppCardDefaults.elevation(),
    containerColor: Color = AppCardDefaults.containerColor(),
    borderColor: Color = AppCardDefaults.borderColor(),
    borderWidth: Dp = AppCardDefaults.borderWidth(),
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val pressModifier = if (onClick != null) Modifier.pressScale() else Modifier
    val cardModifier = pressModifier.then(modifier).shadow(elevation = elevation, shape = shape)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevationSpec = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null
    if (onClick != null) {
        // material3 1.4 的 clickable Card 重载要求非空 onClick，只能分支调用
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            elevation = elevationSpec,
            colors = colors,
            border = border,
            content = content,
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            elevation = elevationSpec,
            colors = colors,
            border = border,
            content = content,
        )
    }
}
