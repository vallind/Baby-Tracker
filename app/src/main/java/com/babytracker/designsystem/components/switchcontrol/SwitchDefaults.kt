package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object SwitchDefaults {
    @Composable fun trackWidth(): Dp = LocalAppComponentTokens.current.switch.trackWidth
    @Composable fun trackHeight(): Dp = LocalAppComponentTokens.current.switch.trackHeight
    @Composable fun thumbSize(): Dp = LocalAppComponentTokens.current.switch.thumbSize
    @Composable fun trackCornerRadius(): Dp = LocalAppComponentTokens.current.switch.trackCornerRadius
    @Composable fun checkedColor(): Color = LocalAppComponentTokens.current.switch.checkedColor
    @Composable fun uncheckedColor(): Color = LocalAppComponentTokens.current.switch.uncheckedColor
    @Composable fun thumbElevation(): Dp = LocalAppComponentTokens.current.switch.thumbElevation
}
