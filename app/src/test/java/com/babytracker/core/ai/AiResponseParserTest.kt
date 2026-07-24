package com.babytracker.core.ai

import com.babytracker.core.ai.provider.OpenAiCompatibleChatAdapter
import com.babytracker.core.ai.provider.OpenAiResponsesAdapter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test

class AiResponseParserTest {
    private val json = Json

    @Test
    fun `compatible chat extracts assistant content`() {
        val response = json.parseToJsonElement(
            """{"choices":[{"message":{"role":"assistant","content":"回答内容"}}]}""",
        ).jsonObject

        assertEquals(
            "回答内容",
            OpenAiCompatibleChatAdapter(OkHttpClient()).parseText(response),
        )
    }

    @Test
    fun `responses extracts nested output text`() {
        val response = json.parseToJsonElement(
            """{"output":[{"type":"message","content":[{"type":"output_text","text":"第一段"},{"type":"output_text","text":"第二段"}]}]}""",
        ).jsonObject

        assertEquals(
            "第一段第二段",
            OpenAiResponsesAdapter(OkHttpClient()).parseText(response),
        )
    }

    @Test
    fun `compatible chat extracts stream content delta`() {
        val adapter = OpenAiCompatibleChatAdapter(OkHttpClient())

        assertEquals(
            "你好",
            adapter.parseStreamDelta(
                """{"choices":[{"delta":{"content":"你好"},"finish_reason":null}]}""",
            )?.text,
        )
    }

    @Test
    fun `compatible chat extracts reasoning from response and stream`() {
        val adapter = OpenAiCompatibleChatAdapter(OkHttpClient())
        val response = json.parseToJsonElement(
            """{"choices":[{"message":{"role":"assistant","reasoning_content":"先分析","content":"最终回答"}}]}""",
        ).jsonObject

        assertEquals("先分析", adapter.parseResponse(response)?.reasoningContent)
        assertEquals(
            "继续分析",
            adapter.parseStreamDelta(
                """{"choices":[{"delta":{"reasoning_content":"继续分析"},"finish_reason":null}]}""",
            )?.reasoningContent,
        )
    }

    @Test
    fun `responses extracts output text stream delta`() {
        val adapter = OpenAiResponsesAdapter(OkHttpClient())

        assertEquals(
            "建议",
            adapter.parseStreamDelta(
                """{"type":"response.output_text.delta","delta":"建议"}""",
            )?.text,
        )
    }

    @Test
    fun `responses extracts reasoning summary`() {
        val adapter = OpenAiResponsesAdapter(OkHttpClient())
        val response = json.parseToJsonElement(
            """{"output":[{"type":"reasoning","summary":[{"type":"summary_text","text":"分析摘要"}]},{"type":"message","content":[{"type":"output_text","text":"回答"}]}]}""",
        ).jsonObject

        assertEquals("分析摘要", adapter.parseResponse(response)?.reasoningContent)
        assertEquals(
            "流式摘要",
            adapter.parseStreamDelta(
                """{"type":"response.reasoning_summary_text.delta","delta":"流式摘要"}""",
            )?.reasoningContent,
        )
    }
}
