package com.babytracker.feature.sleep

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.SleepRepository
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 睡眠页状态：当日记录列表（日期筛选/夜醒拆分是 Screen 本地 UI 态） */
data class SleepUiState(
    val sleeps: List<Sleep> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SleepViewModel(
    private val repo: SleepRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {

    /** 当前宝宝（表单对话框传 babyId 用；0 = 无宝宝，Screen 不渲染） */
    private val _babyId = MutableStateFlow(0)
    val babyId: StateFlow<Int> = _babyId.asStateFlow()

    /** 记录列表：当前宝宝变化自动加载（原 Screen 内 watchByBaby 订阅迁入） */
    val state: StateFlow<SleepUiState> = _babyId
        .filter { it != 0 }
        .flatMapLatest { id -> repo.watchByBaby(id) }
        .map { SleepUiState(sleeps = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SleepUiState())

    /** 撤销删除：暂存最近删除的实体（VM 不持有 UI lambda，Snackbar 展示留 Screen） */
    private var lastDeleted: Sleep? = null

    init {
        // 当前宝宝变化 → 更新 babyId（触发 state 重订阅）；babyCtrl.currentBabyId 是 Compose 状态，用 snapshotFlow 桥接
        viewModelScope.launch {
            snapshotFlow { babyCtrl.currentBabyId }
                .distinctUntilChanged()
                .filter { it != 0 }
                .collectLatest { _babyId.value = it }
        }
    }

    fun add(sleep: Sleep) {
        viewModelScope.launch { repo.insert(sleep) }
    }

    fun update(sleep: Sleep) {
        viewModelScope.launch { repo.update(sleep) }
    }

    fun delete(sleep: Sleep) {
        viewModelScope.launch {
            repo.delete(sleep)
            lastDeleted = sleep
        }
    }

    fun undoDelete() {
        val sleep = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { repo.update(sleep) }
    }
}