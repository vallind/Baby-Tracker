package com.babytracker.feature.ai

import com.babytracker.core.data.repository.AiConversationSummary
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

    @Test
    fun `会话标题取首个问题并清理换行`() {
        val title = aiConversationTitle(
            listOf(
                entry(1, AiChatRole.USER, "宝宝最近\n夜醒很多，该怎么调整？"),
                entry(2, AiChatRole.ASSISTANT, "可以先观察白天小睡。"),
            ),
        )

        assertEquals("宝宝最近 夜醒很多，该怎么调整？", title)
    }

    @Test
    fun `历史搜索同时匹配标题和回答预览`() {
        val conversations = listOf(
            summary(1, "睡眠变化", "最近夜醒次数增加"),
            summary(2, "辅食建议", "注意观察鸡蛋过敏反应"),
        )

        assertEquals(
            listOf(1L),
            filterAiConversations(conversations, "睡眠").map { it.id },
        )
        assertEquals(
            listOf(2L),
            filterAiConversations(conversations, "过敏").map { it.id },
        )
    }

    @Test
    fun `空搜索保留原有更新时间顺序`() {
        val conversations = listOf(
            summary(2, "较新对话", "回答"),
            summary(1, "较早对话", "回答"),
        )

        assertEquals(conversations, filterAiConversations(conversations, "  "))
    }

    @Test
    fun `重新生成只移除最后一个完整回答`() {
        val messages = listOf(
            entry(1, AiChatRole.USER, "第一个问题"),
            entry(2, AiChatRole.ASSISTANT, "第一个回答"),
            entry(3, AiChatRole.USER, "第二个问题"),
            entry(4, AiChatRole.ASSISTANT, "第二个回答"),
        )

        val retained = conversationAfterRemovingLastAnswer(messages)

        assertEquals(listOf(1L, 2L, 3L), retained?.map { it.id })
    }

    @Test
    fun `缺少完整问答轮次时不能重新生成`() {
        val messages = listOf(entry(1, AiChatRole.USER, "尚未回答的问题"))

        assertEquals(null, conversationAfterRemovingLastAnswer(messages))
    }

    @Test
    fun `编辑最后问题会移除该轮问题和回答`() {
        val messages = listOf(
            entry(1, AiChatRole.USER, "保留的问题"),
            entry(2, AiChatRole.ASSISTANT, "保留的回答"),
            entry(3, AiChatRole.USER, "需要修改的问题"),
            entry(4, AiChatRole.ASSISTANT, "需要替换的回答"),
        )

        val edit = conversationForEditingLastQuestion(messages)

        assertEquals("需要修改的问题", edit?.question)
        assertEquals(listOf(1L, 2L), edit?.retainedMessages?.map { it.id })
    }

    @Test
    fun `生成中禁止编辑或重新生成最后回答`() {
        val messages = listOf(
            entry(1, AiChatRole.USER, "问题"),
            entry(2, AiChatRole.ASSISTANT, "流式回答"),
        )

        assertTrue(!AiChatUiState(messages = messages, isSending = true).canReviseLastAnswer)
        assertTrue(AiChatUiState(messages = messages, isSending = false).canReviseLastAnswer)
    }

    private fun entry(id: Long, role: AiChatRole, content: String) =
        AiChatEntry(id = id, role = role, content = content)

    private fun summary(id: Long, title: String, preview: String) =
        AiConversationSummary(
            id = id,
            familyId = "family-a",
            babyId = 1,
            title = title,
            preview = preview,
            createdAt = id,
            updatedAt = id,
        )
}
