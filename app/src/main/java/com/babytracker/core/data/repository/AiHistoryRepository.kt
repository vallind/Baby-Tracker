package com.babytracker.core.data.repository

import com.babytracker.core.database.dao.AiHistoryDao
import com.babytracker.core.database.entity.AiConversationEntity
import com.babytracker.core.database.entity.AiMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class AiConversationSummary(
    val id: Long,
    val familyId: String,
    val babyId: Int,
    val title: String,
    val preview: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class AiStoredMessage(
    val role: String,
    val content: String,
    val reasoningContent: String = "",
    val providerId: String? = null,
    val model: String? = null,
    val references: List<String> = emptyList(),
    val riskLevel: String? = null,
    val safetyStatus: String? = null,
)

data class AiStoredConversation(
    val id: Long,
    val title: String,
    val messages: List<AiStoredMessage>,
)

interface AiHistoryRepository {
    fun watchConversations(
        familyId: String,
        babyId: Int,
    ): Flow<List<AiConversationSummary>>

    suspend fun saveConversation(
        conversationId: Long?,
        familyId: String,
        babyId: Int,
        title: String,
        messages: List<AiStoredMessage>,
    ): Long

    suspend fun loadConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    ): AiStoredConversation?

    suspend fun deleteConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    )
}

class AiHistoryRepositoryImpl(
    private val dao: AiHistoryDao,
) : AiHistoryRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override fun watchConversations(
        familyId: String,
        babyId: Int,
    ): Flow<List<AiConversationSummary>> =
        dao.watchConversations(familyId, babyId).map { conversations ->
            conversations.map { conversation ->
                AiConversationSummary(
                    id = conversation.id,
                    familyId = conversation.familyId,
                    babyId = conversation.babyId,
                    title = conversation.title,
                    preview = conversation.preview.orEmpty(),
                    createdAt = conversation.createdAt,
                    updatedAt = conversation.updatedAt,
                )
            }
        }

    override suspend fun saveConversation(
        conversationId: Long?,
        familyId: String,
        babyId: Int,
        title: String,
        messages: List<AiStoredMessage>,
    ): Long {
        val now = System.currentTimeMillis()
        val scopedConversation = conversationId?.let {
            dao.getConversation(it, familyId, babyId)
        }
        val resolvedId = scopedConversation?.id ?: dao.insertConversation(
            AiConversationEntity(
                familyId = familyId,
                babyId = babyId,
                title = title,
                createdAt = now,
                updatedAt = now,
            ),
        )
        if (scopedConversation != null) {
            dao.updateConversation(
                conversationId = resolvedId,
                familyId = familyId,
                babyId = babyId,
                title = title,
                updatedAt = now,
            )
        }
        dao.replaceMessages(
            conversationId = resolvedId,
            messages = messages.mapIndexed { index, message ->
                AiMessageEntity(
                    conversationId = resolvedId,
                    role = message.role,
                    content = message.content,
                    reasoningContent = message.reasoningContent,
                    providerId = message.providerId,
                    model = message.model,
                    referencesJson = json.encodeToString(message.references),
                    riskLevel = message.riskLevel,
                    safetyStatus = message.safetyStatus,
                    position = index,
                    createdAt = now + index,
                )
            },
        )
        return resolvedId
    }

    override suspend fun loadConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    ): AiStoredConversation? {
        val conversation = dao.getConversation(conversationId, familyId, babyId) ?: return null
        return AiStoredConversation(
            id = conversation.id,
            title = conversation.title,
            messages = dao.getMessages(conversation.id).map { message ->
                AiStoredMessage(
                    role = message.role,
                    content = message.content,
                    reasoningContent = message.reasoningContent,
                    providerId = message.providerId,
                    model = message.model,
                    references = runCatching {
                        json.decodeFromString<List<String>>(message.referencesJson)
                    }.getOrDefault(emptyList()),
                    riskLevel = message.riskLevel,
                    safetyStatus = message.safetyStatus,
                )
            },
        )
    }

    override suspend fun deleteConversation(
        conversationId: Long,
        familyId: String,
        babyId: Int,
    ) {
        dao.deleteConversation(conversationId, familyId, babyId)
    }
}
