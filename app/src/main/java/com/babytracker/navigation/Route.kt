package com.babytracker.navigation

import io.elyon.kmp.nav.core.NavKey
import kotlinx.serialization.Serializable

/**
 * elyon-nav 的类型安全路由表。
 *
 * 全部路由为单例 data object：toString 值派生且稳定，可作为
 * NavDisplay 的内容键（重复内容键会在 reconcile 时被拒绝）。
 */
@Serializable
sealed interface Route : NavKey {
    @Serializable data object Home : Route
    @Serializable data object Timeline : Route
    @Serializable data object Feeding : Route
    @Serializable data object Sleep : Route
    @Serializable data object Growth : Route
    @Serializable data object Vaccination : Route
    @Serializable data object Health : Route
    @Serializable data object Diaper : Route
    @Serializable data object Stats : Route
    @Serializable data object Settings : Route
    @Serializable data object PreferenceSettings : Route
    @Serializable data object DataSettings : Route
    @Serializable data object SupportSettings : Route
    @Serializable data object BabyManagement : Route
    @Serializable data object BabyProfile : Route
    @Serializable data object Backup : Route
    @Serializable data object LogViewer : Route
    @Serializable data object SyncSettings : Route
    @Serializable data object Family : Route
    @Serializable data object Message : Route
    @Serializable data object DevelopmentAssessment : Route
    @Serializable data object Reminder : Route
    @Serializable data object Login : Route
    @Serializable data object AiAssistant : Route
    @Serializable data object AiSettings : Route
}
