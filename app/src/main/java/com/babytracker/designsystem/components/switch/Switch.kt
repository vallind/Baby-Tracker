package com.babytracker.designsystem.components.switch

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.theme.LocalThemeColors

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val c = LocalThemeColors.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = c.primary,
            checkedThumbColor = Color.White,
            uncheckedTrackColor = c.divider,
            uncheckedThumbColor = Color.White,
        ),
    )
}
