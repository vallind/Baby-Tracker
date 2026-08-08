package com.babytracker.designsystem.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

@Immutable
object SnackbarHostDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.snackbarHost.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.snackbarHost.contentColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.snackbarHost.cornerRadius
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.snackbarHost.elevation
}
