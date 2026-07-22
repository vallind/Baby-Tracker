package com.babytracker.core.ai.config

import com.babytracker.core.ai.AiRuntimeBundle
import com.babytracker.core.ai.AiRuntimeConfigValidator
import com.babytracker.core.auth.AuthService
import com.babytracker.core.data.FamilyService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

data class AiRuntimeState(
    val bundle: AiRuntimeBundle? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class AiConfigCoordinator(
    private val authService: AuthService,
    private val familyService: FamilyService,
    private val configStore: AiConfigStore,
    private val deviceKeyStore: AiDeviceKeyStore,
    private val bootstrapClient: AiBootstrapClient,
) {
    private data class ScopeSnapshot(
        val userId: String?,
        val familyId: String?,
        val canRefresh: Boolean,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val refreshMutex = Mutex()
    private val _state = MutableStateFlow(AiRuntimeState())
    val state: StateFlow<AiRuntimeState> = _state.asStateFlow()

    @Volatile
    private var activeScope = ScopeSnapshot(null, null, false)

    fun start() {
        if (!started.compareAndSet(false, true)) return
        scope.launch {
            combine(
                authService.observeAuthState(),
                authService.observeVerifiedAuthState(),
                familyService.sessionState,
            ) { cachedUser, verifiedUser, familyState ->
                val family = familyState.activeFamily
                ScopeSnapshot(
                    userId = cachedUser?.id,
                    familyId = family?.id,
                    canRefresh = cachedUser != null &&
                        verifiedUser?.id == cachedUser.id &&
                        familyState.verifiedFamilyForSync?.id == family?.id,
                )
            }.distinctUntilChanged().collectLatest(::handleScope)
        }
    }

    fun readyBundle(): AiRuntimeBundle? = _state.value.bundle?.takeIf { it.isValidAt() }

    suspend fun refreshNow(): Result<AiRuntimeBundle> = try {
        val snapshot = activeScope
        check(snapshot.canRefresh && snapshot.userId != null && snapshot.familyId != null) {
            "AI 配置刷新需要已验证的用户和家庭"
        }
        Result.success(refresh(snapshot))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun handleScope(snapshot: ScopeSnapshot) {
        activeScope = snapshot
        if (snapshot.userId == null) {
            configStore.clear()
            _state.value = AiRuntimeState()
            return
        }
        val familyId = snapshot.familyId
        if (familyId == null) {
            _state.value = AiRuntimeState()
            return
        }

        val stored = configStore.peek()
        if (stored != null && (stored.userId != snapshot.userId || stored.familyId != familyId)) {
            configStore.clear()
        }
        val cached = configStore.load(snapshot.userId, familyId)?.let { bundle ->
            runCatching { AiRuntimeConfigValidator.requireValid(bundle) }.getOrNull()
        }
        _state.value = AiRuntimeState(bundle = cached)
        if (snapshot.canRefresh) {
            try {
                refresh(snapshot)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // refresh 已记录错误并保留有效缓存，监听器继续等待会话或家庭变化。
            }
        }
    }

    private suspend fun refresh(snapshot: ScopeSnapshot): AiRuntimeBundle = refreshMutex.withLock {
        check(activeScope == snapshot) { "AI 配置范围已变化" }
        _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)
        try {
            val cachedVersion = configStore.load(snapshot.userId!!, snapshot.familyId!!)
                ?.config?.configVersion ?: 0
            val bundle = bootstrapClient.fetch(
                userId = snapshot.userId,
                familyId = snapshot.familyId,
                cachedConfigVersion = cachedVersion,
                devicePublicKey = deviceKeyStore.publicKeyBase64(),
            )
            AiRuntimeConfigValidator.requireValid(bundle)
            check(activeScope == snapshot) { "AI 配置范围已变化" }
            configStore.save(bundle)
            _state.value = AiRuntimeState(bundle = bundle)
            Timber.tag("AI").d(
                "bootstrap ok family=%s configVersion=%d",
                bundle.familyId,
                bundle.config.configVersion,
            )
            bundle
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fallback = _state.value.bundle?.takeIf { it.isValidAt() }
            _state.value = AiRuntimeState(
                bundle = fallback,
                errorMessage = e.message ?: "AI 配置获取失败",
            )
            Timber.tag("AI").w(e, "bootstrap failed; cached=%s", fallback != null)
            throw e
        }
    }
}
