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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.documentfile.provider.DocumentFile
import com.babytracker.core.database.entity.BabyEntity
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalThemeColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.backup.BackupManager
import com.babytracker.designsystem.theme.AppTheme
import com.babytracker.designsystem.theme.ThemeController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.designsystem.components.bottomnav.BottomNavBar
import com.babytracker.navigation.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val themeCtrl: ThemeController = koinInject()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = c.bg,
        bottomBar = { BottomNavBar(navController) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.bg),
        ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // —— 顶部头部区（浅蓝渐变 + 宝宝头像 + "我的" 标题）——
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Gradients.pageHeader(c))
                    .padding(horizontal = DT.pageMargin.dp),
            ) {
                Row(
                    Modifier.padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Gradients.primary(c)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            baby?.name?.take(1) ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            baby?.name ?: "未设置",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${baby?.let { DateUtils.monthAge(java.time.LocalDate.parse(it.birthDate)) } ?: ""} · ${baby?.let { if (it.gender == "男") "男宝" else "女宝" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                        )
                    }
                }
            }

            Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
                Spacer(Modifier.height(DT.cardGap.dp))

                SectionTitle("设置")
                SettingsCard {
                    SettingsRow("👤", "宝宝信息", onClick = { navController.navigate(Screen.BabyManagement.route) })
                    SettingsDivider()
                    SettingsRow("☁️", "数据备份", onClick = { navController.navigate(Screen.Backup.route) })
                    SettingsDivider()
                    SettingsRow("🎨", "主题", trailing = { ThemeDots(themeCtrl.currentTheme.name, onClick = { showPicker = true }) }, onClick = { showPicker = true })
                    SettingsDivider()
                    SettingsRow("🔔", "通知提醒", trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = c.primary,
                                checkedThumbColor = Color.White,
                                uncheckedTrackColor = c.divider,
                                uncheckedThumbColor = Color.White,
                            ),
                        )
                    })
                }
                Spacer(Modifier.height(DT.cardGap.dp))

                SectionTitle("其他")
                SettingsCard {
                    SettingsRow("ℹ️", "关于我们", onClick = { })
                    SettingsDivider()
                    SettingsRow("⭐", "给我们评分", onClick = { })
                }
                Spacer(Modifier.height(40.dp))
            }
        }
        }
    }

    if (showPicker) {
        ThemePickerSheet(themeCtrl = themeCtrl, onDismiss = { showPicker = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(themeCtrl: ThemeController, onDismiss: () -> Unit) {
    val names = mapOf("pure" to "纯净蓝", "aurora" to "极光紫", "warm" to "暖阳粉", "sunny" to "阳光黄", "night" to "暗夜深", "morandi" to "莫兰迪")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = DT.pageMargin.dp, vertical = 16.dp)) {
            Text("选择主题", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTheme.all.forEach { theme ->
                    val selected = themeCtrl.currentTheme.name == theme.name
                    Card(
                        onClick = { themeCtrl.switchTheme(theme.name) },
                        modifier = Modifier.width(120.dp).height(96.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.colors.card),
                    ) {
                        Box(Modifier.fillMaxSize().padding(12.dp)) {
                            Column {
                                Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(theme.colors.primary))
                                Spacer(Modifier.height(6.dp))
                                Text(names[theme.name] ?: theme.name, style = MaterialTheme.typography.bodySmall, color = theme.colors.textPrimary)
                            }
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.TopEnd).size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    val c = LocalThemeColors.current
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

/** 统一白色设置卡片（圆角 DT.cardRadius / 阴影 DT.cardElevation）。 */
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val c = LocalThemeColors.current
    val cardShape = RoundedCornerShape(DT.cardRadius.dp)
    Card(
        Modifier.fillMaxWidth(),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = DT.cardElevation.dp),
        colors = CardDefaults.cardColors(containerColor = c.card),
    ) {
        Column(content = content)
    }
}

/** 设置项分割线（c.divider）。 */
@Composable
fun SettingsDivider() {
    val c = LocalThemeColors.current
    HorizontalDivider(
        color = c.divider,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = DT.cardInnerPadding.dp),
    )
}

@Composable
fun SettingsRow(emoji: String, label: String, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit = {}) {
    val c = LocalThemeColors.current
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = DT.cardInnerPadding.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(DT.iconBgSize.dp)
                .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                .background(c.primaryLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
        if (trailing != null) {
            trailing()
        } else {
            Text("›", color = c.textHint, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ThemeDots(currentTheme: String, onClick: () -> Unit) {
    val c = LocalThemeColors.current
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
    val c = LocalThemeColors.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val vacRepo: VaccinationRepository = koinInject()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var editingBaby by remember { mutableStateOf<BabyEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<BabyEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("宝宝管理") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true; editingBaby = null }) {
                Icon(Icons.Default.Add, contentDescription = "添加宝宝")
            }
        },
    ) { padding ->
        if (babies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("还没有添加宝宝", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = DT.pageMargin.dp, vertical = 8.dp)) {
                babies.forEach { b ->
                    val isCurrent = b.id == babyCtrl.currentBabyId
                    Card(
                        onClick = { editingBaby = b; showForm = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = c.card),
                    ) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Text(b.name.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(b.name, style = MaterialTheme.typography.titleSmall)
                                    if (isCurrent) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                            Text("当前", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text("${b.gender} · ${DateUtils.monthAge(java.time.LocalDate.parse(b.birthDate))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!isCurrent) {
                                TextButton(onClick = { babyCtrl.selectBaby(b.id); navController.popBackStack() }) {
                                    Text("切换", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { showDeleteConfirm = b }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = c.tagText)
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

    showDeleteConfirm?.let { baby ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${baby.name} 的所有数据吗？") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { babyRepo.delete(baby) }
                    showDeleteConfirm = null
                }) { Text("删除", color = c.tagText) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            },
        )
    }
}

@Composable
fun BabyFormDialog(baby: BabyEntity?, onDismiss: () -> Unit, onSave: (BabyEntity) -> Unit) {
    val isEdit = baby != null
    var name by remember { mutableStateOf(baby?.name ?: "") }
    var gender by remember { mutableStateOf(baby?.gender ?: "男") }
    var birthDate by remember { mutableStateOf(baby?.birthDate ?: java.time.LocalDate.now().toString()) }
    var birthWeight by remember { mutableStateOf(baby?.birthWeight?.toString() ?: "") }
    var birthHeight by remember { mutableStateOf(baby?.birthHeight?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑宝宝" else "添加宝宝") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("男", "女").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g) },
                        )
                    }
                }

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("出生日期 (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )

                OutlinedTextField(
                    value = birthWeight,
                    onValueChange = { birthWeight = it },
                    label = { Text("出生体重 (kg，可选)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )

                OutlinedTextField(
                    value = birthHeight,
                    onValueChange = { birthHeight = it },
                    label = { Text("出生身高 (cm，可选)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(BabyEntity(
                        id = baby?.id ?: 0,
                        name = name,
                        gender = gender,
                        birthDate = birthDate,
                        birthWeight = birthWeight.toDoubleOrNull(),
                        birthHeight = birthHeight.toDoubleOrNull(),
                        createdAt = baby?.createdAt ?: java.time.LocalDateTime.now().toString(),
                    ))
                },
                enabled = name.isNotBlank(),
            ) { Text(if (isEdit) "保存" else "添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
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

    val c = LocalThemeColors.current
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("备份管理") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ))
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = DT.pageMargin.dp, vertical = DT.pageMargin.dp)) {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("本地备份", style = MaterialTheme.typography.titleSmall)
                            Text("选择备份保存位置", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { dirPicker.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("选择目录", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (selectedDirName.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(selectedDirName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) { Text("开始备份") }
                }
            }
            if (backupPath.isNotEmpty()) {
                Text("上次备份: $backupPath", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(DT.cardGap.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) {
                            Text("☁️", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("WebDAV 云备份", style = MaterialTheme.typography.titleSmall)
                            Text(webdavStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!showWebDAV) {
                        OutlinedButton(onClick = { showWebDAV = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                            Text("配置 WebDAV", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        OutlinedTextField(value = webdavUrl, onValueChange = { webdavUrl = it }, label = { Text("服务器地址") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.small)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = webdavUser, onValueChange = { webdavUser = it }, label = { Text("用户名") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.small)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = webdavPass, onValueChange = { webdavPass = it }, label = { Text("密码") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium, visualTransformation = PasswordVisualTransformation())
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                scope.launch {
                                    backupManager.saveConfig(webdavUrl, webdavUser, webdavPass)
                                    webdavStatus = "已保存"
                                    Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
                                }
                            }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
                                Text("保存", style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(onClick = {
                                scope.launch {
                                    backupManager.createWebDAVBackup().onSuccess {
                                        webdavStatus = "上次: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                                        Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "备份失败: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
                                Text("备份", style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(onClick = { showWebdavRestoreConfirm = true }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.small) {
                                Text("恢复", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(DT.cardGap.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(containerColor = c.card),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("恢复备份", style = MaterialTheme.typography.titleSmall)
                            Text("从 zip 文件导入数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { restorePicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !restoring,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) { Text(if (restoring) "恢复中..." else "选择备份文件", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("恢复数据") },
            text = { Text("恢复将导入备份中的宝宝和记录数据，已有数据不受影响。确定继续？") },
            confirmButton = {
                TextButton(onClick = {
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
                }) { Text("恢复", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("取消") }
            },
        )
    }
    if (showWebdavRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showWebdavRestoreConfirm = false },
            title = { Text("云端恢复") },
            text = { Text("将从 WebDAV 下载最新备份并恢复") },
            confirmButton = {
                TextButton(onClick = {
                    showWebdavRestoreConfirm = false; restoring = true
                    scope.launch {
                        backupManager.restoreFromWebDAV().onSuccess { Toast.makeText(context, "云端恢复完成", Toast.LENGTH_SHORT).show() }.onFailure { Toast.makeText(context, "恢复失败: ${it.message}", Toast.LENGTH_SHORT).show() }
                        restoring = false
                    }
                }) { Text("恢复", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showWebdavRestoreConfirm = false }) { Text("取消") } },
        )
    }
}