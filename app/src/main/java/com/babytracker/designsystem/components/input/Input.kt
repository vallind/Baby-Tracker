package com.babytracker.designsystem.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.input.InputDefaults as AppInputDefaults
import com.babytracker.designsystem.components.menu.AppMenu
import com.babytracker.designsystem.components.menu.AppMenuItem
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes

/** 输入框样式轴（收敛蓝图：SearchField 不是独立组件，而是本组件的样式档） */
enum class AppInputStyle {
    /** 常规表单输入（默认） */
    Default,

    /** 搜索样式：胶囊外形 + 前置搜索图标 + 内容清空钮 + IME Search */
    Search,
}

/**
 * 输入框尺寸轴（四层 API 契约：几何一律走语义轴，禁止消费者传裸 token 参数）。
 * 三档分别映射 InputTokens 的 height/fontSize/iconSize 分档（Medium 保持历史默认）。
 */
enum class AppInputSize {
    /** 紧凑输入（48dp 高度，小字号小图标） */
    Small,

    /** 标准输入（56dp 高度，历史默认形态） */
    Medium,

    /** 宽松输入（64dp 高度，大字号大图标） */
    Large,
}

/**
 * 补全候选过滤（纯函数，可 JVM 单测）：包含匹配、忽略大小写、最多取 5 条。
 */
internal fun filterSuggestions(suggestions: List<String>, query: String): List<String> {
    if (query.isBlank()) return emptyList()
    val q = query.trim()
    return suggestions.filter { it.contains(q, ignoreCase = true) }.take(5)
}

/**
 * 统一输入框组件 —— 超级参照组件：Focus / Validation / Complex State。
 *
 * Focus 轴：聚焦/失焦边框与光标色由 InputTokens 驱动（focused/unfocused 两态）；
 * Validation 轴：isError + errorMessage 错误态，helperText 常规辅助文案；
 * Complex State 轴：密码可见性切换、单位后缀、字数计数器、IME 动作与键盘动作回调；
 * Style 轴：[AppInputStyle.Search] 胶囊搜索样式（P1：SearchField 收敛归宿）；
 * Suggestions 槽位：传入候选即得自动补全（锚定 AppMenu，过滤纯函数可单测）。
 *
 * 用法：
 *   AppInput(value = text, onValueChange = { text = it }, label = "姓名")
 *   AppInput(value = password, onValueChange = { ... }, label = "密码", isPassword = true)
 *   AppInput(value = q, onValueChange = { q = it }, label = "搜索",
 *            style = AppInputStyle.Search)
 *   AppInput(value = name, onValueChange = { ... }, label = "宝宝",
 *            suggestions = babies, onSuggestionSelected = { pick(it) })
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
    size: AppInputSize = AppInputSize.Medium,
    style: AppInputStyle = AppInputStyle.Default,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val shapes = LocalAppShapes.current
    val isSearch = style == AppInputStyle.Search
    // Size 轴：几何一律经 InputDefaults 从 InputTokens 分档取（禁止裸 token 参数）
    val height = AppInputDefaults.height(size)
    val fontSize = AppInputDefaults.fontSize(size)
    val iconSize = AppInputDefaults.iconSize(size)

    // Style 轴：搜索样式为胶囊外形（shapes.full 走圆角缩放令牌）
    val effectiveShape: Shape = if (isSearch) {
        RoundedCornerShape(shapes.scaled(shapes.full))
    } else {
        RoundedCornerShape(AppInputDefaults.cornerRadius())
    }

    // Focus 轴：suggestions 展开跟随焦点；点击候选或面板外收起
    var isFocused by remember { mutableStateOf(false) }
    var suggestionsCollapsed by remember { mutableStateOf(false) }
    val showSuggestions = isFocused && !suggestionsCollapsed &&
        onSuggestionSelected != null && filterSuggestions(suggestions, value).isNotEmpty()

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                suggestionsCollapsed = false
            },
            enabled = enabled,
            isError = isError,
            singleLine = if (isSearch) true else singleLine,
            label = { androidx.compose.material3.Text(label) },
            placeholder = placeholder?.let { { androidx.compose.material3.Text(it) } },
            // Style 轴：搜索样式默认前置搜索图标，调用方槽位优先
            leadingIcon = leadingIcon ?: if (isSearch) {
                {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(iconSize),
                    )
                }
            } else {
                null
            },
            // 密码开关 > 调用方尾槽 > 搜索清空钮（历史契约顺序不变）
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
                isSearch && value.isNotEmpty() && enabled -> {
                    {
                        AppIconButton(
                            icon = Icons.Default.Close,
                            onClick = { onValueChange("") },
                            contentDescription = AppStrings.clear,
                            iconSize = iconSize,
                        )
                    }
                }
                else -> null
            },
            // 单位后缀（如 ml/g），替代调用方手拼 Row 的旧模式
            suffix = suffix?.let { unit ->
                {
                    androidx.compose.material3.Text(
                        unit,
                        fontSize = fontSize,
                        color = AppInputDefaults.placeholderColor(),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
                    ?: if (isSearch) ImeAction.Search else ImeAction.Default,
            ),
            keyboardActions = keyboardActions,
            minLines = minLines,
            maxLines = if (isSearch) 1 else maxLines,
            shape = effectiveShape,
            modifier = modifier
                .defaultMinSize(minHeight = height)
                .onFocusEvent { state ->
                    val gained = state.isFocused
                    if (gained && !isFocused) suggestionsCollapsed = false
                    isFocused = gained
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppInputDefaults.focusedBorderColor(),
                unfocusedBorderColor = AppInputDefaults.unfocusedBorderColor(),
                errorBorderColor = AppInputDefaults.errorBorderColor(),
                focusedContainerColor = AppInputDefaults.containerColor(),
                unfocusedContainerColor = AppInputDefaults.containerColor(),
                errorContainerColor = AppInputDefaults.containerColor(),
                cursorColor = AppInputDefaults.cursorColor(),
                focusedLabelColor = AppInputDefaults.focusedBorderColor(),
                unfocusedLabelColor = AppInputDefaults.placeholderColor(),
                errorLabelColor = AppInputDefaults.errorBorderColor(),
            ),
            // Validation/Complex State 参照：错误文案 > 常规辅助 + 计数器 的三态支撑区
            supportingText = when {
                isError && errorMessage != null -> {
                    {
                        androidx.compose.material3.Text(
                            errorMessage,
                            color = AppInputDefaults.errorBorderColor(),
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
                                    color = AppInputDefaults.placeholderColor(),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (counterMaxLength != null) {
                                androidx.compose.material3.Text(
                                    AppStrings.charCounter(value.length, counterMaxLength),
                                    color = AppInputDefaults.placeholderColor(),
                                )
                            }
                        }
                    }
                }
                else -> null
            },
        )

        // Suggestions 槽位：过滤后经 AppMenu 锚定展示（复用菜单令牌与交互）
        if (onSuggestionSelected != null) {
            AppMenu(
                expanded = showSuggestions,
                items = filterSuggestions(suggestions, value).map { AppMenuItem(key = it, label = it) },
                onItemClick = { picked ->
                    onValueChange(picked)
                    onSuggestionSelected(picked)
                    suggestionsCollapsed = true
                },
                onDismiss = { suggestionsCollapsed = true },
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}
