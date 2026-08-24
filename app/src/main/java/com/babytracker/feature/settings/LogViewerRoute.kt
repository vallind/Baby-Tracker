package com.babytracker.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * 日志查看路由（组合根）。
 * 该屏无业务状态（过滤/选择/滚动均为 Screen 本地 UI 态），故不引入 ViewModel；
 * Route 只做导航映射，符合「除非真实复杂度否则不增加架构层」。
 */
@Composable
fun LogViewerRoute(navController: NavController) {
    LogViewerScreen(
        onBack = { navController.popBackStack() },
    )
}