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
        private const val SYNTHETIC_DOMAIN = "@baby-tracker.app"
        private const val KEY_LOGGED_IN = "auth_logged_in"
        private const val KEY_DISPLAY_ACCOUNT = "auth_display_account"
        private const val KEY_NICKNAME = "auth_nickname"
    }

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    /** 已登录的账户名（从 SharedPreferences 恢复，用于 UI 展示） */
    private val _displayAccount = MutableStateFlow<String?>(null)
    val displayAccount: StateFlow<String?> = _displayAccount.asStateFlow()

    /** 用户自定义昵称（本地 SharedPreferences 存储，未登录时为 null） */
    private val _nickname = MutableStateFlow<String?>(null)
    val nickname: StateFlow<String?> = _nickname.asStateFlow()

    init {
        try {
            _currentUser.value = client.auth.currentUserOrNull()
        } catch (_: Exception) {
            _currentUser.value = null
        }
        // 无论会话是否立即恢复，都从 SharedPreferences 加载账户名和昵称
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            _displayAccount.value = prefs.getString(KEY_DISPLAY_ACCOUNT, null)
            _nickname.value = prefs.getString(KEY_NICKNAME, null)
        }
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
        // 持久化登录标记 + 账户名
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).putString(KEY_DISPLAY_ACCOUNT, account).apply()
        _displayAccount.value = account
        // 登录时不清空已有昵称（可能从 SharedPreferences 已恢复）
        user
    }

    /**
     * 登录已有账户。
     */
    suspend fun signIn(account: String, password: String): Result<UserInfo> = runCatching {
        client.auth.signInWith(Email) {
            email = toEmail(account)
            this.password = password
        }
        val user = client.auth.retrieveUserForCurrentSession()
        _currentUser.value = user
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).putString(KEY_DISPLAY_ACCOUNT, account).apply()
        _displayAccount.value = account
        user
    }

    /**
     * 退出登录。
     */
    suspend fun signOut() {
        try {
            client.auth.signOut()
        } catch (_: Exception) { }
        _currentUser.value = null
        _displayAccount.value = null
        _nickname.value = null
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).remove(KEY_DISPLAY_ACCOUNT).remove(KEY_NICKNAME).apply()
    }

    // ─── 昵称管理 ───

    /** 设置用户自定义昵称（仅在已登录时生效） */
    fun setNickname(newNickname: String) {
        _nickname.value = newNickname
        prefs.edit().putString(KEY_NICKNAME, newNickname).apply()
    }

    /** 清除昵称 */
    fun clearNickname() {
        _nickname.value = null
        prefs.edit().remove(KEY_NICKNAME).apply()
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
