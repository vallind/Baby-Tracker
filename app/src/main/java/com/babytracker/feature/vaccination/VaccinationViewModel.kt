package com.babytracker.feature.vaccination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Vaccination
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VaccinationUiState(
    val vaccinations: List<Vaccination> = emptyList(),
    /** 当前展示宝宝的 id（Screen 端表单构建实体需要；方法签名不带 babyId） */
    val babyId: Int = 0,
    val loading: Boolean = true,
)

/**
 * 疫苗接种记录 ViewModel（Batch 3 自 Screen 迁入）。
 *
 * - 当前宝宝由 babyRepo.watchAll() 派生（babyCtrl.currentBabyId 是 Compose 状态，
 *   VM 侧不可观察，与 HomeViewModel/ReminderViewModel 同模式）；
 * - init 内监听当前宝宝变化自动加载（原 Screen 内 LaunchedEffect 迁入，无宝宝时不加载）；
 * - 删除/撤销/保存/生成计划均为 fire-and-forget 协程，DB 变化通过 Flow 自动回流；
 * - Tab 筛选（全部/待接种/已接种/已过期）与表单显隐是 UI 展示态，留在 Screen；
 * - 本 VM 不持有 UI lambda，不接触 NavController。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VaccinationViewModel(
    private val vacRepo: VaccinationRepository,
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {
    private val _state = MutableStateFlow(VaccinationUiState())
    val state: StateFlow<VaccinationUiState> = _state.asStateFlow()

    private val _trigger = MutableStateFlow<Int?>(null)

    /** 当前宝宝（无宝宝时为 null）；Screen 用它展示建议月龄（出生日期）。 */
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
                vacRepo.watchByBaby(babyId).map { list -> babyId to list }
            }
            .onEach { (babyId, list) ->
                _state.value = _state.value.copy(
                    vaccinations = list,
                    babyId = babyId,
                    loading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    /** 删除疫苗记录（软删，DB 变化自动回流）。 */
    fun delete(vaccination: Vaccination) {
        viewModelScope.launch {
            vacRepo.delete(vaccination)
        }
    }

    /** 恢复一条被删除的疫苗记录 — Snackbar 撤销路径（原 Screen 内 repo.update 迁入）。 */
    fun restore(vaccination: Vaccination) {
        viewModelScope.launch {
            vacRepo.update(vaccination)
        }
    }

    /** 保存疫苗记录（新增/编辑由调用方按是否处于编辑态传入）。 */
    fun save(vaccination: Vaccination, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) vacRepo.update(vaccination) else vacRepo.insert(vaccination)
        }
    }

    /** 按当前宝宝出生日期生成默认接种计划（原 Screen 内 babyRepo.getById + VaccineSchedule 迁入）。 */
    fun generateSchedule() {
        viewModelScope.launch {
            val b = baby.value ?: return@launch
            VaccineSchedule.createForBaby(b.id, b.birthDate).forEach { vacRepo.insert(it) }
        }
    }
}