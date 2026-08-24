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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.components.button.ButtonDefaults as AppButtonDefaults

/** 按钮变体：Primary 填充 / Secondary 描边 / Text 文本 / Danger 破坏性填充 */
enum class ButtonVariant { Primary, Secondary, Text, Danger }

/** 按钮尺寸档位：映射 AppControlTokens.small / medium / large（默认 Medium = 48dp 基准） */
enum class ButtonSize { Small, Medium, Large }

/**
 * 统一按钮 — 语义 API（Batch 5 收紧）。
 *
 * variant + size 决定全部视觉（颜色 / 圆角 / 字号 / 图标尺寸），细节由 ButtonTokens 派生，
 * 组件参数不再暴露 token 覆盖项：业务代码禁止绕过 Design Token（审计测试守门）。
 *
 * 用法：
 *   AppButton(label = "保存", onClick = { ... })
 *   AppButton(label = "取消", variant = ButtonVariant.Secondary, onClick = { ... })
 *   AppButton(label = "删除", variant = ButtonVariant.Danger, onClick = { ... })
 *   AppButton(label = "复制", variant = ButtonVariant.Text, icon = Icons.Default.ContentCopy, onClick = { ... })
 */
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    modifier: Modifier = Modifier,
) {
    val height: Dp = AppButtonDefaults.height(size)
    val cornerRadius = AppButtonDefaults.cornerRadius()
    val fontSize: TextUnit = AppButtonDefaults.fontSize(size)
    val fontWeight = AppButtonDefaults.fontWeight()
    val iconSize = AppButtonDefaults.iconSize(size)

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
                containerColor = AppButtonDefaults.containerColor(),
                contentColor = AppButtonDefaults.contentColor(),
                disabledContainerColor = AppButtonDefaults.disabledContainerColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            content = content,
        )
        ButtonVariant.Danger -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppButtonDefaults.dangerContainerColor(),
                contentColor = AppButtonDefaults.dangerContentColor(),
                disabledContainerColor = AppButtonDefaults.disabledContainerColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
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
                contentColor = AppButtonDefaults.secondaryContentColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
            content = content,
        )
        ButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.textButtonColors(
                contentColor = AppButtonDefaults.textContentColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            content = content,
        )
    }
}