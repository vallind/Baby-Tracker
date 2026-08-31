package com.babytracker.designsystem.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

@Immutable
object EmptyStateDefaults {
    @Composable fun emojiSize(): TextUnit = LocalAppComponentTokens.current.emptyState.emojiSize
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.emptyState.titleColor
    @Composable fun subtitleColor(): Color = LocalAppComponentTokens.current.emptyState.subtitleColor
    @Composable fun actionSpacing(): Dp = LocalAppComponentTokens.current.emptyState.actionSpacing
}
