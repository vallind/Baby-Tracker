package com.babytracker.designsystem.components.surface

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

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
    color: Color = SurfaceDefaults.color(),
    shape: Shape = SurfaceDefaults.shape(),
    tonalElevation: Dp = SurfaceDefaults.tonalElevation(),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = shape,
        tonalElevation = tonalElevation,
        content = content,
    )
}
