package com.babytracker.core.ui.components.switchcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.elyon.kmp.basic.RadioButton
import io.elyon.kmp.basic.RadioButtonDefaults
import io.elyon.kmp.theme.ElyonTheme

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
    size: Dp = 20.dp,
    selectedColor: Color = ElyonTheme.colorScheme.primary,
    unselectedColor: Color = ElyonTheme.colorScheme.onSecondary,
    disabledColor: Color = ElyonTheme.colorScheme.disabledPrimary,
    modifier: Modifier = Modifier,
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = RadioButtonDefaults.radioButtonColors(
            selectedColor = selectedColor,
            disabledSelectedColor = disabledColor,
        ),
    )
}
