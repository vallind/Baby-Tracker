package com.babytracker.feature.sleep

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.components.chip.AppOptionChipRow
import com.babytracker.ui.patterns.records.AppDateTimeField
import com.babytracker.ui.patterns.records.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.ui.patterns.records.AppTimerRow
import com.babytracker.ui.patterns.records.TimerTickEffect
import com.babytracker.ui.patterns.records.rememberTimerState
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppSpacing
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 睡眠记录表单（共享组件，TimelineScreen 也在使用）。
 *
 * 从 SleepListScreen.kt 拆出：表单持有计时器等持久化 UI 态（SharedPreferences），
 * 属于 Feature 组件而非 *Screen.kt 文件——Screen 边界审计只约束 Screen 文件，
 * 本文件允许使用 koinInject 读取偏好（计时器持久化的业务约定）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepFormDialog(
    babyId: Int,
    editEntity: Sleep? = null,
    onDismiss: () -> Unit,
    onSave: (Sleep) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val isEdit = editEntity != null
    var selectedType by remember { mutableStateOf(editEntity?.let { SleepType.raw(it.type) } ?: "night") }
    val now = LocalDateTime.now()
    val prefs: SharedPreferences = koinInject()

    var startTime by remember {
        mutableStateOf(
            editEntity?.startTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: if (prefs.getBoolean("sleep_timer_running", false)) {
                prefs.getString("sleep_timer_form_start_time", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))!!
            } else {
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }
        )
    }
    var endTime by remember {
        mutableStateOf(
            editEntity?.endTime?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: now.plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }

    // 计时器（持久化：关闭表单再打开继续计时；prefs 约定留在 feature 层，DS 只管 UI 计时）
    val timer = rememberTimerState(
        initialRunning = prefs.getBoolean("sleep_timer_running", false),
        initialStartMs = prefs.getLong("sleep_timer_start_millis", 0L),
        initialElapsedSec = if (editEntity != null && !prefs.getBoolean("sleep_timer_running", false)) {
            val start = try { LocalDateTime.parse(editEntity.startTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
            val end = try { LocalDateTime.parse(editEntity.endTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
            if (start != null && end != null) Duration.between(start, end).seconds.toInt() else 0
        } else {
            0
        },
    )
    TimerTickEffect(timer)

    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val buildEntity = {
        if (timer.running) {
            timer.stop()
            endTime = LocalDateTime.now().format(timeFormatter)
            prefs.edit()
                .putBoolean("sleep_timer_running", false)
                .remove("sleep_timer_form_start_time")
                .apply()
        }
        if (isEdit) {
            editEntity.copy(
                type = SleepType.fromRaw(selectedType),
                startTime = startTime.replace(" ", "T") + ":00",
                endTime = endTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        } else {
            Sleep(
                babyId = babyId,
                type = SleepType.fromRaw(selectedType),
                startTime = startTime.replace(" ", "T") + ":00",
                endTime = endTime.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) AppStringsProduct.editSleep else AppStringsProduct.recordSleep,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStringsProduct.updateLabel else AppStrings.save,
    ) {
        AppOptionChipRow(
            options = listOf("night" to AppStringsProduct.sleepOptionNight, "nap" to AppStringsProduct.sleepOptionNap),
            selectedKey = selectedType,
            onSelect = { selectedType = it },
        )
        Spacer(Modifier.height(spacing.md))
        // 计时器 UI
        AppTimerRow(
            display = timer.formatDisplay(),
            running = timer.running,
            onStart = {
                val currentStartTime = startTime
                timer.start(System.currentTimeMillis())
                prefs.edit()
                    .putBoolean("sleep_timer_running", true)
                    .putLong("sleep_timer_start_millis", timer.startMs)
                    .putString("sleep_timer_form_start_time", currentStartTime)
                    .apply()
            },
            onStop = {
                timer.stop()
                endTime = LocalDateTime.now().format(timeFormatter)
                prefs.edit()
                    .putBoolean("sleep_timer_running", false)
                    .remove("sleep_timer_form_start_time")
                    .apply()
            },
        )
        QuickTimeChipRow(onPick = { startTime = it.format(timeFormatter) })
        Spacer(Modifier.height(spacing.xs))
        // 双时间字段各自持有字段级弹窗（原为共享一个级联 + pickerTarget 分发，行为等价）
        AppDateTimeField(
            label = AppStringsProduct.startTimeLabel,
            value = startTime,
            onPick = { startTime = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppDateTimeField(
            label = AppStringsProduct.endTimeLabel,
            value = endTime,
            onPick = { endTime = it },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        AppInput(value = note, onValueChange = { note = it }, label = AppStringsProduct.detailNote, modifier = Modifier.fillMaxWidth())
    }
}