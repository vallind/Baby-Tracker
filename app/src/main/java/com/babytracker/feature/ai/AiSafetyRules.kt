package com.babytracker.feature.ai

import com.babytracker.core.domain.model.Baby
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class AiRiskLevel {
    EMERGENCY,
    HIGH,
    ATTENTION,
}

internal fun assessAiRisk(
    question: String,
    baby: Baby,
    today: LocalDate = LocalDate.now(),
): AiRiskLevel? {
    val normalized = question.lowercase().replace(Regex("\\s+"), "")
    if (EMERGENCY_KEYWORDS.any { normalized.hasActiveKeyword(it) }) {
        return AiRiskLevel.EMERGENCY
    }

    val ageMonths = runCatching {
        val birthDate = LocalDate.parse(baby.birthDate.take(10))
        ChronoUnit.MONTHS.between(birthDate, today).coerceAtLeast(0)
    }.getOrNull()
    val infantFever = ageMonths != null &&
        ageMonths < 3 &&
        FEVER_KEYWORDS.any { normalized.hasActiveKeyword(it) }
    if (infantFever || HIGH_RISK_KEYWORDS.any { normalized.hasActiveKeyword(it) }) {
        return AiRiskLevel.HIGH
    }

    if (ATTENTION_KEYWORDS.any { normalized.hasActiveKeyword(it) }) {
        return AiRiskLevel.ATTENTION
    }
    return null
}

internal fun safetyPrompt(riskLevel: AiRiskLevel?): String = when (riskLevel) {
    AiRiskLevel.EMERGENCY ->
        "本地规则判定为紧急风险。回答第一句必须建议立即拨打120或前往急诊，不得用居家护理建议延误就医。"
    AiRiskLevel.HIGH ->
        "本地规则判定为高风险。请优先建议尽快联系儿科医生或前往医院，再提供等待就医期间的安全观察要点。"
    AiRiskLevel.ATTENTION ->
        "本地规则判定为需要关注。请给出明确观察指标、停止居家观察并就医的条件。"
    null ->
        "即使本地规则未命中，若问题中存在危险信号，也必须明确指出就医时机。"
}

private fun String.hasActiveKeyword(keyword: String): Boolean {
    var index = indexOf(keyword)
    while (index >= 0) {
        val prefix = substring((index - NEGATION_WINDOW).coerceAtLeast(0), index)
        if (NEGATIONS.none { prefix.contains(it) }) return true
        index = indexOf(keyword, index + keyword.length)
    }
    return false
}

private const val NEGATION_WINDOW = 10
private val NEGATIONS = listOf("没有出现", "并没有", "没有", "未出现", "并未", "不是", "无", "without", "not", "no")
private val EMERGENCY_KEYWORDS = listOf(
    "呼吸困难",
    "喘不上气",
    "嘴唇发紫",
    "脸色发紫",
    "失去意识",
    "昏迷",
    "抽搐",
    "breathingdifficulty",
    "bluelips",
    "unconscious",
    "seizure",
)
private val FEVER_KEYWORDS = listOf("发热", "发烧", "高烧", "高热", "体温38", "体温39", "体温40")
private val HIGH_RISK_KEYWORDS = listOf(
    "严重脱水",
    "完全无尿",
    "误食药物",
    "误服药物",
    "误食成人药",
    "吞了药",
)
private val ATTENTION_KEYWORDS = listOf(
    "精神很差",
    "精神萎靡",
    "持续呕吐",
    "反复呕吐",
    "拒奶",
    "吃奶明显减少",
    "进食明显减少",
    "尿量明显减少",
    "尿量很少",
    "症状持续",
)
