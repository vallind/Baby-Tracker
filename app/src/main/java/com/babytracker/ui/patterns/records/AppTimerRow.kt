package com.babytracker.ui.patterns.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 表单计时器行 — 表单族通用组件（G4 收敛）。
 *
 * 喂养/睡眠记录表单同构的"大号计时展示 + 开始/结束按钮"一行。
 * 全参数化（display/running/onStart/onStop），零持久化依赖：
 * 计时状态用 [com.babytracker.ui.patterns.records.TimerState]，
 * SharedPreferences 读写留在 feature 调用方。
 *
 * 用法：
 *   AppTimerRow(
 *       display = timer.formatDisplay(),
 *       running = timer.running,
 *       onStart = { ... },
 *       onStop = { ... },
 *   )
 */
@Composable
fun AppTimerRow(
    display: String,
    running: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        Text(
            text = display,
            style = LocalAppTypography.current.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (running) c.primary else c.textSecondary,
            modifier = Modifier.weight(1f).widthIn(min = 100.dp),
            textAlign = TextAlign.Start,
        )
        if (running) {
            AppButton(onClick = onStop, label = AppStrings.timerStop)
        } else {
            AppButton(onClick = onStart, label = AppStrings.timerStart)
        }
    }
}