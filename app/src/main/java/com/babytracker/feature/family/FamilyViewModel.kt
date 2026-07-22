package com.babytracker.feature.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilyMember
import com.babytracker.core.data.FamilyService
import com.babytracker.core.database.dao.BabyDao
import com.babytracker.core.sync.RealtimeManager
import com.babytracker.core.sync.SyncEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val familyService: FamilyService,
    private val realtimeManager: RealtimeManager,
    private val babyDao: BabyDao,
    private val syncEngine: SyncEngine,
) : ViewModel() {

    data class UiState(
        val families: List<Family> = emptyList(),
        val currentFamily: Family? = null,
        val members: List<FamilyMember> = emptyList(),
        val isLoading: Boolean = false,
        val isLocalMode: Boolean = false,
        val isSessionVerified: Boolean = false,
        val unscopedBabyCount: Int = 0,
        val migrationTarget: Family? = null,
        val errorMessage: String? = null,
        val newFamilyName: String = "",
        val inviteCode: String = "",
        val showCreateDialog: Boolean = false,
        val showJoinDialog: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(familyService.sessionState, babyDao.watchUnscopedCount()) { session, count ->
                session to count
            }.collect { (session, count) ->
                _uiState.update {
                    it.copy(
                        families = session.families,
                        currentFamily = session.activeFamily,
                        isLoading = session.isLoading,
                        isLocalMode = session.isLocalMode,
                        isSessionVerified = session.sessionVerified,
                        unscopedBabyCount = count,
                        errorMessage = session.errorMessage ?: it.errorMessage,
                    )
                }
            }
        }
        viewModelScope.launch {
            familyService.sessionState.map { it.verifiedFamilyForSync?.id }
                .distinctUntilChanged().collect { familyId ->
                    if (familyId == null) _uiState.update { it.copy(members = emptyList()) }
                    else loadMembers(familyId)
                }
        }
        viewModelScope.launch {
            realtimeManager.familyMembersChanged.collect {
                familyService.sessionState.value.verifiedFamilyForSync?.id?.let(::loadMembers)
            }
        }
    }

    fun showCreateDialog() = _uiState.update { it.copy(showCreateDialog = true) }
    fun hideCreateDialog() = _uiState.update { it.copy(showCreateDialog = false, newFamilyName = "") }
    fun showJoinDialog() = _uiState.update { it.copy(showJoinDialog = true) }
    fun hideJoinDialog() = _uiState.update { it.copy(showJoinDialog = false, inviteCode = "") }
    fun onFamilyNameChange(value: String) = _uiState.update { it.copy(newFamilyName = value) }
    fun onInviteCodeChange(value: String) = _uiState.update { it.copy(inviteCode = value) }

    fun createFamily() {
        val name = _uiState.value.newFamilyName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "家庭名称不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            familyService.createFamily(name).fold(
                onSuccess = { hideCreateDialog() },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } },
            )
        }
    }

    fun joinFamily() {
        val code = _uiState.value.inviteCode.trim().uppercase()
        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "请输入 6 位邀请码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            familyService.joinFamily(code).fold(
                onSuccess = { hideJoinDialog() },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "加入失败") } },
            )
        }
    }

    private fun loadMembers(familyId: String) {
        viewModelScope.launch {
            runCatching { familyService.getFamilyMembers(familyId) }
                .onSuccess { members -> _uiState.update { it.copy(members = members) } }
        }
    }

    fun selectFamily(family: Family) {
        if (!familyService.selectFamily(family)) {
            _uiState.update { it.copy(errorMessage = "该家庭尚未通过当前账号验证") }
        }
    }

    fun selectLocalMode() = familyService.selectLocalMode()
    fun requestMigration(family: Family) = _uiState.update { it.copy(migrationTarget = family) }
    fun cancelMigration() = _uiState.update { it.copy(migrationTarget = null) }

    fun confirmMigration() {
        val target = _uiState.value.migrationTarget ?: return
        if (!familyService.selectFamily(target)) {
            _uiState.update { it.copy(migrationTarget = null, errorMessage = "目标家庭尚未验证") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, migrationTarget = null, errorMessage = null) }
            runCatching {
                check(familyService.sessionState.value.verifiedFamilyForSync?.id == target.id) { "登录会话尚未验证" }
                syncEngine.currentFamilyId = target.id
                syncEngine.claimUnscopedData(target.id)
            }.onSuccess { count ->
                _uiState.update { it.copy(isLoading = false, errorMessage = "已将 $count 个宝宝及其记录归入${target.name}") }
            }.onFailure { e ->
                familyService.selectLocalMode()
                _uiState.update { it.copy(isLoading = false, errorMessage = "归属失败：${e.message}") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
