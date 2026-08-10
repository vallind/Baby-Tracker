package com.babytracker.navigation

import io.elyon.kmp.nav.core.navBackStackOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * elyon-nav 迁移后的 Navigator 行为契约。
 *
 * 全部路由都是单例 data object，elyon NavDisplay 不允许同一内容键在栈中重复，
 * 因此 navigate 必须幂等（栈中已有该路由时不再压栈）。
 */
class NavigatorTest {

    @Test
    fun `navigate 压栈且重复路由不重复入栈`() {
        val navigator = Navigator(navBackStackOf(Route.Home))

        navigator.navigate(Route.Timeline)
        navigator.navigate(Route.Timeline)

        assertEquals(listOf(Route.Home, Route.Timeline), navigator.backStack.toList())
        assertEquals(Route.Timeline, navigator.current())
    }

    @Test
    fun `navigate 已存在于栈中时保持原栈`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Stats))

        navigator.navigate(Route.Home)

        assertEquals(listOf(Route.Home, Route.Stats), navigator.backStack.toList())
    }

    @Test
    fun `pop 弹出非根路由并返回 true`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Settings))

        assertTrue(navigator.pop())
        assertEquals(Route.Home, navigator.current())
    }

    @Test
    fun `pop 在根路由时不动栈并返回 false`() {
        val navigator = Navigator(navBackStackOf(Route.Home))

        assertFalse(navigator.pop())
        assertEquals(Route.Home, navigator.current())
    }

    @Test
    fun `popToRoot 只保留根路由`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Feeding, Route.Sleep))

        navigator.popToRoot()

        assertEquals(listOf(Route.Home), navigator.backStack.toList())
    }

    @Test
    fun `replace 替换栈顶`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Timeline))

        navigator.replace(Route.Growth)

        assertEquals(listOf(Route.Home, Route.Growth), navigator.backStack.toList())
    }

    @Test
    fun `switchTab 已存在时弹回该路由`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Message, Route.AiAssistant))

        navigator.switchTab(Route.Message)

        assertEquals(listOf(Route.Home, Route.Message), navigator.backStack.toList())
    }

    @Test
    fun `switchTab 不存在时清空到根再压入`() {
        val navigator = Navigator(navBackStackOf(Route.Home, Route.Feeding))

        navigator.switchTab(Route.Stats)

        assertEquals(listOf(Route.Home, Route.Stats), navigator.backStack.toList())
    }

    @Test
    fun `空栈 current 返回 null`() {
        val navigator = Navigator(navBackStackOf())

        assertNull(navigator.current())
    }
}
