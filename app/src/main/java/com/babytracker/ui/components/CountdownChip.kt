package com.babytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    val result = remember(dueDate) {
        val localDate = try {
            val clean = if (dueDate.length >= 10) dueDate.take(10) else return@remember null
            LocalDate.parse(clean, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (_: Exception) { return@remember null }
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, localDate).toInt()
        when {
            days < 0 -> Pair(Color(0xFFE53935), "已过期 ${-days} 天")
            days == 0 -> Pair(Color(0xFFF59E0B), "今天")
            days <= 7 -> Pair(Color(0xFF4285F4), "还有 ${days} 天")
            else -> return@remember null
        }
    }

    if (result == null) return
    val (bgColor, label) = result

    Text(
        label,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
