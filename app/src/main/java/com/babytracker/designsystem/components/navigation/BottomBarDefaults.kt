package com.babytracker.designsystem.components.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/** 底部导航栏（AppNavigationBar）组件令牌读取 */
object BottomBarDefaults {
    @Composable fun height(): Dp = LocalAppComponentTokens.current.bottomBar.height
    @Composable fun iconSize(): Dp = LocalAppComponentTokens.current.bottomBar.iconSize
    @Composable fun labelSize(): TextUnit = LocalAppComponentTokens.current.bottomBar.labelSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.bottomBar.fontWeight
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.bottomBar.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.bottomBar.contentColor
    @Composable fun selectedColor(): Color = LocalAppComponentTokens.current.bottomBar.selectedColor
    @Composable fun unselectedColor(): Color = LocalAppComponentTokens.current.bottomBar.unselectedColor
    @Composable fun indicatorColor(): Color = LocalAppComponentTokens.current.bottomBar.indicatorColor
}