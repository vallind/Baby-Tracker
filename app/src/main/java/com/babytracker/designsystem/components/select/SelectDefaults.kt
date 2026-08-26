package com.babytracker.designsystem.components.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * AppSelect 默认值 —— 全部读 AppComponentTokens.select（TT-020 令牌组，此前悬空，本组件首次消费）。
 */
@Immutable
object SelectDefaults {
    @Composable fun menuItemHeight(): Dp = LocalAppComponentTokens.current.select.menuItemHeight
    @Composable fun maxHeight(): Dp = LocalAppComponentTokens.current.select.maxHeight
    @Composable fun itemHorizontalPadding(): Dp = LocalAppComponentTokens.current.select.itemHorizontalPadding
    @Composable fun itemVerticalPadding(): Dp = LocalAppComponentTokens.current.select.itemVerticalPadding

    /** 选中项底色 */
    @Composable fun selectedBgColor(): Color = LocalAppComponentTokens.current.select.selectedBgColor
    @Composable fun dividerColor(): Color = LocalAppComponentTokens.current.select.dividerColor
}
