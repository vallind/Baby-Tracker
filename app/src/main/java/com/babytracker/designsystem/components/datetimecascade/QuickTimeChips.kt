package com.babytracker.designsystem.components.datetimecascade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.theme.LocalAppSpacing
import java.time.LocalDateTime

/**
 * 快捷时间选择条 — 表单里替代高频打开级联滚轮的场景。
 *
 * 纯动作型 chips（点击即回调，无选中态），精确到分钟的时间仍走
 * [DateTimeCascadeDialog]。按项目"两次规则"评估：喂养/尿布/睡眠三个
 * 表单同构出现 ≥3 处，但仅为布局组合、无独立令牌封装，故不做组件注册，
 * 与级联对话框同包共置以便发现。
 *
 * 用法：
 *   QuickTimeChipRow { feedingDateTime = it.format(formatter) }
 */
@Composable
fun QuickTimeChipRow(
    onPick: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    // 预设偏移（分钟）；"刚刚"取整点当前时间，避免与保存值差秒
    val presets = listOf(
        "刚刚" to 0L,
        "15分钟前" to 15L,
        "30分钟前" to 30L,
        "1小时前" to 60L,
    )
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
        presets.forEach { (label, minusMinutes) ->
            AppFilterChip(
                selected = false,
                onClick = {
                    val now = LocalDateTime.now().withNano(0)
                    onPick(if (minusMinutes == 0L) now else now.minusMinutes(minusMinutes))
                },
                label = label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
