package com.babytracker.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 分段选择器默认值 — 从组件令牌读取。
 */
object SegmentedControlDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.segmentedControl.containerColor
    @Composable fun selectedContainerColor(): Color = LocalAppComponentTokens.current.segmentedControl.selectedContainerColor
    @Composable fun selectedContentColor(): Color = LocalAppComponentTokens.current.segmentedControl.selectedContentColor
    @Composable fun unselectedContentColor(): Color = LocalAppComponentTokens.current.segmentedControl.unselectedContentColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.segmentedControl.cornerRadius
    @Composable fun innerCornerRadius(): Dp = LocalAppComponentTokens.current.segmentedControl.innerCornerRadius
    @Composable fun borderWidth(): Dp = LocalAppComponentTokens.current.segmentedControl.borderWidth
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.segmentedControl.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.segmentedControl.fontWeight
    @Composable fun selectedFontWeight(): FontWeight = LocalAppComponentTokens.current.segmentedControl.selectedFontWeight
}
