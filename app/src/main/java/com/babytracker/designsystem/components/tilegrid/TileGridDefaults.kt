package com.babytracker.designsystem.components.tilegrid

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 功能宫格默认值 — 从组件令牌读取（AppComponentTokens.tileGrid）。
 */
object TileGridDefaults {
    @Composable fun tileSize(): Dp = LocalAppComponentTokens.current.tileGrid.tileSize
    @Composable fun tileCornerRadius(): Dp = LocalAppComponentTokens.current.tileGrid.tileCornerRadius
    @Composable fun cellCornerRadius(): Dp = LocalAppComponentTokens.current.tileGrid.cellCornerRadius
    @Composable fun emojiFontSize(): TextUnit = LocalAppComponentTokens.current.tileGrid.emojiFontSize
    @Composable fun labelTextStyle(): TextStyle = LocalAppComponentTokens.current.tileGrid.labelTextStyle
    @Composable fun labelColor(): Color = LocalAppComponentTokens.current.tileGrid.labelColor
    @Composable fun labelTopGap(): Dp = LocalAppComponentTokens.current.tileGrid.labelTopGap
    @Composable fun cellVerticalPadding(): Dp = LocalAppComponentTokens.current.tileGrid.cellVerticalPadding
    @Composable fun crossAxisSpacing(): Dp = LocalAppComponentTokens.current.tileGrid.crossAxisSpacing
}
