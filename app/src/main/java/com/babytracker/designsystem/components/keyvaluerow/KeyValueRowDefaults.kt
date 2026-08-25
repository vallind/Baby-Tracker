package com.babytracker.designsystem.components.keyvaluerow

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object KeyValueRowDefaults {
    @Composable fun rowHeight(): Dp = LocalAppComponentTokens.current.keyValueRow.rowHeight
    @Composable fun labelTextStyle(): TextStyle = LocalAppComponentTokens.current.keyValueRow.labelTextStyle
    @Composable fun labelColor(): Color = LocalAppComponentTokens.current.keyValueRow.labelColor
    @Composable fun valueTextStyle(): TextStyle = LocalAppComponentTokens.current.keyValueRow.valueTextStyle
    @Composable fun valueColor(): Color = LocalAppComponentTokens.current.keyValueRow.valueColor
    @Composable fun valueEmphasizedColor(): Color = LocalAppComponentTokens.current.keyValueRow.valueEmphasizedColor
    @Composable fun captionTextStyle(): TextStyle = LocalAppComponentTokens.current.keyValueRow.captionTextStyle
    @Composable fun captionColor(): Color = LocalAppComponentTokens.current.keyValueRow.captionColor
}
