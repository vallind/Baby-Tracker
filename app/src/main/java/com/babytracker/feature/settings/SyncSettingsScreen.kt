package com.babytracker.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.core.sync.BackgroundSyncInterval
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
    val viewModel: SettingsViewModel = koinViewModel()
    val config by viewModel.syncConfig.collectAsState()
    val status by viewModel.syncStatusText.collectAsState()
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current
    var showDelaySheet by remember { mutableStateOf(false) }
    var showBackgroundSheet by remember { mutableStateOf(false) }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "同步设置",
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.md),
        ) {
            Spacer(Modifier.height(spacing.md))
            SettingsCard {
                SettingsRow(
                    emoji = "🔄",
                    label = "自动同步",
                    subtitle = "登录后自动推送和拉取家庭数据",
                    trailing = {
                        Switch(
                            checked = config.autoSync,
                            onCheckedChange = viewModel::updateAutoSync,
                        )
                    },
                    onClick = { viewModel.updateAutoSync(!config.autoSync) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "⏱️",
                    label = "写入后同步",
                    subtitle = config.delay.label,
                    onClick = { showDelaySheet = true },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "🌙",
                    label = "后台同步",
                    subtitle = config.backgroundInterval.label,
                    onClick = { showBackgroundSheet = true },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "📶",
                    label = "仅非计费网络",
                    subtitle = "只在 Wi-Fi、以太网等非计费网络同步",
                    trailing = {
                        Switch(
                            checked = config.unmeteredOnly,
                            onCheckedChange = viewModel::updateUnmeteredOnly,
                        )
                    },
                    onClick = { viewModel.updateUnmeteredOnly(!config.unmeteredOnly) },
                )
                SettingsDivider()
                SettingsRow(
                    emoji = "☁️",
                    label = "立即同步",
                    subtitle = status,
                    onClick = viewModel::manualSync,
                )
            }

            Spacer(Modifier.height(spacing.md))
            Text(
                text = "当前状态：$status",
                color = colors.textSecondary,
                style = typography.bodySmall,
                modifier = Modifier.padding(horizontal = spacing.sm),
            )
            Text(
                text = "手动同步始终可用，并且不受仅非计费网络限制。关闭自动同步后，Realtime 与后台任务不会主动传输数据。",
                color = colors.textTertiary,
                style = typography.bodySmall,
                modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
            )
        }
    }

    SyncDelaySheet(
        show = showDelaySheet,
        selected = config.delay,
        onSelect = {
            viewModel.updateSyncDelay(it)
            showDelaySheet = false
        },
        onDismiss = { showDelaySheet = false },
    )
    BackgroundIntervalSheet(
        show = showBackgroundSheet,
        selected = config.backgroundInterval,
        onSelect = {
            viewModel.updateBackgroundInterval(it)
            showBackgroundSheet = false
        },
        onDismiss = { showBackgroundSheet = false },
    )
}

@Composable
private fun SyncDelaySheet(
    show: Boolean,
    selected: SyncDelay,
    onSelect: (SyncDelay) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(show = show, onDismiss = onDismiss) {
        OptionSheetTitle("写入后同步")
        SyncDelay.entries.forEach { option ->
            SyncOptionRow(option.label, option == selected) { onSelect(option) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BackgroundIntervalSheet(
    show: Boolean,
    selected: BackgroundSyncInterval,
    onSelect: (BackgroundSyncInterval) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(show = show, onDismiss = onDismiss) {
        OptionSheetTitle("后台同步")
        BackgroundSyncInterval.entries.forEach { option ->
            SyncOptionRow(option.label, option == selected) { onSelect(option) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OptionSheetTitle(title: String) {
    Text(
        text = title,
        style = LocalAppTypography.current.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun SyncOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = LocalAppTypography.current.bodyMedium)
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = "已选择", tint = colors.primary)
        }
    }
}
