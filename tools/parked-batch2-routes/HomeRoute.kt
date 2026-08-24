package com.babytracker.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.util.BabyController
import com.babytracker.navigation.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * 首页路由 — 只负责 DI 装配、当前宝宝派生与导航映射，UI 渲染委托给 [HomeScreen]。
 */
@Composable
fun HomeRoute(navController: NavController) {
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val viewModel: HomeViewModel = koinViewModel()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val currentBabyId = babyCtrl.currentBabyId
    val baby = babies.find { it.id == currentBabyId } ?: babies.firstOrNull()
    val state by viewModel.state.collectAsState()

    // 与 HomeScreen 原有逻辑一致：切到当前宝宝并触发数据加载（已从 Screen 迁入）
    LaunchedEffect(baby) {
        if (baby != null) {
            if (babyCtrl.currentBabyId != baby.id) babyCtrl.selectBaby(baby.id)
            viewModel.loadData(baby.id)
        }
    }

    HomeScreen(
        viewModel = viewModel,
        state = state,
        babies = babies,
        baby = baby,
        onOpenBabyManagement = { navController.navigate(BabyManagement) },
        onOpenProfile = { navController.navigate(BabyProfile) },
        onSeeAll = { navController.navigate(Timeline) },
        onOpenAi = { navController.navigate(AiAssistant) },
        onOpenFeature = { feature -> navController.navigateToRoot(feature.toRoute()) },
        bottomBar = { AppBottomBar(navController) },
    )
}

/** 宫格特性枚举：Screen 只认语义，路由映射留在 Route */
enum class HomeFeature { Feeding, Sleep, Diaper, Growth, Development, Vaccination, Health, Reminder }

/** 特性 → 类型安全路由（与 Screen 原有 navigateToRoot 映射一一对应） */
private fun HomeFeature.toRoute(): Any = when (this) {
    HomeFeature.Feeding -> com.babytracker.navigation.Feeding
    HomeFeature.Sleep -> com.babytracker.navigation.Sleep
    HomeFeature.Diaper -> com.babytracker.navigation.Diaper
    HomeFeature.Growth -> com.babytracker.navigation.Growth
    HomeFeature.Development -> com.babytracker.navigation.DevelopmentAssessment
    HomeFeature.Vaccination -> com.babytracker.navigation.Vaccination
    HomeFeature.Health -> com.babytracker.navigation.Health
    HomeFeature.Reminder -> com.babytracker.navigation.Reminder
}