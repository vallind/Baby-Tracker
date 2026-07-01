package com.babytracker.designsystem.components.skeleton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object SkeletonDefaults {
    @Composable fun shimmerColor1(): Color = LocalAppComponentTokens.current.skeleton.shimmerColor1
    @Composable fun shimmerColor2(): Color = LocalAppComponentTokens.current.skeleton.shimmerColor2
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.skeleton.cornerRadius
    @Composable fun avatarSize(): Dp = LocalAppComponentTokens.current.skeleton.avatarSize
    @Composable fun shimmerDurationMs(): Int = LocalAppComponentTokens.current.skeleton.shimmerDurationMs
}
