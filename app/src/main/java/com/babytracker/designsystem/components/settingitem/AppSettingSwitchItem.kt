package com.babytracker.designsystem.components.settingitem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.babytracker.designsystem.components.switchcontrol.AppSwitch

/**
 * 开关设置行 — 行主体复用 AppSettingItem，尾部为 AppSwitch，点击整行切换开关。
 *
 * 自 feature/ai 的 AiSwitchRow 收编，交互细节与源实现一致：
 * Switch 本身不响应点击（onCheckedChange = null），由整行点击切换；
 * 禁用时仅开关置灰并拦截整行切换，行外观不做降透明处理。
 *
 * 用法：
 *   AppSettingSwitchItem(emoji = "✨", label = "启用助手", checked = on, onCheckedChange = { on = it })
 */
@Composable
fun AppSettingSwitchItem(
    emoji: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    AppSettingItem(
        emoji = emoji,
        label = label,
        modifier = modifier,
        subtitle = subtitle,
        showChevron = false,
        trailing = {
            AppSwitch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        onClick = {
            if (enabled) onCheckedChange(!checked)
        },
    )
}
