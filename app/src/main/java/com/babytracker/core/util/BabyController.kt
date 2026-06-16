package com.babytracker.core.util

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class BabyController(private val sp: SharedPreferences) {
    var currentBabyId by mutableIntStateOf(sp.getInt(KEY_BABY_ID, 0))
        private set

    fun selectBaby(id: Int) {
        currentBabyId = id
        sp.edit().putInt(KEY_BABY_ID, id).apply()
    }

    companion object {
        private const val KEY_BABY_ID = "current_baby_id"
    }
}