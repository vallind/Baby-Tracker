package com.babytracker.designsystem.components.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 底部弹层默认值 — 从组件令牌读取。
 *
 * 用法：
 *   AppBottomSheet(show = showSheet, onDismiss = { ... }) { ... }
 */
object SheetDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.sheet.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.sheet.contentColor
    @Composable fun scrimColor(): Color = LocalAppComponentTokens.current.sheet.scrimColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.sheet.cornerRadius
    @Composable fun dragHandleColor(): Color = LocalAppComponentTokens.current.sheet.dragHandleColor
    @Composable fun dragHandleWidth(): Dp = LocalAppComponentTokens.current.sheet.dragHandleWidth
    @Composable fun dragHandleHeight(): Dp = LocalAppComponentTokens.current.sheet.dragHandleHeight
}
