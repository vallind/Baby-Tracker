package com.babytracker.designsystem.components.quickstat

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 统计胶囊默认值 — 从组件令牌读取。
 */
object QuickStatPillDefaults {
    @Composable fun valueFontSize(): TextUnit = LocalAppComponentTokens.current.quickStatPill.valueFontSize
    @Composable fun valueFontWeight(): FontWeight = LocalAppComponentTokens.current.quickStatPill.valueFontWeight
    @Composable fun unitFontSize(): TextUnit = LocalAppComponentTokens.current.quickStatPill.unitFontSize
    @Composable fun labelFontSize(): TextUnit = LocalAppComponentTokens.current.quickStatPill.labelFontSize
    @Composable fun valueAlpha(): Float = LocalAppComponentTokens.current.quickStatPill.valueAlpha
    @Composable fun unitAlpha(): Float = LocalAppComponentTokens.current.quickStatPill.unitAlpha
    @Composable fun labelAlpha(): Float = LocalAppComponentTokens.current.quickStatPill.labelAlpha
    @Composable fun innerSpacing(): Dp = LocalAppComponentTokens.current.quickStatPill.innerSpacing
}
