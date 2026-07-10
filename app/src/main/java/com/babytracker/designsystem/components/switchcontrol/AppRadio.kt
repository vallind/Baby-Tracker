package com.babytracker.designsystem.components.switchcontrol

import androidx.compose.foundation.layout.size
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults as M3RadioDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 主题化单选按钮 — 对标 Palette Radio，消费 AppComponentTokens.selectionControl。
 *
 * 用法：
 *   AppRadioButton(selected = isSelected, onClick = { selectOption(id) })
 */
@Composable
fun AppRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    enabled: Boolean = true,
    size: Dp = SelectionControlDefaults.size(),
    selectedColor: Color = SelectionControlDefaults.checkedColor(),
    unselectedColor: Color = SelectionControlDefaults.uncheckedColor(),
    disabledColor: Color = SelectionControlDefaults.disabledColor(),
    modifier: Modifier = Modifier,
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size),
        colors = M3RadioDefaults.colors(
            selectedColor = selectedColor,
            unselectedColor = unselectedColor,
            disabledSelectedColor = disabledColor,
            disabledUnselectedColor = disabledColor,
        ),
    )
}
