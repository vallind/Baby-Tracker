package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object SelectionControlDefaults {
    @Composable fun size(): Dp = LocalAppComponentTokens.current.selectionControl.size
    @Composable fun strokeWidth(): Dp = LocalAppComponentTokens.current.selectionControl.strokeWidth
    @Composable fun checkedColor(): Color = LocalAppComponentTokens.current.selectionControl.checkedColor
    @Composable fun uncheckedColor(): Color = LocalAppComponentTokens.current.selectionControl.uncheckedColor
    @Composable fun disabledColor(): Color = LocalAppComponentTokens.current.selectionControl.disabledColor
    @Composable fun animationDurationMs(): Int = LocalAppComponentTokens.current.selectionControl.animationDurationMs
}
