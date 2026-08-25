package com.babytracker.designsystem.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.ButtonDefaults as AppButtonDefaults

/**
 * 按钮变体（完整 type 轴，对标 M3 + Ant 语义）：
 *   Primary  主操作填充（每屏最多一个视觉主按钮）
 *   Tonal    次级容器底填充（比 Primary 弱一档的实心操作）
 *   Outline  描边（原 Secondary 改名）
 *   Ghost    纯文本（原 Text 改名）
 *   Danger   破坏性操作填充
 */
enum class ButtonVariant { Primary, Tonal, Outline, Ghost, Danger }

/** 按钮尺寸档位：映射 AppControlTokens.small / medium / large（默认 Medium = 48dp 基准） */
enum class ButtonSize { Small, Medium, Large }

/**
 * 统一按钮 — 语义 API（Batch 5 收紧，B 批补全三轴）。
 *
 * 三轴模型：
 *   视觉轴：variant × size
 *   交互轴：onClick / enabled / loading（加载中锁交互并显示转圈）/ selected（选中高亮：
 *           Outline/Ghost 内容与描边转主色，Tonal 升为主色填充；Primary/Danger 已是强色不叠加）
 *
 * variant + size 决定全部视觉（颜色/圆角/字号/图标尺寸），细节由 ButtonTokens 派生，
 * 组件参数不暴露 token 覆盖项：业务代码禁止绕过 Design Token（审计测试守门）。
 *
 * 用法：
 *   AppButton(label = "保存", onClick = { ... })
 *   AppButton(label = "取消", variant = ButtonVariant.Outline, onClick = { ... })
 *   AppButton(label = "删除", variant = ButtonVariant.Danger, onClick = { ... })
 *   AppButton(label = "复制", variant = ButtonVariant.Ghost, icon = Icons.Default.ContentCopy, onClick = { ... })
 *   AppButton(label = "保存中…", loading = true, onClick = { ... })
 */
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    modifier: Modifier = Modifier,
) {
    val height: Dp = AppButtonDefaults.height(size)
    val cornerRadius = AppButtonDefaults.cornerRadius()
    val fontSize: TextUnit = AppButtonDefaults.fontSize(size)
    val fontWeight = AppButtonDefaults.fontWeight()
    val iconSize = AppButtonDefaults.iconSize(size)
    // 加载态：锁交互（复用 M3 disabled 通道），转圈替代前导图标
    val interactive = enabled && !loading
    // 各变体前景色：加载圈取色与 selected 高亮共用一套语义
    val foregroundColor = when (variant) {
        ButtonVariant.Primary -> AppButtonDefaults.contentColor()
        ButtonVariant.Danger -> AppButtonDefaults.dangerContentColor()
        ButtonVariant.Tonal -> if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.tonalContentColor()
        ButtonVariant.Outline -> if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.secondaryContentColor()
        ButtonVariant.Ghost -> if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.textContentColor()
    }
    // 内容统一走 RowScope，兼容 M3 Button 的 content 接收者类型（lessons #12）
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize * 0.8f),
                strokeWidth = 2.dp,
                color = foregroundColor,
            )
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = interactive,
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
        ButtonVariant.Tonal -> Button(
            onClick = onClick,
            enabled = interactive,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.tonalContainerColor(),
                contentColor = if (selected) AppButtonDefaults.contentColor() else AppButtonDefaults.tonalContentColor(),
                disabledContainerColor = AppButtonDefaults.disabledContainerColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            content = content,
        )
        ButtonVariant.Danger -> Button(
            onClick = onClick,
            enabled = interactive,
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
        ButtonVariant.Outline -> OutlinedButton(
            onClick = onClick,
            enabled = interactive,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.secondaryContentColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            border = if (selected) {
                BorderStroke(1.dp, AppButtonDefaults.containerColor())
            } else {
                ButtonDefaults.outlinedButtonBorder(enabled = interactive)
            },
            content = content,
        )
        ButtonVariant.Ghost -> TextButton(
            onClick = onClick,
            enabled = interactive,
            modifier = modifier,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (selected) AppButtonDefaults.containerColor() else AppButtonDefaults.textContentColor(),
                disabledContentColor = AppButtonDefaults.disabledContentColor(),
            ),
            content = content,
        )
    }
}
