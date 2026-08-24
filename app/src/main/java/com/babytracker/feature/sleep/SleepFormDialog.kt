package com.babytracker.feature.sleep

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.datetimecascade.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    var showCascadePicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableIntStateOf(0) }

    // 计时器（持久化：关闭表单再打开继续计时）
    var timerRunning by remember {
        mutableStateOf(prefs.getBoolean("sleep_timer_running", false))
    }
    var timerStartMs by remember {
        mutableLongStateOf(prefs.getLong("sleep_timer_start_millis", 0L))
    }
    var elapsed by remember {
        mutableIntStateOf(
            if (editEntity != null && !prefs.getBoolean("sleep_timer_running", false)) {
                val start = try { LocalDateTime.parse(editEntity.startTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
                val end = try { LocalDateTime.parse(editEntity.endTime, DateTimeFormatter.ISO_DATE_TIME) } catch (_: Exception) { null }
                if (start != null && end != null) Duration.between(start, end).seconds.toInt() else 0
            } else {
                0
            }
        )
    }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                elapsed = ((System.currentTimeMillis() - timerStartMs) / 1000).toInt()
                delay(1000L)
            }
        }
    }

    val timerDisplay = String.format(Locale.US, "%02d:%02d", elapsed / 60, elapsed % 60)
    val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val buildEntity = {
        if (timerRunning) {
            timerRunning = false
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
        title = if (isEdit) AppStrings.editSleep else AppStrings.recordSleep,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStrings.updateLabel else AppStrings.save,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            AppFilterChip(selected = selectedType == "night", onClick = { selectedType = "night" }, label = AppStrings.sleepOptionNight, modifier = Modifier.weight(1f))
            AppFilterChip(selected = selectedType == "nap", onClick = { selectedType = "nap" }, label = AppStrings.sleepOptionNap, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(spacing.md))
        // 计时器 UI
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        ) {
            Text(
                text = timerDisplay,
                style = LocalAppTypography.current.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (timerRunning) LocalAppColors.current.primary else LocalAppColors.current.textSecondary,
                modifier = Modifier.weight(1f).widthIn(min = 100.dp),
                textAlign = TextAlign.Start,
            )
            if (timerRunning) {
                AppButton(
                    onClick = {
                        timerRunning = false
                        val endNow = LocalDateTime.now()
                        endTime = endNow.format(timeFormatter)
                        prefs.edit()
                            .putBoolean("sleep_timer_running", false)
                            .remove("sleep_timer_form_start_time")
                            .apply()
                    },
                    label = AppStrings.timerStop,
                )
            } else {
                AppButton(
                    onClick = {
                        val currentStartTime = startTime
                        timerStartMs = System.currentTimeMillis()
                        elapsed = 0
                        timerRunning = true
                        prefs.edit()
                            .putBoolean("sleep_timer_running", true)
                            .putLong("sleep_timer_start_millis", timerStartMs)
                            .putString("sleep_timer_form_start_time", currentStartTime)
                            .apply()
                    },
                    label = AppStrings.timerStart,
                )
            }
        }
        QuickTimeChipRow(onPick = { startTime = it.format(timeFormatter) })
        Spacer(Modifier.height(spacing.xs))
        AppInput(value = startTime, onValueChange = {}, label = AppStrings.startTimeLabel, enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 0; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = endTime, onValueChange = {}, label = AppStrings.endTimeLabel, enabled = false, modifier = Modifier.fillMaxWidth().clickable { pickerTarget = 1; showCascadePicker = true })
        Spacer(Modifier.height(12.dp))
        AppInput(value = note, onValueChange = { note = it }, label = AppStrings.detailNote, modifier = Modifier.fillMaxWidth())
    }

    fun pickerField() = if (pickerTarget == 0) startTime else endTime
    fun updatePickerField(v: String) { if (pickerTarget == 0) startTime = v else endTime = v }

    DateTimeCascadeDialog(
        show = showCascadePicker,
        initialDateTime = pickerField(),
        onConfirm = { updatePickerField(it) },
        onDismiss = { showCascadePicker = false },
    )
}