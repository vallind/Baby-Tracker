package com.babytracker.core.ui.components.iconbutton

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.theme.ElyonTheme

/**
 * 主题化图标按钮 — 对标 Palette IconButton 组件
 *
 * 消除各处重复的 IconButton + tint 样板。
 *
 * 用法：
 *   AppIconButton(icon = Icons.Default.Add, onClick = { showForm = true })
 *   AppIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = { navController.popBackStack() })
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    tint: Color = ElyonTheme.colorScheme.onSurfaceSecondary,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}
