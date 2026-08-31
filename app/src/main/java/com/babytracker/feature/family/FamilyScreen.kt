package com.babytracker.feature.family

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import com.babytracker.designsystem.components.chip.AppChipCarouselRow
import com.babytracker.designsystem.components.chip.AppChipSpec
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilyMember
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.ui.i18n.AppStringsProduct
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.progress.AppCircularProgress
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.dialog.AppDialog
import com.babytracker.designsystem.components.divider.AppDivider

/**
 * 家庭共享屏 — 纯 UI 渲染层：只收 UiState 与命名回调，不感知 Koin / NavController。
 * 回调全部由 [FamilyRoute] 装配（VM 方法引用 + 导航映射）。
 */
@Composable
fun FamilyScreen(
    state: FamilyViewModel.UiState,
    onBack: () -> Unit,
    onSelectFamily: (Family) -> Unit,
    onSelectLocal: () -> Unit,
    onMigrate: (Family) -> Unit,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onFamilyNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismissCreate: () -> Unit,
    onInviteCodeChange: (String) -> Unit,
    onJoin: () -> Unit,
    onDismissJoin: () -> Unit,
    onConfirmMigration: () -> Unit,
    onDismissMigration: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "家庭共享",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md, vertical = spacing.md),
        ) {
            if (state.families.isNotEmpty() || state.unscopedBabyCount > 0) {
                FamilyModeSelector(
                    families = state.families,
                    currentFamily = state.currentFamily,
                    localSelected = state.isLocalMode,
                    localCount = state.unscopedBabyCount,
                    onSelectFamily = onSelectFamily,
                    onSelectLocal = onSelectLocal,
                )
                Spacer(Modifier.height(spacing.md))
            }
            if (state.isLoading && state.families.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
                }
            } else if (state.isLocalMode) {
                LocalDataView(
                    count = state.unscopedBabyCount,
                    families = state.families,
                    onMigrate = onMigrate,
                )
            } else if (state.currentFamily == null) {
                // ── 无家庭：提示创建或加入 ──
                EmptyFamilyView(
                    onCreateClick = onCreateClick,
                    onJoinClick = onJoinClick,
                )
            } else {
                // ── 有家庭：显示详情 ──
                FamilyDetailView(
                    family = state.currentFamily!!,
                    members = state.members,
                    onCreateClick = onCreateClick,
                    onJoinClick = onJoinClick,
                )
            }

            // 错误提示
            state.errorMessage?.let { msg ->
                Spacer(Modifier.height(spacing.md))
                Text(msg, color = c.error, style = typography.bodyLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }

    // ── 创建家庭对话框 ──
    AppDialog(
        show = state.showCreateDialog,
        title = "创建家庭",
        confirmText = "创建",
        cancelText = "取消",
        content = {
            AppInput(
                value = state.newFamilyName,
                onValueChange = onFamilyNameChange,
                label = "家庭名称",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        onConfirm = onCreate,
        onDismiss = onDismissCreate,
    )

    // ── 加入家庭对话框 ──
    AppDialog(
        show = state.showJoinDialog,
        title = "加入家庭",
        confirmText = "加入",
        cancelText = "取消",
        confirmEnabled = state.inviteCode.length == 6,
        content = {
            Column {
                Text("输入家庭邀请码（6 位）", color = c.textSecondary, style = typography.bodyLarge)
                Spacer(Modifier.height(spacing.md))
                AppInput(
                    value = state.inviteCode,
                    onValueChange = { onInviteCodeChange(it.take(6)) },
                    label = "邀请码",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        onConfirm = onJoin,
        onDismiss = onDismissJoin,
    )

    AppConfirmDialog(
        show = state.migrationTarget != null,
        title = "归属本机数据",
        message = "将 ${state.unscopedBabyCount} 个宝宝及其全部记录归入“${state.migrationTarget?.name.orEmpty()}”？归属后这些数据会参与该家庭的云同步。",
        confirmText = "确认归属",
        onConfirm = onConfirmMigration,
        onDismiss = onDismissMigration,
    )
}

// 本机数据档专用 key（与云端家庭 id 同轴参与单选；family.id 为服务端 String id，不会撞键）
private const val LOCAL_MODE_KEY = "local"

@Composable
private fun FamilyModeSelector(
    families: List<Family>,
    currentFamily: Family?,
    localSelected: Boolean,
    localCount: Int,
    onSelectFamily: (Family) -> Unit,
    onSelectLocal: () -> Unit,
) {
    // 模式选择条收编为 AppChipCarouselRow：「本机数据 · N」计数并入 label（key/label 装配留 feature 私有层）
    val options = buildList {
        if (localCount > 0) {
            add(AppChipSpec(key = LOCAL_MODE_KEY, label = "本机数据 · $localCount"))
        }
        families.forEach { family ->
            add(AppChipSpec(key = family.id, label = family.name))
        }
    }
    AppChipCarouselRow(
        options = options,
        selectedKey = if (localSelected) LOCAL_MODE_KEY else currentFamily?.id,
        onSelect = { key ->
            if (key == LOCAL_MODE_KEY) onSelectLocal()
            else families.firstOrNull { it.id == key }?.let(onSelectFamily)
        },
    )
}

@Composable
private fun LocalDataView(
    count: Int,
    families: List<Family>,
    onMigrate: (Family) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(spacing.lg)) {
            Text("本机数据", style = typography.titleMedium)
            Spacer(Modifier.height(spacing.sm))
            Text(
                "$count 个无家庭归属的宝宝及其记录仍保存在本机，不会上传。你可以继续本地使用，或明确选择一个家庭归属。",
                style = typography.bodyLarge,
                color = c.textSecondary,
            )
            if (count > 0 && families.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                families.forEach { family ->
                    AppButton(
                        variant = ButtonVariant.Outline,
                        onClick = { onMigrate(family) },
                        label = "归入 ${family.name}",
                        modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xs),
                    )
                }
            }
        }
    }
}

/** 空态：未加入任何家庭 */
@Composable
private fun EmptyFamilyView(onCreateClick: () -> Unit, onJoinClick: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current

    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👨‍👩‍👧", style = typography.displayLarge)
            Spacer(Modifier.height(spacing.md))
            Text(
                AppStringsProduct.familyEmptyTitle,
                style = typography.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                AppButton(variant = ButtonVariant.Outline, onClick = onJoinClick, label = AppStringsProduct.familyJoin, icon = Icons.Default.GroupAdd)
                AppButton(onClick = onCreateClick, label = AppStringsProduct.familyCreate, icon = Icons.Default.Add)
            }
        }
    }
}

/** 家庭详情：名称、邀请码、成员列表 */
@Composable
private fun FamilyDetailView(
    family: Family,
    members: List<FamilyMember>,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val context = LocalContext.current

    // 家庭名称卡片
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(spacing.xxl)
                        .clip(RoundedCornerShape(shapes.large))
                        .background(Gradients.primary(c)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👨‍👩‍👧", style = typography.headlineMedium)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(family.name, style = typography.titleMedium)
                    Text("${members.size} 位成员", style = typography.labelMedium, color = c.textSecondary)
                }
            }

            Spacer(Modifier.height(spacing.md))

            // 邀请码
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(shapes.large))
                    .background(AppColorScale.fromSeed(c.primary).tintContainer(c))
                    .padding(spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("邀请码", style = typography.labelMedium, color = c.textSecondary)
                    Text(family.inviteCode, style = typography.titleLarge, letterSpacing = 4.sp, color = c.primary)
                }
                AppButton(
                    variant = ButtonVariant.Ghost,
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite", family.inviteCode))
                        Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                    },
                    label = "复制",
                    icon = Icons.Default.ContentCopy,
                )
            }
        }
    }

    Spacer(Modifier.height(spacing.md))

    // 成员列表
    if (members.isNotEmpty()) {
        Text("家庭成员", style = typography.labelMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = spacing.sm))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(horizontal = spacing.md)) {
                members.forEachIndexed { index, member ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(shapes.full))
                                .background(if (member.role == "owner") c.primary else c.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (member.role == "owner") "👑" else "👤",
                                style = typography.titleMedium,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                member.userId.take(8) + "…",
                                style = typography.bodyMedium,
                                color = c.textPrimary,
                            )
                            Text(
                                if (member.role == "owner") "创建者" else "成员",
                                style = typography.labelMedium,
                                color = c.textSecondary,
                            )
                        }
                    }
                    if (index < members.lastIndex) {
                        AppDivider(color = c.divider, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.md))
    }

    // 操作按钮
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        AppButton(variant = ButtonVariant.Outline, onClick = onJoinClick, label = "加入新家庭", icon = Icons.Default.GroupAdd, modifier = Modifier.weight(1f))
        AppButton(variant = ButtonVariant.Outline, onClick = onCreateClick, label = "创建新家庭", icon = Icons.Default.Add, modifier = Modifier.weight(1f))
    }
}