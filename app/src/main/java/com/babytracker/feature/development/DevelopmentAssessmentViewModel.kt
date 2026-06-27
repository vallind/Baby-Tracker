package com.babytracker.feature.development

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.DevelopmentAssessmentRepository
import com.babytracker.core.domain.model.DevelopmentAssessment
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
 * 调用 [load] 订阅指定宝宝的评估列表；[insert] / [delete] 由 UI 触发后，
 * Room Flow 会自动推送新状态，无需手动刷新。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DevelopmentAssessmentViewModel(
    private val repo: DevelopmentAssessmentRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(DevelopmentAssessmentUiState())
    val state: StateFlow<DevelopmentAssessmentUiState> = _state.asStateFlow()

    private val _babyIdTrigger = MutableStateFlow<Int?>(null)

    init {
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

    /** 订阅指定宝宝的发育评估数据。 */
    fun load(babyId: Int) {
        _babyIdTrigger.value = babyId
    }

    /** 便捷方法：直接订阅当前选中宝宝（由 [BabyController] 维护）。 */
    fun loadCurrentBaby() {
        val id = babyCtrl.currentBabyId
        if (id != 0) load(id)
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
