package com.babytracker.designsystem.components.divider

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 分割线 — 对标 M3 HorizontalDivider，消费 AppComponentTokens.divider。
 *
 * 用法：
 *   AppDivider()                          // 使用令牌默认值
 *   AppDivider(color = c.divider, thickness = 0.5.dp)  // 显式指定
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color(),
    thickness: Dp = DividerDefaults.thickness(),
) {
    HorizontalDivider(modifier = modifier, color = color, thickness = thickness)
}
