package com.babytracker.designsystem.components.actionbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ActionBarDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.actionBar.containerColor
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.actionBar.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.actionBar.verticalPadding
}
