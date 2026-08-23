package com.babytracker.designsystem.components.statcell

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object StatCellDefaults {
    @Composable fun valueColor(): Color = LocalAppComponentTokens.current.statCell.valueColor
    @Composable fun unitColor(): Color = LocalAppComponentTokens.current.statCell.unitColor
    @Composable fun labelColor(): Color = LocalAppComponentTokens.current.statCell.labelColor
    @Composable fun valueFontSize(): TextUnit = LocalAppComponentTokens.current.statCell.valueFontSize
    @Composable fun valueFontWeight(): FontWeight = LocalAppComponentTokens.current.statCell.valueFontWeight
    @Composable fun unitFontSize(): TextUnit = LocalAppComponentTokens.current.statCell.unitFontSize
    @Composable fun labelFontSize(): TextUnit = LocalAppComponentTokens.current.statCell.labelFontSize
    @Composable fun emojiFontSize(): TextUnit = LocalAppComponentTokens.current.statCell.emojiFontSize
    @Composable fun innerSpacing(): Dp = LocalAppComponentTokens.current.statCell.innerSpacing
}
