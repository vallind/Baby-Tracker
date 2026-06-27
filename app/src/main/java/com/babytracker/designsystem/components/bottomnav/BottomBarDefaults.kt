package com.babytracker.designsystem.components.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object BottomBarDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.bottomBar.height
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.bottomBar.iconSize
    @Composable fun labelSize(): TextUnit = LocalAppComponentTokens.current.bottomBar.labelSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.bottomBar.fontWeight
}
