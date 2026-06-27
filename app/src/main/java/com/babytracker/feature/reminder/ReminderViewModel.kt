package com.babytracker.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.ReminderRepository
import com.babytracker.core.domain.model.Reminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** 提醒中心 Tab —— 待办 / 历史 */
enum class ReminderTab { PENDING, HISTORY }

data class ReminderUiState(
    val tab: ReminderTab = ReminderTab.PENDING,
    val pending: List<Reminder> = emptyList(),
    val history: List<Reminder> = emptyList(),
    val loading: Boolean = true,
)

/**
 * 提醒中心 ViewModel。
 *
 * - 用 `_trigger: MutableStateFlow<Int?>` + `flatMapLatest` 监听当前 babyId 的
 *   pending + history 两个 Flow，combine 后写入 state（与 StatsViewModel/HomeViewModel 同模式）。
 * - `load(babyId)` 由 Screen 在 LaunchedEffect 中调用。
 * - `switchTab(tab)` 仅切本地 tab 状态，不重新查库（pending/history 已同时在 state 中）。
 * - `markDone(id)` / `setEnabled(id, enabled)` / `delete(reminder)` 均为 fire-and-forget 协程，
 *   DB 变化会通过 Flow 自动回流到 state。
 */
class ReminderViewModel(
    private val reminderRepo: ReminderRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ReminderUiState())
    val state: StateFlow<ReminderUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                combine(
                    reminderRepo.watchPending(babyId),
                    reminderRepo.watchHistory(babyId),
                ) { pending, history -> pending to history }
            }
            .onEach { (pending, history) ->
                _state.value = _state.value.copy(
                    pending = pending,
                    history = history,
                    loading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    fun load(babyId: Int) {
        _trigger.value = babyId
    }

    fun switchTab(tab: ReminderTab) {
        _state.value = _state.value.copy(tab = tab)
    }

    fun markDone(id: Int) {
        viewModelScope.launch {
            reminderRepo.markDone(id, LocalDateTime.now())
        }
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            reminderRepo.setEnabled(id, enabled)
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.delete(reminder)
        }
    }
}
