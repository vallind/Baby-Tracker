package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.VaccinationRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.VaccineSchedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 宝宝管理页状态：在册/已软删宝宝 + 当前宝宝 id（展示派生全部由 Screen 计算） */
data class BabyManagementUiState(
    val activeBabies: List<Baby> = emptyList(),
    val deletedBabies: List<Baby> = emptyList(),
    val currentBabyId: Int = 0,
)

/**
 * 宝宝管理 ViewModel（Batch 4 自 Screen 收编）。
 * 增删改/切换/疫苗计划生成进 VM；删除确认弹层、表单显隐等 UI 态留 Screen。
 */
class BabyManagementViewModel(
    private val babyRepo: BabyRepository,
    private val babyCtrl: BabyController,
    private val vacRepo: VaccinationRepository,
) : ViewModel() {

    val state: StateFlow<BabyManagementUiState> = babyRepo.watchAll()
        .map { babies ->
            BabyManagementUiState(
                activeBabies = babies.filter { it.deletedAt == null },
                deletedBabies = babies.filter { it.deletedAt != null },
                currentBabyId = babyCtrl.currentBabyId,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BabyManagementUiState())

    /** 切换当前宝宝（BabyController 持久化到 SharedPreferences） */
    fun selectBaby(id: Int) {
        babyCtrl.selectBaby(id)
    }

    /** 新增或编辑：编辑走 update；新增成功后按出生日期生成疫苗计划（原 Screen 内逻辑迁入） */
    fun addOrUpdate(baby: Baby, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) {
                babyRepo.update(baby)
            } else {
                val babyId = babyRepo.insert(baby).toInt()
                VaccineSchedule.createForBaby(babyId, baby.birthDate).forEach { vacRepo.insert(it) }
            }
        }
    }

    fun delete(baby: Baby) {
        viewModelScope.launch { babyRepo.delete(baby) }
    }

    fun restore(baby: Baby) {
        viewModelScope.launch { babyRepo.restore(baby) }
    }
}