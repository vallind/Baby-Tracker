package com.babytracker.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Supabase Auth 服务。
 *
 * 设计原则：不强制登录，本地优先。
 * - 未登录时 App 完全本地使用，所有数据存 Room
 * - 登录为可选操作，登录后开启云同步和家庭共享
 * - 支持账户名 + 密码（底层用 Supabase email 字段存储账户名，关闭了邮箱验证）
 */
class AuthService(
    private val client: SupabaseClient,
) {
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // 启动时尝试从本地存储恢复登录态
        try {
            _currentUser.value = client.auth.currentUserOrNull()
        } catch (_: Exception) {
            _currentUser.value = null
        }
    }

    // ─── 登录/注册 ───

    /**
     * 注册新账户。
     * @param account 账户名（底层存入 Supabase email 字段，不验证邮箱格式）
     * @param password 密码
     */
    suspend fun signUp(account: String, password: String): Result<UserInfo> = runCatching {
        client.auth.signUpWith(Email) {
            email = account
            this.password = password
        }
        // signUp 成功后通过 retrieveUser 获取用户信息
        val user = client.auth.retrieveUserForCurrentSession()
        _currentUser.value = user
        user
    }

    /**
     * 登录已有账户。
     * @param account 账户名
     * @param password 密码
     */
    suspend fun signIn(account: String, password: String): Result<UserInfo> = runCatching {
        client.auth.signInWith(Email) {
            email = account
            this.password = password
        }
        val user = client.auth.retrieveUserForCurrentSession()
        _currentUser.value = user
        user
    }

    /**
     * 退出登录。退出后继续本地使用，只是停止云同步。
     */
    suspend fun signOut() {
        try {
            client.auth.signOut()
        } catch (_: Exception) {
            // 忽略网络错误
        }
        _currentUser.value = null
    }

    // ─── 状态查询 ───

    /** 当前是否已登录 */
    fun isLoggedIn(): Boolean = _currentUser.value != null

    /** 获取当前用户 ID，未登录返回 null */
    fun currentUserId(): String? = _currentUser.value?.id

    // ─── 状态监听 ───

    /**
     * 监听登录状态变化（从 Supabase Auth 实时状态流）。
     * 用于同步引擎判断是否应该启动/停止同步。
     */
    fun observeAuthState(): StateFlow<UserInfo?> = client.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user
                is SessionStatus.NotAuthenticated -> null
                is SessionStatus.Initializing -> null
                is SessionStatus.RefreshFailure -> null
            }
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = _currentUser.value,
        )
}
