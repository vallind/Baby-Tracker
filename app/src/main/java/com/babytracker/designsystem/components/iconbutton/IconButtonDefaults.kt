package com.babytracker.designsystem.components.iconbutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 图标按钮默认值 — 从组件令牌读取。
 *
 * 用法：
 *   AppIconButton(
 *       icon = Icons.Default.Add,
 *       onClick = { ... },
 *       tint = IconButtonDefaults.tintColor(),
 *       iconSize = IconButtonDefaults.iconSize(),
 *   )
 */
object IconButtonDefaults {
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.iconButton.iconSize
    @Composable fun tintColor(): Color = LocalAppComponentTokens.current.iconButton.tintColor
}
