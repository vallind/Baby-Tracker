package com.babytracker.designsystem.components.markdown

import com.babytracker.core.ui.components.markdown.MarkdownBlock
import com.babytracker.core.ui.components.markdown.parseMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {
    @Test
    fun `解析常见 AI Markdown 块`() {
        val blocks = parseMarkdown(
            """
            ## 简明结论
            宝宝需要 **规律作息**。

            1. 固定睡前流程
            2. 保持安静

            > 若精神变差请及时就医
            """.trimIndent(),
        )

        assertTrue(blocks.first() is MarkdownBlock.HeadingBlock)
        assertEquals(2, blocks.count { it is MarkdownBlock.ListBlock })
        assertTrue(blocks.last() is MarkdownBlock.QuoteBlock)
    }

    @Test
    fun `图片不保留远程地址`() {
        val blocks = parseMarkdown("![宝宝](https://tracker.example/pixel.png)")
        val text = (blocks.single() as MarkdownBlock.ParagraphBlock).text.text

        assertEquals("宝宝", text)
        assertFalse(text.contains("https://"))
    }
}
