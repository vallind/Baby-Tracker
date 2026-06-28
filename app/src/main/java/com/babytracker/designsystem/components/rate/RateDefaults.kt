package com.babytracker.designsystem.components.rate

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object RateDefaults {
    @Composable fun starSize(): Dp = LocalAppComponentTokens.current.rate.starSize
    @Composable fun starSpacing(): Dp = LocalAppComponentTokens.current.rate.starSpacing
    @Composable fun selectedColor(): Color = LocalAppComponentTokens.current.rate.selectedColor
    @Composable fun unselectedColor(): Color = LocalAppComponentTokens.current.rate.unselectedColor
}
