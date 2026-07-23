package com.babytracker.feature.ai

import com.babytracker.core.domain.model.Baby
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSafetyRulesTest {
    private val today = LocalDate.parse("2026-07-23")

    @Test
    fun `紧急症状立即升级`() {
        assertEquals(AiRiskLevel.EMERGENCY, assessAiRisk("宝宝呼吸困难，嘴唇发紫", baby("2026-06-01"), today))
        assertEquals(AiRiskLevel.EMERGENCY, assessAiRisk("baby has breathing difficulty", baby("2026-06-01"), today))
    }

    @Test
    fun `否定表达不会误判抽搐`() {
        assertNull(assessAiRisk("宝宝没有出现持续抽搐，精神正常", baby("2026-06-01"), today))
        assertNull(assessAiRisk("baby has no seizure", baby("2026-06-01"), today))
    }

    @Test
    fun `三月龄以下发热属于高风险`() {
        assertEquals(AiRiskLevel.HIGH, assessAiRisk("宝宝发烧了", baby("2026-06-01"), today))
        assertNull(assessAiRisk("宝宝发烧了", baby("2026-03-01"), today))
    }

    @Test
    fun `误食药物和明显减少分别升级`() {
        assertEquals(AiRiskLevel.HIGH, assessAiRisk("宝宝误食成人药", baby("2025-06-01"), today))
        assertEquals(AiRiskLevel.ATTENTION, assessAiRisk("宝宝吃奶明显减少", baby("2025-06-01"), today))
    }

    @Test
    fun `紧急Prompt要求立即急救`() {
        val prompt = safetyPrompt(AiRiskLevel.EMERGENCY)

        assertTrue(prompt.contains("立即拨打120"))
        assertTrue(prompt.contains("不得用居家护理建议延误就医"))
    }

    private fun baby(birthDate: String) = Baby(
        name = "测试宝宝",
        gender = "unknown",
        birthDate = birthDate,
    )
}
