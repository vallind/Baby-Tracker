package com.babytracker.designsystem.components.fab

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object FabDefaults {
    @Composable fun size(): Dp = LocalAppComponentTokens.current.fab.size
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.fab.iconSize
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.fab.cornerRadius
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.fab.elevation
}
