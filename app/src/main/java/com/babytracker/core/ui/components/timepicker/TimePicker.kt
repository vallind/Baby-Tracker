package com.babytracker.core.ui.components.timepicker

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
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextButton
import io.elyon.kmp.theme.ElyonTheme

/**
 * TDesign 风格时间选择器对话框 — 滚轮式时/分选择。
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

    val c = ElyonTheme.colorScheme
    val bgColor = c.surface
    val cornerRadius = 16.dp
    val itemHeight = 44.dp
    val visibleItems = 5

    var hour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

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
                TimePickerToolbar(
                    title = title,
                    textColor = c.primary,
                    dividerColor = c.dividerLine,
                    toolbarHeight = 48.dp,
                    onCancel = onDismiss,
                    onConfirm = {
                        onConfirm("%02d:%02d".format(hour, minute))
                        onDismiss()
                    },
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            "小时",
                            style = ElyonTheme.textStyles.footnote2,
                            color = c.onSurfaceVariantSummary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        TimePickerLogic(
                            value = hour,
                            range = 0..23,
                            itemHeight = itemHeight,
                            visibleItems = visibleItems,
                            selectedBgColor = c.primary.copy(alpha = 0.12f),
                            selectedTextColor = c.primary,
                            unselectedTextColor = c.onSurfaceVariantSummary,
                            dividerColor = c.dividerLine,
                            onValueChanged = { hour = it },
                        )
                    }

                    Text(
                        ":",
                        style = ElyonTheme.textStyles.headline2.copy(fontWeight = FontWeight.Bold),
                        color = c.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            "分钟",
                            style = ElyonTheme.textStyles.footnote2,
                            color = c.onSurfaceVariantSummary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        TimePickerLogic(
                            value = minute,
                            range = 0..59,
                            itemHeight = itemHeight,
                            visibleItems = visibleItems,
                            selectedBgColor = c.primary.copy(alpha = 0.12f),
                            selectedTextColor = c.primary,
                            unselectedTextColor = c.onSurfaceVariantSummary,
                            dividerColor = c.dividerLine,
                            onValueChanged = { minute = it },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

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
            TextButton(
                text = "取消",
                onClick = onCancel,
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = textColor),
            )
            Text(
                title,
                style = ElyonTheme.textStyles.title3,
                color = ElyonTheme.colorScheme.onSurface,
            )
            TextButton(
                text = "确认",
                onClick = onConfirm,
                colors = io.elyon.kmp.basic.ButtonDefaults.textButtonColors(textColor = textColor),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(dividerColor),
        )
    }
}
