package com.babytracker.designsystem.components.pininput

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AppPinInput 输入净化纯函数测试。
 * 约定：仅保留数字、截断到 length；Pin/OTP/Code 三场景共用同一净化路径。
 */
class SanitizePinTest {

    @Test
    fun `digits pass through`() {
        assertEquals("1234", sanitizePin("1234", 6))
    }

    @Test
    fun `letters and symbols stripped`() {
        assertEquals("12", sanitizePin("1a2-=", 6))
        assertEquals("", sanitizePin("abcd", 6))
    }

    @Test
    fun `truncated to length`() {
        assertEquals("123456", sanitizePin("1234567890", 6))
        assertEquals("9", sanitizePin("9", 1))
    }

    @Test
    fun `paste with mixed content keeps leading digits only`() {
        assertEquals("42", sanitizePin(" 4a2b! ", 4))
    }
}
