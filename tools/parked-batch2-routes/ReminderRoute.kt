package com.babytracker.feature.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.babytracker.core.data.repository.ReminderRepository
import com.babytracker.core.util.BabyController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * 提醒中心路由 — 只负责 DI 装配与导航映射。
 *
 * - koinViewModel 装配 [ReminderViewModel] 并注入 Screen；
 * - BabyController 提供当前宝宝 id，作为状态参数传入 Screen；
 * - 当前宝宝变化时触发 viewModel.load(babyId)（原在 Screen 中，因依赖 babyCtrl 迁至路由层）；
 * - ReminderRepository 的删除/撤销写操作包成 lambda 回调传给 Screen。
 */
@Composable
fun ReminderRoute(navController: NavController) {
    val viewModel: ReminderViewModel = koinViewModel()
    val babyCtrl: BabyController = koinInject()
    val reminderRepo: ReminderRepository = koinInject()
    val babyId = babyCtrl.currentBabyId

    // 当前宝宝变化时按需加载提醒数据
    LaunchedEffect(babyId) {
        if (babyId != 0) viewModel.load(babyId)
    }

    ReminderScreen(
        viewModel = viewModel,
        babyId = babyId,
        onBack = { navController.popBackStack() },
        // 仓库写操作包成 lambda 回调，Screen 不直接触碰 Repository
        onDeleteReminder = { reminder -> reminderRepo.delete(reminder) },
        onRestoreReminder = { reminder -> reminderRepo.update(reminder) },
    )
}