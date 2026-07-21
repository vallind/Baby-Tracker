package com.babytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.core.sync.BgInterval
import com.babytracker.core.sync.SyncDelay
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun SyncSettingsScreen(navController: NavController) {
    val vm: SettingsViewModel = koinViewModel()
    val config by vm.syncConfig.collectAsState()
    val connectionState by vm.connectionState.collectAsState()
    val isOnline by vm.isOnline.collectAsState()
    val syncState by vm.syncState.collectAsState()
    val syncResult by vm.syncResult.collectAsState()

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
            AppTopBar(title = "同步设置", onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = spacing.md)) {

            SettingsCard {
                SettingsRow(
                    emoji = "🔄",
                    label = "自动同步",
                    trailing = {
                        Switch(checked = config.autoSync, onCheckedChange = { vm.updateAutoSync(it) })
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
                    emoji = "📶",
                    label = "仅 Wi‑Fi",
                    trailing = {
                        Switch(checked = config.wifiOnly, onCheckedChange = { vm.updateWifiOnly(it) })
                    },
                )
            }

            Spacer(Modifier.height(spacing.lg))

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
                        Modifier.fillMaxWidth().height(48.dp).clickable { vm.updateSyncDelay(delay); showDelaySheet = false },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = config.syncDelay == delay, onClick = { vm.updateSyncDelay(delay); showDelaySheet = false })
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
                        Modifier.fillMaxWidth().height(48.dp).clickable { vm.updateBgInterval(interval); showBgSheet = false },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = config.bgInterval == interval, onClick = { vm.updateBgInterval(interval); showBgSheet = false })
                        Spacer(Modifier.width(12.dp))
                        Text(interval.label, style = typography.bodyLarge)
                    }
                }
            }
        }
    }
}
