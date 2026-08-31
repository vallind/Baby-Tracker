package com.babytracker.ui.patterns.records

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 记录详情弹层默认值 — 从组件令牌读取。
 */
object RecordDetailSheetDefaults {
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.recordDetailSheet.titleSize
    @Composable fun titleWeight(): FontWeight = LocalAppComponentTokens.current.recordDetailSheet.titleWeight
    @Composable fun labelColor(): Color = LocalAppComponentTokens.current.recordDetailSheet.labelColor
    @Composable fun valueColor(): Color = LocalAppComponentTokens.current.recordDetailSheet.valueColor
    @Composable fun labelWidth(): Dp = LocalAppComponentTokens.current.recordDetailSheet.labelWidth
    @Composable fun rowSpacing(): Dp = LocalAppComponentTokens.current.recordDetailSheet.rowSpacing
    @Composable fun deleteColor(): Color = LocalAppComponentTokens.current.recordDetailSheet.deleteColor
    @Composable fun actionSpacing(): Dp = LocalAppComponentTokens.current.recordDetailSheet.actionSpacing
}