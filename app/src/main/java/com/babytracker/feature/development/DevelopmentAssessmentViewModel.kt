package com.babytracker.feature.development

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.DevelopmentAssessmentRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.DevelopmentAssessment
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 发育评估 UI 状态。
 * - [latest]：最近一次评估（用于主页展示）；若无则为 null。
 * - [history]：全部历史评估（按 assessDate 倒序）。
 */
data class DevelopmentAssessmentUiState(
    val latest: DevelopmentAssessment? = null,
    val history: List<DevelopmentAssessment> = emptyList(),
)

/**
 * 发育评估 ViewModel —— 遵循 [com.babytracker.feature.stats.StatsViewModel] 的 StateFlow + 触发器模式。
 *
 * init 内监听当前宝宝变化自动加载（原 Screen 内 LaunchedEffect 迁入）；
 * [insert] / [delete] 由 UI 触发后，Room Flow 会自动推送新状态，无需手动刷新。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DevelopmentAssessmentViewModel(
    private val repo: DevelopmentAssessmentRepository,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(DevelopmentAssessmentUiState())
    val state: StateFlow<DevelopmentAssessmentUiState> = _state.asStateFlow()

    private val _babyIdTrigger = MutableStateFlow<Int?>(null)

    /**
     * 当前宝宝（无宝宝时为 null）。
     * babyCtrl.currentBabyId 是 Compose 状态（VM 侧不可观察），因此以 watchAll 列表派生：
     * 列表变化会重新求值，与原本 Screen 内 `find ?: firstOrNull` 语义一致。
     */
    val baby: StateFlow<Baby?> = babyRepo.watchAll()
        .map { babies -> babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // 当前宝宝变化时自动订阅（原 Screen 内 LaunchedEffect 迁入；无宝宝时不加载）
        viewModelScope.launch {
            baby.filterNotNull().collectLatest { b ->
                if (babyCtrl.currentBabyId != b.id) babyCtrl.selectBaby(b.id)
                _babyIdTrigger.value = b.id
            }
        }

        _babyIdTrigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                repo.watchByBaby(babyId).map { list ->
                    DevelopmentAssessmentUiState(
                        latest = list.firstOrNull(),
                        history = list,
                    )
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    /** 新增一次评估。返回值由 Room 推送，无需手动回填。 */
    fun insert(assessment: DevelopmentAssessment) {
        viewModelScope.launch { repo.insert(assessment) }
    }

    /** 删除一次评估。 */
    fun delete(assessment: DevelopmentAssessment) {
        viewModelScope.launch { repo.delete(assessment) }
    }
}