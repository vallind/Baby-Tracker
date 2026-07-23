package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.AiGenerationOptions
import com.babytracker.core.ai.AiProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

interface AiProviderAdapter {
    val protocol: String

    suspend fun complete(
        provider: AiProviderConfig,
        model: String,
        messages: List<AiMessage>,
        maxOutputTokens: Int,
        options: AiGenerationOptions,
        apiKey: String,
        onTextUpdate: suspend (String) -> Unit,
    ): String
}

class AiProviderException(
    val providerId: String,
    val statusCode: Int? = null,
    val retryable: Boolean,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

internal suspend fun executeJsonRequest(
    httpClient: OkHttpClient,
    json: Json,
    provider: AiProviderConfig,
    path: String,
    apiKey: String,
    body: JsonObject,
): JsonObject = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("${provider.baseUrl.trimEnd('/')}/$path")
        .header("Authorization", "Bearer $apiKey")
        .header("Content-Type", "application/json")
        .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()
    try {
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val code = response.code
                throw AiProviderException(
                    providerId = provider.id,
                    statusCode = code,
                    retryable = code == 408 || code == 429 || code in 500..599,
                    message = "供应商 ${provider.id} 请求失败（HTTP $code）",
                )
            }
            val responseBody = response.body.string()
            runCatching { json.parseToJsonElement(responseBody).jsonObject }
                .getOrElse {
                    throw AiProviderException(
                        providerId = provider.id,
                        retryable = false,
                        message = "供应商 ${provider.id} 返回了无效响应",
                        cause = it,
                    )
                }
        }
    } catch (e: AiProviderException) {
        throw e
    } catch (e: IOException) {
        currentCoroutineContext().ensureActive()
        throw AiProviderException(
            providerId = provider.id,
            retryable = true,
            message = "供应商 ${provider.id} 网络连接失败",
            cause = e,
        )
    }
}

private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

internal suspend fun executeStreamingRequest(
    httpClient: OkHttpClient,
    provider: AiProviderConfig,
    path: String,
    apiKey: String,
    body: JsonObject,
    onData: suspend (String) -> Unit,
) = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("${provider.baseUrl.trimEnd('/')}/$path")
        .header("Authorization", "Bearer $apiKey")
        .header("Content-Type", "application/json")
        .header("Accept", "text/event-stream")
        .post(body.toString().toRequestBody(JSON_MEDIA_TYPE))
        .build()
    try {
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val code = response.code
                throw AiProviderException(
                    providerId = provider.id,
                    statusCode = code,
                    retryable = code == 408 || code == 429 || code in 500..599,
                    message = "供应商 ${provider.id} 请求失败（HTTP $code）",
                )
            }
            val source = response.body.source()
            while (!source.exhausted()) {
                currentCoroutineContext().ensureActive()
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data:")) {
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isNotEmpty()) onData(data)
                }
            }
        }
    } catch (e: AiProviderException) {
        throw e
    } catch (e: IOException) {
        currentCoroutineContext().ensureActive()
        throw AiProviderException(
            providerId = provider.id,
            retryable = true,
            message = "供应商 ${provider.id} 网络连接失败",
            cause = e,
        )
    }
}
