package com.babytracker.core.ui.components.datetimecascade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.babytracker.core.ui.components.timepicker.TimePickerLogic
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextButton
import io.elyon.kmp.theme.ElyonTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * TDesign 风格级联日期时间选择器 — 底部面板，先选日期再选时间。
 */
@Composable
fun DateTimeCascadeDialog(
    show: Boolean,
    initialDateTime: String,
    onConfirm: (dateTimeStr: String) -> Unit,
    onDismiss: () -> Unit,
    dateOnly: Boolean = false,
) {
    if (!show) return

    val c = ElyonTheme.colorScheme
    val cornerRadius = 16.dp

    var stage by remember { mutableIntStateOf(0) }
    var selectedDate by remember {
        mutableStateOf(initialDateTime.take(10))
    }
    var currentMonth by remember {
        val parsed = initialDateTime.take(10)
        mutableStateOf(
            YearMonth.of(
                parsed.substring(0, 4).toIntOrNull() ?: LocalDate.now().year,
                parsed.substring(5, 7).toIntOrNull() ?: LocalDate.now().monthValue,
            )
        )
    }
    var selectedHour by remember {
        mutableIntStateOf(initialDateTime.substring(11, 13).toIntOrNull() ?: 12)
    }
    var selectedMinute by remember {
        mutableIntStateOf(initialDateTime.substring(14, 16).toIntOrNull() ?: 0)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = cornerRadius,
                        )
                    )
                    .background(c.surface),
            ) {
                CascadeToolbar(
                    title = if (dateOnly) "选择日期" else if (stage == 0) "选择日期" else "选择时间",
                    textColor = c.primary,
                    dividerColor = c.dividerLine,
                    toolbarHeight = 48.dp,
                    onCancel = onDismiss,
                    onConfirm = {
                        when (stage) {
                            0 -> {
                                if (dateOnly) {
                                    onConfirm("$selectedDate 00:00")
                                    onDismiss()
                                } else {
                                    stage = 1
                                }
                            }
                            1 -> {
                                onConfirm(
                                    "$selectedDate %02d:%02d".format(selectedHour, selectedMinute)
                                )
                                onDismiss()
                            }
                        }
                    },
                    confirmLabel = if (stage == 0 && !dateOnly) "下一步" else "确认",
                )

                when (stage) {
                    0 -> CalendarPanel(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        c = c,
                        onDateSelected = { selectedDate = it },
                        onMonthChanged = { currentMonth = it },
                    )
                    1 -> TimePanel(
                        hour = selectedHour,
                        minute = selectedMinute,
                        c = c,
                        onHourChanged = { selectedHour = it },
                        onMinuteChanged = { selectedMinute = it },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CascadeToolbar(
    title: String,
    textColor: Color,
    dividerColor: Color,
    toolbarHeight: Dp,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(toolbarHeight)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                text = "取消",
                onClick = onCancel,
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = textColor),
            )
            Text(
                title,
                style = ElyonTheme.textStyles.title3,
                color = ElyonTheme.colorScheme.onSurface,
            )
            TextButton(
                text = confirmLabel,
                onClick = onConfirm,
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = textColor),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(dividerColor),
        )
    }
}

@Composable
private fun CalendarPanel(
    currentMonth: YearMonth,
    selectedDate: String,
    c: io.elyon.kmp.theme.Colors,
    onDateSelected: (dateStr: String) -> Unit,
    onMonthChanged: (newMonth: YearMonth) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val selectedDay = remember(selectedDate) {
        selectedDate.substring(8, 10).toIntOrNull() ?: today.dayOfMonth
    }
    val selectedYearMonth = remember(selectedDate) {
        YearMonth.of(
            selectedDate.substring(0, 4).toIntOrNull() ?: today.year,
            selectedDate.substring(5, 7).toIntOrNull() ?: today.monthValue,
        )
    }
    val isCurrentMonth = selectedYearMonth == currentMonth
    val daySize = 40.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                text = "〈",
                onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = c.primary),
            )
            Text(
                "${currentMonth.year}年 ${currentMonth.monthValue}月",
                style = ElyonTheme.textStyles.title3.copy(fontWeight = FontWeight.SemiBold),
                color = c.onSurface,
            )
            TextButton(
                text = "〉",
                onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = c.primary),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val daysOfWeek = remember { listOf("一", "二", "三", "四", "五", "六", "日") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.size(daySize),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        day,
                        style = ElyonTheme.textStyles.footnote1,
                        color = c.onSurfaceVariantSummary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val firstDayOfMonth = currentMonth.atDay(1)
        val startOffset = firstDayOfMonth.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - startOffset + 1

                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val isSelected = isCurrentMonth && day == selectedDay
                        val isToday = date == today
                        DayCell(
                            day = day,
                            isSelected = isSelected,
                            isToday = isToday,
                            daySize = daySize,
                            c = c,
                            onClick = {
                                onDateSelected(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            },
                        )
                    } else {
                        Spacer(modifier = Modifier.size(daySize))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    daySize: Dp,
    c: io.elyon.kmp.theme.Colors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(daySize)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
            }
            .then(
                if (isSelected) {
                    Modifier
                        .clip(CircleShape)
                        .background(c.primary)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$day",
            style = ElyonTheme.textStyles.body2.copy(
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = when {
                isSelected -> c.onPrimary
                isToday -> c.primary
                else -> c.onSurface
            },
        )
    }
}

@Composable
private fun TimePanel(
    hour: Int,
    minute: Int,
    c: io.elyon.kmp.theme.Colors,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "小时",
                style = ElyonTheme.textStyles.footnote2,
                color = c.onSurfaceVariantSummary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TimePickerLogic(
                value = hour,
                range = 0..23,
                itemHeight = 44.dp,
                visibleItems = 5,
                selectedBgColor = c.primary.copy(alpha = 0.12f),
                selectedTextColor = c.primary,
                unselectedTextColor = c.onSurfaceVariantSummary,
                dividerColor = c.dividerLine,
                onValueChanged = onHourChanged,
            )
        }

        Text(
            ":",
            style = ElyonTheme.textStyles.headline2.copy(fontWeight = FontWeight.Bold),
            color = c.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "分钟",
                style = ElyonTheme.textStyles.footnote2,
                color = c.onSurfaceVariantSummary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TimePickerLogic(
                value = minute,
                range = 0..59,
                itemHeight = 44.dp,
                visibleItems = 5,
                selectedBgColor = c.primary.copy(alpha = 0.12f),
                selectedTextColor = c.primary,
                unselectedTextColor = c.onSurfaceVariantSummary,
                dividerColor = c.dividerLine,
                onValueChanged = onMinuteChanged,
            )
        }
    }
}
