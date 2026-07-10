package com.babytracker.feature.family

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.babytracker.designsystem.theme.LocalAppTypography
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                Text(msg, color = c.error, fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                    Text("输入家庭邀请码（6 位）", color = c.textSecondary, fontSize = 14.sp)
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

    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👨‍👩‍👧", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "创建或加入家庭\n与家人共享宝宝的成长记录",
                style = LocalAppTypography.current.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
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
    val context = LocalContext.current

    // 家庭切换（多家庭时显示）
    if (families.size > 1) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            families.forEach { f ->
                val selected = f.id == family.id
                FilterChip(
                    selected = selected,
                    onClick = { onSelectFamily(f) },
                    label = {
                        Text(
                            if (selected) "${f.name} · 当前" else f.name,
                            fontSize = 12.sp,
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    // 家庭名称卡片
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        elevation = 1.dp,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gradients.primary(c)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("👨‍👩‍👧", fontSize = 24.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(family.name, style = LocalAppTypography.current.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${members.size} 位成员", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 邀请码
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.primaryContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("邀请码", style = LocalAppTypography.current.labelSmall, color = c.textSecondary)
                    Text(family.inviteCode, style = LocalAppTypography.current.titleLarge, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, color = c.primary)
                }
                TextButton(onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("invite", family.inviteCode))
                    Toast.makeText(context, "邀请码已复制", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp), tint = c.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("复制", color = c.primary, fontSize = 13.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // 成员列表
    if (members.isNotEmpty()) {
        Text("家庭成员", style = LocalAppTypography.current.labelMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
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
                                .clip(CircleShape)
                                .background(if (member.role == "owner") c.primary else c.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (member.role == "owner") "👑" else "👤",
                                fontSize = 16.sp,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                member.userId.take(8) + "…",
                                style = LocalAppTypography.current.bodyMedium,
                                color = c.textPrimary,
                            )
                            Text(
                                if (member.role == "owner") "创建者" else "成员",
                                style = LocalAppTypography.current.bodySmall,
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

        Spacer(Modifier.height(16.dp))
    }

    // 操作按钮
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SecondaryButton(onClick = onJoinClick, label = "加入新家庭", icon = Icons.Default.GroupAdd, modifier = Modifier.weight(1f))
        SecondaryButton(onClick = onCreateClick, label = "创建新家庭", icon = Icons.Default.Add, modifier = Modifier.weight(1f))
    }
}
