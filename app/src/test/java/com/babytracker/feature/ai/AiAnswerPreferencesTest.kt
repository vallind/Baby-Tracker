package com.babytracker.feature.ai

import com.babytracker.core.ai.settings.AiAnswerDetail
import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.ai.settings.AiAssistantPreferences
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAnswerPreferencesTest {
    @Test
    fun `回答偏好进入系统提示`() {
        val prompt = answerPreferencePrompt(
            AiAssistantPreferences(
                answerDetail = AiAnswerDetail.CONCISE,
                answerTone = AiAnswerTone.GENTLE,
                includeActionChecklist = false,
            ),
        )

        assertTrue(prompt.contains("3 至 5 个要点"))
        assertTrue(prompt.contains("温和支持"))
        assertTrue(prompt.contains("不要固定生成行动清单"))
    }

    @Test
    fun `事实安全提示禁止编造记录和具体药物方案`() {
        val prompt = factualSafetyPrompt()

        assertTrue(prompt.contains("不得编造、推断或默认"))
        assertTrue(prompt.contains("药物或消毒剂的具体名称、浓度、剂量"))
        assertTrue(prompt.contains("疫苗状态"))
    }
}
