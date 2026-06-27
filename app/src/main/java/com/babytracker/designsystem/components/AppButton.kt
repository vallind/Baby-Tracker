package com.babytracker.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.babytracker.designsystem.theme.ButtonDefaults as AppButtonDefaults
import com.babytracker.designsystem.theme.LocalThemeColors

/**
 * 主按钮（填充） — 对标 Palette Button 组件，消费 AppComponentTokens.button
 *
 * 用法：
 *   PrimaryButton(onClick = { ... }, label = "保存")
 *   PrimaryButton(onClick = { ... }, label = "搜索", icon = Icons.Default.Search)
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    label: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = AppButtonDefaults.height(),
    cornerRadius: Dp = AppButtonDefaults.cornerRadius(),
    fontSize: TextUnit = AppButtonDefaults.fontSize(),
    fontWeight: FontWeight = AppButtonDefaults.fontWeight(),
    iconSize: Dp = AppButtonDefaults.iconSize(),
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.height(height),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = Color.White,
            disabledContainerColor = c.primary.copy(alpha = AppButtonDefaults.disabledAlpha()),
            disabledContentColor = Color.White.copy(alpha = AppButtonDefaults.disabledAlpha()),
        ),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }
}

/**
 * 次按钮（描边）— 无填充，主色描边
 */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    label: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = AppButtonDefaults.height(),
    cornerRadius: Dp = AppButtonDefaults.cornerRadius(),
    fontSize: TextUnit = AppButtonDefaults.fontSize(),
    fontWeight: FontWeight = AppButtonDefaults.fontWeight(),
    iconSize: Dp = AppButtonDefaults.iconSize(),
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.height(height),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = c.primary,
            disabledContentColor = c.primary.copy(alpha = AppButtonDefaults.disabledAlpha()),
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }
}

/**
 * 文本按钮（无边框、无背景）
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    label: String,
    enabled: Boolean = true,
    fontSize: TextUnit = AppButtonDefaults.fontSize(),
    fontWeight: FontWeight = AppButtonDefaults.fontWeight(),
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = c.primary,
            disabledContentColor = c.primary.copy(alpha = AppButtonDefaults.disabledAlpha()),
        ),
    ) {
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }
}
