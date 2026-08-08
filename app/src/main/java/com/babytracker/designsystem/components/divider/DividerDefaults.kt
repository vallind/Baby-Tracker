package com.babytracker.designsystem.components.divider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

@Immutable
object DividerDefaults {
    @Composable fun color(): Color = LocalAppComponentTokens.current.divider.color
    @Composable fun thickness(): Dp = LocalAppComponentTokens.current.divider.thickness
}
