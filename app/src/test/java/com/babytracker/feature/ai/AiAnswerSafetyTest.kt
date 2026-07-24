package com.babytracker.feature.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAnswerSafetyTest {
    @Test
    fun `具体儿童用药剂量会阻止展示`() {
        val result = validateAiAnswer(
            answer = "可以给宝宝服用布洛芬，每次5毫升，每天三次。",
            riskLevel = null,
        )

        assertEquals(AiAnswerSafetyStatus.BLOCKED, result.status)
        assertTrue(result.content.contains("已停止展示"))
    }

    @Test
    fun `禁止自行停药的安全表达不会误判`() {
        val result = validateAiAnswer(
            answer = "不要自行停药或换药，如有疑问请联系儿科医生。",
            riskLevel = null,
        )

        assertNull(result.status)
    }

    @Test
    fun `确定性诊断和绝对保证会阻止展示`() {
        assertEquals(
            AiAnswerSafetyStatus.BLOCKED,
            validateAiAnswer("宝宝肯定是细菌感染。", null).status,
        )
        assertEquals(
            AiAnswerSafetyStatus.BLOCKED,
            validateAiAnswer("这种情况肯定没事，不用看医生。", null).status,
        )
    }

    @Test
    fun `不确定性说明不会被当作确定诊断`() {
        assertNull(validateAiAnswer("仅凭这些信息不一定是感染，无法确诊。", null).status)
        assertNull(validateAiAnswer("不能保证没事，症状加重时请及时就医。", null).status)
    }

    @Test
    fun `紧急回答缺少急救指引时由本地补充`() {
        val result = validateAiAnswer(
            answer = "请保持宝宝侧卧并持续观察。",
            riskLevel = AiRiskLevel.EMERGENCY,
        )

        assertEquals(AiAnswerSafetyStatus.SUPPLEMENTED, result.status)
        assertTrue(result.content.startsWith("请立即拨打 120"))
    }

    @Test
    fun `已有急救指引时不重复补充`() {
        val result = validateAiAnswer(
            answer = "请立即拨打120或前往急诊。",
            riskLevel = AiRiskLevel.EMERGENCY,
        )

        assertNull(result.status)
    }

    @Test
    fun `健康问题缓冲而普通睡眠问题继续流式展示`() {
        assertTrue(shouldBufferAiAnswer("宝宝发烧了怎么办", AiRiskLevel.HIGH))
        assertTrue(shouldBufferAiAnswer("布洛芬应该吃多少", null))
        assertFalse(shouldBufferAiAnswer("如何建立睡前流程", null))
    }
}
