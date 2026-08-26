package com.babytracker.designsystem.components.input

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.input.InputDefaults
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 统一输入框组件 —— 超级参照组件：Focus / Validation / Complex State。
 *
 * Focus 轴：聚焦/失焦边框与光标色由 InputTokens 驱动（focused/unfocused 两态）；
 * Validation 轴：isError + errorMessage 错误态，helperText 常规辅助文案；
 * Complex State 轴：密码可见性切换、单位后缀、字数计数器、IME 动作与键盘动作回调。
 *
 * 用法：
 *   AppInput(value = text, onValueChange = { text = it }, label = "姓名")
 *   AppInput(value = password, onValueChange = { ... }, label = "密码", isPassword = true)
 *   AppInput(value = note, onValueChange = { ... }, label = "备注",
 *            helperText = "记录宝宝今天的表现", counterMaxLength = 140,
 *            imeAction = ImeAction.Done)
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
    helperText: String? = null,
    counterMaxLength: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    suffix: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    height: Dp = InputDefaults.height(),
    cornerRadius: Dp = InputDefaults.cornerRadius(),
    fontSize: TextUnit = InputDefaults.fontSize(),
    borderWidth: Dp = InputDefaults.borderWidth(),
    borderWidthFocus: Dp = InputDefaults.borderWidthFocus(),
    iconSize: Dp = InputDefaults.iconSize(),
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        label = { androidx.compose.material3.Text(label) },
        placeholder = placeholder?.let { { androidx.compose.material3.Text(it) } },
        leadingIcon = leadingIcon,
        // 密码开关优先（历史契约）；其余场景走通用尾槽（如清除按钮）
        trailingIcon = when {
            isPassword -> {
                {
                    AppIconButton(
                        icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        onClick = { onPasswordToggle?.invoke() },
                        contentDescription = if (passwordVisible) AppStrings.hidePassword else AppStrings.showPassword,
                        iconSize = iconSize,
                    )
                }
            }
            trailingIcon != null -> trailingIcon
            else -> null
        },
        // 单位后缀（如 ml/g），替代调用方手拼 Row 的旧模式
        suffix = suffix?.let { unit ->
            {
                androidx.compose.material3.Text(
                    unit,
                    fontSize = fontSize,
                    color = InputDefaults.placeholderColor(),
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction ?: ImeAction.Default,
        ),
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.defaultMinSize(minHeight = height),
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
        // Validation/Complex State 参照：错误文案 > 常规辅助 + 计数器 的三态支撑区
        supportingText = when {
            isError && errorMessage != null -> {
                {
                    androidx.compose.material3.Text(
                        errorMessage,
                        color = InputDefaults.errorBorderColor(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            helperText != null || counterMaxLength != null -> {
                {
                    Row(Modifier.fillMaxWidth()) {
                        if (helperText != null) {
                            androidx.compose.material3.Text(
                                helperText,
                                color = InputDefaults.placeholderColor(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (counterMaxLength != null) {
                            androidx.compose.material3.Text(
                                AppStrings.charCounter(value.length, counterMaxLength),
                                color = InputDefaults.placeholderColor(),
                            )
                        }
                    }
                }
            }
            else -> null
        },
    )
}
