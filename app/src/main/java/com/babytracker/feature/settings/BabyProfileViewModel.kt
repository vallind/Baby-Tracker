package com.babytracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.util.BabyController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 宝宝资料页状态：当前宝宝 + 三类最新生长数据（原 Screen 内 watchAll/watchByBaby 派生逻辑迁入） */
data class BabyProfileUiState(
    val baby: Baby? = null,
    val latestHeight: Growth? = null,
    val latestWeight: Growth? = null,
    val latestHead: Growth? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class BabyProfileViewModel(
    private val babyRepo: BabyRepository,
    private val growthRepo: GrowthRepository,
    private val babyCtrl: BabyController,
) : ViewModel() {

    /** 宝宝列表变化 → 派生当前宝宝（find ?: firstOrNull，与原 Screen 一致）→ 加载其生长记录 */
    val state: StateFlow<BabyProfileUiState> = babyRepo.watchAll()
        .flatMapLatest { babies ->
            val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()
            growthRepo.watchByBaby(baby?.id ?: 0).map { growths ->
                val activeGrowths = growths.filter { it.deletedAt == null }
                BabyProfileUiState(
                    baby = baby,
                    latestHeight = activeGrowths.filter { it.type == GrowthType.HEIGHT }.maxByOrNull { it.measuredAt },
                    latestWeight = activeGrowths.filter { it.type == GrowthType.WEIGHT }.maxByOrNull { it.measuredAt },
                    latestHead = activeGrowths.filter { it.type == GrowthType.HEAD }.maxByOrNull { it.measuredAt },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BabyProfileUiState())

    fun updateBaby(updated: Baby) {
        viewModelScope.launch { babyRepo.update(updated) }
    }
}