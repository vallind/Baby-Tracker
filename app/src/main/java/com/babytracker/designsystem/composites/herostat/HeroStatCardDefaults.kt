package com.babytracker.designsystem.composites.herostat

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 渐变统计大卡默认值 — 从组件令牌读取（AppComponentTokens.heroStatCard）。
 */
object HeroStatCardDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.heroStatCard.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.heroStatCard.innerPadding
    @Composable fun badgeSize(): Dp = LocalAppComponentTokens.current.heroStatCard.badgeSize
    @Composable fun badgeCornerRadius(): Dp = LocalAppComponentTokens.current.heroStatCard.badgeCornerRadius
    @Composable fun iconContainerAlpha(): Float = LocalAppComponentTokens.current.heroStatCard.iconContainerAlpha
    @Composable fun titleAlpha(): Float = LocalAppComponentTokens.current.heroStatCard.titleAlpha
    @Composable fun titleStyle(): TextStyle = LocalAppComponentTokens.current.heroStatCard.titleStyle
    @Composable fun emojiStyle(): TextStyle = LocalAppComponentTokens.current.heroStatCard.emojiStyle
    @Composable fun headerGap(): Dp = LocalAppComponentTokens.current.heroStatCard.headerGap
    @Composable fun contentGap(): Dp = LocalAppComponentTokens.current.heroStatCard.contentGap
}
