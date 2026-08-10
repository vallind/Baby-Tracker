package com.babytracker.core.ui.components.surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Surface
import io.elyon.kmp.theme.ElyonTheme

/**
 * 表面容器 — 对标 M3 Surface，消费 AppComponentTokens.surface。
 *
 * 用法：
 *   AppSurface { ... }                                      // 使用令牌默认值
 *   AppSurface(color = c.primaryContainer, shape = ...) { }  // 显式指定
 */
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    color: Color = ElyonTheme.colorScheme.surface,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = shape,
        shadowElevation = tonalElevation,
        content = content,
    )
}
