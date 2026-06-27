package com.babytracker.feature.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.domain.model.AppMessage
import com.babytracker.core.domain.model.MessageType
import com.babytracker.core.data.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * 消息中心 UI 状态。
 *
 * - [currentTab] 当前选中的 Tab（互动 / 系统 / 服务）
 * - [messages] 当前 Tab 下排序后的消息列表
 * - [interactionUnread] / [systemUnread] / [serviceUnread] 各 Tab 未读数（用于 Tab 红点）
 * - [totalUnread] 总未读数（页面标题"全部已读"按钮可禁用判断）
 */
data class MessageUiState(
    val currentTab: MessageType = MessageType.INTERACTION,
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
        // 订阅全量消息 → 计算每 Tab 未读数 + 当前 Tab 列表
        repo.watchAll()
            .onEach { all ->
                val cur = _state.value.currentTab
                _state.value = _state.value.copy(
                    messages = all.filter { it.type == cur },
                    interactionUnread = all.count { it.type == MessageType.INTERACTION && !it.isRead },
                    systemUnread = all.count { it.type == MessageType.SYSTEM && !it.isRead },
                    serviceUnread = all.count { it.type == MessageType.SERVICE && !it.isRead },
                    totalUnread = all.count { !it.isRead },
                )
            }
            .launchIn(viewModelScope)

        // 首次进入若库为空，播种若干演示消息便于审阅
        seedDemoIfEmpty()
    }

    fun switchTab(type: MessageType) {
        viewModelScope.launch {
            // 切 Tab 时立即基于最新数据计算当前 Tab 列表
            val all = repo.watchAll().first()
            _state.value = _state.value.copy(
                currentTab = type,
                messages = all.filter { it.type == type },
                interactionUnread = all.count { it.type == MessageType.INTERACTION && !it.isRead },
                systemUnread = all.count { it.type == MessageType.SYSTEM && !it.isRead },
                serviceUnread = all.count { it.type == MessageType.SERVICE && !it.isRead },
                totalUnread = all.count { !it.isRead },
            )
        }
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

    /**
     * 首次进入若消息库为空，插入若干演示消息，方便 UI 审阅。
     * 若用户已清空所有消息，则下次进入会再次播种（轻量、不影响真实数据）。
     */
    private fun seedDemoIfEmpty() {
        viewModelScope.launch {
            val existing = repo.watchAll().first()
            if (existing.isNotEmpty()) return@launch
            val now = LocalDateTime.now()
            repo.insert(
                AppMessage(
                    type = MessageType.INTERACTION,
                    title = "小宝的妈妈",
                    content = "赞了你的喂养记录：母乳 15分钟",
                    senderAvatar = null,
                    createTime = now.minusMinutes(20),
                    isRead = false,
                )
            )
            repo.insert(
                AppMessage(
                    type = MessageType.INTERACTION,
                    title = "豆豆的爸爸",
                    content = "评论了你的睡眠记录：宝宝今天睡得真好！",
                    senderAvatar = null,
                    createTime = now.minusHours(2),
                    isRead = true,
                )
            )
            repo.insert(
                AppMessage(
                    type = MessageType.SYSTEM,
                    title = "您的月度报告已生成",
                    content = "本月宝宝的喂养、睡眠、生长汇总报告已生成，点击查看",
                    senderAvatar = null,
                    createTime = now.minusHours(5),
                    isRead = false,
                )
            )
            repo.insert(
                AppMessage(
                    type = MessageType.SYSTEM,
                    title = "疫苗接种提醒",
                    content = "乙肝疫苗第二针即将到期，请尽快安排接种",
                    senderAvatar = null,
                    createTime = now.minusDays(1),
                    isRead = true,
                )
            )
            repo.insert(
                AppMessage(
                    type = MessageType.SERVICE,
                    title = "体检套餐限时优惠",
                    content = "宝宝专属体检套餐 8 折优惠，仅限本周，立即抢购",
                    senderAvatar = null,
                    createTime = now.minusDays(2),
                    isRead = false,
                )
            )
            repo.insert(
                AppMessage(
                    type = MessageType.SERVICE,
                    title = "新功能上线：成长曲线",
                    content = "可视化追踪宝宝身高体重曲线，一键生成报告",
                    senderAvatar = null,
                    createTime = now.minusDays(3),
                    isRead = true,
                )
            )
        }
    }
}
