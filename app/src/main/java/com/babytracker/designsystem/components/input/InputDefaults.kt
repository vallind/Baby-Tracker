package com.babytracker.designsystem.components.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object InputDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.input.height
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.input.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.input.fontSize
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.input.borderWidth
    @Composable fun borderWidthFocus(): Dp = LocalAppComponentTokens.current.input.borderWidthFocus
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.input.iconSize
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.input.containerColor
    @Composable fun focusedBorderColor(): Color = LocalAppComponentTokens.current.input.focusedBorderColor
    @Composable fun unfocusedBorderColor(): Color = LocalAppComponentTokens.current.input.unfocusedBorderColor
    @Composable fun errorBorderColor(): Color = LocalAppComponentTokens.current.input.errorBorderColor
    @Composable fun placeholderColor(): Color = LocalAppComponentTokens.current.input.placeholderColor
    @Composable fun cursorColor(): Color = LocalAppComponentTokens.current.input.cursorColor
}
