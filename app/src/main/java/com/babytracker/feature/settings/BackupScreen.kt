package com.babytracker.feature.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.tintContainer

/**
 * 备份/恢复页 — 纯 UI 渲染层（Batch 4）。
 * 目录/文件选择、确认弹层显隐留 Screen；BackupManager 全部操作在 BackupViewModel。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    state: BackupUiState,
    onLoadConfig: () -> Unit,
    onBack: () -> Unit,
    onWebdavUrlChange: (String) -> Unit,
    onWebdavUserChange: (String) -> Unit,
    onWebdavPassChange: (String) -> Unit,
    onSaveWebdav: () -> Unit,
    onCreateLocalBackup: (Uri?) -> Unit,
    onCreateWebdavBackup: () -> Unit,
    onRestoreFromUri: (Uri) -> Unit,
    onRestoreFromWebdav: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedDirUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDirName by remember { mutableStateOf("") }
    var showWebDAV by remember { mutableStateOf(state.webdavUrl.isNotEmpty()) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var restoreFileUri by remember { mutableStateOf<Uri?>(null) }
    var showWebdavRestoreConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoadConfig() }

    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        restoreFileUri = uri
        if (uri != null) showRestoreConfirm = true
    }

    val dirPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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
            onBack = onBack,
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
                    Spacer(Modifier.height(spacing.md))
                    AppButton(
                        variant = ButtonVariant.Secondary,
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
                    Spacer(Modifier.height(spacing.md))
                    AppButton(
                        onClick = { onCreateLocalBackup(selectedDirUri) },
                        label = "开始备份",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (state.backupPath.isNotEmpty()) {
                Text("上次备份: ${state.backupPath}", style = LocalAppTypography.current.bodySmall, color = c.textSecondary, modifier = Modifier.padding(top = spacing.sm))
            }
            Spacer(Modifier.height(spacing.md))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = elev.level1,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(shapes.large)).background(AppColorScale.fromSeed(c.tertiary).tintContainer(c)), contentAlignment = Alignment.Center) {
                            Text("☁️", style = LocalAppTypography.current.titleMedium)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("WebDAV 云备份", style = LocalAppTypography.current.titleSmall)
                            Text(state.webdavStatus, style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(spacing.md))
                    if (!showWebDAV) {
                        AppButton(variant = ButtonVariant.Secondary, onClick = { showWebDAV = true }, label = "配置 WebDAV", modifier = Modifier.fillMaxWidth())
                    } else {
                        AppInput(value = state.webdavUrl, onValueChange = onWebdavUrlChange, label = "服务器地址", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(spacing.sm))
                        AppInput(value = state.webdavUser, onValueChange = onWebdavUserChange, label = "用户名", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(spacing.sm))
                        AppInput(value = state.webdavPass, onValueChange = onWebdavPassChange, label = "密码", modifier = Modifier.fillMaxWidth(), isPassword = true)
                        Spacer(Modifier.height(spacing.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            AppButton(variant = ButtonVariant.Secondary, onClick = onSaveWebdav, label = "保存", modifier = Modifier.weight(1f))
                            AppButton(onClick = onCreateWebdavBackup, label = "备份", modifier = Modifier.weight(1f))
                            AppButton(onClick = { showWebdavRestoreConfirm = true }, label = "恢复", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.md))

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = elev.level1,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(shapes.large)).background(AppColorScale.fromSeed(c.error).tintContainer(c)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = c.error)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("恢复备份", style = LocalAppTypography.current.titleSmall)
                            Text("从 zip 文件导入数据", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                    }
                    Spacer(Modifier.height(spacing.md))
                    AppButton(
                        onClick = { restorePicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                        label = if (state.restoring) "恢复中..." else "选择备份文件",
                        enabled = !state.restoring,
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
            restoreFileUri?.let { uri -> onRestoreFromUri(uri) }
        },
        onDismiss = { showRestoreConfirm = false },
    )
    AppConfirmDialog(
        show = showWebdavRestoreConfirm,
        title = "云端恢复",
        message = "将从 WebDAV 下载最新备份并恢复",
        confirmText = "恢复",
        onConfirm = {
            showWebdavRestoreConfirm = false
            onRestoreFromWebdav()
        },
        onDismiss = { showWebdavRestoreConfirm = false },
    )
}