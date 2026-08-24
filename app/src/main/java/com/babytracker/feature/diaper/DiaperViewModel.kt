package com.babytracker.feature.diaper

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.DiaperRepository
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 尿布记录页状态。
 * - [diapers]：当前宝宝全部尿布记录（按日过滤 / 分类计数等展示派生由 Screen 计算，纯 UI 逻辑）
 * - [babyId]：当前宝宝 id（0 = 无宝宝，Screen 不渲染）
 * - [loading]：首屏加载中
 *
 * 日期筛选属于页面状态（原则 #9：筛选默认留 Screen 本地态，仅需跨页面/跨生命周期保留才进 VM），
 * 故 selectedDate 不在此处。
 */
data class DiaperUiState(
    val diapers: List<Diaper> = emptyList(),
    val babyId: Int = 0,
    val loading: Boolean = true,
)

/**
 * 尿布记录 ViewModel（Batch 3 自 Screen 收编）。
 *
 * - 当前宝宝用 `snapshotFlow { babyCtrl.currentBabyId }` 监听并自动加载
 *   （同 StatsViewModel / TimelineViewModel 模式，无宝宝时不加载）；
 * - `_trigger` + `flatMapLatest` 订阅 `diaperRepo.watchByBaby` Flow 写入 state；
 * - 删除在内部暂存实体副本，撤销时 `repo.update` 回写（软删恢复，参照 TimelineViewModel）；
 * - 表单保存按实体 id 区分新增（insert）/ 编辑（update），方法均不带 babyId。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiaperViewModel(
    private val diaperRepo: DiaperRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(DiaperUiState())
    val state: StateFlow<DiaperUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    /** 最近一次删除的实体副本，供 Snackbar 撤销（内部暂存，参照 TimelineViewModel） */
    private var lastDeletedDiaper: Diaper? = null

    init {
        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                diaperRepo.watchByBaby(babyId).map { diapers -> diapers to babyId }
            }
            .onEach { (diapers, babyId) ->
                _state.value = _state.value.copy(
                    diapers = diapers,
                    babyId = babyId,
                    loading = false,
                )
            }
            .launchIn(viewModelScope)

        // 当前宝宝变化自动加载（原 Screen 内 LaunchedEffect(babyId) 迁入；无宝宝时不加载）
        viewModelScope.launch {
            snapshotFlow { babyCtrl.currentBabyId }
                .distinctUntilChanged()
                .filter { it != 0 }
                .collectLatest { babyId -> _trigger.value = babyId }
        }
    }

    /** 删除尿布记录，并在内部暂存实体副本用于可能的撤销操作（原 Screen 内 repo.delete 迁入） */
    fun delete(diaper: Diaper) {
        viewModelScope.launch {
            lastDeletedDiaper = diaper
            diaperRepo.delete(diaper)
        }
    }

    /** 撤销最近一次删除（原 Screen 内 Snackbar 撤销回调里的 repo.update 迁入） */
    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedDiaper?.let { diaperRepo.update(it) }
            lastDeletedDiaper = null
        }
    }

    /** 表单保存：id > 0 为编辑走 update，否则新增走 insert（原 Screen 内 repo 调用迁入） */
    fun save(diaper: Diaper) {
        viewModelScope.launch {
            if (diaper.id > 0) diaperRepo.update(diaper) else diaperRepo.insert(diaper)
        }
    }
}