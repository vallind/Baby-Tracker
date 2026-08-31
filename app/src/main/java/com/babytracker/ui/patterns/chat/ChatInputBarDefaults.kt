package com.babytracker.ui.patterns.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ChatInputBarDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.chatInputBar.containerColor
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.chatInputBar.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.chatInputBar.verticalPadding
    @Composable fun fieldActionGap(): Dp = LocalAppComponentTokens.current.chatInputBar.fieldActionGap
}