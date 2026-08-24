package com.babytracker.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.babytracker.navigation.AppBottomBar
import com.babytracker.navigation.AiAssistant
import com.babytracker.navigation.BabyManagement
import com.babytracker.navigation.BabyProfile
import com.babytracker.navigation.DevelopmentAssessment
import com.babytracker.navigation.Diaper as DiaperRoute
import com.babytracker.navigation.Feeding as FeedingRoute
import com.babytracker.navigation.Growth
import com.babytracker.navigation.Health
import com.babytracker.navigation.Reminder
import com.babytracker.navigation.Sleep as SleepRoute
import com.babytracker.navigation.Timeline
import com.babytracker.navigation.Vaccination
import com.babytracker.navigation.navigateToRoot
import org.koin.androidx.compose.koinViewModel

/**
 * 首页路由（组合根）— 只做 DI 装配 + 导航映射；当前宝宝解析与数据加载在 ViewModel。
 * UI 渲染全部委托给 [HomeScreen]（纯 UI，不接触导航与 DI）。
 */
@Composable
fun HomeRoute(navController: NavController) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val baby by viewModel.baby.collectAsState()

    HomeScreen(
        state = state,
        baby = baby,
        bottomBar = { AppBottomBar(navController) },
        onOpenBabyManagement = { navController.navigate(BabyManagement) },
        onOpenProfile = { navController.navigate(BabyProfile) },
        onOpenFeature = { feature -> navController.navigateToRoot(feature.toRoute()) },
        onOpenAiAssistant = { navController.navigate(AiAssistant) },
        onSeeAll = { navController.navigate(Timeline) },
    )
}

/** 宫格特性枚举：Screen 只认语义，路由映射留在 Route（不建全局 Destination 抽象） */
enum class HomeFeature { Feeding, Sleep, Diaper, Growth, Development, Vaccination, Health, Reminder }

/** 特性 → 类型安全路由（与 HomeScreen 原 navigateToRoot 映射一一对应） */
private fun HomeFeature.toRoute(): Any = when (this) {
    HomeFeature.Feeding -> FeedingRoute
    HomeFeature.Sleep -> SleepRoute
    HomeFeature.Diaper -> DiaperRoute
    HomeFeature.Growth -> Growth
    HomeFeature.Development -> DevelopmentAssessment
    HomeFeature.Vaccination -> Vaccination
    HomeFeature.Health -> Health
    HomeFeature.Reminder -> Reminder
}