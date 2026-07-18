package com.babytracker.feature.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.MessageType
import com.babytracker.core.data.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 消息中心 UI 状态。
 *
 * - [messages] 全量消息列表（按时间倒序），Screen 层自行做分类筛选
 * - [interactionUnread] / [systemUnread] / [serviceUnread] 各分类未读数（供概览卡角标）
 * - [totalUnread] 总未读数（"全部已读"按钮可用性判断）
 */
data class MessageUiState(
    val messages: List<AppMessage> = emptyList(),
    val interactionUnread: Int = 0,
    val systemUnread: Int = 0,
    val serviceUnread: Int = 0,
    val totalUnread: Int = 0,
)

class MessageViewModel(
    private val repo: MessageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MessageUiState())
    val state: StateFlow<MessageUiState> = _state.asStateFlow()

    init {
        // 订阅全量消息 → 计算每分类未读数 + 全量列表（按时间倒序）
        repo.watchAll()
            .onEach { all ->
                val sorted = all.sortedByDescending { it.createTime }
                _state.value = _state.value.copy(
                    messages = sorted,
                    interactionUnread = all.count { it.type == MessageType.INTERACTION && !it.isRead },
                    systemUnread = all.count { it.type == MessageType.SYSTEM && !it.isRead },
                    serviceUnread = all.count { it.type == MessageType.SERVICE && !it.isRead },
                    totalUnread = all.count { !it.isRead },
                )
            }
            .launchIn(viewModelScope)
    }

    fun markRead(id: Long) {
        viewModelScope.launch { repo.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repo.markAllRead() }
    }

    fun delete(message: AppMessage) {
        viewModelScope.launch { repo.delete(message) }
    }

}
