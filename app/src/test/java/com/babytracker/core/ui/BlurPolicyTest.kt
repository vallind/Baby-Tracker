package com.babytracker.core.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 毛玻璃启用策略：elyon-blur 的 Android 实现要求 API 33+，
 * 且运行时着色器（textureBlur）必须受支持。
 */
class BlurPolicyTest {

    @Test
    fun `API 32 不支持 blur`() {
        assertFalse(BlurPolicy.isBlurSupported(runtimeSdk = 32, shaderSupported = true))
    }

    @Test
    fun `API 33 但着色器不支持时禁用 blur`() {
        assertFalse(BlurPolicy.isBlurSupported(runtimeSdk = 33, shaderSupported = false))
    }

    @Test
    fun `API 33 且着色器支持时启用 blur`() {
        assertTrue(BlurPolicy.isBlurSupported(runtimeSdk = 33, shaderSupported = true))
    }

    @Test
    fun `更高 API 保持启用 blur`() {
        assertTrue(BlurPolicy.isBlurSupported(runtimeSdk = 36, shaderSupported = true))
    }
}
