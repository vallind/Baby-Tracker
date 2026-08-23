package com.babytracker.designsystem.components.actionbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.babytracker.designsystem.components.button.AppButton

/**
 * 底部主操作条 — 列表页底部固定的全宽主按钮容器，全站统一规格。
 *
 * 替代喂养/尿布/睡眠三页复制粘贴的 Box(surface 背景 + padding + 全宽 AppButton)。
 *
 * 用法：
 *   AppActionBar(label = "记录喂养", icon = Icons.Default.Add, onClick = { ... })
 */
@Composable
fun AppActionBar(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(ActionBarDefaults.containerColor())
            .padding(
                horizontal = ActionBarDefaults.horizontalPadding(),
                vertical = ActionBarDefaults.verticalPadding(),
            ),
    ) {
        AppButton(label = label, onClick = onClick, icon = icon, modifier = Modifier.fillMaxWidth())
    }
}
