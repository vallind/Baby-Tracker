package com.babytracker.core.sync

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class SyncState { Idle, Syncing, Success, Error }

class SyncStateHolder(private val syncEngine: SyncEngine) {
    val state: StateFlow<SyncState> = syncEngine.syncState
        .map { engineState -> when (engineState) {
            EngineSyncState.IDLE -> SyncState.Idle
            EngineSyncState.SYNCING, EngineSyncState.PUSHING, EngineSyncState.PULLING -> SyncState.Syncing
        }}
        .stateIn(GlobalScope, SharingStarted.WhileSubscribed(5_000), SyncState.Idle)
}
