package com.babytracker.designsystem.components.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppSurface 默认值 —— 全部读 AppComponentTokens.surface，禁止硬编码（审计守门）。
 */
@Immutable
object SurfaceDefaults {
    @Composable fun color(): Color = LocalAppComponentTokens.current.surface.color
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.surface.contentColor
    @Composable fun shape(): Shape = LocalAppComponentTokens.current.surface.shape
    @Composable fun tonalElevation(): Dp = LocalAppComponentTokens.current.surface.tonalElevation
    @Composable fun shadowElevation(): Dp = LocalAppComponentTokens.current.surface.shadowElevation
    @Composable fun borderColor(): Color = LocalAppComponentTokens.current.surface.borderColor
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.surface.borderWidth
}
