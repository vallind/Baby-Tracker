package com.babytracker.designsystem.components.fab

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.fab.FabDefaults

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
    size: Dp = FabDefaults.size(),
    iconSize: Dp = FabDefaults.iconSize(),
    cornerRadius: Dp = FabDefaults.cornerRadius(),
    elevation: Dp = FabDefaults.elevation(),
    modifier: Modifier = Modifier,
) {
    if (label != null) {
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize)) },
            text = { androidx.compose.material3.Text(label) },
            shape = RoundedCornerShape(cornerRadius),
            containerColor = FabDefaults.containerColor(),
            contentColor = FabDefaults.contentColor(),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = elevation),
            modifier = modifier,
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            shape = RoundedCornerShape(cornerRadius),
            containerColor = FabDefaults.containerColor(),
            contentColor = FabDefaults.contentColor(),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = elevation),
            modifier = modifier.size(size),
        ) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}
