package com.babytracker.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.util.DateUtils
import com.babytracker.designsystem.components.avatar.AppInitialAvatar
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.card.CardColors
import com.babytracker.designsystem.components.card.CardVariant
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.surface.AppSurface
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.tintContainer

/**
 * 宝宝管理页 — 纯 UI 渲染层（Batch 4）。
 * 数据/增删改/切换在 BabyManagementViewModel；弹层与确认框显隐留 Screen。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyManagementScreen(
    state: BabyManagementUiState,
    onBack: () -> Unit,
    onSelectBaby: (Int) -> Unit,
    onAddOrUpdate: (Baby, Boolean) -> Unit,
    onDelete: (Baby) -> Unit,
    onRestore: (Baby) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    val activeBabies = state.activeBabies
    val deletedBabies = state.deletedBabies
    val currentBabyId = state.currentBabyId
    var showDeleted by remember { mutableStateOf(false) }

    var showForm by remember { mutableStateOf(false) }
    var editingBaby by remember { mutableStateOf<Baby?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Baby?>(null) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "宝宝管理",
                onBack = onBack,
            )
        },
        fab = {
            AppFAB(
                icon = Icons.Default.Add,
                onClick = { showForm = true; editingBaby = null },
            )
        },
    ) { padding ->
        if (activeBabies.isEmpty() && deletedBabies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("还没有添加宝宝", color = c.textSecondary)
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = spacing.md, vertical = spacing.sm)) {
                activeBabies.forEach { b ->
                    val isCurrent = b.id == currentBabyId
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xs),
                        variant = CardVariant.Outlined,
                        onClick = { editingBaby = b; showForm = true },
                        selected = isCurrent,
                    ) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            AppInitialAvatar(name = b.name, size = 44.dp)
                            Spacer(Modifier.width(spacing.md))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(b.name, style = LocalAppTypography.current.titleSmall)
                                    if (isCurrent) {
                                        Spacer(Modifier.width(spacing.sm))
                                        AppSurface(color = AppColorScale.fromSeed(c.primary).tintContainer(c), shape = RoundedCornerShape(shapes.medium)) {
                                            Text("当前", style = LocalAppTypography.current.labelSmall, color = c.primary, modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xxs))
                                        }
                                    }
                                }
                                Text("${b.gender} · ${DateUtils.monthAge(java.time.LocalDate.parse(b.birthDate))}", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                                if (b.uuid != null) {
                                    Text(
                                        "UUID: ${b.uuid.take(8)}…",
                                        style = LocalAppTypography.current.labelSmall,
                                        color = c.textSecondary.copy(alpha = 0.5f),
                                    )
                                }
                            }
                            if (!isCurrent) {
                                AppButton(
                                    variant = ButtonVariant.Ghost,
                                    onClick = { onSelectBaby(b.id) },
                                    label = "切换",
                                )
                            }
                            AppIconButton(icon = Icons.Default.Delete, onClick = { showDeleteConfirm = b }, contentDescription = "删除", tint = c.textSecondary)
                        }
                    }
                }

                if (deletedBabies.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.md))
                    AppButton(
                        variant = ButtonVariant.Ghost,
                        onClick = { showDeleted = !showDeleted },
                        label = "已删除的宝宝 (${deletedBabies.size}) ${if (showDeleted) "▲" else "▼"}",
                    )
                    if (showDeleted) {
                        deletedBabies.forEach { b ->
                            AppCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xs),
                                colors = CardColors(elevation = elev.level1),
                            ) {
                                Row(Modifier.padding(spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                    AppInitialAvatar(name = b.name, size = 36.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(b.name, style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
                                        Text("已删除", style = LocalAppTypography.current.labelSmall, color = c.textTertiary)
                                    }
                                    AppButton(
                                        variant = ButtonVariant.Ghost,
                                        onClick = { onRestore(b) },
                                        label = "恢复",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        BabyFormDialog(
            baby = editingBaby,
            onDismiss = { showForm = false; editingBaby = null },
            onSave = { baby ->
                onAddOrUpdate(baby, editingBaby != null)
                showForm = false
                editingBaby = null
            },
        )
    }

    AppConfirmDialog(
        show = showDeleteConfirm != null,
        title = "确认删除",
        message = "确定要删除 ${showDeleteConfirm?.name} 的所有数据吗？",
        onConfirm = {
            showDeleteConfirm?.let { baby -> onDelete(baby) }
            showDeleteConfirm = null
        },
        onDismiss = { showDeleteConfirm = null },
    )
}