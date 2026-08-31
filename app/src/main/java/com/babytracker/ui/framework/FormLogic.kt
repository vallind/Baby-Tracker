package com.babytracker.ui.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.babytracker.designsystem.hooks.consoleWarn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 表单逻辑 — 纯 Kotlin，可 JVM 单测
 *
 * 管理 fields/errors/touched/submitting 状态，无 Compose 依赖。
 * 四层架构归属：`app/ui/framework`（app 层 UI 框架，非 Design System 职责）。
 *
 * 用法：
 *   data class LoginForm(val email: String = "", val password: String = "")
 *   val logic = rememberFormLogic(viewModelScope, LoginForm())
 *   logic.onFieldChange("email") { it.copy(email = "a@b.com") }
 *   logic.submit { form -> repo.login(form) }
 */
class FormLogic<F : Any>(
    private val scope: CoroutineScope,
    initial: F,
    private val validator: (suspend (F) -> List<String>)? = null,
) {
    private val _fields = MutableStateFlow(initial)
    val fields: StateFlow<F> = _fields.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    private val _touched = MutableStateFlow<Set<String>>(emptySet())
    val touched: StateFlow<Set<String>> = _touched.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isValid = MutableStateFlow(true)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    fun onFieldChange(field: String, updater: (F) -> F) {
        _fields.update { updater(it) }
        _touched.update { it + field }
        _errors.update { it - field }
    }

    suspend fun validate(): Boolean {
        if (validator == null) return true
        val errors = validator(_fields.value)
        val errorMap = if (errors.isEmpty()) emptyMap() else mapOf("form" to errors.joinToString("\n"))
        _errors.value = errorMap
        _isValid.value = errors.isEmpty()
        return errors.isEmpty()
    }

    fun submit(action: suspend (F) -> Unit) {
        if (_isSubmitting.value) return
        scope.launch {
            _isSubmitting.value = true
            try {
                if (validate()) {
                    action(_fields.value)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun reset(fields: F) {
        _fields.value = fields
        _errors.value = emptyMap()
        _touched.value = emptySet()
        _isSubmitting.value = false
        _isValid.value = true
    }
}

/**
 * 桥接：创建 FormLogic，绑定到可注入的 CoroutineScope。
 * （四层架构 Phase 5：从 designsystem/hooks 外移至 app/ui/framework）
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