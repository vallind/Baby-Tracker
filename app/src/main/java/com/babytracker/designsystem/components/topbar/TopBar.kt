package com.babytracker.designsystem.components.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.i18n.AppStrings
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.TopAppBar
import io.elyon.kmp.theme.ElyonTheme

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
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    showBack: Boolean = true,
    height: Dp = 56.dp,
    // elyon TopAppBar 使用自身字号体系，这两个参数仅保留签名兼容
    titleSize: TextUnit = 18.sp,
    titleWeight: FontWeight = FontWeight.SemiBold,
    backIconSize: Dp = 22.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val containerColor = ElyonTheme.colorScheme.primaryContainer
    val contentColor = ElyonTheme.colorScheme.onPrimaryContainer
    TopAppBar(
        title = title,
        modifier = Modifier.height(height),
        color = containerColor,
        titleColor = contentColor,
        navigationIcon = {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.back,
                        modifier = Modifier.size(backIconSize),
                        tint = contentColor,
                    )
                }
            }
        },
        actions = { actions() },
    )
}
