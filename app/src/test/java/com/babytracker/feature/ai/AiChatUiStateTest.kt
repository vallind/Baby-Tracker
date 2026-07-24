package com.babytracker.feature.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatUiStateTest {
    @Test
    fun `尚未收到正文时显示等待状态`() {
        val state = AiChatUiState(
            messages = listOf(
                AiChatEntry(id = 1, role = AiChatRole.USER, content = "问题"),
            ),
            isSending = true,
        )

        assertFalse(state.hasStreamingAnswer)
    }

    @Test
    fun `流式正文只标记最后一条助手消息`() {
        val previousAnswer = AiChatEntry(id = 1, role = AiChatRole.ASSISTANT, content = "旧回答")
        val streamingAnswer = AiChatEntry(id = 3, role = AiChatRole.ASSISTANT, content = "正在生成")
        val state = AiChatUiState(
            messages = listOf(
                previousAnswer,
                AiChatEntry(id = 2, role = AiChatRole.USER, content = "问题"),
                streamingAnswer,
            ),
            isSending = true,
        )

        assertTrue(state.hasStreamingAnswer)
        assertFalse(state.isStreaming(previousAnswer))
        assertTrue(state.isStreaming(streamingAnswer))
    }

    @Test
    fun `回答完成后不再标记流式消息`() {
        val answer = AiChatEntry(id = 2, role = AiChatRole.ASSISTANT, content = "完整回答")
        val state = AiChatUiState(
            messages = listOf(answer),
            isSending = false,
        )

        assertFalse(state.hasStreamingAnswer)
        assertFalse(state.isStreaming(answer))
    }
}
