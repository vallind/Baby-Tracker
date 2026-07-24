package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.AiGenerationOptions
import com.babytracker.core.ai.AiProtocols
import com.babytracker.core.ai.AiProviderConfig
import com.babytracker.core.ai.AiTextOutput
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
        onTextUpdate: suspend (AiTextOutput) -> Unit,
    ): AiTextOutput {
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
                put("reasoning", buildJsonObject {
                    put("effort", it)
                    if (it != "none") put("summary", "auto")
                })
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
            val reasoning = StringBuilder()
            executeStreamingRequest(
                httpClient = httpClient,
                provider = provider,
                path = "responses",
                apiKey = apiKey,
                body = body,
            ) { data ->
                val delta = parseStreamDelta(data)
                if (delta != null) {
                    text.append(delta.text)
                    reasoning.append(delta.reasoningContent)
                    onTextUpdate(
                        AiTextOutput(
                            text = text.toString(),
                            reasoningContent = reasoning.toString(),
                        ),
                    )
                }
            }
            if (text.isBlank()) throw AiProviderException(
                providerId = provider.id,
                retryable = false,
                message = "供应商 ${provider.id} 的回答为空",
            )
            return AiTextOutput(text.toString(), reasoning.toString())
        }
        val response = executeJsonRequest(
            httpClient = httpClient,
            json = json,
            provider = provider,
            path = "responses",
            apiKey = apiKey,
            body = body,
        )
        return parseResponse(response) ?: throw AiProviderException(
            providerId = provider.id,
            retryable = false,
            message = "供应商 ${provider.id} 的回答为空",
        )
    }

    internal fun parseResponse(response: JsonObject): AiTextOutput? {
        val direct = runCatching {
            response["output_text"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()?.takeIf { it.isNotBlank() }
        val output = runCatching { response["output"]?.jsonArray.orEmpty() }.getOrDefault(emptyList())
        val text = direct ?: runCatching {
            output.flatMap { item -> item.jsonObject["content"]?.jsonArray.orEmpty() }
                .mapNotNull { content -> content.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
                .joinToString(separator = "")
                .takeIf { it.isNotBlank() }
        }.getOrNull() ?: return null
        val reasoning = runCatching {
            output.filter { item ->
                item.jsonObject["type"]?.jsonPrimitive?.contentOrNull == "reasoning"
            }.flatMap { item -> item.jsonObject["summary"]?.jsonArray.orEmpty() }
                .mapNotNull { summary -> summary.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
                .joinToString(separator = "")
        }.getOrDefault("")
        return AiTextOutput(text, reasoning)
    }

    internal fun parseText(response: JsonObject): String? = parseResponse(response)?.text

    internal fun parseStreamDelta(data: String): AiTextOutput? = runCatching {
        val event = json.parseToJsonElement(data).jsonObject
        val delta = event["delta"]?.jsonPrimitive?.contentOrNull.orEmpty()
        when (event["type"]?.jsonPrimitive?.contentOrNull) {
            "response.output_text.delta" -> AiTextOutput(text = delta)
            "response.reasoning_summary_text.delta" -> AiTextOutput(reasoningContent = delta)
            else -> null
        }
    }.getOrNull()
}
