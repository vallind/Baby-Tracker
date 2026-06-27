package com.babytracker.designsystem.foundation.border

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalThemeColors

/**
 * 带边框的容器 — 对标 Palette BorderContainer
 *
 * 用法：
 *   BorderContainer { Text("内容") }
 *   BorderContainer(borderColor = Color.Red, cornerRadius = 8.dp) { ... }
 */
@Composable
fun BorderContainer(
    borderColor: Color = LocalThemeColors.current.cardBorder,
    cornerRadius: Dp = 12.dp,
    padding: Dp = 16.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .border(width = 1.dp, color = borderColor, shape = shape)
            .background(Color.Transparent, shape)
            .padding(padding),
    ) {
        content()
    }
}
