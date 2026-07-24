package com.babytracker.designsystem.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SortConfig(
    val column: String = "",
    val ascending: Boolean = true,
)

data class PageConfig(
    val page: Int = 0,
    val pageSize: Int = 20,
)

/**
 * 表格逻辑 — 纯 Kotlin，可 JVM 单测
 *
 * 管理 sorting/selection/pagination，无 Compose 依赖。
 */
class TableLogic<T : Any>(
    private val scope: CoroutineScope,
    initialData: List<T> = emptyList(),
    private val idExtractor: ((T) -> String)? = null,
) {
    private val _data = MutableStateFlow(initialData)
    val data: StateFlow<List<T>> = _data.asStateFlow()

    private val _sort = MutableStateFlow(SortConfig())
    val sort: StateFlow<SortConfig> = _sort.asStateFlow()

    private val _page = MutableStateFlow(PageConfig())
    val page: StateFlow<PageConfig> = _page.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val sortedData: List<T>
        get() {
            val s = _sort.value
            if (s.column.isEmpty()) return _data.value
            val comparator = Comparator<T> { a, b ->
                val prop = a::class.members.firstOrNull { it.name == s.column }
                val va = prop?.call(a)
                val vb = prop?.call(b)
                val cmp = when {
                    va is Number && vb is Number -> (va.toDouble() - vb.toDouble()).toInt()
                    va is Comparable<*> && vb is Comparable<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        (va as Comparable<Any>).compareTo(vb as Any)
                    }
                    else -> (va?.toString() ?: "").compareTo(vb?.toString() ?: "", ignoreCase = true)
                }
                if (s.ascending) cmp else -cmp
            }
            return _data.value.sortedWith(comparator)
        }

    val totalPages: Int
        get() = maxOf(1, (_data.value.size + _page.value.pageSize - 1) / _page.value.pageSize)

    fun setData(data: List<T>) {
        _data.value = data
    }

    fun sortBy(column: String) {
        _sort.update { current ->
            if (current.column == column) {
                current.copy(ascending = !current.ascending)
            } else {
                SortConfig(column = column, ascending = true)
            }
        }
    }

    fun select(id: String) {
        _selectedIds.update { it + id }
    }

    fun deselect(id: String) {
        _selectedIds.update { it - id }
    }

    fun toggleSelect(id: String) {
        _selectedIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun selectAll() {
        val extractor = idExtractor
        if (extractor != null) {
            _selectedIds.value = _data.value.map(extractor).toSet()
        } else {
            _selectedIds.value = _data.value.mapIndexed { index, _ -> index.toString() }.toSet()
        }
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun goToPage(page: Int) {
        _page.update { it.copy(page = page.coerceIn(0, maxOf(0, totalPages - 1))) }
    }

    fun nextPage() = goToPage(_page.value.page + 1)
    fun prevPage() = goToPage(_page.value.page - 1)

    val currentPageData: List<T>
        get() {
            val p = _page.value
            val start = p.page * p.pageSize
            val end = minOf(start + p.pageSize, _data.value.size)
            if (start >= _data.value.size) return emptyList()
            return sortedData.subList(start, end)
        }
}
