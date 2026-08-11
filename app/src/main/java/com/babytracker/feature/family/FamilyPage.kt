package com.babytracker.feature.family
import com.babytracker.core.ui.AppShapes
import com.babytracker.core.ui.AppSpacing

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import io.elyon.kmp.basic.Text
import com.babytracker.core.ui.components.chip.AppFilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.navigation.Navigator
import io.elyon.kmp.theme.ElyonTheme
import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilyMember
import com.babytracker.core.ui.components.card.AppCard
import com.babytracker.core.ui.components.topbar.AppTopBar
import com.babytracker.core.ui.Gradients
import com.babytracker.core.ui.components.scaffold.AppScaffold
import com.babytracker.core.ui.components.button.AppButton
import com.babytracker.core.ui.components.button.ButtonVariant
import com.babytracker.core.ui.components.input.AppInput
import com.babytracker.core.ui.components.progress.AppCircularProgress
import com.babytracker.core.ui.components.dialog.AppConfirmDialog
import com.babytracker.core.ui.components.dialog.AppDialog
import com.babytracker.core.ui.components.divider.AppDivider
import org.koin.androidx.compose.koinViewModel

@Composable
fun FamilyPage(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val vm: FamilyViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "家庭共享",
                onBack = { navigator.pop() },
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
            if (uiState.families.isNotEmpty() || uiState.unscopedBabyCount > 0) {
                FamilyModeSelector(
                    families = uiState.families,
                    currentFamily = uiState.currentFamily,
                    localSelected = uiState.isLocalMode,
                    localCount = uiState.unscopedBabyCount,
                    onSelectFamily = vm::selectFamily,
                    onSelectLocal = vm::selectLocalMode,
                )
                Spacer(Modifier.height(spacing.md))
            }
            if (uiState.isLoading && uiState.families.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
                }
            } else if (uiState.isLocalMode) {
                LocalDataView(
                    count = uiState.unscopedBabyCount,
                    families = uiState.families,
                    onMigrate = vm::requestMigration,
                )
            } else if (uiState.currentFamily == null) {
                // ── 无家庭：提示创建或加入 ──
                EmptyFamilyView(
                    onCreateClick = { vm.showCreateDialog() },
                    onJoinClick = { vm.showJoinDialog() },
                )
            } else {
                // ── 有家庭：显示详情 ──
                FamilyDetailView(
                    family = uiState.currentFamily!!,
                    members = uiState.members,
                    onCreateClick = { vm.showCreateDialog() },
                    onJoinClick = { vm.showJoinDialog() },
                )
            }

            // 错误提示
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = c.error, style = typography.body1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }

    // ── 创建家庭对话框 ──
    AppDialog(
        show = uiState.showCreateDialog,
        title = "创建家庭",
        confirmText = "创建",
        cancelText = "取消",
        content = {
            AppInput(
                value = uiState.newFamilyName,
                onValueChange = { vm.onFamilyNameChange(it) },
                label = "家庭名称",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        onConfirm = { vm.createFamily() },
        onDismiss = { vm.hideCreateDialog() },
    )

    // ── 加入家庭对话框 ──
    AppDialog(
        show = uiState.showJoinDialog,
        title = "加入家庭",
        confirmText = "加入",
        cancelText = "取消",
        confirmEnabled = uiState.inviteCode.length == 6,
        content = {
            Column {
                Text("输入家庭邀请码（6 位）", color = c.onSurfaceVariantSummary, style = typography.body1)
                Spacer(Modifier.height(12.dp))
                AppInput(
                    value = uiState.inviteCode,
                    onValueChange = { vm.onInviteCodeChange(it.take(6)) },
                    label = "邀请码",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        onConfirm = { vm.joinFamily() },
        onDismiss = { vm.hideJoinDialog() },
    )

    AppConfirmDialog(
        show = uiState.migrationTarget != null,
        title = "归属本机数据",
        message = "将 ${uiState.unscopedBabyCount} 个宝宝及其全部记录归入“${uiState.migrationTarget?.name.orEmpty()}”？归属后这些数据会参与该家庭的云同步。",
        confirmText = "确认归属",
        onConfirm = vm::confirmMigration,
        onDismiss = vm::cancelMigration,
    )
}

@Composable
private fun FamilyModeSelector(
    families: List<Family>,
    currentFamily: Family?,
    localSelected: Boolean,
    localCount: Int,
    onSelectFamily: (Family) -> Unit,
    onSelectLocal: () -> Unit,
) {
    val spacing = com.babytracker.core.ui.AppSpacing
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (localCount > 0) {
            AppFilterChip(
                selected = localSelected,
                onClick = onSelectLocal,
                label = "本机数据 · $localCount",
            )
        }
        families.forEach { family ->
            AppFilterChip(
                selected = !localSelected && currentFamily?.id == family.id,
                onClick = { onSelectFamily(family) },
                label = family.name,
            )
        }
    }
}

@Composable
private fun LocalDataView(
    count: Int,
    families: List<Family>,
    onMigrate: (Family) -> Unit,
) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    AppCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
        Column(Modifier.padding(spacing.lg)) {
            Text("本机数据", style = typography.title3)
            Spacer(Modifier.height(spacing.sm))
            Text(
                "$count 个无家庭归属的宝宝及其记录仍保存在本机，不会上传。你可以继续本地使用，或明确选择一个家庭归属。",
                style = typography.body1,
                color = c.onSurfaceVariantSummary,
            )
            if (count > 0 && families.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                families.forEach { family ->
                    AppButton(
                        variant = ButtonVariant.Secondary,
                        onClick = { onMigrate(family) },
                        label = "归入 ${family.name}",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/** 空态：未加入任何家庭 */
@Composable
private fun EmptyFamilyView(onCreateClick: () -> Unit, onJoinClick: () -> Unit) {
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles

    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👨‍👩‍👧", style = typography.headline1)
            Spacer(Modifier.height(spacing.md))
            Text(
                "创建或加入家庭\n与家人共享宝宝的成长记录",
                style = typography.body1,
                color = c.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppButton(variant = ButtonVariant.Secondary, onClick = onJoinClick, label = "加入家庭", icon = Icons.Default.GroupAdd)
                AppButton(onClick = onCreateClick, label = "创建家庭", icon = Icons.Default.Add)
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
    val c = ElyonTheme.colorScheme
    val spacing = com.babytracker.core.ui.AppSpacing
    val typography = ElyonTheme.textStyles
    val shapes = com.babytracker.core.ui.AppShapes
    val context = LocalContext.current

    // 家庭名称卡片
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(spacing.xxl)
                        .clip(RoundedCornerShape(shapes.large))
                        .background(Gradients.primary(ElyonTheme.colorScheme)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👨‍👩‍👧", style = typography.headline2)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(family.name, style = typography.title3)
                    Text("${members.size} 位成员", style = typography.footnote1, color = c.onSurfaceVariantSummary)
                }
            }

            Spacer(Modifier.height(spacing.md))

            // 邀请码
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.primaryContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("邀请码", style = typography.footnote1, color = c.onSurfaceVariantSummary)
                    Text(family.inviteCode, style = typography.title1, letterSpacing = 4.sp, color = c.primary)
                }
                AppButton(
                    variant = ButtonVariant.Text,
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite", family.inviteCode))
                        Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                    },
                    label = "复制",
                    icon = Icons.Default.ContentCopy,
                    contentColor = c.primary,
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 成员列表
    if (members.isNotEmpty()) {
        Text("家庭成员", style = typography.footnote1, color = c.onSurfaceVariantSummary, modifier = Modifier.padding(bottom = spacing.sm))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
        ) {
            Column(Modifier.padding(horizontal = 12.dp)) {
                members.forEachIndexed { index, member ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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
                                style = typography.title3,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                member.userId.take(8) + "…",
                                style = typography.body2,
                                color = c.onSurface,
                            )
                            Text(
                                if (member.role == "owner") "创建者" else "成员",
                                style = typography.footnote1,
                                color = c.onSurfaceVariantSummary,
                            )
                        }
                    }
                    if (index < members.lastIndex) {
                        AppDivider(color = c.dividerLine, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.md))
    }

    // 操作按钮
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        AppButton(variant = ButtonVariant.Secondary, onClick = onJoinClick, label = "加入新家庭", icon = Icons.Default.GroupAdd, modifier = Modifier.weight(1f))
        AppButton(variant = ButtonVariant.Secondary, onClick = onCreateClick, label = "创建新家庭", icon = Icons.Default.Add, modifier = Modifier.weight(1f))
    }
}
