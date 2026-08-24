package com.babytracker.feature.health

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.HealthRepository
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.core.domain.model.HealthRecord
import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HealthUiState(
    val records: List<HealthRecord> = emptyList(),
    val vaccinations: List<Vaccination> = emptyList(),
    /** 当前展示宝宝的 id（Screen 端共享表单 HealthFormDialog 构建实体需要；方法签名不带 babyId） */
    val babyId: Int = 0,
    val loading: Boolean = true,
)

/**
 * 健康档案 ViewModel（Batch 3 自 Screen 迁入，与 StatsViewModel 同 snapshotFlow 模式）。
 *
 * - babyCtrl.currentBabyId 是 Compose 状态（VM 侧不可观察），用 snapshotFlow 观察，
 *   无宝宝（id == 0）时不加载；
 * - health + vaccination 两个 watch 订阅 combine 后写入 state（疫苗完成数用于右上卡片）；
 * - 删除/撤销/保存均为 fire-and-forget 协程，DB 变化通过 Flow 自动回流；
 * - 卡片展开/表单显隐/详情弹层是 UI 展示态，留在 Screen；
 * - 本 VM 不持有 UI lambda，不接触 NavController。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModel(
    private val healthRepo: HealthRepository,
    private val vacRepo: VaccinationRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(HealthUiState())
    val state: StateFlow<HealthUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    init {
        // 当前宝宝变化自动加载（原 Screen 内 LaunchedEffect(babyId) 迁入；无宝宝时不加载）
        viewModelScope.launch {
            snapshotFlow { babyCtrl.currentBabyId }
                .distinctUntilChanged()
                .filter { it != 0 }
                .collectLatest { babyId -> _trigger.value = babyId }
        }

        _trigger
            .filterNotNull()
            .flatMapLatest { babyId ->
                combine(
                    healthRepo.watchByBaby(babyId),
                    vacRepo.watchByBaby(babyId),
                ) { records, vaccinations -> Triple(babyId, records, vaccinations) }
            }
            .onEach { (babyId, records, vaccinations) ->
                _state.value = _state.value.copy(
                    records = records,
                    vaccinations = vaccinations,
                    babyId = babyId,
                    loading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    /** 删除健康记录（软删，DB 变化自动回流）。 */
    fun delete(record: HealthRecord) {
        viewModelScope.launch {
            healthRepo.delete(record)
        }
    }

    /** 恢复一条被删除的健康记录 — Snackbar 撤销路径（原 Screen 内 repo.update 迁入）。 */
    fun restore(record: HealthRecord) {
        viewModelScope.launch {
            healthRepo.update(record)
        }
    }

    /** 保存健康记录（新增/编辑由调用方按是否处于编辑态传入）。 */
    fun save(record: HealthRecord, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) healthRepo.update(record) else healthRepo.insert(record)
        }
    }
}