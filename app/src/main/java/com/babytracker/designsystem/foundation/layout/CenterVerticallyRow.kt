package com.babytracker.designsystem.foundation.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 垂直居中行 — 对标 Palette CenterVerticallyRow
 *
 * 简化 Row(verticalAlignment = Alignment.CenterVertically) 样板。
 *
 * 用法：
 *   CenterVerticallyRow { Icon(...); Text(...) }
 *   CenterVerticallyRow(modifier = Modifier.fillMaxWidth()) { ... }
 */
@Composable
fun CenterVerticallyRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
