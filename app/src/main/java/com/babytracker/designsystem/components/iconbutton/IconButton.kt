package com.babytracker.designsystem.components.iconbutton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.iconbutton.IconButtonDefaults as AppIconButtonDefaults

/**
 * 图标按钮变体：
 *   Standard  无底（历史默认形态，仅着色图标）
 *   Filled    主色填充圆底
 *   Tonal     次级容器底
 *   Outlined  描边
 */
enum class IconButtonVariant { Standard, Filled, Tonal, Outlined }

/**
 * 统一图标按钮 — 消费 AppComponentTokens.iconButton
 *
 * 三轴：variant × 尺寸（容器尺寸随 iconSize 令牌派生）× enabled。
 * tint 仅 Standard 变体可自定义；带底变体颜色组由令牌决定。
 *
 * 用法：
 *   AppIconButton(icon = Icons.Default.Add, onClick = { showForm = true })
 *   AppIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = { navController.popBackStack() })
 *   AppIconButton(icon = Icons.Default.Check, onClick = {...}, variant = IconButtonVariant.Filled)
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    variant: IconButtonVariant = IconButtonVariant.Standard,
    enabled: Boolean = true,
    tint: Color = AppIconButtonDefaults.contentColorFor(variant),
    iconSize: Dp = AppIconButtonDefaults.iconSize(),
) {
    val sizedIcon: @Composable () -> Unit = {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
    when (variant) {
        IconButtonVariant.Standard -> IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
        ) { sizedIcon() }
        IconButtonVariant.Filled -> FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.size(AppIconButtonDefaults.containerSize(iconSize)),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AppIconButtonDefaults.filledContainerColor(),
                contentColor = AppIconButtonDefaults.filledContentColor(),
            ),
        ) { sizedIcon() }
        IconButtonVariant.Tonal -> FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.size(AppIconButtonDefaults.containerSize(iconSize)),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = AppIconButtonDefaults.tonalContainerColor(),
                contentColor = AppIconButtonDefaults.tonalContentColor(),
            ),
        ) { sizedIcon() }
        IconButtonVariant.Outlined -> OutlinedIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.size(AppIconButtonDefaults.containerSize(iconSize)),
            colors = IconButtonDefaults.outlinedIconButtonColors(
                contentColor = AppIconButtonDefaults.outlinedContentColor(),
            ),
            border = BorderStroke(1.dp, AppIconButtonDefaults.outlinedBorderColor()),
        ) { sizedIcon() }
    }
}
