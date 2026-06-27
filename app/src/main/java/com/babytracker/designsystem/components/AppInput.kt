package com.babytracker.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.InputDefaults
import com.babytracker.designsystem.theme.LocalThemeColors

/**
 * 统一输入框组件 — 对标 Palette TextField，消费 AppComponentTokens.input
 *
 * 用法：
 *   AppInput(value = text, onValueChange = { text = it }, label = "姓名")
 *   AppInput(value = password, onValueChange = { ... }, label = "密码", isPassword = true)
 */
@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    height: Dp = InputDefaults.height(),
    cornerRadius: Dp = InputDefaults.cornerRadius(),
    fontSize: TextUnit = InputDefaults.fontSize(),
    borderWidth: Dp = InputDefaults.borderWidth(),
    borderWidthFocus: Dp = InputDefaults.borderWidthFocus(),
    iconSize: Dp = InputDefaults.iconSize(),
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        label = { androidx.compose.material3.Text(label) },
        placeholder = placeholder?.let { { androidx.compose.material3.Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { /* TODO: 密码可见切换由外层管理 */ }) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(iconSize))
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.height(height),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.primary,
            unfocusedBorderColor = c.cardBorder,
            errorBorderColor = c.danger,
            focusedContainerColor = c.card,
            unfocusedContainerColor = c.card,
            errorContainerColor = c.card,
            cursorColor = c.primary,
            focusedLabelColor = c.primary,
            unfocusedLabelColor = c.textSecondary,
            errorLabelColor = c.danger,
        ),
        supportingText = if (isError && errorMessage != null) {
            { androidx.compose.material3.Text(errorMessage, color = c.danger) }
        } else null,
    )
}
