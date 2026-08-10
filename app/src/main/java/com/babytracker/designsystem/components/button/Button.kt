package com.babytracker.designsystem.components.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    height: Dp = 44.dp,
    cornerRadius: Dp = 24.dp,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    iconSize: Dp = 20.dp,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    // Color.Unspecified 表示未显式传参，按变体走 Elyon 主题色
    val resolvedContainer = if (containerColor == Color.Unspecified) {
        if (variant == ButtonVariant.Secondary) Color.Transparent else ElyonTheme.colorScheme.primary
    } else {
        containerColor
    }
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        variant == ButtonVariant.Secondary -> ElyonTheme.colorScheme.primary
        variant == ButtonVariant.Text -> ElyonTheme.colorScheme.primary
        else -> ElyonTheme.colorScheme.onPrimary
    }
    val resolvedDisabledContainer =
        if (disabledContainerColor == Color.Unspecified) Color.Transparent else disabledContainerColor
    val resolvedDisabledContent =
        if (disabledContentColor == Color.Unspecified) ElyonTheme.colorScheme.disabledOnPrimary else disabledContentColor

    // 内容统一走 RowScope，兼容 Elyon Button 的 content 接收者类型
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(
            label,
            style = ElyonTheme.textStyles.main.copy(fontSize = fontSize, fontWeight = fontWeight),
        )
    }
    val shape = RoundedCornerShape(cornerRadius)
    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .then(
                if (variant == ButtonVariant.Secondary) {
                    Modifier.border(1.dp, ElyonTheme.colorScheme.outline, shape)
                } else {
                    Modifier
                },
            ),
        enabled = enabled,
        cornerRadius = cornerRadius,
        colors = ButtonDefaults.buttonColors(
            color = resolvedContainer,
            disabledColor = resolvedDisabledContainer,
            contentColor = resolvedContent,
            disabledContentColor = resolvedDisabledContent,
        ),
        content = content,
    )
}
