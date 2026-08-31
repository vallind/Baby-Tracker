package com.babytracker.ui.patterns.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

/**
 * 聊天气泡 — 「角色对齐轴 + 双色气泡壳 + 自由内容槽」复合组件（G4 聊天族收编）。
 *
 * [fromUser] 轴同时决定对齐与配色：
 * - 用户侧：整行右对齐，primary 实底气泡；
 * - 助手侧：整行左对齐，浅色浮起底气泡。
 * 气泡内是自由内容槽——思考过程块、回答依据、风险卡、操作行等业务组合留在调用方，
 * 通过槽注入；壳只负责几何（最大宽度/圆角/内边距走组件令牌）。
 * 内容前景色由槽内元素自带（如正文 onPrimary/textPrimary 的映射留在调用方）。
 *
 * 用法：
 *   AppChatBubble(fromUser = true) {
 *       Text("你好", style = typography.bodyLarge, color = c.onPrimary)
 *   }
 */
@Composable
fun AppChatBubble(
    fromUser: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier
                .widthIn(max = ChatBubbleDefaults.maxWidth())
                .clip(RoundedCornerShape(ChatBubbleDefaults.cornerRadius()))
                .background(
                    if (fromUser) {
                        ChatBubbleDefaults.userContainerColor()
                    } else {
                        ChatBubbleDefaults.assistantContainerColor()
                    },
                )
                .padding(ChatBubbleDefaults.innerPadding()),
            content = content,
        )
    }
}