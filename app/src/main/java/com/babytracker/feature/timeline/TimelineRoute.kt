package com.babytracker.feature.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import com.babytracker.navigation.Growth
import com.babytracker.navigation.Health
import com.babytracker.navigation.Vaccination
import org.koin.androidx.compose.koinViewModel

/**
 * 记录时间线路由（组合根）— 只做 DI 装配与导航映射。
 * 当前宝宝、编辑目标与数据加载全部在 ViewModel；
 * 动态选择器（生长/疫苗/健康）跳转映射为本路由的 onOpen* 回调。
 */
@Composable
fun TimelineRoute(navController: NavController) {
    val viewModel: TimelineViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val babyId by viewModel.babyId.collectAsState()
    val editing by viewModel.editing.collectAsState()

    TimelineScreen(
        state = state,
        babyId = babyId,
        editing = editing,
        bottomBar = { AppBottomBar(navController) },
        onDelete = viewModel::delete,
        onUndoDelete = viewModel::undoLastDelete,
        onRequestEdit = viewModel::requestEdit,
        onDismissEdit = viewModel::dismissEdit,
        onAddFeeding = viewModel::addFeeding,
        onAddSleep = viewModel::addSleep,
        onAddDiaper = viewModel::addDiaper,
        onUpdateFeeding = viewModel::updateFeeding,
        onUpdateSleep = viewModel::updateSleep,
        onUpdateDiaper = viewModel::updateDiaper,
        onUpdateGrowth = viewModel::updateGrowth,
        onUpdateHealth = viewModel::updateHealth,
        onOpenGrowth = { navController.navigate(Growth) },
        onOpenVaccination = { navController.navigate(Vaccination) },
        onOpenHealth = { navController.navigate(Health) },
    )
}