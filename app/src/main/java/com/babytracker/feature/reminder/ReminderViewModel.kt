package com.babytracker.feature.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.ReminderRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Reminder
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class ReminderUiState(
    val pending: List<Reminder> = emptyList(),
    val history: List<Reminder> = emptyList(),
    val loading: Boolean = true,
)

/**
 * 提醒中心 ViewModel。
 *
 * - 用 `_trigger: MutableStateFlow<Int?>` + `flatMapLatest` 监听当前 babyId 的
 *   pending + history 两个 Flow，combine 后写入 state（与 StatsViewModel/HomeViewModel 同模式）。
 * - init 内监听当前宝宝变化自动加载（原 Screen 内 LaunchedEffect 迁入）。
 * - `markDone(id)` / `setEnabled(id, enabled)` / `delete(reminder)` / `restore(reminder)`
 *   均为 fire-and-forget 协程，DB 变化会通过 Flow 自动回流到 state。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModel(
    private val reminderRepo: ReminderRepository,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(ReminderUiState())
    val state: StateFlow<ReminderUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    /**
     * 当前宝宝（无宝宝时为 null）。
     * babyCtrl.currentBabyId 是 Compose 状态（VM 侧不可观察），因此以 watchAll 列表派生：
     * 列表变化会重新求值，与原本 Screen 内 `find ?: firstOrNull` 语义一致。
     */
    val baby: StateFlow<Baby?> = babyRepo.watchAll()
        .map { babies -> babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // 当前宝宝变化时自动加载（原 Screen 内 LaunchedEffect 迁入；无宝宝时不加载）
        viewModelScope.launch {
            baby.filterNotNull().collectLatest { b ->
                if (babyCtrl.currentBabyId != b.id) babyCtrl.selectBaby(b.id)
                _trigger.value = b.id
            }
        }

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

    /** 删除提醒（软删，DB 变化自动回流）。 */
    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.delete(reminder)
        }
    }

    /** 恢复一条被删除的提醒 — Snackbar 撤销路径（原 Screen 内 repo.update 迁入）。 */
    fun restore(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.update(reminder)
        }
    }
}