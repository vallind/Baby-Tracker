package com.babytracker.designsystem.components.slider

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object SliderDefaults {
    @Composable fun trackHeight(): Dp = LocalAppComponentTokens.current.slider.trackHeight
    @Composable fun thumbSize(): Dp = LocalAppComponentTokens.current.slider.thumbSize
    @Composable fun activeColor(): Color = LocalAppComponentTokens.current.slider.activeColor
    @Composable fun inactiveColor(): Color = LocalAppComponentTokens.current.slider.inactiveColor
}
