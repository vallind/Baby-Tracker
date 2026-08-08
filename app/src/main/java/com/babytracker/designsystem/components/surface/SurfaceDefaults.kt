package com.babytracker.designsystem.components.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

@Immutable
object SurfaceDefaults {
    @Composable fun color(): Color = LocalAppComponentTokens.current.surface.color
    @Composable fun shape(): Shape = LocalAppComponentTokens.current.surface.shape
    @Composable fun tonalElevation(): Dp = 0.dp
}
