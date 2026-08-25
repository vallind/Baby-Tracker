package com.babytracker.designsystem.components.chatheader

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object CollapsedHeaderDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.collapsedHeader.containerColor
    @Composable fun emojiStyle(): TextStyle = LocalAppComponentTokens.current.collapsedHeader.emojiStyle
    @Composable fun titleStyle(): TextStyle = LocalAppComponentTokens.current.collapsedHeader.titleStyle
    @Composable fun titleFontWeight(): FontWeight = LocalAppComponentTokens.current.collapsedHeader.titleFontWeight
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.collapsedHeader.titleColor
    @Composable fun subtitleStyle(): TextStyle = LocalAppComponentTokens.current.collapsedHeader.subtitleStyle
    @Composable fun subtitleColor(): Color = LocalAppComponentTokens.current.collapsedHeader.subtitleColor
    @Composable fun chevronTint(): Color = LocalAppComponentTokens.current.collapsedHeader.chevronTint
    @Composable fun chevronSize(): Dp = LocalAppComponentTokens.current.collapsedHeader.chevronSize
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.collapsedHeader.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.collapsedHeader.verticalPadding
    @Composable fun emojiTitleGap(): Dp = LocalAppComponentTokens.current.collapsedHeader.emojiTitleGap
    @Composable fun titleTrailingGap(): Dp = LocalAppComponentTokens.current.collapsedHeader.titleTrailingGap
}
