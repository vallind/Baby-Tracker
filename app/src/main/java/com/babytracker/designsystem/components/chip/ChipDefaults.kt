package com.babytracker.designsystem.components.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    @Composable fun backgroundColor(): Color = LocalAppComponentTokens.current.chip.backgroundColor
    @Composable fun textColor(): Color = LocalAppComponentTokens.current.chip.textColor
    // B 批交互胶囊：选中态颜色组
    @Composable fun selectedContainerColor(): Color = LocalAppComponentTokens.current.chip.selectedContainerColor
    @Composable fun selectedTextColor(): Color = LocalAppComponentTokens.current.chip.selectedTextColor
}
