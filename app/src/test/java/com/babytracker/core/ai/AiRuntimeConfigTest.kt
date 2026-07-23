package com.babytracker.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiRuntimeConfigTest {
    @Test
    fun `multi-provider config is valid`() {
        val config = validConfig()

        assertEquals(config, AiRuntimeConfigValidator.requireValid(config))
    }

    @Test
    fun `provider endpoint must use https`() {
        val config = validConfig().copy(
            providers = validConfig().providers.mapIndexed { index, provider ->
                if (index == 0) provider.copy(baseUrl = "http://insecure.example/v1") else provider
            },
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiRuntimeConfigValidator.requireValid(config)
        }
    }

    @Test
    fun `target cannot reference unknown provider`() {
        val config = validConfig().copy(
            options = listOf(
                validConfig().options.first().copy(
                    targets = listOf(AiModelTarget("missing", "model", 0)),
                ),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiRuntimeConfigValidator.requireValid(config)
        }
    }

    @Test
    fun `backend capability config does not cap output tokens`() {
        val config = validConfig().copy(
            options = listOf(
                validConfig().options.first().copy(
                    maxOutputTokens = 100_000,
                    capabilities = AiModelCapabilities(
                        streaming = true,
                        thinking = true,
                        reasoningEfforts = listOf("high", "max"),
                    ),
                ),
            ),
        )

        assertEquals(config, AiRuntimeConfigValidator.requireValid(config))
    }

    @Test
    fun `credential version must match provider`() {
        val config = validConfig()
        val bundle = AiRuntimeBundle(
            userId = "user",
            familyId = "family",
            expiresAt = System.currentTimeMillis() + 60_000,
            config = config,
            credentials = config.providers.map { provider ->
                AiCredentialEnvelope(provider.id, provider.credentialVersion + 1, "encrypted")
            },
        )

        assertThrows(IllegalArgumentException::class.java) {
            AiRuntimeConfigValidator.requireValid(bundle)
        }
    }

    private fun validConfig() = AiRuntimeConfig(
        configVersion = 3,
        defaultOption = "balanced",
        providers = listOf(
            AiProviderConfig("provider_a", AiProtocols.OPENAI_RESPONSES, "https://a.example/v1", 1),
            AiProviderConfig("provider_b", AiProtocols.OPENAI_COMPATIBLE_CHAT, "https://b.example/v1", 2),
        ),
        options = listOf(
            AiModelOption(
                id = "balanced",
                name = "均衡",
                targets = listOf(
                    AiModelTarget("provider_a", "model-a", 0),
                    AiModelTarget("provider_b", "model-b", 1),
                ),
            ),
        ),
    )
}
