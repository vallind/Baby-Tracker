package com.babytracker.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
import com.babytracker.BabyTrackerApp
import com.babytracker.core.util.LogBuffer
import com.babytracker.core.util.LogEntry
import com.babytracker.designsystem.components.dialog.AppConfirmDialog
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogViewerScreen(navController: NavController) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val context = LocalContext.current

    var filter by remember { mutableStateOf("") }
    var autoScroll by remember { mutableStateOf(true) }
    var logs by remember { mutableStateOf(LogBuffer.getEntries()) }
    var showClearConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            logs = LogBuffer.getEntries()
            if (autoScroll && logs.isNotEmpty()) {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    val filteredLogs = remember(logs, filter) {
        if (filter.isBlank()) logs
        else logs.filter {
            it.tag.contains(filter, ignoreCase = true) ||
                it.message.contains(filter, ignoreCase = true)
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "日志查看",
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppInput(
                    value = filter,
                    onValueChange = { filter = it },
                    label = "过滤",
                    placeholder = "tag 或消息",
                    modifier = Modifier.weight(1f),
                )
                AppIconButton(
                    icon = Icons.Default.Refresh,
                    onClick = { logs = LogBuffer.getEntries() },
                    contentDescription = "刷新",
                )
                AppIconButton(
                    icon = Icons.Default.Delete,
                    onClick = { showClearConfirm = true },
                    contentDescription = "清除",
                )
                AppIconButton(
                    icon = if (autoScroll) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    onClick = { autoScroll = !autoScroll },
                    contentDescription = if (autoScroll) "关闭自动滚动" else "开启自动滚动",
                )
                AppIconButton(
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        val text = filteredLogs.joinToString("\n") { e ->
                            val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(e.timestamp))
                            "[${e.level}] $ts ${e.tag}: ${e.message}"
                        }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("app_logs", text))
                        Toast.makeText(context, "已复制 ${filteredLogs.size} 条日志", Toast.LENGTH_SHORT).show()
                    },
                    contentDescription = "复制日志",
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = spacing.sm),
            ) {
                items(filteredLogs, key = {
                    "${it.timestamp}-${it.level}-${it.tag}-${it.message.hashCode()}"
                }) { entry ->
                    LogEntryRow(entry = entry, c = c)
                    HorizontalDivider(
                        color = c.divider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
            }

            Surface(
                color = c.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "共 ${filteredLogs.size} 条${if (filter.isNotBlank()) "（已过滤）" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textSecondary,
                    modifier = Modifier.padding(
                        horizontal = spacing.md,
                        vertical = spacing.xs,
                    ),
                )
            }
        }
    }

    if (showClearConfirm) {
        AppConfirmDialog(
            show = showClearConfirm,
            title = "清除日志",
            message = "确定清除所有日志？此操作不可恢复。",
            confirmText = "清除",
            onConfirm = {
                val app = context.applicationContext as BabyTrackerApp
                app.appLogTree.clearLogs()
                logs = emptyList()
                showClearConfirm = false
            },
            onDismiss = { showClearConfirm = false },
        )
    }
}

@Composable
private fun LogEntryRow(entry: LogEntry, c: AppColors) {
    val levelColor = when (entry.level) {
        'V' -> c.textTertiary
        'D' -> c.info
        'I' -> c.success
        'W' -> c.warning
        'E' -> c.danger
        else -> c.textPrimary
    }
    val ts = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "[${entry.level}]",
            color = levelColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = ts,
            color = c.textTertiary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = entry.tag,
            color = levelColor,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        Text(
            text = ": ",
            color = c.textTertiary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = entry.message,
            color = c.textPrimary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 100,
        )
    }
}
