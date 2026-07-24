package com.babytracker.designsystem.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 按钮逻辑状态机 — 纯 Kotlin，可 JVM 单测
 *
 * 管理 isPressed/isLoading/防抖，避免 UI 层耦合业务状态。
 *
 * 用法：
 *   val logic = rememberButtonLogic(scope = viewModelScope)
 *   Button(onClick = { logic.onClick { repo.save() } }) {
 *       if (logic.isLoading.collectAsState().value) Text("加载中...")
 *       else Text("保存")
 *   }
 */
class ButtonLogic(
    private val scope: CoroutineScope,
    private val debounceMs: Long = 0L,
) {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPressed = MutableStateFlow(false)

    /**
     * 触发点击动作。若 isLoading 为 true 则忽略。
     * 防抖在 onSurface 之前而非之后执行。
     */
    fun onClick(action: suspend () -> Unit) {
        if (_isLoading.value) return

        scope.launch {
            _isPressed.value = true
            _isLoading.value = true

            if (debounceMs > 0) {
                delay(debounceMs)
            }

            try {
                action()
            } finally {
                _isPressed.value = false
                _isLoading.value = false
            }
        }
    }

    fun dispose() {
        _isPressed.value = false
        _isLoading.value = false
    }
}
