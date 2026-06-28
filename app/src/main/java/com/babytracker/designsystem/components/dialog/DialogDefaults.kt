package com.babytracker.designsystem.components.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object DialogDefaults {
    @Composable fun scrimColor(): Color = LocalAppComponentTokens.current.dialog.scrimColor
    @Composable fun scrimOpacity(): Float = LocalAppComponentTokens.current.dialog.scrimOpacity
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.dialog.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.dialog.contentColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.dialog.cornerRadius
    @Composable fun contentHorizontalPadding(): Dp = LocalAppComponentTokens.current.dialog.contentHorizontalPadding
    @Composable fun contentVerticalPadding(): Dp = LocalAppComponentTokens.current.dialog.contentVerticalPadding
    @Composable fun dividerColor(): Color = LocalAppComponentTokens.current.dialog.dividerColor
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.dialog.elevation
}
