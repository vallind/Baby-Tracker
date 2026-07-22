package com.babytracker.core.ai.config

import com.babytracker.core.ai.AiBootstrapRequest
import com.babytracker.core.ai.AiBootstrapResponse
import com.babytracker.core.ai.AiRuntimeBundle
import com.babytracker.core.sync.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AiBootstrapException(
    val statusCode: Int,
    message: String,
) : Exception(message)

class AiBootstrapClient(
    private val supabase: SupabaseClient,
    private val httpClient: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun fetch(
        userId: String,
        familyId: String,
        cachedConfigVersion: Int,
        devicePublicKey: String,
    ): AiRuntimeBundle = withContext(Dispatchers.IO) {
        val accessToken = supabase.auth.currentAccessTokenOrNull()
            ?: throw AiBootstrapException(401, "Supabase 会话尚未验证")
        val payload = AiBootstrapRequest(
            familyId = familyId,
            cachedConfigVersion = cachedConfigVersion,
            devicePublicKey = devicePublicKey,
        )
        val request = Request.Builder()
            .url("${SupabaseProvider.PROJECT_URL}/functions/v1/ai-bootstrap")
            .header("Authorization", "Bearer $accessToken")
            .header("apikey", SupabaseProvider.PUBLIC_API_KEY)
            .header("Cache-Control", "no-store")
            .post(json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AiBootstrapException(response.code, "AI 配置获取失败（HTTP ${response.code}）")
            }
            val body = response.body.string()
            val remote = runCatching { json.decodeFromString<AiBootstrapResponse>(body) }
                .getOrElse { throw AiBootstrapException(502, "AI 配置响应格式无效") }
            if (remote.configVersion != remote.config.configVersion) {
                throw AiBootstrapException(502, "AI 配置版本不一致")
            }
            AiRuntimeBundle(
                userId = userId,
                familyId = familyId,
                expiresAt = remote.expiresAt,
                config = remote.config,
                credentials = remote.credentials,
            )
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
