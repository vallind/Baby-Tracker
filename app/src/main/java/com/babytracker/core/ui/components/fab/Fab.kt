package com.babytracker.core.ui.components.fab

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.FloatingActionButton
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.Text
import io.elyon.kmp.theme.ElyonTheme

/**
 * 浮动操作按钮 — 对标 Palette FAB，消费 AppComponentTokens.fab
 *
 * 用法：
 *   AppFAB(icon = Icons.Default.Add, onClick = { showForm = true })
 *   AppFAB(icon = Icons.Default.Add, onClick = { ... }, label = "添加记录")
 */
@Composable
fun AppFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    label: String? = null,
    contentDescription: String? = null,
    size: Dp = 56.dp,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 4.dp,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        containerColor = ElyonTheme.colorScheme.primary,
        shadowElevation = elevation,
        minWidth = if (label == null) size else 120.dp,
        minHeight = size,
    ) {
        if (label != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
                Spacer(Modifier.width(8.dp))
                Text(label, color = ElyonTheme.colorScheme.onPrimary)
            }
        } else {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}
