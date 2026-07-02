package com.babytracker.designsystem.components.input

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
import com.babytracker.designsystem.components.input.InputDefaults

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
    height: Dp = InputDefaults.height(),
    cornerRadius: Dp = InputDefaults.cornerRadius(),
    fontSize: TextUnit = InputDefaults.fontSize(),
    iconSize: Dp = InputDefaults.iconSize(),
    modifier: Modifier = Modifier,
) {
    val visualTransformation = if (isPassword && !passwordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        visualTransformation = visualTransformation,
        label = { androidx.compose.material3.Text(label) },
        placeholder = placeholder?.let { { androidx.compose.material3.Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            {
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(icon, contentDescription = if (passwordVisible) "隐藏密码" else "显示密码", modifier = Modifier.size(iconSize))
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword && !passwordVisible) KeyboardType.Password else keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.height(height),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputDefaults.focusedBorderColor(),
            unfocusedBorderColor = InputDefaults.unfocusedBorderColor(),
            errorBorderColor = InputDefaults.errorBorderColor(),
            focusedContainerColor = InputDefaults.containerColor(),
            unfocusedContainerColor = InputDefaults.containerColor(),
            errorContainerColor = InputDefaults.containerColor(),
            cursorColor = InputDefaults.cursorColor(),
            focusedLabelColor = InputDefaults.focusedBorderColor(),
            unfocusedLabelColor = InputDefaults.placeholderColor(),
            errorLabelColor = InputDefaults.errorBorderColor(),
        ),
        supportingText = if (isError && errorMessage != null) {
            { androidx.compose.material3.Text(errorMessage, color = InputDefaults.errorBorderColor()) }
        } else null,
    )
}
