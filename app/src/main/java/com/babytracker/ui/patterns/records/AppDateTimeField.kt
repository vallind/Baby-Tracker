package com.babytracker.ui.patterns.records

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.input.AppInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 只读日期时间字段 — 表单族通用组件（G4 收敛）。
 *
 * 外观与 [AppInput] 完全一致（禁用态只读），点击弹出 [DateTimeCascadeDialog]
 * 级联选择器，选中后经 [onPick] 回写。替代各表单手拼的
 * `AppInput(enabled=false) + clickable + 体外 DateTimeCascadeDialog` 三件套：
 * 弹窗显隐为字段内部状态，调用方不再持有 picker flag。
 *
 * 回写格式契约（与现表单存储一致）：
 * - dateOnly = false：返回 "yyyy-MM-dd HH:mm"；
 * - dateOnly = true ：返回 "yyyy-MM-dd"（取级联确认值前 10 位）。
 *
 * 初始定位：value 为空时回落到当前时间；只有日期段（长度 ≤ 10）时自动补 " 00:00"，
 * 与原各调用点手工补零的行为一致。
 *
 * 用法：
 *   AppDateTimeField(
 *       label = AppStrings.detailTime,
 *       value = diaperDateTime,
 *       onPick = { diaperDateTime = it },
 *   )
 */
@Composable
fun AppDateTimeField(
    label: String,
    value: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    dateOnly: Boolean = false, // Health/Vaccination 用纯日期
) {
    var showPicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val initialDateTime = when {
        value.isBlank() -> LocalDateTime.now().format(formatter)
        value.length <= 10 -> "$value 00:00"
        else -> value
    }

    AppInput(
        value = value,
        onValueChange = {},
        label = label,
        enabled = false,
        modifier = modifier.clickable { showPicker = true },
    )

    DateTimeCascadeDialog(
        show = showPicker,
        initialDateTime = initialDateTime,
        dateOnly = dateOnly,
        onConfirm = { picked ->
            onPick(if (dateOnly) picked.take(10) else picked)
        },
        // 级联对话框确认后自身也会回调 onDismiss，这里统一负责收起弹窗
        onDismiss = { showPicker = false },
    )
}