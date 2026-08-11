package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CountdownChip(
    dueDate: String?,
    modifier: Modifier = Modifier,
) {
    if (dueDate.isNullOrBlank()) return

    val colors = LocalAppColors.current
    val result = remember(dueDate) {
        val localDate = try {
            val clean = if (dueDate.length >= 10) dueDate.take(10) else return@remember null
            LocalDate.parse(clean, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (_: Exception) { return@remember null }
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, localDate).toInt()
        when {
            days < 0 -> Triple(colors.danger, colors.onError, "已过期 ${-days} 天")
            days == 0 -> Triple(colors.warning, colors.onWarning, "今天")
            days <= 7 -> Triple(colors.primary, colors.onPrimary, "还有 ${days} 天")
            else -> return@remember null
        }
    }

    if (result == null) return
    val (bgColor, contentColor, label) = result

    Text(
        label,
        color = contentColor,
        style = LocalAppTypography.current.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
