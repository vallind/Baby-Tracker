package com.babytracker.designsystem.components.divider

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 分割线 — 对标 M3 HorizontalDivider，消费 AppComponentTokens.divider。
 *
 * 用法：
 *   AppDivider()                          // 使用令牌默认值
 *   AppDivider(color = c.divider, thickness = 0.5.dp)  // 显式指定
 *   AppDivider(horizontalInset = spacing.md)           // 两侧留白（内部转水平 padding）
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color(),
    thickness: Dp = DividerDefaults.thickness(),
    horizontalInset: Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = horizontalInset),
        color = color,
        thickness = thickness,
    )
}
