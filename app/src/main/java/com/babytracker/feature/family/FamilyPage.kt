package com.babytracker.feature.family

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import com.babytracker.designsystem.components.chip.AppFilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilyMember
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypographyStyle
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.button.SecondaryButton
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.progress.AppCircularProgress
import org.koin.androidx.compose.koinViewModel

@Composable
fun FamilyPage(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val vm: FamilyViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "家庭共享",
                onBack = { navController.popBackStack() },
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
            if (uiState.isLoading && uiState.families.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    AppCircularProgress(indicatorColor = c.primary)
                }
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
                    families = uiState.families,
                    onSelectFamily = { vm.selectFamily(it) },
                    onCreateClick = { vm.showCreateDialog() },
                    onJoinClick = { vm.showJoinDialog() },
                )
            }

            // 错误提示
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = c.error, style = typography.bodyLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }

    // ── 创建家庭对话框 ──
    if (uiState.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideCreateDialog() },
            title = { Text("创建家庭") },
            text = {
                AppInput(
                    value = uiState.newFamilyName,
                    onValueChange = { vm.onFamilyNameChange(it) },
                    label = "家庭名称",
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                AppTextButton(onClick = { vm.createFamily() }, label = "创建")
            },
            dismissButton = {
                AppTextButton(onClick = { vm.hideCreateDialog() }, label = "取消")
            },
        )
    }

    // ── 加入家庭对话框 ──
    if (uiState.showJoinDialog) {
        AlertDialog(
            onDismissRequest = { vm.hideJoinDialog() },
            title = { Text("加入家庭") },
            text = {
                Column {
                    Text("输入家庭邀请码（6 位）", color = c.textSecondary, style = typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    AppInput(
                        value = uiState.inviteCode,
                        onValueChange = { vm.onInviteCodeChange(it.take(6)) },
                        label = "邀请码",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                AppTextButton(
                    onClick = { vm.joinFamily() },
                    label = "加入",
                    enabled = uiState.inviteCode.length == 6,
                )
            },
            dismissButton = {
                AppTextButton(onClick = { vm.hideJoinDialog() }, label = "取消")
            },
        )
    }
}

/** 空态：未加入任何家庭 */
@Composable
private fun EmptyFamilyView(onCreateClick: () -> Unit, onJoinClick: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current

    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👨‍👩‍👧", style = typography.display)
            Spacer(Modifier.height(spacing.md))
            Text(
                "创建或加入家庭\n与家人共享宝宝的成长记录",
                style = typography.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(onClick = onJoinClick, label = "加入家庭", icon = Icons.Default.GroupAdd)
                PrimaryButton(onClick = onCreateClick, label = "创建家庭", icon = Icons.Default.Add)
            }
        }
    }
}

/** 家庭详情：名称、邀请码、成员列表 */
@Composable
private fun FamilyDetailView(
    family: Family,
    members: List<FamilyMember>,
    families: List<Family>,
    onSelectFamily: (Family) -> Unit,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypographyStyle.current
    val shapes = LocalAppShapes.current
    val context = LocalContext.current

    // 家庭切换（多家庭时显示）
    if (families.size > 1) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            families.forEach { f ->
                val selected = f.id == family.id
                AppFilterChip(
                    selected = selected,
                    onClick = { onSelectFamily(f) },
                    label = if (selected) "${f.name} · 当前" else f.name,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(spacing.md))
    }

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
                        .background(Gradients.primary(c)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👨‍👩‍👧", style = typography.headline)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(family.name, style = typography.titleMedium)
                    Text("${members.size} 位成员", style = typography.label, color = c.textSecondary)
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
                    Text("邀请码", style = typography.label, color = c.textSecondary)
                    Text(family.inviteCode, style = typography.titleLarge, letterSpacing = 4.sp, color = c.primary)
                }
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite", family.inviteCode))
                    Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(spacing.md), tint = c.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("复制", color = c.primary, style = typography.bodyMedium)
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 成员列表
    if (members.isNotEmpty()) {
        Text("家庭成员", style = typography.label, color = c.textSecondary, modifier = Modifier.padding(bottom = spacing.sm))
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
                                style = typography.label,
                                color = c.textSecondary,
                            )
                        }
                    }
                    if (index < members.lastIndex) {
                        HorizontalDivider(color = c.divider, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.md))
    }

    // 操作按钮
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        SecondaryButton(onClick = onJoinClick, label = "加入新家庭", icon = Icons.Default.GroupAdd, modifier = Modifier.weight(1f))
        SecondaryButton(onClick = onCreateClick, label = "创建新家庭", icon = Icons.Default.Add, modifier = Modifier.weight(1f))
    }
}
