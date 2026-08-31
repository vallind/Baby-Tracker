package com.babytracker.designsystem.components.table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.feedback.EmptyState
import com.babytracker.designsystem.components.divider.DividerDefaults
import com.babytracker.designsystem.components.table.TableDefaults as AppTableDefaults
import com.babytracker.designsystem.hooks.SortConfig
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 表格列定义 —— cell 槽位拿到 RowScope，可自行控制对齐与省略策略。
 *
 * @param sortKey 提供即代表该列可排序；点击表头回调 onHeaderSortClick(sortKey)
 */
@Immutable
data class AppTableColumn<T>(
    val header: String,
    val weight: Float = 1f,
    val width: Dp? = null,
    val sortKey: String? = null,
    val cell: @Composable androidx.compose.foundation.layout.RowScope.(T) -> Unit,
)

/**
 * 数据表格 —— 消费 AppComponentTokens.table（P0 五件套收官件）。
 *
 * Composition 轴：列由 [AppTableColumn] 声明（weight 弹性 / width 固定），单元格是槽位；
 * Sorting 轴：表头可排序列显示方向箭头，状态经 [activeSort]/[onHeaderSortClick]
 *   与 hooks/TableLogic（SortConfig/sortBy）直连——组件无内部排序状态；
 * Selection 轴：行选中底色 selectedBgColor + 选中朗读（AppStrings.selected）；
 * 四态规范：rows 为空时默认渲染 EmptyState（可用 emptyContent 覆盖）。
 * 长表滚动：纵向滚动交给调用方的 Lazy 列表/scroll 容器，本组件只渲染给定页的 rows
 *   （配合 TableLogic.currentPageData 或外接 AppPagination）。
 *
 * 用法：
 *   AppDataTable(
 *       columns = listOf(
 *           AppTableColumn<AppTableLogicRow>("日期", sortKey = "date") { Text(it.date) },
 *           AppTableColumn("奶量", width = 80.dp) { Text("${it.ml}ml") },
 *       ),
 *       rows = logic.currentPageData,
 *       activeSort = logic.sort.value,
 *       onHeaderSortClick = logic::sortBy,
 *   )
 */
@Composable
fun <T> AppDataTable(
    columns: List<AppTableColumn<T>>,
    rows: List<T>,
    modifier: Modifier = Modifier,
    activeSort: SortConfig? = null,
    onHeaderSortClick: ((String) -> Unit)? = null,
    isSelected: ((T) -> Boolean)? = null,
    onRowClick: ((T) -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
) {
    val colors = LocalAppColors.current

    Column(modifier) {
        HeaderRow(columns, activeSort, onHeaderSortClick)
        if (rows.isEmpty()) {
            if (emptyContent != null) {
                emptyContent()
            } else {
                EmptyStateDefault()
            }
        } else {
            rows.forEachIndexed { index, row ->
                HorizontalDivider(color = DividerDefaults.color(), thickness = AppTableDefaults.dividerThickness())
                BodyRow(
                    columns = columns,
                    row = row,
                    isSelected = isSelected?.invoke(row) == true,
                    clickable = onRowClick != null,
                    onClick = onRowClick?.let { cb -> { cb(row) } },
                )
            }
        }
    }
}

/** 空表默认态：四态规范 Empty → EmptyState */
@Composable
private fun EmptyStateDefault() {
    EmptyState(
        emoji = "📋",
        title = AppStrings.noData,
        subtitle = "",
    )
}

@Composable
private fun HeaderRow(
    columns: List<AppTableColumn<*>>,
    activeSort: SortConfig?,
    onHeaderSortClick: ((String) -> Unit)?,
) {
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(AppTableDefaults.headerHeight())
            .background(AppTableDefaults.headerBgColor()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            val sortable = column.sortKey != null && onHeaderSortClick != null
            val isActive = sortable && activeSort?.column == column.sortKey
            val headerCell = Modifier
                .let { m -> if (column.width != null) m.width(column.width) else m.weight(column.weight) }
                .fillMaxHeight()
                .then(
                    if (sortable) {
                        Modifier.clickable { onHeaderSortClick?.invoke(column.sortKey!!) }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = AppTableDefaults.cellHorizontalPadding())
            Row(
                modifier = headerCell,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    column.header,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) colors.primary else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // 无障碍：表头声明 heading 角色
                    modifier = Modifier.semantics { heading() },
                )
                if (isActive) {
                    SortArrow(ascending = activeSort?.ascending ?: true)
                }
            }
        }
    }
}

@Composable
private fun SortArrow(ascending: Boolean) {
    val icon: ImageVector = if (ascending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    Icon(
        icon,
        contentDescription = if (ascending) AppStrings.sortAscending else AppStrings.sortDescending,
        tint = LocalAppColors.current.primary,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(14.dp),
    )
}

@Composable
private fun <T> BodyRow(
    columns: List<AppTableColumn<T>>,
    row: T,
    isSelected: Boolean,
    clickable: Boolean,
    onClick: (() -> Unit)?,
) {
    val colors = LocalAppColors.current
    val bg = when {
        isSelected -> AppTableDefaults.selectedBgColor()
        else -> Color.Transparent
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(AppTableDefaults.rowHeight())
            .background(bg)
            .then(if (clickable) Modifier.clickable(onClick = onClick!!) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEach { column ->
            Box(
                Modifier
                    .let { m -> if (column.width != null) m.width(column.width) else m.weight(column.weight) }
                    .fillMaxHeight()
                    .padding(horizontal = AppTableDefaults.cellHorizontalPadding()),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    column.cell(this, row)
                }
            }
        }
    }
}
