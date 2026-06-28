package com.babytracker.core.auth

import android.content.SharedPreferences
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
 * - 支持账户名 + 密码。Supabase Email Auth 要求合法邮箱格式，
 *   纯账户名自动拼接虚拟域名后传给 Supabase。
 *   需在 Supabase Dashboard → Authentication → Settings 关闭 "Confirm email"。
 * - 登录态通过 Supabase Auth 内置存储持久化（autoLoadFromStorage + alwaysAutoRefresh），
 *   SharedPreferences 作为双重保障标记。
 */
class AuthService(
    private val client: SupabaseClient,
    private val prefs: SharedPreferences,
) {

    companion object {
        /** 纯账户名自动拼接的虚拟域名（需合法 TLD，.local 会被拒绝） */
        private const val SYNTHETIC_DOMAIN = "@baby-tracker.app"
        private const val KEY_LOGGED_IN = "auth_logged_in"
    }

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // 启动时从 Supabase Auth 内置存储恢复登录态
        try {
            _currentUser.value = client.auth.currentUserOrNull()
        } catch (_: Exception) {
            _currentUser.value = null
        }
        // 同步 SharedPreferences 标记（以 Supabase 实际状态为准）
        if (_currentUser.value != null) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        }
    }

    /** 将用户输入的账户名转换为 Supabase 接受的 email 格式 */
    private fun toEmail(account: String): String =
        if (account.contains("@")) account else account + SYNTHETIC_DOMAIN

    // ─── 登录/注册 ───

    /**
     * 注册新账户。
     * @param account 账户名（纯用户名自动拼接 @baby-tracker.app）
     * @param password 密码
     */
    suspend fun signUp(account: String, password: String): Result<UserInfo> = runCatching {
        // signUpWith(Email) 返回值取决于 Confirm email 设置：
        // - 确认开启 → 返回 User，无会话 → 用返回值
        // - 确认关闭 → 返回 null，自动登录 → 从会话获取
        val signUpResult = client.auth.signUpWith(Email) {
            email = toEmail(account)
            this.password = password
        }

        val user: UserInfo = if (signUpResult != null) {
            // 确认开启：直接用返回值
            _currentUser.value = signUpResult
            signUpResult
        } else {
            // 确认关闭：从当前会话获取
            val sessionUser = client.auth.retrieveUserForCurrentSession()
            _currentUser.value = sessionUser
            sessionUser
        }
        // 持久化登录标记
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        user
    }

    /**
     * 登录已有账户。
     * @param account 账户名（纯用户名自动拼接 @baby.local）
     * @param password 密码
     */
    suspend fun signIn(account: String, password: String): Result<UserInfo> = runCatching {
        client.auth.signInWith(Email) {
            email = toEmail(account)
            this.password = password
        }
        val user = client.auth.retrieveUserForCurrentSession()
        _currentUser.value = user
        // 持久化登录标记
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
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
        // 清除持久化登录标记
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
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
