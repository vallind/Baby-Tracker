package com.babytracker.feature.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConversationHistoryTest {
    @Test
    fun `消息上限只保留完整轮次和当前问题`() {
        val messages = buildList {
            repeat(6) { index ->
                add(entry(index * 2L, AiChatRole.USER, "问题$index"))
                add(entry(index * 2L + 1, AiChatRole.ASSISTANT, "回答$index"))
            }
            add(entry(20, AiChatRole.USER, "当前问题"))
        }

        val selected = selectAiHistory(messages, maxMessages = 10, maxCharacters = 1_000)

        assertEquals(9, selected.size)
        assertEquals(AiChatRole.USER, selected.first().role)
        assertEquals("问题2", selected.first().content)
        assertEquals("当前问题", selected.last().content)
    }

    @Test
    fun `字符上限按轮次丢弃旧消息`() {
        val messages = listOf(
            entry(1, AiChatRole.USER, "旧问题"),
            entry(2, AiChatRole.ASSISTANT, "旧回答很长"),
            entry(3, AiChatRole.USER, "新问题"),
            entry(4, AiChatRole.ASSISTANT, "新回答"),
        )

        val selected = selectAiHistory(messages, maxMessages = 10, maxCharacters = 6)

        assertEquals(listOf("新问题", "新回答"), selected.map { it.content })
    }

    @Test
    fun `忽略没有用户问题的孤立回答`() {
        val selected = selectAiHistory(
            messages = listOf(
                entry(1, AiChatRole.ASSISTANT, "孤立回答"),
                entry(2, AiChatRole.USER, "有效问题"),
            ),
            maxMessages = 10,
            maxCharacters = 100,
        )

        assertEquals(1, selected.size)
        assertTrue(selected.none { it.content == "孤立回答" })
    }

    private fun entry(id: Long, role: AiChatRole, content: String) =
        AiChatEntry(id = id, role = role, content = content)
}
