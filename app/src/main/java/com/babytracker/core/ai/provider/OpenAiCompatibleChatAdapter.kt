package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.AiGenerationOptions
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
        options: AiGenerationOptions,
        apiKey: String,
        onTextUpdate: suspend (String) -> Unit,
    ): String {
        val body = buildJsonObject {
            put("model", model)
            put("stream", options.streaming)
            put("max_tokens", options.maxOutputTokens ?: maxOutputTokens)
            options.temperature?.let { put("temperature", it) }
            options.thinking?.let { thinking ->
                put("thinking", buildJsonObject {
                    put("type", thinking)
                    options.reasoningEffort?.let { put("reasoning_effort", it) }
                })
            }
            put("messages", buildJsonArray {
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message.role)
                        put("content", message.content)
                    })
                }
            })
        }
        if (options.streaming) {
            val text = StringBuilder()
            executeStreamingRequest(
                httpClient = httpClient,
                provider = provider,
                path = "chat/completions",
                apiKey = apiKey,
                body = body,
            ) { data ->
                val delta = parseStreamDelta(data)
                if (!delta.isNullOrEmpty()) {
                    text.append(delta)
                    onTextUpdate(text.toString())
                }
            }
            return text.toString().takeIf { it.isNotBlank() } ?: throw AiProviderException(
                providerId = provider.id,
                retryable = false,
                message = "供应商 ${provider.id} 的回答为空",
            )
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

    internal fun parseStreamDelta(data: String): String? = runCatching {
        json.parseToJsonElement(data).jsonObject["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("delta")?.jsonObject
            ?.get("content")?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}
