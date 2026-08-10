package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Switch
import io.elyon.kmp.basic.SwitchDefaults
import io.elyon.kmp.theme.ElyonTheme

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
    checkedColor: Color = ElyonTheme.colorScheme.primary,
    uncheckedColor: Color = ElyonTheme.colorScheme.onSecondary,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.switchColors(
            checkedThumbColor = checkedColor,
            uncheckedThumbColor = uncheckedColor,
            checkedTrackColor = checkedColor,
            uncheckedTrackColor = uncheckedColor,
        ),
    )
}
