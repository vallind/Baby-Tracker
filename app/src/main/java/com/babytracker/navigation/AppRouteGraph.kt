package com.babytracker.navigation

/**
 * 路由注册表：AppNavigation 的 NavDisplay 按此表注册全部 entry。
 * 保持顺序即业务路由清单，RouteGraphTest 校验覆盖完整且无重复。
 */
object AppRouteGraph {
    val routes: List<Route> = listOf(
        Route.Home,
        Route.Timeline,
        Route.Feeding,
        Route.Sleep,
        Route.Growth,
        Route.Vaccination,
        Route.Health,
        Route.Diaper,
        Route.Stats,
        Route.Settings,
        Route.PreferenceSettings,
        Route.DataSettings,
        Route.SupportSettings,
        Route.BabyManagement,
        Route.BabyProfile,
        Route.Backup,
        Route.LogViewer,
        Route.SyncSettings,
        Route.Family,
        Route.Message,
        Route.DevelopmentAssessment,
        Route.Reminder,
        Route.Login,
        Route.AiAssistant,
        Route.AiSettings,
    )
}
