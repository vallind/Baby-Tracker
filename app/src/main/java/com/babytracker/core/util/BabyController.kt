package com.babytracker.core.util

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.babytracker.core.data.repository.BabyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 当前宝宝上下文管理。
 *
 * 自动监听 BabyRepository.watchAll()：
 * - 若当前 currentBabyId 对应的宝宝被删除，自动 fallback 到第一个宝宝；
 * - 若当前 id == 0 且列表非空，自动选中第一个宝宝。
 *
 * 调用方仍可主动通过 selectBaby(id) 切换。
 */
class BabyController(
    private val sp: SharedPreferences,
    babyRepo: BabyRepository,
) {
    var currentBabyId by mutableIntStateOf(sp.getInt(KEY_BABY_ID, 0))
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // 监听宝宝列表变化，自动 fallback
        babyRepo.watchAll().onEach { babies ->
            if (babies.isEmpty()) {
                // 列表清空：重置 id 为 0
                if (currentBabyId != 0) {
                    currentBabyId = 0
                    sp.edit().putInt(KEY_BABY_ID, 0).apply()
                }
            } else {
                // 列表非空：检查当前 id 是否仍存在
                val exists = babies.any { it.id == currentBabyId }
                if (!exists) {
                    // fallback 到第一个
                    val firstId = babies.first().id
                    currentBabyId = firstId
                    sp.edit().putInt(KEY_BABY_ID, firstId).apply()
                } else if (currentBabyId == 0) {
                    // 之前是 0，现在有宝宝了，自动选中第一个
                    val firstId = babies.first().id
                    currentBabyId = firstId
                    sp.edit().putInt(KEY_BABY_ID, firstId).apply()
                }
            }
        }.launchIn(scope)
    }

    fun selectBaby(id: Int) {
        currentBabyId = id
        sp.edit().putInt(KEY_BABY_ID, id).apply()
    }

    companion object {
        private const val KEY_BABY_ID = "current_baby_id"
    }
}