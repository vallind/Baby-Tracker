package com.babytracker.feature.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.babytracker.core.ui.components.chip.AppFilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.babytracker.navigation.Navigator
import com.babytracker.BabyTrackerApp
import com.babytracker.core.util.LogBuffer
import com.babytracker.core.util.LogEntry
import com.babytracker.core.ui.components.dialog.AppConfirmDialog
import kotlinx.coroutines.flow.collectLatest
import com.babytracker.core.ui.components.input.AppInput
import io.elyon.kmp.basic.HorizontalDivider
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.Scaffold
import io.elyon.kmp.basic.Surface
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TopAppBar
import io.elyon.kmp.theme.Colors
import io.elyon.kmp.theme.ElyonTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LEVELS = listOf(
    LevelOption('V', "详细"),
    LevelOption('D', "调试"),
    LevelOption('I', "信息"),
    LevelOption('W', "警告"),
    LevelOption('E', "错误"),
)

private data class LevelOption(val level: Char, val label: String)

@Composable
fun LogViewerScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val typography = ElyonTheme.textStyles
    val context = LocalContext.current

    var filter by remember { mutableStateOf("") }
    var autoScroll by remember { mutableStateOf(true) }
    var logs by remember { mutableStateOf(LogBuffer.getEntries()) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedLevels by remember { mutableStateOf(LEVELS.map { it.level }.toSet()) }
    var selectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        LogBuffer.entries.collectLatest { entries ->
            logs = entries
            if (autoScroll && entries.isNotEmpty()) {
                listState.animateScrollToItem(entries.size - 1)
            }
        }
    }

    val filteredLogs = remember(logs, filter, selectedLevels) {
        logs.filter { it.level in selectedLevels }
            .filter {
                filter.isBlank() ||
                    it.tag.contains(filter, ignoreCase = true) ||
                    it.message.contains(filter, ignoreCase = true)
            }
    }

    fun entryKey(e: LogEntry) = "log-${e.seq}"

    fun copyText(text: String, label: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("app_logs", text))
        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
    }

    fun exitSelectMode() {
        selectMode = false
        selectedIds = emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "日志查看",
                color = c.primaryContainer,
                titleColor = c.onPrimaryContainer,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 搜索 + 操作栏
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppInput(
                    value = filter,
                    onValueChange = { filter = it },
                    label = "过滤",
                    placeholder = "tag 或消息",
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { logs = LogBuffer.getEntries() },
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                IconButton(
                    onClick = { showClearConfirm = true },
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "清除")
                }
                IconButton(
                    onClick = { autoScroll = !autoScroll },
                ) {
                    Icon(
                        if (autoScroll) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (autoScroll) "关闭自动滚动" else "开启自动滚动",
                    )
                }
                IconButton(
                    onClick = {
                        if (selectMode) {
                            val text = filteredLogs
                                .filter { entryKey(it) in selectedIds }
                                .joinToString("\n") { e ->
                                    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(e.timestamp))
                                    "[${e.level}] $ts ${e.tag}: ${e.message}"
                                }
                            copyText(text, "已复制 ${selectedIds.size} 条")
                            exitSelectMode()
                        } else {
                            selectMode = true
                            selectedIds = emptySet()
                        }
                    },
                ) {
                    Icon(
                        if (selectMode) Icons.Default.ContentCopy else Icons.Default.Checklist,
                        contentDescription = if (selectMode) "复制选中" else "选择",
                    )
                }
            }
            if (selectMode && selectedIds.isNotEmpty()) {
                // 选择模式操作提示
                Surface(
                    color = c.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "已选 ${selectedIds.size} 条，点击图标复制",
                        style = typography.footnote2,
                        color = c.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // 日志等级过滤
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LEVELS.forEach { opt ->
                    val selected = opt.level in selectedLevels
                    AppFilterChip(
                        selected = selected,
                        onClick = {
                            selectedLevels = if (selected) selectedLevels - opt.level
                            else selectedLevels + opt.level
                        },
                        label = opt.label,
                        selectedColor = when (opt.level) {
                            'V' -> c.onSurfaceVariantSummary
                            'D' -> c.primary
                            'I' -> c.tertiaryContainer
                            'W' -> c.secondary
                            'E' -> c.error
                            else -> c.primary
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // 日志列表
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(filteredLogs, key = { entryKey(it) }) { entry ->
                    val key = entryKey(entry)
                    val isSelected = key in selectedIds
                    LogEntryRow(
                        entry = entry,
                        c = c,
                        selectMode = selectMode,
                        isSelected = isSelected,
                        onClick = {
                            if (selectMode) {
                                selectedIds = if (isSelected) selectedIds - key
                                else selectedIds + key
                            }
                        },
                        onLongClick = {
                            if (!selectMode) {
                                selectMode = true
                                selectedIds = setOf(key)
                            }
                        },
                    )
                    HorizontalDivider(
                        color = c.dividerLine.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = if (isSelected) 0.dp else 4.dp, end = 4.dp),
                    )
                }
            }

            // 底部统计
            Surface(
                color = c.surface,
                shadowElevation = 1.dp,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "共 ${filteredLogs.size} 条${if (filter.isNotBlank() || selectedLevels.size < 5) "（已过滤）" else ""}",
                    style = typography.footnote2,
                    color = c.onSurfaceVariantSummary,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogEntryRow(
    entry: LogEntry,
    c: Colors,
    selectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val levelColor = when (entry.level) {
        'V' -> c.onSurfaceVariantSummary
        'D' -> c.primary
        'I' -> c.tertiaryContainer
        'W' -> c.secondary
        'E' -> c.error
        else -> c.onSurface
    }
    val ts = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    val bgColor = when {
        isSelected -> levelColor.copy(alpha = 0.12f)
        selectMode -> c.surface
        else -> c.surface
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(
                if (selectMode) Modifier.clickable { onClick() }
                else Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            )
            .padding(vertical = 2.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isSelected) "✓" else "[${entry.level}]",
            color = levelColor,
            fontWeight = FontWeight.Bold,
            style = ElyonTheme.textStyles.footnote2,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = ts,
            color = c.onSurfaceVariantSummary,
            style = ElyonTheme.textStyles.footnote2,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = entry.tag,
            color = levelColor,
            fontWeight = FontWeight.Medium,
            style = ElyonTheme.textStyles.footnote2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        Text(
            text = ": ",
            color = c.onSurfaceVariantSummary,
            style = ElyonTheme.textStyles.footnote2,
        )
        Text(
            text = entry.message,
            color = c.onSurface,
            style = ElyonTheme.textStyles.footnote2,
            maxLines = 100,
        )
    }
}
