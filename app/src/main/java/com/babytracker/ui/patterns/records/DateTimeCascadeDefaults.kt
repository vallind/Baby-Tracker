package com.babytracker.ui.patterns.records

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.DatePickerTokens
import com.babytracker.designsystem.theme.LocalAppComponentTokens
import com.babytracker.designsystem.theme.TimePickerTokens

/**
 * 级联日期时间选择器默认值 — 从组件令牌读取。
 */
object DateTimeCascadeDefaults {
    @Composable fun backgroundColor(): Color = LocalAppComponentTokens.current.dateTimeCascade.backgroundColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.dateTimeCascade.cornerRadius
    @Composable fun datePickerTokens(): DatePickerTokens = LocalAppComponentTokens.current.datePicker
    @Composable fun timePickerTokens(): TimePickerTokens = LocalAppComponentTokens.current.timePicker
}