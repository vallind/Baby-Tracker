package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.Checkbox
import io.elyon.kmp.basic.CheckboxDefaults
import io.elyon.kmp.theme.ElyonTheme

/**
 * 主题化复选框 — 对标 Palette Checkbox，消费 AppComponentTokens.selectionControl。
 *
 * 用法：
 *   AppCheckbox(checked = isChecked, onCheckedChange = { isChecked = it })
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    size: Dp = 20.dp,
    checkedColor: Color = ElyonTheme.colorScheme.primary,
    uncheckedColor: Color = ElyonTheme.colorScheme.onSecondary,
    disabledColor: Color = ElyonTheme.colorScheme.disabledPrimary,
    modifier: Modifier = Modifier,
) {
    Checkbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = { onCheckedChange?.invoke(!checked) },
        enabled = enabled,
        modifier = modifier,
        colors = CheckboxDefaults.checkboxColors(
            checkedBackgroundColor = checkedColor,
            uncheckedBackgroundColor = uncheckedColor,
            disabledCheckedBackgroundColor = disabledColor,
            disabledUncheckedBackgroundColor = disabledColor,
        ),
    )
}
