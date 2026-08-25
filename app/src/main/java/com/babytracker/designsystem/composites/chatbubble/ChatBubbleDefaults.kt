package com.babytracker.designsystem.composites.chatbubble

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ChatBubbleDefaults {
    @Composable fun userContainerColor(): Color = LocalAppComponentTokens.current.chatBubble.userContainerColor
    @Composable fun assistantContainerColor(): Color = LocalAppComponentTokens.current.chatBubble.assistantContainerColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.chatBubble.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.chatBubble.innerPadding
    @Composable fun maxWidth(): Dp = LocalAppComponentTokens.current.chatBubble.maxWidth
}
