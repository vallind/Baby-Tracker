package com.babytracker.ui.patterns.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 摘要卡片默认值 — 从组件令牌读取（SummaryCardTokens）。
 */
object SummaryCardDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.summaryCard.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.summaryCard.innerPadding
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.summaryCard.contentColor
    @Composable fun iconContainerAlpha(): Float = LocalAppComponentTokens.current.summaryCard.iconContainerAlpha
    @Composable fun titleAlpha(): Float = LocalAppComponentTokens.current.summaryCard.titleAlpha
    @Composable fun subtitleAlpha(): Float = LocalAppComponentTokens.current.summaryCard.subtitleAlpha
}