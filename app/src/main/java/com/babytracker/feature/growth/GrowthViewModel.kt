package com.babytracker.feature.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 生长记录页状态。
 * - [growths]：当前宝宝全部生长记录（类型切换 / 按日分组等展示派生由 Screen 计算，纯 UI 逻辑）
 * - [babyId]：当前宝宝 id（0 = 无宝宝，Screen 不渲染）
 * - [loading]：首屏加载中
 *
 * 日期筛选属于页面状态（原则 #9：筛选默认留 Screen 本地态，仅需跨页面/跨生命周期保留才进 VM），
 * 故 selectedDate 不在此处。
 */
data class GrowthUiState(
    val growths: List<Growth> = emptyList(),
    val babyId: Int = 0,
    val loading: Boolean = true,
)

/**
 * 生长记录 ViewModel（Batch 3 自 Screen 收编）。
 *
 * - 用 `_trigger: MutableStateFlow<Int?>` + `flatMapLatest` 监听当前 babyId 的
 *   `growthRepo.watchByBaby` Flow，写入 state（与 StatsViewModel/ReminderViewModel 同模式）；
 * - 当前宝宝通过 `babyRepo.watchAll()` 列表派生（babyCtrl.currentBabyId 是 Compose 状态，
 *   VM 侧不可观察，同 HomeViewModel/ReminderViewModel）；
 * - 删除在内部暂存实体副本，撤销时 `repo.update` 回写（软删恢复，参照 TimelineViewModel）；
 * - 表单保存按实体 id 区分新增（insert）/ 编辑（update），方法均不带 babyId。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GrowthViewModel(
    private val growthRepo: GrowthRepository,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(GrowthUiState())
    val state: StateFlow<GrowthUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    /**
     * 当前宝宝（无宝宝时为 null，Screen 据此计算参考区间月龄）。
     * 以 watchAll 列表派生：列表变化会重新求值，与原本 Screen 内 `find ?: firstOrNull` 语义一致。
     */
    val baby: StateFlow<Baby?> = babyRepo.watchAll()
        .map { babies -> babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 最近一次删除的实体副本，供 Snackbar 撤销（内部暂存，参照 TimelineViewModel） */
    private var lastDeletedGrowth: Growth? = null

    init {
        // 当前宝宝变化自动加载（原 Screen 内 LaunchedEffect 迁入；无宝宝时不加载）
        viewModelScope.launch {
            baby.filterNotNull().collectLatest { b ->
                if (babyCtrl.currentBabyId != b.id) babyCtrl.selectBaby(b.id)
                _trigger.value = b.id
            }
        }

        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                growthRepo.watchByBaby(babyId).map { growths -> growths to babyId }
            }
            .onEach { (growths, babyId) ->
                _state.value = _state.value.copy(
                    growths = growths,
                    babyId = babyId,
                    loading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    /** 删除生长记录，并在内部暂存实体副本用于可能的撤销操作（原 Screen 内 repo.delete 迁入） */
    fun delete(growth: Growth) {
        viewModelScope.launch {
            lastDeletedGrowth = growth
            growthRepo.delete(growth)
        }
    }

    /** 撤销最近一次删除（原 Screen 内 Snackbar 撤销回调里的 repo.update 迁入） */
    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedGrowth?.let { growthRepo.update(it) }
            lastDeletedGrowth = null
        }
    }

    /** 表单保存：id > 0 为编辑走 update，否则新增走 insert（原 Screen 内 repo 调用迁入） */
    fun save(growth: Growth) {
        viewModelScope.launch {
            if (growth.id > 0) growthRepo.update(growth) else growthRepo.insert(growth)
        }
    }
}