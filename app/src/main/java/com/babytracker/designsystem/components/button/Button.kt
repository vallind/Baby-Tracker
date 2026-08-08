package com.babytracker.designsystem.components.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.ButtonDefaults as AppButtonDefaults

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
    height: Dp = AppButtonDefaults.height(),
    cornerRadius: Dp = AppButtonDefaults.cornerRadius(),
    fontSize: TextUnit = AppButtonDefaults.fontSize(),
    fontWeight: FontWeight = AppButtonDefaults.fontWeight(),
    iconSize: Dp = AppButtonDefaults.iconSize(),
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    // Color.Unspecified 表示未显式传参，按变体走对应令牌；显式参数 > XxxDefaults 约定
    val resolvedContainer = if (containerColor == Color.Unspecified) AppButtonDefaults.containerColor() else containerColor
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        variant == ButtonVariant.Secondary -> AppButtonDefaults.secondaryContentColor()
        variant == ButtonVariant.Text -> AppButtonDefaults.textContentColor()
        else -> AppButtonDefaults.contentColor()
    }
    val resolvedDisabledContainer =
        if (disabledContainerColor == Color.Unspecified) AppButtonDefaults.disabledContainerColor() else disabledContainerColor
    val resolvedDisabledContent =
        if (disabledContentColor == Color.Unspecified) AppButtonDefaults.disabledContentColor() else disabledContentColor

    // 内容统一走 RowScope，兼容 M3 Button 的 content 接收者类型
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }
    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.buttonColors(
                containerColor = resolvedContainer,
                contentColor = resolvedContent,
                disabledContainerColor = resolvedDisabledContainer,
                disabledContentColor = resolvedDisabledContent,
            ),
            content = content,
        )
        ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContent,
                disabledContentColor = resolvedDisabledContent,
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
            content = content,
        )
        ButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.textButtonColors(
                contentColor = resolvedContent,
                disabledContentColor = resolvedDisabledContent,
            ),
            content = content,
        )
    }
}
