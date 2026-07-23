package com.babytracker.core.ai

import kotlinx.serialization.Serializable
import java.net.URI

object AiProtocols {
    const val OPENAI_RESPONSES = "openai_responses"
    const val OPENAI_COMPATIBLE_CHAT = "openai_compatible_chat"

    val supported = setOf(OPENAI_RESPONSES, OPENAI_COMPATIBLE_CHAT)
}

@Serializable
data class AiProviderConfig(
    val id: String,
    val protocol: String,
    val baseUrl: String,
    val credentialVersion: Int,
)

@Serializable
data class AiModelTarget(
    val providerId: String,
    val model: String,
    val priority: Int = 0,
)

@Serializable
data class AiModelOption(
    val id: String,
    val name: String,
    val targets: List<AiModelTarget>,
    val maxOutputTokens: Int = 2_400,
    val capabilities: AiModelCapabilities = AiModelCapabilities(),
)

@Serializable
data class AiModelCapabilities(
    val streaming: Boolean = false,
    val thinking: Boolean = false,
    val reasoningEfforts: List<String> = emptyList(),
    val temperature: Boolean = false,
)

@Serializable
data class AiRuntimeConfig(
    val configVersion: Int,
    val defaultOption: String,
    val providers: List<AiProviderConfig>,
    val options: List<AiModelOption>,
)

@Serializable
data class AiCredentialEnvelope(
    val providerId: String,
    val credentialVersion: Int,
    val envelope: String,
)

@Serializable
data class AiRuntimeBundle(
    val userId: String,
    val familyId: String,
    val expiresAt: Long,
    val config: AiRuntimeConfig,
    val credentials: List<AiCredentialEnvelope>,
) {
    fun isValidAt(now: Long = System.currentTimeMillis()): Boolean = expiresAt > now
}

@Serializable
internal data class AiBootstrapRequest(
    val familyId: String,
    val cachedConfigVersion: Int,
    val devicePublicKey: String,
)

@Serializable
internal data class AiBootstrapResponse(
    val configVersion: Int,
    val expiresAt: Long,
    val config: AiRuntimeConfig,
    val credentials: List<AiCredentialEnvelope>,
)

data class AiMessage(
    val role: String,
    val content: String,
)

data class AiCompletion(
    val providerId: String,
    val model: String,
    val text: String,
)

data class AiGenerationOptions(
    val maxOutputTokens: Int? = null,
    val streaming: Boolean = false,
    val thinking: String? = null,
    val reasoningEffort: String? = null,
    val temperature: Double? = null,
)

object AiRuntimeConfigValidator {
    fun requireValid(bundle: AiRuntimeBundle): AiRuntimeBundle {
        require(bundle.userId.isNotBlank()) { "AI 配置缺少用户范围" }
        require(bundle.familyId.isNotBlank()) { "AI 配置缺少家庭范围" }
        require(bundle.isValidAt()) { "AI 配置已过期" }
        requireValid(bundle.config)

        val credentials = bundle.credentials.associateBy { it.providerId }
        bundle.config.providers.forEach { provider ->
            val credential = credentials[provider.id]
                ?: error("供应商 ${provider.id} 缺少凭据")
            require(credential.credentialVersion == provider.credentialVersion) {
                "供应商 ${provider.id} 的凭据版本不匹配"
            }
            require(credential.envelope.isNotBlank()) { "供应商 ${provider.id} 的凭据为空" }
        }
        return bundle
    }

    fun requireValid(config: AiRuntimeConfig): AiRuntimeConfig {
        require(config.configVersion > 0) { "AI 配置版本无效" }
        require(config.providers.isNotEmpty()) { "AI 配置没有供应商" }
        require(config.options.isNotEmpty()) { "AI 配置没有模型选项" }
        require(config.providers.map { it.id }.distinct().size == config.providers.size) {
            "AI 供应商 ID 重复"
        }
        require(config.options.map { it.id }.distinct().size == config.options.size) {
            "AI 模型选项 ID 重复"
        }

        val providers = config.providers.associateBy { it.id }
        config.providers.forEach { provider ->
            require(provider.id.isNotBlank()) { "AI 供应商 ID 为空" }
            require(provider.protocol in AiProtocols.supported) {
                "不支持的 AI 协议：${provider.protocol}"
            }
            require(provider.credentialVersion > 0) { "供应商 ${provider.id} 的凭据版本无效" }
            val uri = runCatching { URI(provider.baseUrl) }.getOrNull()
            require(uri?.scheme == "https" && !uri.host.isNullOrBlank()) {
                "供应商 ${provider.id} 必须使用有效的 HTTPS 地址"
            }
        }

        require(config.defaultOption in config.options.map { it.id }) { "默认模型选项不存在" }
        config.options.forEach { option ->
            require(option.id.isNotBlank() && option.name.isNotBlank()) { "AI 模型选项信息不完整" }
            require(option.targets.isNotEmpty()) { "模型选项 ${option.id} 没有调用目标" }
            require(option.maxOutputTokens > 0) { "模型选项 ${option.id} 的默认输出 Token 无效" }
            require(option.capabilities.reasoningEfforts.all { it in VALID_REASONING_EFFORTS }) {
                "模型选项 ${option.id} 的推理强度声明无效"
            }
            option.targets.forEach { target ->
                require(target.providerId in providers) {
                    "模型选项 ${option.id} 引用了未知供应商 ${target.providerId}"
                }
                require(target.model.isNotBlank()) { "模型选项 ${option.id} 的模型 ID 为空" }
            }
        }
        return config
    }

    private val VALID_REASONING_EFFORTS = setOf("low", "medium", "high", "max")
}
