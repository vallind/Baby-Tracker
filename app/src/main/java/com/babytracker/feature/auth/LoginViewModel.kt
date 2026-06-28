package com.babytracker.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 登录/注册页面的 ViewModel。
 *
 * 登录为可选操作：不强制登录，用户可在 Settings 中手动进入。
 */
class LoginViewModel(
    private val authService: AuthService,
) : ViewModel() {

    data class UiState(
        val account: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoggedIn: Boolean = false,
        val isRegisterMode: Boolean = false,  // false=登录, true=注册
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onAccountChange(value: String) {
        _uiState.update { it.copy(account = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.account.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "账户名和密码不能为空") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "密码长度至少 6 位") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (state.isRegisterMode) {
                authService.signUp(state.account, state.password)
                    .onSuccess { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "注册失败，请重试",
                            )
                        }
                    }
            } else {
                authService.signIn(state.account, state.password)
                    .onSuccess { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "登录失败，请检查账户名和密码",
                            )
                        }
                    }
            }
        }
    }
}
