package com.babytracker.designsystem.components.stepper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppStepper 默认值 —— 全部读 AppComponentTokens.steps（TT-028 令牌组，此前悬空，本组件首次消费）。
 */
@Immutable
object StepperDefaults {
    @Composable fun dotSize(): Dp = LocalAppComponentTokens.current.steps.dotSize
    @Composable fun connectorThickness(): Dp = LocalAppComponentTokens.current.steps.connectorThickness
    @Composable fun activeColor(): Color = LocalAppComponentTokens.current.steps.activeColor
    @Composable fun inactiveColor(): Color = LocalAppComponentTokens.current.steps.inactiveColor
    @Composable fun completedColor(): Color = LocalAppComponentTokens.current.steps.completedColor
    @Composable fun labelStyle(): TextStyle = LocalAppComponentTokens.current.steps.labelStyle
}
