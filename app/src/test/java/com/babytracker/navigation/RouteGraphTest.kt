package com.babytracker.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 路由注册表完整性测试：AppNavigation 的 NavDisplay 必须覆盖全部路由，
 * 且每个路由只能注册一次（内容键唯一，否则 elyon 会在 reconcile 时抛异常）。
 */
class RouteGraphTest {

    @Test
    fun `路由注册表覆盖全部业务路由`() {
        val expected = listOf(
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

        assertEquals(expected, AppRouteGraph.routes)
        assertEquals(expected.size, AppRouteGraph.routes.distinct().size)
    }

    @Test
    fun `所有路由的 toString 是值派生的稳定内容键`() {
        AppRouteGraph.routes.forEach { route ->
            assertTrue("路由 ${route::class.simpleName} 的 toString 不能包含对象地址", !route.toString().contains('@'))
            assertEquals(route.toString(), route.toString())
        }
    }
}
