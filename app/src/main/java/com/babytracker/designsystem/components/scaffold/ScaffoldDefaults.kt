package com.babytracker.designsystem.components.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.theme.LocalAppComponentTokens

/**
 * 脚手架默认值 — 从组件令牌读取。
 *
 * 用法：
 *   AppScaffold { ... }
 */
object ScaffoldDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.scaffold.containerColor
}
