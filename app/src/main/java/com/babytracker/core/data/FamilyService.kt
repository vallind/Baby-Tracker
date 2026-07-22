package com.babytracker.core.data

import android.content.SharedPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong

@Serializable
data class Family(
    val id: String = "",
    val name: String = "",
    @SerialName("invite_code") val inviteCode: String = "",
    @SerialName("created_by") val createdBy: String? = null,
)

@Serializable
data class FamilyMember(
    @SerialName("family_id") val familyId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String? = null,
)

data class FamilySessionState(
    val userId: String? = null,
    val families: List<Family> = emptyList(),
    val selectedFamily: Family? = null,
    val sessionVerified: Boolean = false,
    val isLoading: Boolean = false,
    val isLocalMode: Boolean = false,
    val errorMessage: String? = null,
) {
    val activeFamily: Family? get() = if (isLocalMode) null else selectedFamily
    val verifiedFamilyForSync: Family?
        get() = activeFamily?.takeIf { sessionVerified && families.any { family -> family.id == it.id } }
}

/** 家庭状态唯一来源；缓存只负责离线展示，云同步必须经过实时会话校验。 */
class FamilyService(
    private val client: SupabaseClient,
    private val prefs: SharedPreferences,
) {
    companion object {
        private const val LEGACY_CURRENT_FAMILY_ID = "current_family_id"
        private const val KEY_SELECTED_ID_PREFIX = "family_selected_id_"
        private const val KEY_SELECTED_JSON_PREFIX = "family_selected_json_"
        private const val KEY_LOCAL_MODE_PREFIX = "family_local_mode_"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val requestVersion = AtomicLong(0)
    private val _sessionState = MutableStateFlow(FamilySessionState())
    val sessionState: StateFlow<FamilySessionState> = _sessionState.asStateFlow()
    val myFamilies: StateFlow<List<Family>> = sessionState.map { it.families }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
    val currentFamily: StateFlow<Family?> = sessionState.map { it.activeFamily }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private fun selectedIdKey(userId: String) = KEY_SELECTED_ID_PREFIX + userId
    private fun selectedJsonKey(userId: String) = KEY_SELECTED_JSON_PREFIX + userId
    private fun localModeKey(userId: String) = KEY_LOCAL_MODE_PREFIX + userId

    /** 恢复账号专属缓存。该状态不能驱动同步。 */
    fun restoreCachedForUser(userId: String) {
        requestVersion.incrementAndGet()
        val cached = prefs.getString(selectedJsonKey(userId), null)?.let {
            runCatching { json.decodeFromString<Family>(it) }.getOrNull()
        }
        _sessionState.value = FamilySessionState(
            userId = userId,
            families = cached?.let(::listOf).orEmpty(),
            selectedFamily = cached,
            isLocalMode = prefs.getBoolean(localModeKey(userId), false),
        )
    }

    /** 使用已验证 Supabase 会话刷新家庭，过期请求不会覆盖新账号状态。 */
    suspend fun refreshForUser(userId: String): List<Family> {
        val version = requestVersion.incrementAndGet()
        val previous = _sessionState.value.takeIf { it.userId == userId }
        _sessionState.value = (previous ?: FamilySessionState(userId = userId)).copy(
            isLoading = true,
            sessionVerified = false,
            errorMessage = null,
        )
        return try {
            check(client.auth.currentUserOrNull()?.id == userId) { "登录会话尚未验证" }
            val members: List<FamilyMember> = client.postgrest.from("family_members")
                .select(columns = Columns.ALL) { filter { eq("user_id", userId) } }
                .decodeList()
            val families = members.flatMap { member ->
                client.postgrest.from("families")
                    .select(columns = Columns.ALL) { filter { eq("id", member.familyId) } }
                    .decodeList<Family>()
            }.distinctBy { it.id }
            check(client.auth.currentUserOrNull()?.id == userId) { "登录账号已切换" }
            if (version != requestVersion.get()) return families

            val savedId = prefs.getString(selectedIdKey(userId), null)
            val legacyId = prefs.getString(LEGACY_CURRENT_FAMILY_ID, null)
            val selected = families.find { it.id == savedId }
                ?: families.find { it.id == legacyId }
                ?: families.firstOrNull()
            _sessionState.value = FamilySessionState(
                userId = userId,
                families = families,
                selectedFamily = selected,
                sessionVerified = true,
                isLocalMode = prefs.getBoolean(localModeKey(userId), false),
            )
            persistSelection(userId, selected)
            // 旧全局值只有在成员关系验证后才允许迁移。
            prefs.edit().remove(LEGACY_CURRENT_FAMILY_ID).apply()
            Timber.tag("Family").d("refresh count=%d current=%s", families.size, selected?.id)
            families
        } catch (e: Exception) {
            if (version == requestVersion.get() && _sessionState.value.userId == userId) {
                _sessionState.value = _sessionState.value.copy(
                    isLoading = false,
                    sessionVerified = false,
                    errorMessage = e.message ?: "家庭加载失败",
                )
            }
            throw e
        }
    }

    fun clearSession() {
        requestVersion.incrementAndGet()
        _sessionState.value = FamilySessionState()
    }

    fun selectFamily(family: Family): Boolean {
        val state = _sessionState.value
        if (state.userId == null || state.families.none { it.id == family.id }) return false
        _sessionState.value = state.copy(selectedFamily = family, isLocalMode = false)
        prefs.edit().putBoolean(localModeKey(state.userId), false).apply()
        persistSelection(state.userId, family)
        return true
    }

    fun selectLocalMode() {
        val state = _sessionState.value
        val userId = state.userId ?: return
        _sessionState.value = state.copy(isLocalMode = true)
        prefs.edit().putBoolean(localModeKey(userId), true).apply()
    }

    private fun persistSelection(userId: String, family: Family?) {
        val editor = prefs.edit()
        if (family == null) {
            editor.remove(selectedIdKey(userId)).remove(selectedJsonKey(userId))
        } else {
            editor.putString(selectedIdKey(userId), family.id)
                .putString(selectedJsonKey(userId), json.encodeToString(family))
        }
        editor.apply()
    }

    private fun genInviteCode(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val random = java.security.SecureRandom()
        return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    suspend fun createFamily(name: String): Result<Family> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id ?: error("请先登录")
        val familyId = java.util.UUID.randomUUID().toString()
        val inviteCode = genInviteCode()
        client.postgrest.from("families").insert(
            mapOf("id" to familyId, "name" to name, "invite_code" to inviteCode),
        )
        client.postgrest.from("family_members").insert(
            mapOf("family_id" to familyId, "user_id" to userId, "role" to "owner"),
        )
        refreshForUser(userId)
        val family = _sessionState.value.families.firstOrNull { it.id == familyId }
            ?: error("创建家庭失败")
        selectFamily(family)
        family
    }

    suspend fun joinFamily(inviteCode: String): Result<Family> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id ?: error("请先登录")
        client.postgrest.rpc("join_family", mapOf("invite_code" to inviteCode.uppercase()))
        refreshForUser(userId)
        val family = _sessionState.value.families.firstOrNull {
            it.inviteCode == inviteCode.uppercase()
        } ?: error("加入家庭失败")
        selectFamily(family)
        family
    }

    suspend fun loadMyFamilies(): List<Family> {
        val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
        return refreshForUser(userId)
    }

    suspend fun getFamilyMembers(familyId: String): List<FamilyMember> {
        check(_sessionState.value.verifiedFamilyForSync?.id == familyId) { "家庭成员关系尚未验证" }
        return client.postgrest.from("family_members")
            .select(columns = Columns.ALL) { filter { eq("family_id", familyId) } }
            .decodeList()
    }
}
