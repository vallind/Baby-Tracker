package com.babytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.navigation.Navigator
import com.babytracker.core.sync.BgInterval
import com.babytracker.core.sync.SyncDelay
import io.elyon.kmp.basic.Button
import io.elyon.kmp.basic.ButtonDefaults
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.RadioButton
import io.elyon.kmp.basic.Scaffold
import io.elyon.kmp.basic.Switch
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TopAppBar
import io.elyon.kmp.overlay.OverlayBottomSheet
import io.elyon.kmp.theme.ElyonTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SyncSettingsScreen(navigator: Navigator) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val syncViewModel: SyncViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val config = settings.sync
    val connectionState by syncViewModel.connectionState.collectAsState()
    val isOnline by syncViewModel.isOnline.collectAsState()
    val syncState by syncViewModel.syncState.collectAsState()
    val syncResult by syncViewModel.syncResult.collectAsState()
    val syncRunId by syncViewModel.syncRunId.collectAsState()

    val c = ElyonTheme.colorScheme
    val typography = ElyonTheme.textStyles

    var showDelaySheet by remember { mutableStateOf(false) }
    var showBgSheet by remember { mutableStateOf(false) }

    val syncStatus = when {
        syncState.name == "SYNCING" || syncState.name == "PUSHING" || syncState.name == "PULLING" -> "同步中..."
        !isOnline -> "离线"
        connectionState.name == "CONNECTED" -> "已连接"
        connectionState.name == "CONNECTING" -> "连接中..."
        connectionState.name == "ERROR" -> "连接失败"
        else -> "待同步"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "同步设置",
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
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            SettingsCard {
                SettingsRow(
                    emoji = "🔄",
                    label = "自动同步",
                    trailing = {
                        Switch(
                            checked = config.autoSync,
                            onCheckedChange = { enabled ->
                                settingsViewModel.updateSync { it.copy(autoSync = enabled) }
                            },
                        )
                    },
                )
                SettingsDivider()

                SettingsRow(
                    emoji = "⏱",
                    label = "同步延迟",
                    subtitle = config.syncDelay.label,
                    onClick = { showDelaySheet = true },
                )
                SettingsDivider()

                SettingsRow(
                    emoji = "📅",
                    label = "后台同步",
                    subtitle = config.bgInterval.label,
                    onClick = { showBgSheet = true },
                )
                SettingsDivider()

                SettingsRow(
                    emoji = "🚪",
                    label = "退出时同步",
                    trailing = {
                        Switch(
                            checked = config.syncOnExit,
                            onCheckedChange = { enabled ->
                                settingsViewModel.updateSync { it.copy(syncOnExit = enabled) }
                            },
                        )
                    },
                )
                SettingsDivider()

                SettingsRow(
                    emoji = "📶",
                    label = "仅 Wi‑Fi",
                    trailing = {
                        Switch(
                            checked = config.wifiOnly,
                            onCheckedChange = { enabled ->
                                settingsViewModel.updateSync { it.copy(wifiOnly = enabled) }
                            },
                        )
                    },
                )
            }

            Spacer(Modifier.height(24.dp))

            var syncing by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    syncing = true
                    syncViewModel.manualSync()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    color = c.primary,
                    contentColor = c.onPrimary,
                ),
            ) {
                Text(if (syncing) "同步中..." else "立即同步")
            }

            // 用自增 runId 复位：结果字符串相同（如连续两次"无数据需同步"）时 StateFlow 去重不会重新发射
            LaunchedEffect(syncRunId) {
                if (syncRunId > 0) syncing = false
            }

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        syncResult != null -> "$syncStatus·${syncResult}"
                        else -> syncStatus
                    },
                    style = typography.footnote1,
                    color = c.onSurfaceVariantSummary,
                )
            }
        }
    }

    if (showDelaySheet) {
        OverlayBottomSheet(show = true, onDismissRequest = { showDelaySheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("同步延迟", style = typography.title1, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                SyncDelay.entries.forEach { delay ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).clickable {
                            settingsViewModel.updateSync { it.copy(syncDelay = delay) }
                            showDelaySheet = false
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = config.syncDelay == delay,
                            onClick = {
                                settingsViewModel.updateSync { it.copy(syncDelay = delay) }
                                showDelaySheet = false
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(delay.label, style = typography.body1)
                    }
                }
            }
        }
    }

    if (showBgSheet) {
        OverlayBottomSheet(show = true, onDismissRequest = { showBgSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("后台同步", style = typography.title1, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                BgInterval.entries.forEach { interval ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).clickable {
                            settingsViewModel.updateSync { it.copy(bgInterval = interval) }
                            showBgSheet = false
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = config.bgInterval == interval,
                            onClick = {
                                settingsViewModel.updateSync { it.copy(bgInterval = interval) }
                                showBgSheet = false
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(interval.label, style = typography.body1)
                    }
                }
            }
        }
    }
}

private fun SettingsViewModel.updateSync(
    transform: (com.babytracker.core.sync.SyncConfig) -> com.babytracker.core.sync.SyncConfig,
) {
    updateSettings { current ->
        current.copy(sync = transform(current.sync))
    }
}
