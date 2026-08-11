package com.babytracker.core.ui.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Button
import io.elyon.kmp.basic.ButtonDefaults
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.Text
import io.elyon.kmp.theme.ElyonTheme

/** 按钮变体：Primary 填充 / Secondary 描边 / Text 文本 */
enum class ButtonVariant { Primary, Secondary, Text }

/**
 * 统一按钮 — 变体走枚举参数，替代 PrimaryButton/SecondaryButton/AppTextButton。
 *
 * 用法：
 *   AppButton(label = "保存", onClick = { ... })
 *   AppButton(label = "取消", variant = ButtonVariant.Secondary, onClick = { ... })
 *   AppButton(label = "复制", variant = ButtonVariant.Text, icon = Icons.Default.ContentCopy, onClick = { ... })
 */
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val c = ElyonTheme.colorScheme
    // 显式传参优先，否则按变体走 Elyon 默认角色色
    val resolvedContainer = when {
        containerColor != Color.Unspecified -> containerColor
        variant == ButtonVariant.Primary -> c.primary
        variant == ButtonVariant.Secondary -> c.secondaryVariant
        else -> Color.Transparent
    }
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        variant == ButtonVariant.Primary -> c.onPrimary
        else -> c.primary
    }
    val resolvedDisabledContainer = when {
        disabledContainerColor != Color.Unspecified -> disabledContainerColor
        variant == ButtonVariant.Primary -> c.disabledPrimaryButton
        else -> c.disabledSecondaryVariant
    }
    val resolvedDisabledContent = when {
        disabledContentColor != Color.Unspecified -> disabledContentColor
        variant == ButtonVariant.Primary -> c.disabledOnPrimaryButton
        else -> c.disabledOnSecondaryVariant
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            color = resolvedContainer,
            disabledColor = resolvedDisabledContainer,
            contentColor = resolvedContent,
            disabledContentColor = resolvedDisabledContent,
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            label,
            style = ElyonTheme.textStyles.button,
        )
    }
}
