package com.babytracker.designsystem.components.pininput

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.pininput.PinInputDefaults as AppPinDefaults
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 输入净化（纯函数，可 JVM 单测）：仅保留数字并截断到 length。
 * 收敛蓝图：Pin / OTP / CodeInput 三者收敛为本组件的 length + obscure 参数。
 */
internal fun sanitizePin(raw: String, length: Int): String =
    raw.filter { it.isDigit() }.take(length)

/**
 * 验证码输入 —— P1 组件，单组件吸收 PinInput / OTPInput / CodeInput。
 *
 * State 轴：待输入格高亮 focused 边框（宽度走 connectorThickness 同级 2dp 语义，
 *   颜色读 InputTokens focused/error），已填格常规描边；
 * obscure 掩码：OTP 场景以 • 回显；填满瞬间回调 onComplete（不重复触发）；
 * 语义：透明承载字段带 AppStrings 读屏描述，格子对读屏合并静默；点击任意格聚焦。
 *
 * 用法：
 *   var code by remember { mutableStateOf("") }
 *   AppPinInput(
 *       value = code,
 *       onValueChange = { code = it },
 *       onComplete = { verify(code) },
 *       length = 6,
 *       obscure = true,
 *   )
 */
@Composable
fun AppPinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 4,
    obscure: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    autoFocus: Boolean = false,
    onComplete: (() -> Unit)? = null,
) {
    require(length >= 1) { "length 必须 ≥ 1" }
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    val focusRequester = remember { FocusRequester() }

    if (autoFocus && enabled) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    Box(
        modifier = modifier.then(
            if (enabled) {
                Modifier.clickable { focusRequester.requestFocus() }
            } else {
                Modifier
            }
        ),
    ) {
        // ── 绘制层：length 个格子 ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(LocalAppSpacing.current.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(length) { index ->
                val char = value.getOrNull(index)
                // 待输入位：已填数量所指的下一格
                val isCaretCell = enabled && index == value.length
                val borderColor = when {
                    isError -> AppPinDefaults.errorBorderColor()
                    isCaretCell -> AppPinDefaults.focusedBorderColor()
                    else -> AppPinDefaults.unfocusedBorderColor()
                }
                Box(
                    modifier = Modifier
                        .size(AppPinDefaults.cellSize())
                        .clip(AppPinDefaults.cellRadius())
                        .background(AppPinDefaults.containerColor())
                        .border(
                            width = if (isCaretCell || isError) 2.dp else 1.dp,
                            color = borderColor,
                            shape = AppPinDefaults.cellRadius(),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when {
                            char == null -> ""
                            obscure -> "•"
                            else -> char.toString()
                        },
                        style = typography.headlineSmall,
                        color = if (enabled) {
                            AppPinDefaults.contentColor()
                        } else {
                            AppPinDefaults.disabledContentColor()
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // ── 输入层：透明字段覆盖全部格子，承接焦点与键盘 ──
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val next = sanitizePin(raw, length)
                val wasFull = value.length >= length
                onValueChange(next)
                if (!wasFull && next.length == length) onComplete?.invoke()
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .semantics { contentDescription = AppStrings.pinCodeField },
        )
    }
}
