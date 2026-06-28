package com.babytracker.designsystem.components.timepicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 时间选择器默认值 — 从组件令牌读取。
 */
object TimePickerDefaults {
    @Composable fun hourColor(): Color = LocalAppComponentTokens.current.timePicker.hourColor
    @Composable fun minuteColor(): Color = LocalAppComponentTokens.current.timePicker.minuteColor
    @Composable fun separatorColor(): Color = LocalAppComponentTokens.current.timePicker.separatorColor
    @Composable fun labelColor(): Color = LocalAppComponentTokens.current.timePicker.labelColor
    @Composable fun arrowColor(): Color = LocalAppComponentTokens.current.timePicker.arrowColor
    @Composable fun backgroundColor(): Color = LocalAppComponentTokens.current.timePicker.backgroundColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.timePicker.cornerRadius
    // TDesign 新增字段
    @Composable fun selectedBackgroundColor(): Color = LocalAppComponentTokens.current.timePicker.selectedBackgroundColor
    @Composable fun selectedTextColor(): Color = LocalAppComponentTokens.current.timePicker.selectedTextColor
    @Composable fun unselectedTextColor(): Color = LocalAppComponentTokens.current.timePicker.unselectedTextColor
    @Composable fun dividerColor(): Color = LocalAppComponentTokens.current.timePicker.dividerColor
    @Composable fun itemHeight(): Dp = LocalAppComponentTokens.current.timePicker.itemHeight
    @Composable fun visibleItems(): Int = LocalAppComponentTokens.current.timePicker.visibleItems
    @Composable fun toolbarHeight(): Dp = LocalAppComponentTokens.current.timePicker.toolbarHeight
    @Composable fun toolbarTextColor(): Color = LocalAppComponentTokens.current.timePicker.toolbarTextColor
    @Composable fun toolbarDividerColor(): Color = LocalAppComponentTokens.current.timePicker.toolbarDividerColor
}
