package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.AiProtocols
import com.babytracker.core.ai.AiProviderConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient

class OpenAiCompatibleChatAdapter(
    private val httpClient: OkHttpClient,
) : AiProviderAdapter {
    override val protocol: String = AiProtocols.OPENAI_COMPATIBLE_CHAT
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(
        provider: AiProviderConfig,
        model: String,
        messages: List<AiMessage>,
        maxOutputTokens: Int,
        apiKey: String,
    ): String {
        val body = buildJsonObject {
            put("model", model)
            put("stream", false)
            put("max_tokens", maxOutputTokens)
            put("messages", buildJsonArray {
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message.role)
                        put("content", message.content)
                    })
                }
            })
        }
        val response = executeJsonRequest(
            httpClient = httpClient,
            json = json,
            provider = provider,
            path = "chat/completions",
            apiKey = apiKey,
            body = body,
        )
        return parseText(response) ?: throw AiProviderException(
            providerId = provider.id,
            retryable = false,
            message = "供应商 ${provider.id} 的回答为空",
        )
    }

    internal fun parseText(response: kotlinx.serialization.json.JsonObject): String? = runCatching {
        response["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()
}
