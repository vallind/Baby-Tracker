package com.babytracker.feature.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.babytracker.designsystem.components.chip.AppFilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.documentfile.provider.DocumentFile
import com.babytracker.core.domain.model.Baby
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.PrimaryButton
import com.babytracker.designsystem.components.button.SecondaryButton
import com.babytracker.designsystem.components.button.AppTextButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.dialog.AppDialog
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.babytracker.core.backup.BackupManager
import com.babytracker.designsystem.theme.AppTheme
import com.babytracker.designsystem.theme.ThemeController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.fab.AppFAB
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.section.AppListItem
import com.babytracker.navigation.Screen
import com.babytracker.core.auth.AuthService
import com.babytracker.designsystem.i18n.AppStrings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val authService: AuthService = koinInject()
    val displayAccount by authService.displayAccount.collectAsState()
    val nickname by authService.nickname.collectAsState()
    val authState by authService.observeAuthState().collectAsState(initial = null)
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()

    val isLoggedIn = authState != null
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "我的",
                showBack = false,
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
        ) {
            Spacer(Modifier.height(12.dp))

            UserInfoCard(
                babyName = baby?.name ?: "未设置",
                displayAccount = displayAccount,
                nickname = nickname,
                isLoggedIn = isLoggedIn,
                onClick = {
                    if (isLoggedIn) {
                        navController.navigate(Screen.Family.route)
                    } else {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onEditNickname = if (isLoggedIn) {
                    { showNicknameDialog = true }
                } else null,
            )

            Spacer(Modifier.height(12.dp))

            SettingsSectionTitle("宝宝与家庭")
            SettingsCard {
                SettingsRow(
                    emoji = "👶",
                    label = "宝宝管理",
                    subtitle = "资料、成长信息与宝宝切换",
                    onClick = { navController.navigate(Screen.BabyManagement.route) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "👨‍👩‍👧",
                    label = "家庭与账号",
                    subtitle = if (isLoggedIn) "成员管理与账号信息" else "登录后与家人共享记录",
                    onClick = {
                        navController.navigate(
                            if (isLoggedIn) Screen.Family.route else Screen.Login.route
                        )
                    },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🔔",
                    label = "提醒设置",
                    subtitle = "喂养、睡眠与护理提醒",
                    onClick = { navController.navigate(Screen.Reminder.route) },
                )
            }

            Spacer(Modifier.height(12.dp))

            SettingsSectionTitle("更多设置")
            SettingsCard {
                SettingsRow(
                    emoji = "🎨",
                    label = "使用偏好",
                    subtitle = "主题与 AI 助手",
                    onClick = { navController.navigate(Screen.PreferenceSettings.route) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🔒",
                    label = "数据与同步",
                    subtitle = "云同步、备份与隐私",
                    onClick = { navController.navigate(Screen.DataSettings.route) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "❓",
                    label = "帮助与关于",
                    subtitle = "问题反馈、运行日志与版本信息",
                    onClick = { navController.navigate(Screen.SupportSettings.route) },
                )
            }

            Spacer(Modifier.height(spacing.lg))

            if (isLoggedIn) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    AppTextButton(
                        onClick = { showLogoutConfirm = true },
                        label = "退出登录",
                        color = c.danger,
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))
        }
    }

    AppConfirmDialog(
        show = showLogoutConfirm,
        title = "退出登录",
        message = "退出后数据保留在本地，云同步将停止。确定退出？",
        confirmText = "退出",
        onConfirm = {
            scope.launch { authService.signOut() }
            showLogoutConfirm = false
        },
        onDismiss = { showLogoutConfirm = false },
    )

    if (showNicknameDialog) {
        NicknameEditDialog(
            currentNickname = nickname ?: "",
            onDismiss = { showNicknameDialog = false },
            onSave = { newNickname ->
                authService.setNickname(newNickname)
                showNicknameDialog = false
                Toast.makeText(context, AppStrings.nicknameSaved, Toast.LENGTH_SHORT).show()
            },
            onClear = {
                authService.clearNickname()
                showNicknameDialog = false
            },
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  用户信息卡片
// ═══════════════════════════════════════════════════════════

@Composable
private fun UserInfoCard(
    babyName: String,
    displayAccount: String?,
    nickname: String?,
    isLoggedIn: Boolean,
    onClick: (() -> Unit)? = null,
    onEditNickname: (() -> Unit)? = null,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val elev = LocalAppElevation.current
    val displayName = nickname ?: displayAccount ?: babyName

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        elevation = elev.level2,
        containerColor = c.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(c.primary, c.primary.copy(alpha = 0.7f)),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    displayName.take(1).ifEmpty { "?" },
                    style = LocalAppTypography.current.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimary,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = LocalAppTypography.current.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                    )
                    if (isLoggedIn && onEditNickname != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "修改昵称",
                            tint = c.textTertiary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(onClick = onEditNickname),
                        )
                    }
                }
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = if (isLoggedIn && displayAccount != null) {
                        if (nickname != null) "账号: ${displayAccount.take(8)}…" else "ID: ${displayAccount.take(8)}…"
                    } else "点击登录账号",
                    style = LocalAppTypography.current.bodySmall,
                    color = c.textTertiary,
                    maxLines = 1,
                )
            }

            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = c.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  设置区块标题
// ═══════════════════════════════════════════════════════════

@Composable
private fun SettingsSectionTitle(title: String) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    Text(
        title,
        style = LocalAppTypography.current.labelMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(bottom = spacing.sm),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(themeCtrl: ThemeController, onDismiss: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    val names = mapOf("pure" to "纯净蓝", "aurora" to "极光紫", "warm" to "暖阳粉", "sunny" to "阳光黄", "night" to "暗夜深", "morandi" to "莫兰迪")
    AppBottomSheet(
        show = true,
        onDismiss = onDismiss,
    ) {
        Column(Modifier.padding(spacing.md)) {
            Text("选择主题", style = LocalAppTypography.current.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTheme.all.forEach { theme ->
                    val selected = themeCtrl.currentTheme.name == theme.name
                    AppCard(
                        modifier = Modifier
                            .width(120.dp)
                            .height(96.dp)
                            .border(
                                if (selected) BorderStroke(2.dp, c.primary) else BorderStroke(1.dp, c.outline),
                                RoundedCornerShape(shapes.large),
                            )
                            .clickable { themeCtrl.switchTheme(theme.name) },
                        elevation = elev.level1,
                        containerColor = theme.colors.card,
                    ) {
                        Box(Modifier.fillMaxSize().padding(12.dp)) {
                            Column {
                                Box(Modifier.size(36.dp).clip(RoundedCornerShape(shapes.large)).background(theme.colors.primary))
                                Spacer(Modifier.height(6.dp))
                                Text(names[theme.name] ?: theme.name, style = LocalAppTypography.current.bodySmall, color = theme.colors.textPrimary)
                            }
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = c.primary, modifier = Modifier.align(Alignment.TopEnd).size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    AppCardGroup(content = content)
}

@Composable
fun SettingsDivider() {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    HorizontalDivider(
        color = c.divider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = spacing.md),
    )
}

@Composable
fun SettingsRow(emoji: String, label: String, subtitle: String? = null, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit = {}) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    AppListItem(
        leadingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = LocalAppTypography.current.titleMedium)
            }
        },
        headlineContent = {
            Text(label, style = LocalAppTypography.current.bodyMedium, color = c.textPrimary)
        },
        supportingContent = subtitle?.let {
            {
                Text(it, style = LocalAppTypography.current.bodySmall, color = c.textTertiary)
            }
        },
        trailingContent = trailing ?: {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        },
        onClick = onClick,
    )
}

@Composable
fun ThemeDots(currentTheme: String, onClick: () -> Unit) {
    val c = LocalAppColors.current
    val themesColors = mapOf(
        "pure" to 0xFF2563EB, "aurora" to 0xFF7C3AED, "warm" to 0xFFFF8A80,
        "sunny" to 0xFFF59E0B, "night" to 0xFF1E293B, "morandi" to 0xFF94A3B8,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        themesColors.forEach { (name, colorInt) ->
            Box(
                Modifier.size(20.dp).clip(CircleShape).background(Color(colorInt))
                    .then(if (name == currentTheme) Modifier.border(2.dp, c.primary, CircleShape) else Modifier),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyManagementScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val vacRepo: VaccinationRepository = koinInject()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val activeBabies = babies.filter { it.deletedAt == null }
    val deletedBabies = babies.filter { it.deletedAt != null }
    var showDeleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var editingBaby by remember { mutableStateOf<Baby?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Baby?>(null) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "宝宝管理",
                onBack = { navController.popBackStack() },
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
                    val isCurrent = b.id == babyCtrl.currentBabyId
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xs)
                            .border(
                                BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) c.primary else c.outline),
                                RoundedCornerShape(shapes.large),
                            )
                            .clickable { editingBaby = b; showForm = true },
                        elevation = elev.level1,
                    ) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(c.primaryContainer), contentAlignment = Alignment.Center) {
                                Text(b.name.take(1), color = c.primary, fontWeight = FontWeight.SemiBold, style = LocalAppTypography.current.titleMedium)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(b.name, style = LocalAppTypography.current.titleSmall)
                                    if (isCurrent) {
                                        Spacer(Modifier.width(spacing.sm))
                                        Surface(color = c.primaryContainer, shape = RoundedCornerShape(shapes.medium)) {
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
                                AppTextButton(
                                    onClick = { babyCtrl.selectBaby(b.id); navController.popBackStack() },
                                    label = "切换",
                                )
                            }
                            AppIconButton(icon = Icons.Default.Delete, onClick = { showDeleteConfirm = b }, contentDescription = "删除", tint = c.textSecondary)
                        }
                    }
                }

                if (deletedBabies.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.md))
                    AppTextButton(
                        onClick = { showDeleted = !showDeleted },
                        label = "已删除的宝宝 (${deletedBabies.size}) ${if (showDeleted) "▲" else "▼"}",
                        color = c.textSecondary,
                    )
                    if (showDeleted) {
                        deletedBabies.forEach { b ->
                            AppCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.xs),
                                elevation = elev.level1,
                            ) {
                                Row(Modifier.padding(spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).clip(CircleShape).background(c.textTertiary), contentAlignment = Alignment.Center) {
                                        Text(b.name.take(1), color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(b.name, style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
                                        Text("已删除", style = LocalAppTypography.current.labelSmall, color = c.textTertiary)
                                    }
                                    AppTextButton(
                                        onClick = { scope.launch { babyRepo.restore(b) } },
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
                scope.launch {
                    if (editingBaby != null) babyRepo.update(baby)
                    else {
                        val babyId = babyRepo.insert(baby).toInt()
                        VaccineSchedule.createForBaby(babyId, baby.birthDate).forEach { vacRepo.insert(it) }
                    }
                }
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
            showDeleteConfirm?.let { baby ->
                scope.launch { babyRepo.delete(baby) }
            }
            showDeleteConfirm = null
        },
        onDismiss = { showDeleteConfirm = null },
    )
}

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun NicknameEditDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
) {
    val c = LocalAppColors.current
    var input by remember(currentNickname) { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.editNickname) },
        text = {
            AppInput(
                value = input,
                onValueChange = { input = it },
                label = AppStrings.nicknameHint,
                placeholder = "输入你喜欢的昵称",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            AppTextButton(
                onClick = { onSave(input.trim()) },
                enabled = input.isNotBlank(),
                label = AppStrings.save,
            )
        },
        dismissButton = {
            Row {
                if (currentNickname.isNotEmpty()) {
                    AppTextButton(onClick = onClear, label = "清除", color = c.textTertiary)
                }
                AppTextButton(onClick = onDismiss, label = AppStrings.cancel)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(navController: NavController) {
    val backupManager: BackupManager = koinInject()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var backupPath by remember { mutableStateOf("") }
    var selectedDirUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDirName by remember { mutableStateOf("") }
    var webdavUrl by remember { mutableStateOf("") }
    var webdavUser by remember { mutableStateOf("") }
    var webdavPass by remember { mutableStateOf("") }
    var webdavStatus by remember { mutableStateOf("未配置") }
    var showWebDAV by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreFileUri by remember { mutableStateOf<Uri?>(null) }
    var showWebdavRestoreConfirm by remember { mutableStateOf(false) }
    var restoring by remember { mutableStateOf(false) }

    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        restoreFileUri = uri
        if (uri != null) showRestoreConfirm = true
    }

    LaunchedEffect(Unit) {
        backupManager.loadConfig()?.let { c ->
            webdavUrl = c.webdavUrl ?: ""
            webdavUser = c.webdavUser ?: ""
            webdavPass = c.webdavPass ?: ""
            if (c.webdavUrl != null) {
                showWebDAV = true
                webdavStatus = c.lastBackupAt?.let { "上次: $it" } ?: "已配置"
            }
        }
    }

    val dirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            selectedDirUri = it
            val doc = DocumentFile.fromTreeUri(context, it)
            selectedDirName = doc?.name ?: doc?.uri?.lastPathSegment ?: "已选择目录"
        }
    }

    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    AppScaffold(topBar = {
        AppTopBar(
            title = "备份管理",
            onBack = { navController.popBackStack() },
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = spacing.md, vertical = spacing.md)) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = elev.level1,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(shapes.large)).background(c.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = c.primary)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("本地备份", style = LocalAppTypography.current.titleSmall)
                            Text("选择备份保存位置", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        onClick = { dirPicker.launch(null) },
                        label = "选择目录",
                        icon = Icons.Default.FolderOpen,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (selectedDirName.isNotEmpty()) {
                        Spacer(Modifier.height(spacing.sm))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(shapes.medium)).background(c.surfaceElevated).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", style = LocalAppTypography.current.bodyMedium)
                            Spacer(Modifier.width(spacing.sm))
                            Text(selectedDirName, style = LocalAppTypography.current.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        onClick = {
                            scope.launch {
                                val path = if (selectedDirUri != null) {
                                    backupManager.createLocalBackupToUri(context, selectedDirUri!!)
                                } else {
                                    backupManager.createLocalBackup(context)
                                }
                                backupPath = path ?: ""
                                Toast.makeText(context, if (path != null) "备份完成" else "备份失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = "开始备份",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (backupPath.isNotEmpty()) {
                Text("上次备份: $backupPath", style = LocalAppTypography.current.bodySmall, color = c.textSecondary, modifier = Modifier.padding(top = spacing.sm))
            }
            Spacer(Modifier.height(12.dp))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = elev.level1,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(shapes.large)).background(c.tertiary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Text("☁️", style = LocalAppTypography.current.titleMedium)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("WebDAV 云备份", style = LocalAppTypography.current.titleSmall)
                            Text(webdavStatus, style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!showWebDAV) {
                        SecondaryButton(onClick = { showWebDAV = true }, label = "配置 WebDAV", modifier = Modifier.fillMaxWidth())
                    } else {
                        AppInput(value = webdavUrl, onValueChange = { webdavUrl = it }, label = "服务器地址", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(spacing.sm))
                        AppInput(value = webdavUser, onValueChange = { webdavUser = it }, label = "用户名", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(spacing.sm))
                        AppInput(value = webdavPass, onValueChange = { webdavPass = it }, label = "密码", modifier = Modifier.fillMaxWidth(), isPassword = true)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            SecondaryButton(onClick = {
                                scope.launch {
                                    backupManager.saveConfig(webdavUrl, webdavUser, webdavPass)
                                    webdavStatus = "已保存"
                                    Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                                }
                            }, label = "保存", modifier = Modifier.weight(1f))
                            PrimaryButton(onClick = {
                                scope.launch {
                                    backupManager.createWebDAVBackup().onSuccess {
                                        webdavStatus = "上次: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                                        Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "备份失败: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, label = "备份", modifier = Modifier.weight(1f))
                            PrimaryButton(onClick = { showWebdavRestoreConfirm = true }, label = "恢复", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = elev.level1,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(shapes.large)).background(c.error.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = c.error)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("恢复备份", style = LocalAppTypography.current.titleSmall)
                            Text("从 zip 文件导入数据", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        onClick = { restorePicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                        label = if (restoring) "恢复中..." else "选择备份文件",
                        enabled = !restoring,
                        containerColor = c.error,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    AppConfirmDialog(
        show = showRestoreConfirm,
        title = "恢复数据",
        message = "恢复将导入备份中的宝宝和记录数据，已有数据不受影响。确定继续？",
        confirmText = "恢复",
        onConfirm = {
            showRestoreConfirm = false
            restoring = true
            scope.launch {
                restoreFileUri?.let { uri ->
                    backupManager.restoreFromUri(context, uri).onSuccess {
                        Toast.makeText(context, "恢复完成", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "恢复失败: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                restoring = false
            }
        },
        onDismiss = { showRestoreConfirm = false },
    )
    AppConfirmDialog(
        show = showWebdavRestoreConfirm,
        title = "云端恢复",
        message = "将从 WebDAV 下载最新备份并恢复",
        confirmText = "恢复",
        onConfirm = {
            showWebdavRestoreConfirm = false; restoring = true
            scope.launch {
                backupManager.restoreFromWebDAV().onSuccess { Toast.makeText(context, "云端恢复完成", Toast.LENGTH_SHORT).show() }.onFailure { Toast.makeText(context, "恢复失败: ${it.message}", Toast.LENGTH_SHORT).show() }
                restoring = false
            }
        },
        onDismiss = { showWebdavRestoreConfirm = false },
    )
}
