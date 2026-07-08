package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.theme.CareType
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalCareTypePalette
import com.babytracker.core.domain.model.Reminder
import com.babytracker.feature.home.ActiveCareState

@Composable
fun HomeStatusCard(
    activeCare: ActiveCareState?,
    upcomingReminder: Reminder?,
    onPauseCare: () -> Unit,
    onCompleteCare: () -> Unit,
    onDismissReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    val typography = LocalAppTypography.current

    AppCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        cornerRadius = shapes.medium,
        autoPadding = false,
    ) {
        if (activeCare != null) {
            val palette = LocalCareTypePalette.current.of(activeCare.type)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(palette.container.copy(alpha = 0.08f), RoundedCornerShape(shapes.medium))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(shapes.small))
                        .background(palette.container.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(palette.emoji, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("正在${activeCare.label}", style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    Spacer(Modifier.height(2.dp))
                    val min = activeCare.elapsedSeconds / 60
                    val sec = activeCare.elapsedSeconds % 60
                    Text("已进行 ${min}分${sec}秒", style = typography.bodyMedium, color = c.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton(
                        onClick = onPauseCare,
                        label = "暂停",
                        fontWeight = FontWeight.Medium,
                    )
                    PrimaryButton(
                        onClick = onCompleteCare,
                        label = "完成",
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else if (upcomingReminder != null) {
            val palette = LocalCareTypePalette.current.of(CareType.REMINDER)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(palette.container, RoundedCornerShape(shapes.medium))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(shapes.small))
                        .background(palette.content.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(palette.emoji, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(upcomingReminder.title, style = typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text("即将到期", style = typography.bodyMedium, color = c.textSecondary)
                }
                PrimaryButton(
                    onClick = onDismissReminder,
                    label = "知道了",
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
