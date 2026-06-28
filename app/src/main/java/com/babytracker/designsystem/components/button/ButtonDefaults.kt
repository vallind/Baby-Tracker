package com.babytracker.designsystem.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ButtonDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.button.height
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.button.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.button.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.button.fontWeight
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.button.iconSize
    @Composable fun disabledAlpha(): Float = LocalAppComponentTokens.current.button.disabledAlpha
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.button.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.button.contentColor
    @Composable fun disabledContainerColor(): Color = LocalAppComponentTokens.current.button.disabledContainerColor
    @Composable fun disabledContentColor(): Color = LocalAppComponentTokens.current.button.disabledContentColor
}
