package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 同步设置路由（组合根）— 装配双 VM（SettingsViewModel + SyncViewModel），
 * 状态收集后传给 [SyncSettingsScreen] 纯 UI 渲染；本路由不承载业务逻辑。
 */
@Composable
fun SyncSettingsRoute(navController: NavController) {
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val syncViewModel: SyncViewModel = koinViewModel()
    val settings by settingsViewModel.settings.collectAsState()
    val connectionState by syncViewModel.connectionState.collectAsState()
    val isOnline by syncViewModel.isOnline.collectAsState()
    val syncState by syncViewModel.syncState.collectAsState()
    val syncResult by syncViewModel.syncResult.collectAsState()
    val syncRunId by syncViewModel.syncRunId.collectAsState()

    SyncSettingsScreen(
        config = settings.sync,
        syncState = syncState,
        connectionState = connectionState,
        isOnline = isOnline,
        syncResult = syncResult,
        syncRunId = syncRunId,
        onBack = { navController.popBackStack() },
        onSetAutoSync = { enabled -> settingsViewModel.updateSettings { current -> current.copy(sync = current.sync.copy(autoSync = enabled)) } },
        onSetSyncOnExit = { enabled -> settingsViewModel.updateSettings { current -> current.copy(sync = current.sync.copy(syncOnExit = enabled)) } },
        onSetWifiOnly = { enabled -> settingsViewModel.updateSettings { current -> current.copy(sync = current.sync.copy(wifiOnly = enabled)) } },
        onSetSyncDelay = { delay -> settingsViewModel.updateSettings { current -> current.copy(sync = current.sync.copy(syncDelay = delay)) } },
        onSetBgInterval = { interval -> settingsViewModel.updateSettings { current -> current.copy(sync = current.sync.copy(bgInterval = interval)) } },
        onManualSync = syncViewModel::manualSync,
    )
}