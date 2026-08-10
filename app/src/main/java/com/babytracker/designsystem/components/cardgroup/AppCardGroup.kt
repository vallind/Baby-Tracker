package com.babytracker.designsystem.components.cardgroup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.AppCard
import io.elyon.kmp.theme.ElyonTheme

/**
 * 设置列表卡片组，统一承载连续的 AppListItem。
 */
@Composable
fun AppCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp,
        containerColor = ElyonTheme.colorScheme.surface,
        content = content,
    )
}
