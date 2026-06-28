package com.babytracker.designsystem.components.topbar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.topbar.TopBarDefaults

/**
 * 统一导航栏组件 — 对标 Palette AppBar 组件，消费 AppComponentTokens.appBar
 *
 * 自动应用主题色（primaryContainer 背景），带返回箭头 + 标题。
 *
 * 用法：
 *   Scaffold(topBar = { AppTopBar(title = "喂养记录", onBack = { navController.popBackStack() }) }) { ... }
 *   // 不需要返回按钮时
 *   AppTopBar(title = "首页", showBack = false)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    showBack: Boolean = true,
    height: Dp = TopBarDefaults.height(),
    titleSize: TextUnit = TopBarDefaults.titleSize(),
    titleWeight: FontWeight = TopBarDefaults.titleWeight(),
    backIconSize: Dp = TopBarDefaults.backIconSize(),
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontSize = titleSize,
                fontWeight = titleWeight,
                color = TopBarDefaults.titleColor(),
            )
        },
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(backIconSize),
                        tint = TopBarDefaults.iconColor(),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TopBarDefaults.containerColor(),
            titleContentColor = TopBarDefaults.titleColor(),
            navigationIconContentColor = TopBarDefaults.iconColor(),
        ),
    )
}
