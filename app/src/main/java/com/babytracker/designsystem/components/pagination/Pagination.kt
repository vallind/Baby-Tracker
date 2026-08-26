package com.babytracker.designsystem.components.pagination

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.components.pagination.PaginationDefaults as AppPaginationDefaults
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 分页器 —— 消费 AppComponentTokens.pagination（P0 五件套之一）。
 *
 * 页码策略：首页/末页常驻 + 当前页 ±1 数字窗口 + 省略号（visibleCount 为数字位上限，≥3）。
 * 页码从 1 开始；箭头在边界自动禁用。内容仅数字与"…"符号，无用户文案，无需 i18n 条目。
 *
 * 用法：
 *   AppPagination(
 *       pageCount = 12,
 *       currentPage = page,
 *       onPageChange = { page = it },
 *   )
 */
@Composable
fun AppPagination(
    pageCount: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
) {
    require(pageCount >= 1) { "pageCount 必须 ≥ 1" }
    val safeCurrent = currentPage.coerceIn(1, pageCount)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppPaginationDefaults.itemSpacing()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArrowItem(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            enabled = safeCurrent > 1,
            onClick = { onPageChange(safeCurrent - 1) },
        )
        pageSequence(pageCount, safeCurrent, visibleCount).forEach { entry ->
            when (entry) {
                null -> EllipsisItem()
                else -> PageItem(
                    page = entry,
                    active = entry == safeCurrent,
                    onClick = { onPageChange(entry) },
                )
            }
        }
        ArrowItem(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            enabled = safeCurrent < pageCount,
            onClick = { onPageChange(safeCurrent + 1) },
        )
    }
}

/** 折叠序列：[1, …, w0..w1, …, n]，数字窗宽 = visibleCount - 2（首尾常驻），越界自动贴边 */
internal fun pageSequence(pageCount: Int, current: Int, visibleCount: Int): List<Int?> {
    if (pageCount <= visibleCount + 2) return (1..pageCount).toList()
    val windowWidth = (visibleCount - 2).coerceAtLeast(3)
    val rawStart = current - windowWidth / 2
    val winStart = rawStart.coerceIn(2, pageCount - 1 - windowWidth + 1)
    val winEnd = winStart + windowWidth - 1

    val out = mutableListOf<Int?>(1)
    if (winStart > 2) out += null
    out += (winStart..winEnd).toList()
    if (winEnd < pageCount - 1) out += null
    out += pageCount
    return out
}

@Composable
private fun ArrowItem(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Icon(
        icon,
        contentDescription = null,
        tint = if (enabled) colors.textSecondary else colors.textDisabled,
        modifier = Modifier
            .clip(AppPaginationDefaults.cornerRadius())
            .size(AppPaginationDefaults.itemSize())
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(AppPaginationDefaults.itemSpacing() * 2),
    )
}

@Composable
private fun EllipsisItem() {
    Text(
        "…",
        color = LocalAppColors.current.textSecondary,
        fontSize = 12.sp,
        modifier = Modifier.size(AppPaginationDefaults.itemSize()),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun PageItem(page: Int, active: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val container = if (active) AppPaginationDefaults.activeColor() else colors.surfaceMuted
    val contentColor = if (active) colors.onPrimary else colors.textSecondary
    Box(
        modifier = Modifier
            .size(AppPaginationDefaults.itemSize())
            .clip(AppPaginationDefaults.cornerRadius())
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            page.toString(),
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
