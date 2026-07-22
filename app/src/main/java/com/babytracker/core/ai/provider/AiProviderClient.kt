package com.babytracker.core.ai.provider

import com.babytracker.core.ai.AiCompletion
import com.babytracker.core.ai.AiMessage
import com.babytracker.core.ai.config.AiConfigCoordinator
import com.babytracker.core.ai.config.AiDeviceKeyStore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class AiProviderClient(
    adapters: List<AiProviderAdapter>,
    private val configCoordinator: AiConfigCoordinator,
    private val deviceKeyStore: AiDeviceKeyStore,
) {
    private val adaptersByProtocol = adapters.associateBy { it.protocol }

    suspend fun complete(
        messages: List<AiMessage>,
        optionId: String? = null,
    ): AiCompletion {
        require(messages.isNotEmpty()) { "AI 问题不能为空" }
        require(messages.all { it.content.isNotBlank() }) { "AI 消息内容不能为空" }
        val bundle = configCoordinator.readyBundle() ?: error("AI 配置尚未就绪")
        val selectedId = optionId ?: bundle.config.defaultOption
        val option = bundle.config.options.firstOrNull { it.id == selectedId }
            ?: error("AI 模型选项不存在：$selectedId")
        val providers = bundle.config.providers.associateBy { it.id }
        val credentials = bundle.credentials.associateBy { it.providerId }
        var lastRetryableError: AiProviderException? = null

        option.targets.sortedBy { it.priority }.forEach { target ->
            val provider = providers[target.providerId]
                ?: error("AI 供应商配置不存在：${target.providerId}")
            val credential = credentials[target.providerId]
                ?.takeIf { it.credentialVersion == provider.credentialVersion }
                ?: error("AI 供应商凭据不可用：${target.providerId}")
            val adapter = adaptersByProtocol[provider.protocol]
                ?: error("AI 协议未实现：${provider.protocol}")
            val apiKey = deviceKeyStore.decrypt(credential.envelope)
            try {
                val text = adapter.complete(
                    provider = provider,
                    model = target.model,
                    messages = messages,
                    maxOutputTokens = option.maxOutputTokens,
                    apiKey = apiKey,
                )
                return AiCompletion(provider.id, target.model, text)
            } catch (e: AiProviderException) {
                currentCoroutineContext().ensureActive()
                if (e.statusCode == 401) {
                    // 凭据可能已轮换，先刷新配置；本次请求不跨供应商掩盖认证错误。
                    configCoordinator.refreshNow()
                    throw e
                }
                if (!e.retryable) throw e
                lastRetryableError = e
            }
        }
        throw lastRetryableError ?: error("AI 模型选项没有可用目标")
    }
}
