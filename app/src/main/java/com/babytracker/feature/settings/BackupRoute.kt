package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 备份/恢复路由（组合根）— 只做 DI 装配与导航映射；
 * BackupManager 全部操作在 BackupViewModel；目录/文件选择等平台交互留 Screen。
 */
@Composable
fun BackupRoute(navController: NavController) {
    val viewModel: BackupViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    BackupScreen(
        state = state,
        onLoadConfig = viewModel::loadConfig,
        onBack = { navController.popBackStack() },
        onWebdavUrlChange = viewModel::onWebdavUrlChange,
        onWebdavUserChange = viewModel::onWebdavUserChange,
        onWebdavPassChange = viewModel::onWebdavPassChange,
        onSaveWebdav = viewModel::saveWebdav,
        onCreateLocalBackup = viewModel::createLocalBackup,
        onCreateWebdavBackup = viewModel::createWebdavBackup,
        onRestoreFromUri = viewModel::restoreFromUri,
        onRestoreFromWebdav = viewModel::restoreFromWebdav,
    )
}