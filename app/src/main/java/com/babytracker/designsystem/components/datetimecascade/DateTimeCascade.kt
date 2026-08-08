package com.babytracker.designsystem.components.datetimecascade

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.timepicker.TimePickerLogic
import com.babytracker.designsystem.theme.DatePickerTokens
import com.babytracker.designsystem.theme.TimePickerTokens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * TDesign 风格级联日期时间选择器 — 底部面板，先选日期再选时间。
 *
 * 替代原 M3 DatePickerDialog + AlertDialog 组合。
 *
 * 视觉特征（参照 TDesign Mobile DateTimePicker）：
 * - 全屏半透明遮罩 + 底部弹出面板
 * - 日历面板：月/年头 + 左右箭头切换 + 日期网格（圆形选中标记）
 * - 时间面板：滚轮式时/分选择
 * - 两步流程：日历 → 点击"下一步" → 时间滚轮 → 确认
 *
 * @param show 是否显示
 * @param initialDateTime 初始日期时间字符串，格式 "yyyy-MM-dd HH:mm"
 * @param onConfirm 确认回调，返回完整的 "yyyy-MM-dd HH:mm" 字符串
 * @param onDismiss 取消回调
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

    val tokens = DateTimeCascadeDefaults
    val dpTokens = tokens.datePickerTokens()
    val tpTokens = tokens.timePickerTokens()

    // 阶段 0=日期选择，1=时间选择
    var stage by remember { mutableIntStateOf(0) }
    // 选中的日期
    var selectedDate by remember {
        mutableStateOf(initialDateTime.take(10))
    }
    // 当前显示的月份
    var currentMonth by remember {
        val parsed = initialDateTime.take(10)
        mutableStateOf(
            YearMonth.of(
                parsed.substring(0, 4).toIntOrNull() ?: LocalDate.now().year,
                parsed.substring(5, 7).toIntOrNull() ?: LocalDate.now().monthValue,
            )
        )
    }
    // 时间
    var selectedHour by remember {
        mutableIntStateOf(initialDateTime.substring(11, 13).toIntOrNull() ?: 12)
    }
    var selectedMinute by remember {
        mutableIntStateOf(initialDateTime.substring(14, 16).toIntOrNull() ?: 0)
    }

    // 全屏 Dialog 确保覆盖在所有内容之上（包括 ModalBottomSheet）
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,  // 全屏宽度
            decorFitsSystemWindows = false,   // 延伸到状态栏/导航栏
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
                            topStart = tokens.cornerRadius(),
                            topEnd = tokens.cornerRadius(),
                        )
                    )
                    .background(tokens.backgroundColor()),
            ) {
                // 顶部工具栏
                CascadeToolbar(
                    title = if (dateOnly) "选择日期" else if (stage == 0) "选择日期" else "选择时间",
                    textColor = dpTokens.toolbarTextColor,
                    dividerColor = dpTokens.toolbarDividerColor,
                    toolbarHeight = dpTokens.toolbarHeight,
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
                        tokens = dpTokens,
                        onDateSelected = { selectedDate = it },
                        onMonthChanged = { currentMonth = it },
                    )
                    1 -> TimePanel(
                        hour = selectedHour,
                        minute = selectedMinute,
                        tokens = tpTokens,
                        onHourChanged = { selectedHour = it },
                        onMinuteChanged = { selectedMinute = it },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 顶部工具栏。
 */
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
            TextButton(onClick = onCancel) {
                Text("取消", color = textColor, fontSize = 15.sp)
            }
            Text(
                title,
                style = LocalAppTypography.current.titleMedium,
                color = LocalAppColors.current.onSurface,
            )
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(dividerColor),
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  日历面板 — TDesign 风格
// ═══════════════════════════════════════════════════════════

/**
 * TDesign 风格日历面板。
 *
 * 特征：
 * - 月/年标题 + 左右箭头切换
 * - 星期缩写行（一二三四五六日）
 * - 日期网格：选中日期圆形品牌色高亮，今天文字品牌色
 */
@Composable
private fun CalendarPanel(
    currentMonth: YearMonth,
    selectedDate: String,  // "yyyy-MM-dd"
    tokens: DatePickerTokens,
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // 月/年标题 + 箭头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左箭头
            TextButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Text("〈", color = tokens.navArrowColor, fontSize = 18.sp)
            }

            Text(
                "${currentMonth.year}年 ${currentMonth.monthValue}月",
                style = LocalAppTypography.current.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = tokens.monthYearTextColor,
            )

            // 右箭头
            TextButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Text("〉", color = tokens.navArrowColor, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 星期标题
        val daysOfWeek = remember {
            listOf("一", "二", "三", "四", "五", "六", "日")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.size(tokens.daySize),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        day,
                        style = LocalAppTypography.current.bodySmall,
                        color = tokens.weekHeaderColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 日期网格
        val firstDayOfMonth = currentMonth.atDay(1)
        // ISO dayOfWeek: 1=Monday ... 7=Sunday
        val startOffset = firstDayOfMonth.dayOfWeek.value - 1  // 0=Monday
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
                            tokens = tokens,
                            onClick = {
                                onDateSelected(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                            },
                        )
                    } else {
                        Spacer(modifier = Modifier.size(tokens.daySize))
                    }
                }
            }
        }
    }
}

/**
 * 日期单元格 — TDesign 风格圆形选中。
 */
@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    tokens: DatePickerTokens,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(tokens.daySize)
            .then(
                if (isSelected) {
                    Modifier
                        .clip(CircleShape)
                        .background(tokens.selectedDayColor)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$day",
            style = LocalAppTypography.current.bodyMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = when {
                isSelected -> tokens.selectedDayContentColor
                isToday -> tokens.todayColor
                else -> tokens.dayTextColor
            },
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  时间面板 — TDesign 风格滚轮
// ═══════════════════════════════════════════════════════════

/**
 * TDesign 风格时间滚轮面板。
 */
@Composable
private fun TimePanel(
    hour: Int,
    minute: Int,
    tokens: TimePickerTokens,
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
        // 小时
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "小时",
                style = LocalAppTypography.current.labelSmall,
                color = tokens.unselectedTextColor,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TimePickerLogic(
                value = hour,
                range = 0..23,
                itemHeight = tokens.itemHeight,
                visibleItems = tokens.visibleItems,
                selectedBgColor = tokens.selectedBackgroundColor,
                selectedTextColor = tokens.selectedTextColor,
                unselectedTextColor = tokens.unselectedTextColor,
                dividerColor = tokens.dividerColor,
                onValueChanged = onHourChanged,
            )
        }

        Text(
            ":",
            style = LocalAppTypography.current.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = tokens.separatorColor,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        // 分钟
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "分钟",
                style = LocalAppTypography.current.labelSmall,
                color = tokens.unselectedTextColor,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TimePickerLogic(
                value = minute,
                range = 0..59,
                itemHeight = tokens.itemHeight,
                visibleItems = tokens.visibleItems,
                selectedBgColor = tokens.selectedBackgroundColor,
                selectedTextColor = tokens.selectedTextColor,
                unselectedTextColor = tokens.unselectedTextColor,
                dividerColor = tokens.dividerColor,
                onValueChanged = onMinuteChanged,
            )
        }
    }
}
