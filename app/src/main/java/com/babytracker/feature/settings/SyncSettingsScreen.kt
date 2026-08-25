package com.babytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.core.sync.BgInterval
import com.babytracker.core.sync.RealtimeState
import com.babytracker.core.sync.SyncConfig
import com.babytracker.core.sync.SyncDelay
import com.babytracker.core.sync.SyncState
import com.babytracker.designsystem.components.cardgroup.AppCardGroup
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.settingitem.AppSettingItem
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.switchcontrol.AppRadioButton
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

@Composable
fun SyncSettingsScreen(
    config: SyncConfig,
    syncState: SyncState,
    connectionState: RealtimeState,
    isOnline: Boolean,
    syncResult: String?,
    syncRunId: Int,
    onBack: () -> Unit,
    onSetAutoSync: (Boolean) -> Unit,
    onSetSyncOnExit: (Boolean) -> Unit,
    onSetWifiOnly: (Boolean) -> Unit,
    onSetSyncDelay: (SyncDelay) -> Unit,
    onSetBgInterval: (BgInterval) -> Unit,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current

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

    AppScaffold(
        topBar = {
            AppTopBar(title = "同步设置", onBack = onBack)
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = spacing.md)) {

            AppCardGroup {
                AppSettingItem(
                    emoji = "🔄",
                    label = "自动同步",
                    trailing = {
                        AppSwitch(
                            checked = config.autoSync,
                            onCheckedChange = onSetAutoSync,
                        )
                    },
                )
                SettingsDivider()

                AppSettingItem(
                    emoji = "⏱",
                    label = "同步延迟",
                    subtitle = config.syncDelay.label,
                    onClick = { showDelaySheet = true },
                )
                SettingsDivider()

                AppSettingItem(
                    emoji = "📅",
                    label = "后台同步",
                    subtitle = config.bgInterval.label,
                    onClick = { showBgSheet = true },
                )
                SettingsDivider()

                AppSettingItem(
                    emoji = "🚪",
                    label = "退出时同步",
                    trailing = {
                        AppSwitch(
                            checked = config.syncOnExit,
                            onCheckedChange = onSetSyncOnExit,
                        )
                    },
                )
                SettingsDivider()

                AppSettingItem(
                    emoji = "📶",
                    label = "仅 Wi‑Fi",
                    trailing = {
                        AppSwitch(
                            checked = config.wifiOnly,
                            onCheckedChange = onSetWifiOnly,
                        )
                    },
                )
            }

            Spacer(Modifier.height(spacing.lg))

            var syncing by remember { mutableStateOf(false) }

            AppButton(
                label = if (syncing) "同步中..." else "立即同步",
                onClick = {
                    syncing = true
                    onManualSync()
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.sm),
            )

            // 用自增 runId 复位：结果字符串相同（如连续两次"无数据需同步"）时 StateFlow 去重不会重新发射
            LaunchedEffect(syncRunId) {
                if (syncRunId > 0) syncing = false
            }

            Spacer(Modifier.height(spacing.md))

            Box(
                Modifier.fillMaxWidth().padding(horizontal = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when {
                        syncResult != null -> "$syncStatus·${syncResult}"
                        else -> syncStatus
                    },
                    style = typography.bodySmall,
                    color = c.textTertiary,
                )
            }
        }
    }

    if (showDelaySheet) {
        AppBottomSheet(show = true, onDismiss = { showDelaySheet = false }) {
            Column(Modifier.padding(spacing.md)) {
                Text("同步延迟", style = typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                SyncDelay.entries.forEach { delay ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).clickable {
                            onSetSyncDelay(delay)
                            showDelaySheet = false
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppRadioButton(
                            selected = config.syncDelay == delay,
                            onClick = {
                                onSetSyncDelay(delay)
                                showDelaySheet = false
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(delay.label, style = typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showBgSheet) {
        AppBottomSheet(show = true, onDismiss = { showBgSheet = false }) {
            Column(Modifier.padding(spacing.md)) {
                Text("后台同步", style = typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                BgInterval.entries.forEach { interval ->
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).clickable {
                            onSetBgInterval(interval)
                            showBgSheet = false
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppRadioButton(
                            selected = config.bgInterval == interval,
                            onClick = {
                                onSetBgInterval(interval)
                                showBgSheet = false
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(interval.label, style = typography.bodyLarge)
                    }
                }
            }
        }
    }
}
