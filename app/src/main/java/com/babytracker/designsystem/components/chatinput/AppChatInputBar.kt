package com.babytracker.designsystem.components.chatinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.chatinput.ChatInputBarDefaults as AppChatInputBarDefaults
import com.babytracker.designsystem.i18n.AppStrings

/**
 * 聊天输入条 — 「多行输入 + 发送/停止按钮切换」复合组件（G4 聊天族收编）。
 *
 * [isSending] 轴切换尾部动作：发送态点按触发 [onSend]，进行中点按触发 [onStop]。
 * 输入框可用性（enabled）、发送可用性（canSend，仅非发送态生效）与错误态由调用方映射传入——
 * 业务前置条件/错误枚举不进入本组件 API。文案参数默认取既有聊天族 key，
 * 其他会话形态可整组覆盖。
 *
 * 用法：
 *   AppChatInputBar(
 *       value = input, onValueChange = { input = it },
 *       isSending = sending, onSend = ::send, onStop = ::stop,
 *       enabled = ready && !sending, canSend = canSend,
 *   )
 */
@Composable
fun AppChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    canSend: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    inputLabel: String = AppStrings.aiInputLabel,
    placeholder: String? = AppStrings.aiInputPlaceholder,
    sendLabel: String = AppStrings.aiSend,
    stopLabel: String = AppStrings.aiStop,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(AppChatInputBarDefaults.containerColor())
            .padding(
                horizontal = AppChatInputBarDefaults.horizontalPadding(),
                vertical = AppChatInputBarDefaults.verticalPadding(),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppInput(
            value = value,
            onValueChange = onValueChange,
            label = inputLabel,
            placeholder = placeholder,
            enabled = enabled,
            isError = isError,
            errorMessage = errorMessage,
            singleLine = false,
            minLines = 1,
            maxLines = 4,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(AppChatInputBarDefaults.fieldActionGap()))
        AppButton(
            onClick = if (isSending) onStop else onSend,
            label = if (isSending) stopLabel else sendLabel,
            enabled = isSending || canSend,
        )
    }
}
