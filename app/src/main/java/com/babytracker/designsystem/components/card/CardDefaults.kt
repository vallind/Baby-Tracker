package com.babytracker.designsystem.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object CardDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.card.containerColor
    @Composable fun borderColor(): Color = LocalAppComponentTokens.current.card.borderColor
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.card.borderWidth
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.card.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.card.innerPadding
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.card.elevation
}
