package com.babytracker.designsystem.components.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppTypography

@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    chipLabel: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    val shape = RoundedCornerShape(shapes.scaled(shapes.small))
    val bgColor = if (selected) c.primaryContainer else Color.Transparent
    val chipBorder = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, c.outline)
    Row(
        modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(chipBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(4.dp))
        }
        Text(chipLabel, style = LocalAppTypography.current.labelSmall, color = if (selected) c.primary else c.textSecondary)
    }
}
