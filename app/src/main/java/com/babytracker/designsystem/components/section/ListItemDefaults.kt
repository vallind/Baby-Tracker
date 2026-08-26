package com.babytracker.designsystem.components.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ListItemDefaults {
    /** 行高随密度档位：Compact 紧凑行 / Regular 常规行（密度轴参照实现，映射组件令牌） */
    @Composable fun minHeight(density: ListItemDensity): Dp = when (density) {
        ListItemDensity.Compact -> LocalAppComponentTokens.current.listItem.compactMinHeight
        ListItemDensity.Regular -> LocalAppComponentTokens.current.listItem.minHeight
    }

    /** 原单参重载保留：等价 Regular 档，存量调用不受影响 */
    @Composable fun minHeight(): Dp = minHeight(ListItemDensity.Regular)

    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.listItem.horizontalPadding

    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.listItem.verticalPadding

    /** leading 槽与文本列间距 */
    @Composable fun itemGap(): Dp = LocalAppComponentTokens.current.listItem.itemGap

    @Composable fun dividerThickness(): Dp = LocalAppComponentTokens.current.listItem.dividerThickness

    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.listItem.iconSize
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.listItem.titleSize
    @Composable fun subtitleSize(): TextUnit = LocalAppComponentTokens.current.listItem.subtitleSize
    @Composable fun dividerAlpha(): Float = LocalAppComponentTokens.current.listItem.dividerAlpha
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.listItem.titleColor
    @Composable fun subtitleColor(): Color = LocalAppComponentTokens.current.listItem.subtitleColor
    @Composable fun dividerColor(): Color = LocalAppComponentTokens.current.listItem.dividerColor
    @Composable fun actionColor(): Color = LocalAppComponentTokens.current.listItem.actionColor

    // D 批状态轴
    @Composable fun selectedContainerColor(): Color = LocalAppComponentTokens.current.listItem.selectedContainerColor
    @Composable fun disabledAlpha(): Float = LocalAppComponentTokens.current.listItem.disabledAlpha
}
