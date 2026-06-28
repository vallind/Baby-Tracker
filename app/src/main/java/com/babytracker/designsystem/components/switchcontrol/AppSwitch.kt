package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults as M3SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.components.switchcontrol.SwitchDefaults

/**
 * 主题化 Switch — 对标 Palette Switch，消费 AppComponentTokens.switch。
 *
 * 用法：
 *   AppSwitch(checked = isOn, onCheckedChange = { isOn = it })
 *   AppSwitch(checked = isOn, onCheckedChange = { isOn = it }, enabled = false)
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    checkedColor: Color = SwitchDefaults.checkedColor(),
    uncheckedColor: Color = SwitchDefaults.uncheckedColor(),
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = M3SwitchDefaults.colors(
            checkedThumbColor = checkedColor,
            checkedTrackColor = checkedColor.copy(alpha = 0.38f),
            uncheckedThumbColor = uncheckedColor,
            uncheckedTrackColor = uncheckedColor.copy(alpha = 0.38f),
            disabledCheckedThumbColor = checkedColor.copy(alpha = 0.38f),
            disabledCheckedTrackColor = checkedColor.copy(alpha = 0.12f),
            disabledUncheckedThumbColor = uncheckedColor.copy(alpha = 0.38f),
            disabledUncheckedTrackColor = uncheckedColor.copy(alpha = 0.12f),
        ),
    )
}
