package com.babytracker.designsystem.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════
//  作用域桥接（TT-038）— 所有 rememberXxx 接受 CoroutineScope?
//  为空时使用 rememberCoroutineScope + 编译警告
// ═══════════════════════════════════════════════════════════

/**
 * 桥接：创建 ButtonLogic，绑定到可注入的 CoroutineScope。
 *
 * @param scope 外部作用域（如 viewModelScope），为空则使用 rememberCoroutineScope
 */
@Composable
fun rememberButtonLogic(
    scope: CoroutineScope? = null,
    debounceMs: Long = 0L,
): ButtonLogic {
    val s = scope ?: run {
        consoleWarn("rememberButtonLogic: scope is null, using rememberCoroutineScope. " +
            "Pass viewModelScope to survive rotation.")
        rememberCoroutineScope()
    }
    return remember(s) { ButtonLogic(s, debounceMs) }
}

/**
 * 桥接：创建 FormLogic，绑定到可注入的 CoroutineScope。
 */
@Composable
fun <F : Any> rememberFormLogic(
    scope: CoroutineScope? = null,
    initial: F,
    validator: (suspend (F) -> List<String>)? = null,
): FormLogic<F> {
    val s = scope ?: run {
        consoleWarn("rememberFormLogic: scope is null, using rememberCoroutineScope.")
        rememberCoroutineScope()
    }
    return remember(s, initial) { FormLogic(s, initial, validator) }
}

/**
 * 桥接：创建 TableLogic，绑定到可注入的 CoroutineScope。
 */
@Composable
fun <T : Any> rememberTableLogic(
    scope: CoroutineScope? = null,
    initialData: List<T> = emptyList(),
): TableLogic<T> {
    val s = scope ?: run {
        consoleWarn("rememberTableLogic: scope is null, using rememberCoroutineScope.")
        rememberCoroutineScope()
    }
    return remember(s, initialData) { TableLogic(s, initialData) }
}

/**
 * 防抖值 — 对标 Palette useDebounce。
 * 优先使用 ButtonLogic（带 scope 桥接）替代。
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
 * 受控状态 — 对标 Palette useState。
 * 简单值场景使用，复杂表单请使用 FormLogic。
 */
@Composable
fun <T> useState(initial: T): Pair<T, (T) -> Unit> {
    val state = remember { mutableStateOf(initial) }
    return state.value to { state.value = it }
}

/**
 * 获取最新值稳定引用 — 对标 Palette useLatestState。
 * 用于 lambda 闭包中捕获最新值，避免重组重建。
 */
@Composable
fun <T> useLatestState(value: T): T {
    val ref = remember { mutableStateOf(value) }
    ref.value = value
    return ref.value
}

// ═══════════════════════════════════════════════════════════
//  编译期警告（TT-036：UI 层不含业务状态逻辑）
// ═══════════════════════════════════════════════════════════

@PublishedApi
internal fun consoleWarn(message: String) {
    println("WARNING: $message")
}
