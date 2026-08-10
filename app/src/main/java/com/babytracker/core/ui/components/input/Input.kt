package com.babytracker.core.ui.components.input

import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.i18n.AppStrings
import io.elyon.kmp.theme.ElyonTheme

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
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    height: Dp = 48.dp,
    cornerRadius: Dp = 12.dp,
    fontSize: TextUnit = 15.sp,
    borderWidth: Dp = 1.dp,
    borderWidthFocus: Dp = 2.dp,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    // TODO: 迁移到 Elyon TextField（Elyon 当前无 error 态/支持文本，迁移前保留 M3）
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
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) AppStrings.hidePassword else AppStrings.showPassword,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.defaultMinSize(minHeight = height),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElyonTheme.colorScheme.primary,
            unfocusedBorderColor = ElyonTheme.colorScheme.outline,
            errorBorderColor = ElyonTheme.colorScheme.error,
            focusedContainerColor = ElyonTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = ElyonTheme.colorScheme.surfaceContainer,
            errorContainerColor = ElyonTheme.colorScheme.surfaceContainer,
            cursorColor = ElyonTheme.colorScheme.primary,
            focusedLabelColor = ElyonTheme.colorScheme.primary,
            unfocusedLabelColor = ElyonTheme.colorScheme.onSurfaceVariantSummary,
            errorLabelColor = ElyonTheme.colorScheme.error,
        ),
        supportingText = if (isError && errorMessage != null) {
            { androidx.compose.material3.Text(errorMessage, color = ElyonTheme.colorScheme.error) }
        } else null,
    )
}
