package com.babytracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 统一空状态组件。所有列表页为空时调用此组件，避免散落各处的 "无数据" 文案。
 *
 * @param emoji 主题 emoji（如 "🍼"）
 * @param title 主标题
 * @param subtitle 副标题（更详细的说明）
 * @param actionText 可选行动按钮文案
 * @param onAction 可选行动按钮回调
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAction, shape = MaterialTheme.shapes.small) {
                Text(actionText)
            }
        }
    }
}

/**
 * 列表项进入动画包装器 — 错峰淡入 + 上滑。
 */
@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit,
) {
    val visible = remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 25L)
        visible.value = true
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible.value,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
            androidx.compose.animation.slideInVertically(
                animationSpec = androidx.compose.animation.core.tween(300),
                initialOffsetY = { it / 4 },
            ),
    ) {
        content()
    }
}

/**
 * 数字滚动动画辅助。
 *
 * 用法：
 *   val animated by animateIntAsState(targetValue = feedCount, label = "feed")
 *   Text(animated.toString())
 */
@Composable
fun animateNumber(target: Int): Int {
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = target,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "number",
    )
    return animated
}
