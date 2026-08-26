package com.babytracker.designsystem.components.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppTimeline 默认值 —— 全部读 AppComponentTokens.timeline（P1 新注册令牌组）。
 */
@Immutable
object TimelineDefaults {
    @Composable fun lineWidth(): Dp = LocalAppComponentTokens.current.timeline.lineWidth

    /** 常规节点尺寸 */
    @Composable fun dotSize(): Dp = LocalAppComponentTokens.current.timeline.dotSize

    /** 高亮节点（当前/最新）尺寸 */
    @Composable fun activeDotSize(): Dp = LocalAppComponentTokens.current.timeline.activeDotSize

    @Composable fun lineColor(): Color = LocalAppComponentTokens.current.timeline.lineColor
    @Composable fun dotColor(): Color = LocalAppComponentTokens.current.timeline.dotColor
    @Composable fun activeDotColor(): Color = LocalAppComponentTokens.current.timeline.activeDotColor
}
