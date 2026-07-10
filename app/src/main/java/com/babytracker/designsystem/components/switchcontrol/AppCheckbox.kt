package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults as M3CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

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
    size: Dp = SelectionControlDefaults.size(),
    checkedColor: Color = SelectionControlDefaults.checkedColor(),
    uncheckedColor: Color = SelectionControlDefaults.uncheckedColor(),
    disabledColor: Color = SelectionControlDefaults.disabledColor(),
    modifier: Modifier = Modifier,
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier.size(size),
        colors = M3CheckboxDefaults.colors(
            checkedColor = checkedColor,
            uncheckedColor = uncheckedColor,
            checkmarkColor = Color.White,
            disabledCheckedColor = disabledColor,
            disabledUncheckedColor = disabledColor,
        ),
    )
}
