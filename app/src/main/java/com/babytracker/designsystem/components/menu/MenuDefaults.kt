package com.babytracker.designsystem.components.menu

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppMenu 默认值 —— 全部读 AppComponentTokens.menu（TT-025 令牌组，此前悬空，本组件首次消费）。
 */
@Immutable
object MenuDefaults {
    @Composable fun itemHeight(): Dp = LocalAppComponentTokens.current.menu.itemHeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.menu.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.menu.verticalPadding
    @Composable fun minWidth(): Dp = LocalAppComponentTokens.current.menu.minWidth
    @Composable fun cornerRadius(): Shape = RoundedCornerShape(LocalAppComponentTokens.current.menu.cornerRadius)

    /** 菜单浮层 tonal 层次 */
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.menu.elevation

    /** 选中项背景（hoverBgColor 为桌面态预留） */
    @Composable fun selectedBgColor(): Color = LocalAppComponentTokens.current.menu.selectedBgColor
}
