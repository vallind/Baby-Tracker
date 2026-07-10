package com.babytracker.designsystem.components.slider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults as M3SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 主题化滑块 — 对标 Palette Slider，消费 AppComponentTokens.slider。
 *
 * 用法：
 *   AppSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
 */
@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    steps: Int = 0,
    trackHeight: Dp = SliderDefaults.trackHeight(),
    thumbSize: Dp = SliderDefaults.thumbSize(),
    activeColor: Color = SliderDefaults.activeColor(),
    inactiveColor: Color = SliderDefaults.inactiveColor(),
    modifier: Modifier = Modifier,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        enabled = enabled,
        steps = steps,
        modifier = modifier,
        colors = M3SliderDefaults.colors(
            thumbColor = activeColor,
            activeTrackColor = activeColor,
            inactiveTrackColor = inactiveColor,
            disabledThumbColor = inactiveColor,
            disabledActiveTrackColor = activeColor.copy(alpha = 0.38f),
            disabledInactiveTrackColor = inactiveColor.copy(alpha = 0.38f),
        ),
    )
}

/**
 * 带标签的滑块 — 左侧标签 + 滑块 + 右侧数值。
 *
 * 用法：
 *   AppLabeledSlider(label = "音量", value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
 */
@Composable
fun AppLabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    valueSuffix: String = "",
    modifier: Modifier = Modifier,
) {
    val textColor = LocalAppColors.current.textPrimary

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = textColor)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (valueRange.endInclusive <= 100f) "${value.toInt()}$valueSuffix" else String.format("%.1f$valueSuffix", value),
                color = textColor.copy(alpha = 0.64f),
            )
        }
        AppSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
        )
    }
}
