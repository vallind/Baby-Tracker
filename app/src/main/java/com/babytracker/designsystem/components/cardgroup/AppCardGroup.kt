package com.babytracker.designsystem.components.cardgroup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation

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
        elevation = LocalAppElevation.current.level2,
        containerColor = LocalAppColors.current.surface,
        content = content,
    )
}
