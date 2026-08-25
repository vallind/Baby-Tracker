package com.babytracker.designsystem.components.scaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppSpacing

/**
 * 二级页通用骨架 — 设置子页等二级页的「标题顶栏 + 返回 + 纵向滚动内容区」。
 *
 * 自 feature/settings 的 SettingsMenuScaffold 收编：AppScaffold + AppTopBar +
 * 垂直滚动 Column，内容区水平/垂直均为 spacing.md 内边距。
 *
 * 用法：
 *   SubPageScaffold(title = "同步设置", onBack = onBack) {
 *       AppCardGroup { ... }
 *   }
 */
@Composable
fun SubPageScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spacing = LocalAppSpacing.current
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = title,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md, vertical = spacing.md),
        ) {
            content()
        }
    }
}
