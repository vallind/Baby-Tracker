package com.babytracker.designsystem.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════
//  轻量 Hooks — 对标 Palette core/hooks/
// ═══════════════════════════════════════════════════════════

/**
 * 防抖值 — 对标 Palette useDebounce
 *
 * 用法：
 *   val debouncedQuery by useDebounce(query, delayMs = 300)
 */
@Composable
fun useDebounce(value: String, delayMs: Int = 300): String {
    var debounced by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        delay(delayMs.toLong())
        debounced = value
    }
    return debounced
}

/**
 * 受控状态 — 对标 Palette useState
 *
 * 简化 mutableStateOf 样板，提供 getter/setter 对。
 *
 * 用法：
 *   val (count, setCount) = useState(0)
 *   Text("$count")
 *   Button(onClick = { setCount(count + 1) }) { Text("+1") }
 */
@Composable
fun <T> useState(initial: T): Pair<T, (T) -> Unit> {
    val state = remember { mutableStateOf(initial) }
    return state.value to { state.value = it }
}

/**
 * 获取最新值的稳定引用 — 对标 Palette useLatestState
 *
 * 用于 lambda 闭包中捕获最新的状态值，避免重组重建 lambda。
 *
 * 用法：
 *   val latest by rememberUpdatedState(currentValue)
 */
@Composable
fun <T> useLatestState(value: T): T {
    val ref = remember { mutableStateOf(value) }
    ref.value = value
    return ref.value
}
