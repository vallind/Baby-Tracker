package com.babytracker.feature.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babytracker.core.backup.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 备份/恢复页状态：WebDAV 表单字段、状态文案、本地备份路径、恢复中标记。
 * 目录/文件选择与确认弹层是平台交互与 UI 态，留在 Screen。
 */
data class BackupUiState(
    val webdavUrl: String = "",
    val webdavUser: String = "",
    val webdavPass: String = "",
    val webdavStatus: String = "未配置",
    val backupPath: String = "",
    val restoring: Boolean = false,
)

/**
 * 备份/恢复 ViewModel（Batch 4 自 Screen 收编）。
 * BackupManager 全部操作进 VM；Toast 用 applicationContext（无需 Activity）。
 */
class BackupViewModel(
    private val backupManager: BackupManager,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun loadConfig() {
        viewModelScope.launch {
            backupManager.loadConfig()?.let { config ->
                _state.update {
                    it.copy(
                        webdavUrl = config.webdavUrl ?: "",
                        webdavUser = config.webdavUser ?: "",
                        webdavPass = config.webdavPass ?: "",
                        webdavStatus = if (config.webdavUrl != null) {
                            config.lastBackupAt?.let { ts -> "上次: $ts" } ?: "已配置"
                        } else {
                            "未配置"
                        },
                    )
                }
            }
        }
    }

    fun onWebdavUrlChange(value: String) = _state.update { it.copy(webdavUrl = value) }
    fun onWebdavUserChange(value: String) = _state.update { it.copy(webdavUser = value) }
    fun onWebdavPassChange(value: String) = _state.update { it.copy(webdavPass = value) }

    fun saveWebdav() {
        viewModelScope.launch {
            val s = _state.value
            backupManager.saveConfig(s.webdavUrl, s.webdavUser, s.webdavPass)
            _state.update { it.copy(webdavStatus = "已保存") }
            toast("配置已保存")
        }
    }

    /** 本地备份：有目录走 URI 备份，否则走默认位置（原 Screen 内逻辑迁入） */
    fun createLocalBackup(dirUri: Uri?) {
        viewModelScope.launch {
            val path = if (dirUri != null) {
                backupManager.createLocalBackupToUri(context, dirUri)
            } else {
                backupManager.createLocalBackup(context)
            }
            _state.update { it.copy(backupPath = path ?: "") }
            toast(if (path != null) "备份完成" else "备份失败")
        }
    }

    fun createWebdavBackup() {
        viewModelScope.launch {
            backupManager.createWebDAVBackup()
                .onSuccess {
                    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    _state.update { it.copy(webdavStatus = "上次: $now") }
                    toast("备份完成")
                }
                .onFailure { toast("备份失败: ${it.message}") }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(restoring = true) }
            backupManager.restoreFromUri(context, uri)
                .onSuccess { toast("恢复完成") }
                .onFailure { toast("恢复失败: ${it.message}") }
            _state.update { it.copy(restoring = false) }
        }
    }

    fun restoreFromWebdav() {
        viewModelScope.launch {
            _state.update { it.copy(restoring = true) }
            backupManager.restoreFromWebDAV()
                .onSuccess { toast("云端恢复完成") }
                .onFailure { toast("恢复失败: ${it.message}") }
            _state.update { it.copy(restoring = false) }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}