package com.babytracker.feature.ai

/**
 * 按完整问答轮次截取历史，避免简单 takeLast 后以孤立的 assistant 消息开头。
 * 当前正在发送的用户问题允许作为最后一个未完成轮次保留。
 */
internal fun selectAiHistory(
    messages: List<AiChatEntry>,
    maxMessages: Int,
    maxCharacters: Int,
): List<AiChatEntry> {
    if (maxMessages <= 0 || maxCharacters <= 0) return emptyList()

    val rounds = mutableListOf<MutableList<AiChatEntry>>()
    messages.forEach { message ->
        when (message.role) {
            AiChatRole.USER -> rounds += mutableListOf(message)
            AiChatRole.ASSISTANT -> rounds.lastOrNull()?.add(message)
        }
    }

    val selected = ArrayDeque<List<AiChatEntry>>()
    var messageCount = 0
    var characterCount = 0
    for (round in rounds.asReversed()) {
        val roundCharacters = round.sumOf { it.content.length }
        val exceedsBudget = messageCount + round.size > maxMessages ||
            characterCount + roundCharacters > maxCharacters
        if (exceedsBudget && selected.isNotEmpty()) break

        selected.addFirst(round)
        messageCount += round.size
        characterCount += roundCharacters
    }
    return selected.flatten()
}
