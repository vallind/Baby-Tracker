package com.babytracker.designsystem.components.input

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AppInput suggestions 过滤纯函数测试。
 * 约定：空白查询不弹候选；包含匹配忽略大小写；最多 5 条。
 */
class FilterSuggestionsTest {

    private val pool = listOf("喂养", "喂奶", "睡眠记录", "换尿布", "洗澡", "量体温", "拍嗝")

    @Test
    fun `blank query returns empty`() {
        assertEquals(emptyList<String>(), filterSuggestions(pool, ""))
        assertEquals(emptyList<String>(), filterSuggestions(pool, "   "))
    }

    @Test
    fun `contains match is case insensitive`() {
        assertEquals(listOf("Sleep Record"), filterSuggestions(listOf("Sleep Record"), "sleep"))
    }

    @Test
    fun `chinese contains match works without case`() {
        assertEquals(listOf("喂养", "喂奶"), filterSuggestions(pool, "喂"))
    }

    @Test
    fun `query is trimmed`() {
        assertEquals(listOf("喂养", "喂奶"), filterSuggestions(pool, " 喂 "))
    }

    @Test
    fun `results capped at five`() {
        val big = (1..10).map { "选项$it" }
        assertEquals(5, filterSuggestions(big, "选项").size)
    }

    @Test
    fun `no match returns empty`() {
        assertEquals(emptyList<String>(), filterSuggestions(pool, "疫苗"))
    }
}
