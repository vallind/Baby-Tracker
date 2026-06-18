package com.babytracker.core.util

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.babytracker.data.repository.BabyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class BabyController(
    private val sp: SharedPreferences,
    private val babyRepo: BabyRepository,
) {
    var currentBabyId by mutableIntStateOf(sp.getInt(KEY_BABY_ID, 0))
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        babyRepo.watchAll()
            .catch { /* ignore */ }
            .onEach { babies ->
                val stored = currentBabyId
                if (stored != 0 && babies.none { it.id == stored }) {
                    val fallback = babies.firstOrNull()?.id ?: 0
                    currentBabyId = fallback
                    sp.edit().putInt(KEY_BABY_ID, fallback).apply()
                }
            }
            .launchIn(scope)
    }

    fun selectBaby(id: Int) {
        currentBabyId = id
        sp.edit().putInt(KEY_BABY_ID, id).apply()
    }

    companion object {
        private const val KEY_BABY_ID = "current_baby_id"
    }
}