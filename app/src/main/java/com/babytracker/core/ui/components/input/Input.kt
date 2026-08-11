package com.babytracker.core.ui.components.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.babytracker.i18n.AppStrings
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextField
import io.elyon.kmp.basic.TextFieldDefaults
import io.elyon.kmp.theme.ElyonTheme

/**
 * 统一输入框组件 — Elyon TextField 封装。
 *
 * Elyon TextField 暂无原生 error 态/独立 placeholder，因此：
 * - label 兼作 placeholder（空态内嵌、聚焦后浮动）；
 * - error 态通过 errorContainer 背景 + error 边框/标签 + 下方错误文案表达。
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
    modifier: Modifier = Modifier,
) {
    val c = ElyonTheme.colorScheme
    Column(modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = if (isError) c.errorContainer else c.secondaryContainer,
                labelColor = if (isError) c.error else c.onSecondaryContainer,
                borderColor = if (isError) c.error else c.primary,
            ),
            label = label,
            // Elyon 无独立 placeholder，label 空态内嵌、聚焦后浮动，符合 Elyon 输入框风格
            useLabelAsPlaceholder = true,
            enabled = enabled,
            textStyle = ElyonTheme.textStyles.main,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordToggle?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) AppStrings.hidePassword else AppStrings.showPassword,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        )
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                errorMessage,
                style = ElyonTheme.textStyles.footnote2,
                color = c.error,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
