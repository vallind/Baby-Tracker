package com.babytracker.designsystem.components.datenav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.datenav.DateNavCapsuleDefaults
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 日期导航胶囊 — 记录四页/统计页统一的「前一后一 + 日期胶囊 + 回今天」行。
 *
 * 收敛此前喂养/睡眠/尿布/生长四页各自 80 行复制与统计页的异形实现。
 * 日期胶囊为装饰性点击区：内部文本已承担读屏朗读，不再叠加描述（lessons #15）。
 *
 * 用法：
 *   DateNavCapsule(
 *       dateLabel = "今天 · 8月23日",
 *       onPrev = { ... }, onNext = { ... },
 *       onOpenPicker = { ... },
 *       onToday = if (selectedDate != today) ({ ... }) else null,
 *   )
 */
@Composable
fun DateNavCapsule(
    modifier: Modifier = Modifier,
    dateLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenPicker: () -> Unit,
    onToday: (() -> Unit)? = null,
    contentDescriptionPrev: String = AppStrings.prevDay,
    contentDescriptionNext: String = AppStrings.nextDay,
) {
    val typography = LocalAppTypography.current
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIconButton(
            icon = Icons.Default.ChevronLeft,
            onClick = onPrev,
            contentDescription = contentDescriptionPrev,
            tint = DateNavCapsuleDefaults.iconColor(),
            iconSize = 22.dp,
        )
        // 中间胶囊：点击开日期选择（内部文本承担读屏朗读）
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .clip(DateNavCapsuleDefaults.capsuleShape())
                    .background(DateNavCapsuleDefaults.capsuleColor())
                    .clickable(onClick = onOpenPicker)
                    .padding(
                        horizontal = DateNavCapsuleDefaults.horizontalPadding(),
                        vertical = DateNavCapsuleDefaults.verticalPadding(),
                    ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateLabel,
                        style = typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DateNavCapsuleDefaults.textColor(),
                    )
                    Spacer(Modifier.width(6.dp))
                    // 下拉箭头纯装饰，对读屏静默
                    androidx.compose.material3.Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DateNavCapsuleDefaults.iconColor(),
                        modifier = Modifier
                            .size(16.dp)
                            .clearAndSetSemantics {},
                    )
                }
            }
        }
        // 回今天胶囊（非今天时由调用方传入）
        if (onToday != null) {
            Text(
                AppStrings.today,
                style = typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = DateNavCapsuleDefaults.todayContentColor(),
                modifier = Modifier
                    .clip(DateNavCapsuleDefaults.capsuleShape())
                    .background(DateNavCapsuleDefaults.todayContainerColor())
                    .clickable(onClick = onToday)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        AppIconButton(
            icon = Icons.Default.ChevronRight,
            onClick = onNext,
            contentDescription = contentDescriptionNext,
            tint = DateNavCapsuleDefaults.iconColor(),
            iconSize = 22.dp,
        )
    }
}
