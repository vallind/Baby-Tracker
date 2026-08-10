package com.babytracker.navigation

import io.elyon.kmp.nav.core.NavBackStack

/**
 * 业务侧导航门面，封装 elyon-nav 的 [NavBackStack]。
 *
 * 所有路由都是单例，elyon NavDisplay 不允许同一内容键在栈中重复，
 * 因此 navigate 幂等：栈中已存在时不再压栈。
 */
class Navigator(
    val backStack: NavBackStack,
) {

    /** 压栈；路由已在栈中时保持原栈不变。 */
    fun navigate(route: Route) {
        if (route !in backStack) {
            backStack.add(route)
        }
    }

    /** 替换栈顶；空栈时等价于压栈。 */
    fun replace(route: Route) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = route
        } else {
            backStack.add(route)
        }
    }

    /** 弹出栈顶；仅剩根路由时不做任何操作。 */
    fun pop(): Boolean = if (backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
        true
    } else {
        false
    }

    /** 弹到只剩根路由。 */
    fun popToRoot() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    /**
     * 底部 Tab 切换：Tab 已在栈中则弹回该路由，否则清空到根再压入，
     * 近似 AndroidX 导航的 popUpTo(start) + launchSingleTop 语义。
     */
    fun switchTab(route: Route) {
        val index = backStack.indexOf(route)
        if (index >= 0) {
            while (backStack.size > index + 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        } else {
            popToRoot()
            backStack.add(route)
        }
    }

    /** 当前栈顶路由；空栈返回 null。 */
    fun current(): Route? = backStack.lastOrNull() as? Route
}
