package com.babytracker.designsystem.components.badge

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object BadgeDefaults {
    @Composable fun size(): Dp = LocalAppComponentTokens.current.badge.size
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.badge.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.badge.fontSize
}
