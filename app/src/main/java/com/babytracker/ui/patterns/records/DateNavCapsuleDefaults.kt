package com.babytracker.ui.patterns.records

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 日期导航胶囊默认值 — 从组件令牌读取（显式参数 > XxxDefaults > 令牌约定）。
 */
object DateNavCapsuleDefaults {
    @Composable fun capsuleColor(): Color = LocalAppComponentTokens.current.dateNavCapsule.capsuleColor
    @Composable fun textColor(): Color = LocalAppComponentTokens.current.dateNavCapsule.textColor
    @Composable fun iconColor(): Color = LocalAppComponentTokens.current.dateNavCapsule.iconColor
    @Composable fun todayContainerColor(): Color = LocalAppComponentTokens.current.dateNavCapsule.todayContainerColor
    @Composable fun todayContentColor(): Color = LocalAppComponentTokens.current.dateNavCapsule.todayContentColor
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.dateNavCapsule.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.dateNavCapsule.verticalPadding
    @Composable fun capsuleShape(): Shape = RoundedCornerShape(LocalAppComponentTokens.current.dateNavCapsule.cornerRadius)
}