package com.babytracker.feature.ai

enum class AiAnswerSafetyStatus {
    SUPPLEMENTED,
    BLOCKED,
}

data class AiAnswerSafetyResult(
    val content: String,
    val status: AiAnswerSafetyStatus? = null,
)

internal fun validateAiAnswer(
    answer: String,
    riskLevel: AiRiskLevel?,
): AiAnswerSafetyResult {
    val normalized = answer.lowercase()
    if (containsUnsafeMedicationAdvice(normalized) ||
        containsUnsafeClaim(normalized, UNSAFE_DIAGNOSIS) ||
        containsUnsafeClaim(normalized, UNSAFE_REASSURANCE)
    ) {
        return AiAnswerSafetyResult(
            content = blockedAnswer(riskLevel),
            status = AiAnswerSafetyStatus.BLOCKED,
        )
    }

    val requiredGuidance = when {
        riskLevel == AiRiskLevel.EMERGENCY && !hasEmergencyGuidance(normalized) ->
            "请立即拨打 120 或前往最近的急诊，不要等待或只依赖线上建议。"
        riskLevel == AiRiskLevel.HIGH && !hasMedicalGuidance(normalized) ->
            "宝宝情况可能属于高风险，请尽快联系儿科医生或前往医院。"
        else -> null
    }
    return if (requiredGuidance == null) {
        AiAnswerSafetyResult(answer)
    } else {
        AiAnswerSafetyResult(
            content = "$requiredGuidance\n\n$answer",
            status = AiAnswerSafetyStatus.SUPPLEMENTED,
        )
    }
}

internal fun shouldBufferAiAnswer(
    question: String,
    riskLevel: AiRiskLevel?,
): Boolean {
    if (riskLevel != null || classifyAiQuestion(question) == AiQuestionCategory.HEALTH) return true
    val normalized = question.lowercase().replace(Regex("\\s+"), "")
    return SAFETY_REVIEW_KEYWORDS.any(normalized::contains) ||
        MEDICATION_KEYWORDS.any(normalized::contains)
}

private fun containsUnsafeClaim(answer: String, phrases: List<String>): Boolean =
    answer.split(Regex("[。！？!?；;\\n]")).any { sentence ->
        phrases.any(sentence::contains) && UNCERTAINTY_MARKERS.none(sentence::contains)
    }

private fun containsUnsafeMedicationAdvice(answer: String): Boolean {
    val sentences = answer.split(Regex("[。！？!?；;\\n]"))
    return sentences.any { sentence ->
        val hasMedication = MEDICATION_KEYWORDS.any(sentence::contains)
        val hasDose = MEDICATION_DOSE_PATTERNS.any { it.containsMatchIn(sentence) }
        val hasUnsafeChange = UNSAFE_MEDICATION_CHANGES.any(sentence::contains)
        hasMedication && (hasDose || hasUnsafeChange) && !sentence.hasSafetyNegation()
    }
}

private fun String.hasSafetyNegation(): Boolean =
    SAFETY_NEGATIONS.any(::contains)

private fun hasEmergencyGuidance(answer: String): Boolean =
    EMERGENCY_GUIDANCE.any(answer::contains)

private fun hasMedicalGuidance(answer: String): Boolean =
    hasEmergencyGuidance(answer) || MEDICAL_GUIDANCE.any(answer::contains)

private fun blockedAnswer(riskLevel: AiRiskLevel?): String = when (riskLevel) {
    AiRiskLevel.EMERGENCY ->
        "原回答包含不适合由 AI 提供的医疗建议，已停止展示。请立即拨打 120 或前往最近的急诊。"
    AiRiskLevel.HIGH ->
        "原回答包含不适合由 AI 提供的医疗建议，已停止展示。请尽快联系儿科医生或前往医院。"
    AiRiskLevel.ATTENTION, null ->
        "原回答包含不适合由 AI 提供的诊断或用药建议，已停止展示。请咨询儿科医生或药师。"
}

private val SAFETY_REVIEW_KEYWORDS = listOf(
    "药",
    "发烧",
    "发热",
    "体温",
    "咳嗽",
    "呕吐",
    "腹泻",
    "皮疹",
    "抽搐",
    "呼吸",
    "误食",
    "诊断",
    "什么病",
)
private val MEDICATION_KEYWORDS = listOf(
    "药",
    "布洛芬",
    "对乙酰氨基酚",
    "抗生素",
    "退烧",
    "剂量",
)
private val MEDICATION_DOSE_PATTERNS = listOf(
    Regex("""\d+(?:\.\d+)?\s*(?:mg|ml|毫克|毫升|片|粒|滴)\s*(?:/kg|每公斤|每次|一次|每日|每天)"""),
    Regex("""(?:每公斤|每次|一次|每日|每天)\s*\d+(?:\.\d+)?\s*(?:mg|ml|毫克|毫升|片|粒|滴)"""),
)
private val UNSAFE_MEDICATION_CHANGES = listOf(
    "自行停药",
    "可以停药",
    "立即停药",
    "直接停药",
    "换成",
    "改用",
    "加大剂量",
    "增加剂量",
    "减少剂量",
)
private val SAFETY_NEGATIONS = listOf(
    "不要",
    "不能",
    "不可",
    "禁止",
    "避免",
    "请勿",
    "不建议",
    "不得",
)
private val UNCERTAINTY_MARKERS = SAFETY_NEGATIONS + listOf(
    "不一定",
    "无法",
    "未必",
    "并非",
    "不能判断",
    "不能确诊",
)
private val UNSAFE_DIAGNOSIS = listOf(
    "可以确诊",
    "已经确诊",
    "肯定是",
    "一定是",
    "就是得了",
    "就是患有",
)
private val UNSAFE_REASSURANCE = listOf(
    "肯定没事",
    "绝对没事",
    "保证没事",
    "无需就医",
    "不用看医生",
    "不需要看医生",
)
private val EMERGENCY_GUIDANCE = listOf(
    "拨打120",
    "拨打 120",
    "立即急救",
    "立即前往急诊",
    "马上前往急诊",
    "立即就医",
)
private val MEDICAL_GUIDANCE = listOf(
    "尽快就医",
    "尽快联系医生",
    "尽快联系儿科",
    "尽快前往医院",
    "及时就医",
)
