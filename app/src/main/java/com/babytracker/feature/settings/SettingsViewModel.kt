package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.settings.AppSettings
import com.babytracker.core.settings.SettingsStore
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置主页展示状态：当前宝宝 + 登录态（薄聚合；登录/昵称/宝宝各自逻辑仍在
 * AuthService / BabyRepository，本 VM 只编排主页需要展示的组合状态）。
 */
data class SettingsUiState(
    val baby: Baby? = null,
    val isLoggedIn: Boolean = false,
    val displayAccount: String? = null,
    val nickname: String? = null,
)

/**
 * 设置主页 ViewModel（Batch 4 拆解后保持「薄」）：
 * 只聚合设置主页自己的展示状态与退出/改名动作；
 * 备份 → BackupViewModel，宝宝管理 → BabyManagementViewModel，主题/密度 → Controller。
 */
class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val authService: AuthService,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsStore.settings

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SettingsUiState> = combine(
        babyRepo.watchAll().map { babies ->
            babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()
        },
        authService.displayAccount,
        authService.nickname,
        authService.observeAuthState(),
    ) { baby, displayAccount, nickname, authState ->
        SettingsUiState(
            baby = baby,
            isLoggedIn = authState != null,
            displayAccount = displayAccount,
            nickname = nickname,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            settingsStore.update(transform)
        }
    }

    fun signOut() {
        viewModelScope.launch { authService.signOut() }
    }

    fun setNickname(nickname: String) {
        viewModelScope.launch { authService.setNickname(nickname) }
    }
}