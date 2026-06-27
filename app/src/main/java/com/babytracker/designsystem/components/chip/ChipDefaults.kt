package com.babytracker.designsystem.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ChipDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.chip.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.chip.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.chip.fontWeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.chip.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.chip.verticalPadding
}
