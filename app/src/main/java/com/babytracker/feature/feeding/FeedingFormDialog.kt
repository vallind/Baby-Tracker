package com.babytracker.feature.feeding

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.BreastSide
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.chip.AppOptionChipRow
import com.babytracker.designsystem.components.datetimecascade.AppDateTimeField
import com.babytracker.designsystem.components.datetimecascade.QuickTimeChipRow
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.timer.AppTimerRow
import com.babytracker.designsystem.hooks.TimerTickEffect
import com.babytracker.designsystem.hooks.rememberTimerState
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import org.koin.compose.koinInject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 喂养记录表单（共享组件，TimelineScreen 也在使用）。
 *
 * 从 FeedingListScreen.kt 拆出：表单持有计时器等持久化 UI 态（SharedPreferences），
 * 属于 Feature 组件而非 *Screen.kt 文件——Screen 边界审计只约束 Screen 文件，
 * 本文件允许使用 koinInject 读取偏好（计时器持久化的业务约定）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingFormDialog(
    babyId: Int,
    editEntity: Feeding? = null,
    onDismiss: () -> Unit,
    onSave: (Feeding) -> Unit,
) {
    val spacing = LocalAppSpacing.current
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.let { FeedingType.raw(it.type) } ?: "breast") }
    var amountMl by remember { mutableStateOf(editEntity?.amountMl?.toString() ?: "") }
    var durationMin by remember { mutableStateOf(editEntity?.durationMin?.toString() ?: "") }
    var breastSide by remember { mutableStateOf(editEntity?.breastSide?.let { BreastSide.raw(it) } ?: AppStrings.breastSideBoth) }
    var foodName by remember { mutableStateOf(editEntity?.foodName ?: "") }
    var amountG by remember { mutableStateOf(editEntity?.amountG?.toString() ?: "") }
    var brand by remember { mutableStateOf(editEntity?.brand ?: "") }
    val now = LocalDateTime.now()
    val prefs: SharedPreferences = koinInject()

    var feedingDateTime by remember {
        mutableStateOf(
            editEntity?.timestamp?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) { now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }
            } ?: if (prefs.getBoolean("feeding_timer_running", false)) {
                prefs.getString("feeding_timer_form_start_time", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))!!
            } else {
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }
        )
    }

    // 计时器（持久化：关闭表单再打开继续计时；prefs 约定留在 feature 层，DS 只管 UI 计时）
    val timer = rememberTimerState(
        initialRunning = prefs.getBoolean("feeding_timer_running", false),
        initialStartMs = prefs.getLong("feeding_timer_start_millis", 0L),
        initialElapsedSec = if (editEntity != null && !prefs.getBoolean("feeding_timer_running", false)) {
            (editEntity.durationMin ?: 0) * 60
        } else {
            0
        },
    )
    TimerTickEffect(timer)

    val buildEntity = {
        if (timer.running) {
            val elapsedSec = timer.stop()
            durationMin = (elapsedSec / 60).toString()
            feedingDateTime = java.time.Instant.ofEpochMilli(timer.startMs)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            prefs.edit()
                .putBoolean("feeding_timer_running", false)
                .remove("feeding_timer_form_start_time")
                .apply()
        }
        if (isEdit) {
            editEntity.copy(
                type = FeedingType.fromRaw(type),
                amountMl = amountMl.toIntOrNull(),
                durationMin = durationMin.toIntOrNull(),
                breastSide = if (type == "breast") BreastSide.fromRaw(breastSide) else null,
                foodName = if (type == "food") foodName else null,
                amountG = amountG.toIntOrNull(),
                brand = brand.ifBlank { null },
                timestamp = feedingDateTime.replace(" ", "T") + ":00",
            )
        } else {
            Feeding(
                babyId = babyId, type = FeedingType.fromRaw(type),
                amountMl = amountMl.toIntOrNull(),
                durationMin = durationMin.toIntOrNull(),
                breastSide = if (type == "breast") BreastSide.fromRaw(breastSide) else null,
                foodName = if (type == "food") foodName else null,
                amountG = amountG.toIntOrNull(),
                brand = brand.ifBlank { null },
                timestamp = feedingDateTime.replace(" ", "T") + ":00",
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) AppStrings.editFeeding else AppStrings.recordFeeding,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStrings.updateLabel else AppStrings.save,
    ) {
        AppOptionChipRow(
            options = listOf("breast" to AppStrings.feedingOptionBreast, "formula" to AppStrings.feedingOptionFormula, "food" to AppStrings.feedingOptionFood, "water" to AppStrings.feedingOptionWater),
            selectedKey = type,
            onSelect = { type = it },
            modifier = Modifier.padding(bottom = spacing.md),
        )

        when (type) {
            "breast" -> {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    listOf(AppStrings.breastSideLeft, AppStrings.breastSideRight, AppStrings.breastSideBoth).forEach { s ->
                        AppFilterChip(selected = breastSide == s, onClick = { breastSide = s }, label = s, modifier = Modifier.weight(1f))
                    }
                }
                // 计时器
                AppTimerRow(
                    display = timer.formatDisplay(),
                    running = timer.running,
                    onStart = {
                        timer.start(System.currentTimeMillis())
                        prefs.edit()
                            .putBoolean("feeding_timer_running", true)
                            .putLong("feeding_timer_start_millis", timer.startMs)
                            .putString("feeding_timer_form_start_time", feedingDateTime)
                            .apply()
                    },
                    onStop = {
                        val elapsedSec = timer.stop()
                        durationMin = (elapsedSec / 60).toString()
                        feedingDateTime = java.time.Instant.ofEpochMilli(timer.startMs)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        prefs.edit()
                            .putBoolean("feeding_timer_running", false)
                            .remove("feeding_timer_form_start_time")
                            .apply()
                    },
                )
                AppInput(
                    value = durationMin,
                    onValueChange = { durationMin = it.filter { c -> c.isDigit() } },
                    label = AppStrings.feedingDurationLabel,
                    leadingIcon = { Text("⏱", style = LocalAppTypography.current.titleLarge) },
                    isError = durationMin.toIntOrNull()?.let { it < 0 || it > 600 } ?: false,
                    errorMessage = if (durationMin.toIntOrNull()?.let { it < 0 || it > 600 } == true) AppStrings.durationRangeError else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "formula" -> {
                AppInput(
                    value = amountMl,
                    onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                    label = AppStrings.feedingAmountLabel,
                    leadingIcon = { Text("💧", style = LocalAppTypography.current.titleLarge) },
                    isError = amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } ?: false,
                    errorMessage = if (amountMl.toIntOrNull()?.let { it <= 0 || it > 500 } == true) AppStrings.amountRangeError500 else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                // 常用量一键填
                Row(Modifier.fillMaxWidth().padding(top = spacing.xs), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    listOf("60", "90", "120", "180").forEach { v ->
                        AppFilterChip(selected = amountMl == v, onClick = { amountMl = v }, label = "${v}ml", modifier = Modifier.weight(1f))
                    }
                }
                AppInput(
                    value = brand,
                    onValueChange = { brand = it },
                    label = AppStrings.brandOptional,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "food" -> {
                AppInput(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = AppStrings.foodNameLabel,
                    leadingIcon = { Text("🥣", style = LocalAppTypography.current.titleLarge) },
                    modifier = Modifier.fillMaxWidth(),
                )
                AppInput(
                    value = amountG,
                    onValueChange = { amountG = it.filter { c -> c.isDigit() } },
                    label = AppStrings.portionGramLabel,
                    isError = amountG.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                    errorMessage = if (amountG.toIntOrNull()?.let { it < 0 || it > 1000 } == true) AppStrings.amountRangeError1000 else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            "water" -> {
                AppInput(
                    value = amountMl,
                    onValueChange = { amountMl = it.filter { c -> c.isDigit() } },
                    label = AppStrings.waterAmountLabel,
                    // 饮水量预设由下方 chip 行提供
                    leadingIcon = { Text("🥤", style = LocalAppTypography.current.titleLarge) },
                    isError = amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } ?: false,
                    errorMessage = if (amountMl.toIntOrNull()?.let { it < 0 || it > 1000 } == true) AppStrings.amountRangeError1000 else null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                // 常用饮水量一键填
                Row(Modifier.fillMaxWidth().padding(top = spacing.xs), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    listOf("50", "100", "150", "200").forEach { v ->
                        AppFilterChip(selected = amountMl == v, onClick = { amountMl = v }, label = "${v}ml", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // 高频场景免开滚轮：一键回填时间（精确调整仍点字段开级联选择器）
        QuickTimeChipRow(onPick = { feedingDateTime = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) })
        Spacer(Modifier.height(12.dp))
        AppDateTimeField(
            label = AppStrings.feedingTimeLabel,
            value = feedingDateTime,
            onPick = { feedingDateTime = it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}