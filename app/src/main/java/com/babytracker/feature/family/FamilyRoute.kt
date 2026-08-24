package com.babytracker.feature.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

/**
 * 家庭共享路由 — 组合根：只做 DI 装配、状态收集与导航映射，不承载业务逻辑。
 * UI 渲染委托给 [FamilyScreen]（收 UiState + 命名回调）。
 */
@Composable
fun FamilyRoute(navController: NavController) {
    val viewModel: FamilyViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    FamilyScreen(
        state = state,
        onBack = { navController.popBackStack() },
        onSelectFamily = viewModel::selectFamily,
        onSelectLocal = viewModel::selectLocalMode,
        onMigrate = viewModel::requestMigration,
        onCreateClick = viewModel::showCreateDialog,
        onJoinClick = viewModel::showJoinDialog,
        onFamilyNameChange = viewModel::onFamilyNameChange,
        onCreate = viewModel::createFamily,
        onDismissCreate = viewModel::hideCreateDialog,
        onInviteCodeChange = viewModel::onInviteCodeChange,
        onJoin = viewModel::joinFamily,
        onDismissJoin = viewModel::hideJoinDialog,
        onConfirmMigration = viewModel::confirmMigration,
        onDismissMigration = viewModel::cancelMigration,
    )
}