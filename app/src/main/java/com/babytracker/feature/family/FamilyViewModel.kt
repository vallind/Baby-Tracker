package com.babytracker.feature.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.Family
import com.babytracker.core.data.FamilyMember
import com.babytracker.core.data.FamilyService
import com.babytracker.core.sync.RealtimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val familyService: FamilyService,
    private val realtimeManager: RealtimeManager,
) : ViewModel() {

    data class UiState(
        val families: List<Family> = emptyList(),
        val currentFamily: Family? = null,
        val members: List<FamilyMember> = emptyList(),
        val isLoading: Boolean = false,
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
            try {
                _uiState.update { it.copy(isLoading = true) }
                val families = familyService.loadMyFamilies()
                _uiState.update {
                    it.copy(
                        families = families,
                        currentFamily = families.firstOrNull(),
                        isLoading = false,
                    )
                }
                // 加载当前家庭成员列表
                families.firstOrNull()?.let { loadMembers(it.id) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "加载失败")
                }
            }
        }

        // 监听 Realtime 推送的家庭成员变更，实时刷新 UI
        viewModelScope.launch {
            realtimeManager.familyMembersChanged.collect {
                _uiState.value.currentFamily?.let { family ->
                    // 重新加载家庭列表（可能有新家庭加入）和成员列表
                    try {
                        val families = familyService.loadMyFamilies()
                        _uiState.update {
                            it.copy(
                                families = families,
                                currentFamily = families.find { f -> f.id == family.id } ?: families.firstOrNull(),
                            )
                        }
                        _uiState.value.currentFamily?.let { loadMembers(it.id) }
                    } catch (_: Exception) { }
                }
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
            familyService.createFamily(name)
                .onSuccess { family ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showCreateDialog = false,
                            newFamilyName = "",
                            currentFamily = family,
                            families = it.families + family,
                        )
                    }
                    loadMembers(family.id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
        }
    }

    fun joinFamily() {
        val code = _uiState.value.inviteCode.trim().uppercase()
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入邀请码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            familyService.joinFamily(code)
                .onSuccess { family ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showJoinDialog = false,
                            inviteCode = "",
                            currentFamily = family,
                        )
                    }
                    // 重新加载
                    val families = familyService.loadMyFamilies()
                    _uiState.update { it.copy(families = families) }
                    loadMembers(family.id)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "加入失败，请检查邀请码")
                    }
                }
        }
    }

    fun loadMembers(familyId: String) {
        viewModelScope.launch {
            try {
                val members = familyService.getFamilyMembers(familyId)
                _uiState.update { it.copy(members = members) }
            } catch (_: Exception) { }
        }
    }

    fun selectFamily(family: Family) {
        _uiState.update { it.copy(currentFamily = family) }
        loadMembers(family.id)
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
