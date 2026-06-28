package com.babytracker.designsystem.components.topbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object TopBarDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.appBar.height
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.appBar.titleSize
    @Composable fun titleWeight(): FontWeight = LocalAppComponentTokens.current.appBar.titleWeight
    @Composable fun backIconSize(): Dp = LocalAppComponentTokens.current.appBar.backIconSize
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.appBar.containerColor
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.appBar.titleColor
    @Composable fun iconColor(): Color = LocalAppComponentTokens.current.appBar.iconColor
}
