package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.AiGenerationOptions
import com.babytracker.core.ai.AiProtocols
import com.babytracker.core.ai.AiProviderConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient

class OpenAiResponsesAdapter(
    private val httpClient: OkHttpClient,
) : AiProviderAdapter {
    override val protocol: String = AiProtocols.OPENAI_RESPONSES
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
            put("max_output_tokens", options.maxOutputTokens ?: maxOutputTokens)
            options.temperature?.let { put("temperature", it) }
            val effort = when (options.thinking) {
                "disabled" -> "none"
                "enabled" -> options.reasoningEffort ?: "medium"
                else -> options.reasoningEffort
            }
            effort?.let {
                put("reasoning", buildJsonObject { put("effort", it) })
            }
            put("input", buildJsonArray {
                messages.forEach { message ->
                    add(buildJsonObject {
                        // Responses API 使用 developer 表达应用级指令。
                        put("role", if (message.role == "system") "developer" else message.role)
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
                path = "responses",
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
            path = "responses",
            apiKey = apiKey,
            body = body,
        )
        return parseText(response) ?: throw AiProviderException(
            providerId = provider.id,
            retryable = false,
            message = "供应商 ${provider.id} 的回答为空",
        )
    }

    internal fun parseText(response: JsonObject): String? {
        val direct = runCatching {
            response["output_text"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()?.takeIf { it.isNotBlank() }
        if (direct != null) return direct

        return runCatching {
            response["output"]?.jsonArray.orEmpty()
                .flatMap { item -> item.jsonObject["content"]?.jsonArray.orEmpty() }
                .mapNotNull { content -> content.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
                .joinToString(separator = "")
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    internal fun parseStreamDelta(data: String): String? = runCatching {
        val event = json.parseToJsonElement(data).jsonObject
        event.takeIf { it["type"]?.jsonPrimitive?.contentOrNull == "response.output_text.delta" }
            ?.get("delta")?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}
