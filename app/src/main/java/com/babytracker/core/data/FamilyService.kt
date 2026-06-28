package com.babytracker.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── 序列化数据模型（必须在顶层，嵌套类序列化器运行时可能找不到）──

@Serializable
data class Family(
    val id: String = "",
    val name: String = "",
    @SerialName("invite_code") val inviteCode: String = "",
)

@Serializable
data class FamilyMember(
    @SerialName("family_id") val familyId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "member",
    @SerialName("joined_at") val joinedAt: String? = null,
)

/**
 * 家庭共享服务 —— 调用 Supabase API 管理家庭和成员。
 * 数据仅存于 Supabase，不在 Room 本地存储。
 */
class FamilyService(
    private val client: SupabaseClient,
) {

    // ── 状态 ──

    private val _myFamilies = MutableStateFlow<List<Family>>(emptyList())
    val myFamilies: StateFlow<List<Family>> = _myFamilies.asStateFlow()

    private val _currentFamily = MutableStateFlow<Family?>(null)
    val currentFamily: StateFlow<Family?> = _currentFamily.asStateFlow()

    // ── API 操作 ──

    /** 创建家庭并自动成为 owner */
    suspend fun createFamily(name: String): Result<Family> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("请先登录")

        // 客户端生成 UUID（避免 RLS 阻止回查：必须先加入成员才能读 families）
        val familyId = java.util.UUID.randomUUID().toString()

        // 1. 创建家庭（显式指定 ID）
        client.postgrest.from("families").insert(mapOf("id" to familyId, "name" to name))

        // 2. 将自己加入为 owner（必须在查询前，否则 RLS 拦截；重复插入不算错）
        try {
            client.postgrest.from("family_members").insert(
                mapOf("family_id" to familyId, "user_id" to userId, "role" to "owner")
            )
        } catch (_: Exception) {
            // 已存在则忽略（PK 冲突说明已在家庭中）
        }

        // 3. 现在可以通过 RLS（is_family_member 返回 true）
        val families: List<Family> = client.postgrest.from("families")
            .select(columns = Columns.ALL) {
                filter { eq("id", familyId) }
            }
            .decodeList<Family>()

        val family = families.firstOrNull() ?: throw Exception("创建家庭失败")

        _myFamilies.value = _myFamilies.value + family
        _currentFamily.value = family
        family
    }

    /** 通过邀请码加入家庭 */
    suspend fun joinFamily(inviteCode: String): Result<Family> = runCatching {
        // 查找邀请码对应的家庭
        val families: List<Family> = client.postgrest.from("families")
            .select(columns = Columns.ALL) {
                filter { eq("invite_code", inviteCode) }
            }
            .decodeList<Family>()

        val family = families.firstOrNull() ?: throw Exception("邀请码无效")
        val userId = client.auth.currentUserOrNull()?.id ?: throw Exception("请先登录")

        // 加入家庭
        client.postgrest.from("family_members").insert(
            mapOf("family_id" to family.id, "user_id" to userId, "role" to "member")
        )

        loadMyFamilies()
        family
    }

    /** 获取当前用户的所有家庭 */
    suspend fun loadMyFamilies(): List<Family> {
        val userId = client.auth.currentUserOrNull()?.id ?: run {
            _myFamilies.value = emptyList()
            _currentFamily.value = null
            return emptyList()
        }

        val members: List<FamilyMember> = client.postgrest.from("family_members")
            .select(columns = Columns.ALL) {
                filter { eq("user_id", userId) }
            }
            .decodeList<FamilyMember>()

        if (members.isEmpty()) {
            _myFamilies.value = emptyList()
            _currentFamily.value = null
            return emptyList()
        }

        val allFamilies = mutableListOf<Family>()
        for (member in members) {
            val result: List<Family> = client.postgrest.from("families")
                .select(columns = Columns.ALL) {
                    filter { eq("id", member.familyId) }
                }
                .decodeList<Family>()
            allFamilies.addAll(result)
        }

        _myFamilies.value = allFamilies
        if (_currentFamily.value == null) _currentFamily.value = allFamilies.firstOrNull()
        return allFamilies
    }

    /** 获取家庭成员列表 */
    suspend fun getFamilyMembers(familyId: String): List<FamilyMember> {
        return client.postgrest.from("family_members")
            .select(columns = Columns.ALL) {
                filter { eq("family_id", familyId) }
            }
            .decodeList<FamilyMember>()
    }
}
