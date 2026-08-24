package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.Backup
import com.babytracker.navigation.SyncSettings
import org.koin.androidx.compose.koinViewModel

/**
 * 数据与同步路由（组合根）— 装配 SettingsViewModel，收集展示字段传给纯 Screen。
 */
@Composable
fun DataSettingsRoute(navController: NavController) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val syncConfig = settings.sync

    DataSettingsScreen(
        autoSync = syncConfig.autoSync,
        syncDelayLabel = syncConfig.syncDelay.label,
        onBack = { navController.popBackStack() },
        onOpenSyncSettings = { navController.navigate(SyncSettings) },
        onOpenBackup = { navController.navigate(Backup) },
    )
}