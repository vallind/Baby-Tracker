package com.babytracker.designsystem.components.timepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * TDesign 风格时间选择器对话框 — 滚轮式时/分选择。
 *
 * 视觉特征（参照 TDesign Mobile TimePicker）：
 * - 全屏半透明遮罩 + 底部弹出面板
 * - 滚轮式选择器，选中行品牌色高亮背景
 * - 上/下分隔线标识选中区域
 * - 顶部工具栏：取消 | 标题 | 确认
 *
 * @param show 是否显示
 * @param initialHour 初始小时 (0-23)
 * @param initialMinute 初始分钟 (0-59)
 * @param onConfirm 确认回调，返回 "HH:mm" 格式字符串
 * @param onDismiss 取消回调
 * @param title 标题，默认 "选择时间"
 */
@Composable
fun TimePickerDialog(
    show: Boolean,
    initialHour: Int = 12,
    initialMinute: Int = 0,
    onConfirm: (timeStr: String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "选择时间",
) {
    if (!show) return

    val tokens = TimePickerDefaults
    val bgColor = tokens.backgroundColor()
    val cornerRadius = tokens.cornerRadius()
    val itemHeight = tokens.itemHeight()
    val visibleItems = tokens.visibleItems()

    var hour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    // 全屏 Dialog 确保覆盖在所有内容之上（包括 ModalBottomSheet）
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                    .background(bgColor),
            ) {
            // 顶部工具栏
            TimePickerToolbar(
                title = title,
                textColor = tokens.toolbarTextColor(),
                dividerColor = tokens.toolbarDividerColor(),
                toolbarHeight = tokens.toolbarHeight(),
                onCancel = onDismiss,
                onConfirm = {
                    onConfirm("%02d:%02d".format(hour, minute))
                    onDismiss()
                },
            )

            // 滚轮选择区
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                // 小时
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        "小时",
                        style = LocalAppTypography.current.labelSmall,
                        color = tokens.unselectedTextColor(),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    TimePickerLogic(
                        value = hour,
                        range = 0..23,
                        itemHeight = itemHeight,
                        visibleItems = visibleItems,
                        selectedBgColor = tokens.selectedBackgroundColor(),
                        selectedTextColor = tokens.selectedTextColor(),
                        unselectedTextColor = tokens.unselectedTextColor(),
                        dividerColor = tokens.dividerColor(),
                        onValueChanged = { hour = it },
                    )
                }

                Text(
                    ":",
                    style = LocalAppTypography.current.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.separatorColor(),
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                // 分钟
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        "分钟",
                        style = LocalAppTypography.current.labelSmall,
                        color = tokens.unselectedTextColor(),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    TimePickerLogic(
                        value = minute,
                        range = 0..59,
                        itemHeight = itemHeight,
                        visibleItems = visibleItems,
                        selectedBgColor = tokens.selectedBackgroundColor(),
                        selectedTextColor = tokens.selectedTextColor(),
                        unselectedTextColor = tokens.unselectedTextColor(),
                        dividerColor = tokens.dividerColor(),
                        onValueChanged = { minute = it },
                    )
                }
            }

            // 安全区域占位
            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

/**
 * 顶部工具栏：取消 | 标题 | 确认。
 */
@Composable
private fun TimePickerToolbar(
    title: String,
    textColor: Color,
    dividerColor: Color,
    toolbarHeight: androidx.compose.ui.unit.Dp,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(toolbarHeight)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text("取消", color = textColor, fontSize = 15.sp)
            }
            Text(
                title,
                style = LocalAppTypography.current.titleMedium,
                color = LocalAppColors.current.onSurface,
            )
            TextButton(onClick = onConfirm) {
                Text("确认", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        // 底部分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(dividerColor),
        )
    }
}
