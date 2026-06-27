package com.babytracker.designsystem.components.button

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 最简化按钮工厂（TT-037）— 自动从 Defaults 填充参数。
 *
 * 用法：
 *   PaiButton("保存", onClick = { vm.save() })
 *   PaiButton("编辑", icon = Icons.Default.Edit, onClick = { ... })
 *   PaiButton("删除", variant = ButtonVariant.SECONDARY, onClick = { ... })
 */

enum class ButtonVariant { PRIMARY, SECONDARY, TEXT }

@Composable
fun PaiButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    modifier: Modifier = Modifier,
) {
    when (variant) {
        ButtonVariant.PRIMARY -> PrimaryButton(
            onClick = onClick,
            label = text,
            icon = icon,
            enabled = enabled,
            modifier = modifier,
        )

        ButtonVariant.SECONDARY -> SecondaryButton(
            onClick = onClick,
            label = text,
            icon = icon,
            enabled = enabled,
            modifier = modifier,
        )

        ButtonVariant.TEXT -> AppTextButton(
            onClick = onClick,
            label = text,
            enabled = enabled,
            modifier = modifier,
        )
    }
}
