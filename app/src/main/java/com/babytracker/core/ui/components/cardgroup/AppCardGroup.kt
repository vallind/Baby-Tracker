package com.babytracker.core.ui.components.cardgroup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Card
import io.elyon.kmp.basic.CardDefaults
import io.elyon.kmp.theme.ElyonTheme

/**
 * 设置列表卡片组，统一承载连续的 AppListItem。
 */
@Composable
fun AppCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        colors = CardDefaults.defaultColors(color = ElyonTheme.colorScheme.surface),
        content = content,
    )
}
