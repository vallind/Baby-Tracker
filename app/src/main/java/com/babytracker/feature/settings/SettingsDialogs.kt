package com.babytracker.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Baby
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.dialog.AppDialog
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppSpacing

// ═══════════════════════════════════════════════════════════
//  Settings 共享对话框（自 SettingsScreen.kt 拆出，Batch 4）
//  BabyProfileScreen / TimelineScreen 等跨屏复用，签名保持不变
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyFormDialog(baby: Baby?, onDismiss: () -> Unit, onSave: (Baby) -> Unit) {
    val spacing = LocalAppSpacing.current
    val isEdit = baby != null
    var name by remember { mutableStateOf(baby?.name ?: "") }
    var gender by remember { mutableStateOf(baby?.gender ?: "男") }
    var birthDate by remember { mutableStateOf(baby?.birthDate ?: java.time.LocalDate.now().toString()) }
    var birthWeight by remember { mutableStateOf(baby?.birthWeight?.toString() ?: "") }
    var birthHeight by remember { mutableStateOf(baby?.birthHeight?.toString() ?: "") }

    AppDialog(
        show = true,
        title = if (isEdit) "编辑宝宝" else "添加宝宝",
        confirmText = if (isEdit) "保存" else "添加",
        cancelText = "取消",
        confirmEnabled = name.isNotBlank(),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                AppInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "姓名",
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    listOf("男", "女").forEach { g ->
                    AppFilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = g,
                        modifier = Modifier.weight(1f),
                    )
                    }
                }

                AppInput(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = "出生日期 (yyyy-MM-dd)",
                    modifier = Modifier.fillMaxWidth(),
                )

                AppInput(
                    value = birthWeight,
                    onValueChange = { birthWeight = it },
                    label = "出生体重 (kg，可选)",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )

                AppInput(
                    value = birthHeight,
                    onValueChange = { birthHeight = it },
                    label = "出生身高 (cm，可选)",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        onConfirm = {
            onSave(Baby(
                id = baby?.id ?: 0,
                name = name,
                gender = gender,
                birthDate = birthDate,
                birthWeight = birthWeight.toDoubleOrNull(),
                birthHeight = birthHeight.toDoubleOrNull(),
                createdAt = baby?.createdAt ?: java.time.LocalDateTime.now().toString(),
            ))
        },
        onDismiss = onDismiss,
    )
}

@Composable
fun NicknameEditDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var input by remember(currentNickname) { mutableStateOf(currentNickname) }

    AppDialog(
        show = true,
        title = AppStringsProduct.editNickname,
        content = {
            AppInput(
                value = input,
                onValueChange = { input = it },
                label = AppStringsProduct.nicknameHint,
                placeholder = "输入你喜欢的昵称",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmText = AppStrings.save,
        cancelText = AppStrings.cancel,
        confirmEnabled = input.isNotBlank(),
        onConfirm = { onSave(input.trim()) },
        onDismiss = onDismiss,
    )
}