package com.babytracker.designsystem.components.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ProgressDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.progress.height
    @Composable fun circularSize(): Dp = LocalAppComponentTokens.current.progress.circularSize
    @Composable fun strokeWidth(): Dp = LocalAppComponentTokens.current.progress.strokeWidth
    @Composable fun trackColor(): Color = LocalAppComponentTokens.current.progress.trackColor
    @Composable fun indicatorColor(): Color = LocalAppComponentTokens.current.progress.indicatorColor
}
