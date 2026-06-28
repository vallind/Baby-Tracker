package com.babytracker.designsystem.components.iconbutton

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

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
    tint: Color = LocalAppComponentTokens.current.iconButton.tintColor,
    iconSize: Dp = LocalAppComponentTokens.current.iconButton.iconSize,
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
