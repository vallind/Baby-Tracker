package com.babytracker.designsystem.components.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 分区标题默认值 — 从组件令牌读取。
 *
 * 用法：
 *   SectionHeader(title = "最近记录")
 */
object SectionHeaderDefaults {
    @Composable fun titleColor(): Color = LocalAppComponentTokens.current.listItem.titleColor
    @Composable fun actionColor(): Color = LocalAppComponentTokens.current.listItem.actionColor
    @Composable fun titleSize(): TextUnit = LocalAppComponentTokens.current.listItem.titleSize
    @Composable fun subtitleSize(): TextUnit = LocalAppComponentTokens.current.listItem.subtitleSize
}
