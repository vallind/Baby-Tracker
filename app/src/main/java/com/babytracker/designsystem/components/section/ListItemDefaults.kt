package com.babytracker.designsystem.components.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ListItemDefaults {
    @Composable fun minHeight(): Dp = LocalAppComponentTokens.current.listItem.minHeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.listItem.horizontalPadding
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.listItem.iconSize
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.listItem.titleSize
    @Composable fun subtitleSize(): TextUnit = LocalAppComponentTokens.current.listItem.subtitleSize
    @Composable fun dividerAlpha(): Float = LocalAppComponentTokens.current.listItem.dividerAlpha
}
