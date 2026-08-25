package com.babytracker.feature.growth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.babytracker.designsystem.theme.LocalAppMotion
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.babytracker.designsystem.components.chip.AppOptionChipRow
import com.babytracker.designsystem.components.recorddetail.RecordDetailSheet
import com.babytracker.designsystem.components.datenav.DateNavCapsule
import com.babytracker.designsystem.components.datenav.appDateNavLabel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.babytracker.core.domain.model.Baby
import com.babytracker.core.domain.model.Growth
import com.babytracker.core.domain.model.GrowthType
import com.babytracker.core.util.DateUtils
import com.babytracker.core.util.GrowthReference
import com.babytracker.designsystem.components.datetimecascade.AppDateTimeField
import com.babytracker.designsystem.components.datetimecascade.DateTimeCascadeDialog
import com.babytracker.designsystem.components.dialog.AppFormSheet
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.components.snackbar.AppSnackbar
import com.babytracker.designsystem.components.snackbar.AppSnackbarHost
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.iconbutton.AppIconButton
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 生长记录页 — 纯 UI 渲染层：只收 state + baby + 命名回调，不接触导航 / Koin / Repository / Controller。
 * SegmentedControl 类型切换、表单 Sheet 显隐、日期选择器显隐等 UI 临时状态留在本地 remember；
 * 数据加载、删除/撤销、表单保存全部在 GrowthViewModel；
 * 日期筛选为页面状态（原则 #9），与表单显隐等一起留 Screen 本地 remember。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GrowthScreen(
    state: GrowthUiState,
    baby: Baby?,
    bottomBar: @Composable () -> Unit = {},
    onBack: () -> Unit = {},
    onDelete: (Growth) -> Unit = {},
    onUndoDelete: () -> Unit = {},
    onSave: (Growth) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val typography = LocalAppTypography.current
    val shapes = LocalAppShapes.current
    val scope = rememberCoroutineScope()
    val babyId = state.babyId
    if (babyId == 0) return
    val growths = state.growths
    var showForm by remember { mutableStateOf(false) }
    var editingGrowth by remember { mutableStateOf<Growth?>(null) }
    var detailGrowth by remember { mutableStateOf<Growth?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val appSnackbar = remember { AppSnackbar(snackbarHostState) }

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf(AppStrings.growthHeight, AppStrings.growthWeight, AppStrings.growthHead)
    val types = listOf(GrowthType.HEIGHT, GrowthType.WEIGHT, GrowthType.HEAD)
    val units = listOf("cm", "kg", "cm")
    AppScaffold(
        modifier = modifier,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = AppStrings.growthRecords,
                onBack = onBack,
                actions = {
                    AppIconButton(
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true },
                        contentDescription = AppStrings.growthCalendar,
                        tint = c.textPrimary,
                    )
                },
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(c.pageBackground),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
            ) {
                SegmentedControl(
                    labels = tabs,
                    selectedIndex = tab,
                    onSelect = { tab = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val dateLabel = remember(selectedDate, today) { appDateNavLabel(selectedDate, today) }

            // 日期选择行（现代胶囊行）
            DateNavCapsule(
                dateLabel = dateLabel,
                onPrev = { selectedDate = selectedDate.minusDays(1) },
                onNext = { selectedDate = selectedDate.plusDays(1) },
                onOpenPicker = { showDatePicker = true },
                onToday = if (selectedDate != today) ({ selectedDate = today }) else null,
                modifier = Modifier.padding(horizontal = spacing.md),
            )

            val chartData = remember(growths, tab) {
                growths.filter { it.type == types[tab] }.sortedBy { it.measuredAt }
            }
            val latest = chartData.lastOrNull()
            val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val grouped = remember(growths, tab, selectedDate) {
                growths.filter { it.type == types[tab] }
                    .filter { it.measuredAt.take(10) == dateStr }
                    .sortedByDescending { it.measuredAt }
                    .groupBy { it.measuredAt.take(10) }
            }

            if (chartData.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        emoji = when (tab) { 0 -> "📏"; 1 -> "⚖️"; else -> "📐" },
                        title = "${String.format(Locale.US, AppStrings.growthEmptyTitleTemplate, tabs[tab])}",
                        subtitle = "${String.format(Locale.US, AppStrings.growthEmptySubtitleTemplate, tabs[tab])}",
                        actionText = "${String.format(Locale.US, AppStrings.growthRecordTemplate, tabs[tab])}",
                        onAction = {
                            editingGrowth = null
                            showForm = true
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = spacing.md,
                        end = spacing.md,
                        top = spacing.xs,
                        bottom = spacing.sm,
                    ),
                ) {
                    if (latest != null) {
                        item {
                            val valText = when {
                                latest.value == latest.value.toLong().toDouble() && latest.value != 0.0 ->
                                    String.format(Locale.US, "%.0f", latest.value)
                                else -> String.format(Locale.US, "%.1f", latest.value)
                            }
                            val measuredDate = try {
                                LocalDateTime.parse(latest.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (_: Exception) { "" }

                            // 动态参考区间（T4：按宝宝月龄取 GrowthReference，替代写死值）
                            val ageMonths = remember(baby) {
                                baby?.let {
                                    val birth = java.time.LocalDate.parse(it.birthDate.take(10))
                                    val p = java.time.Period.between(birth, java.time.LocalDate.now())
                                    (p.years * 12 + p.months).coerceAtLeast(0)
                                } ?: 0
                            }
                            val ref = GrowthReference.range(ageMonths, types[tab])
                            val outOfRange = ref != null && (latest.value < ref.min || latest.value > ref.max)
                            val referenceText = GrowthReference.rangeText(ageMonths, types[tab], units[tab])

                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = spacing.md),
                            ) {
                                Column(Modifier.fillMaxWidth().padding(spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "${String.format(Locale.US, AppStrings.growthCurrentTemplate, tabs[tab])}",
                                        style = LocalAppTypography.current.bodyMedium,
                                        color = c.textSecondary,
                                    )
                                    Spacer(Modifier.height(spacing.sm))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Text(
                                            valText,
                                            style = typography.displayLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = c.textPrimary,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            units[tab],
                                            style = typography.titleLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = c.textSecondary,
                                            modifier = Modifier.padding(bottom = 10.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "${String.format(Locale.US, AppStrings.growthMeasuredAtTemplate, measuredDate)}",
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textTertiary,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        referenceText,
                                        style = LocalAppTypography.current.labelMedium,
                                        fontWeight = if (outOfRange) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (outOfRange) c.warning else c.textTertiary,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        val chartProgress by animateFloatAsState(
                            targetValue = if (chartData.size > 1) 1f else 0f,
                            animationSpec = tween(LocalAppMotion.current.duration.long, easing = LocalAppMotion.current.easing.standard),
                        )
                        val chartCardShape = RoundedCornerShape(shapes.medium)   // shapes.medium（原硬编码 12dp 收编）
                        val gridColor = c.divider
                        val lineColor = c.primary
                        val bgColor = c.surface
                        val areaBrush = Gradients.growthChart(c)
                        val minVal = chartData.minOfOrNull { it.value } ?: 0.0
                        val maxVal = chartData.maxOfOrNull { it.value } ?: 100.0
                        val range = (maxVal - minVal).coerceAtLeast(1.0)
                        val yLabels = remember(minVal, maxVal) {
                            (0..3).map { i ->
                                val v = maxVal - (range * i / 3)
                                String.format(Locale.US, "%.1f", v)
                            }
                        }

                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = spacing.md),
                        ) {
                            Box(Modifier.fillMaxWidth().height(300.dp).padding(spacing.md)) {
                                Column(
                                    Modifier.fillMaxHeight().width(36.dp).padding(bottom = spacing.lg),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    yLabels.forEach { Text(it, style = LocalAppTypography.current.labelSmall, color = c.textSecondary) }
                                }

                                Canvas(Modifier.fillMaxSize().padding(start = 36.dp, bottom = 24.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    for (i in 0..3) {
                                        drawLine(gridColor, Offset(0f, h * i / 4), Offset(w, h * i / 4), strokeWidth = 1f)
                                    }
                                    val whoLines = whoReferenceLines(GrowthType.raw(types[tab]), minVal, maxVal, range)
                                    whoLines.forEach { percentile ->
                                        val y = h * (1f - ((percentile - minVal) / range).toFloat()).coerceIn(0f, h)
                                        val dashed = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                            floatArrayOf(10f, 10f), 0f,
                                        )
                                        drawLine(
                                            color = lineColor.copy(alpha = 0.25f),
                                            start = Offset(0f, y),
                                            end = Offset(w, y),
                                            strokeWidth = 1f,
                                            pathEffect = dashed,
                                        )
                                    }
                                    if (chartData.size > 1 && chartProgress > 0f) {
                                        val points = chartData.mapIndexed { i, g ->
                                            Offset(w * i / (chartData.size - 1), h * (1f - ((g.value - minVal) / range).toFloat()))
                                        }
                                        val visibleCount = ((points.size - 1) * chartProgress).toInt().coerceIn(0, points.size - 1)
                                        val areaPath = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(points[0].x, h)
                                            for (i in 0..visibleCount) { lineTo(points[i].x, points[i].y) }
                                            lineTo(points[visibleCount].x, h)
                                            close()
                                        }
                                        drawPath(areaPath, brush = areaBrush)
                                        for (i in 0 until visibleCount) {
                                            drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        }
                                        for (i in 0..visibleCount) {
                                            drawCircle(lineColor, 5.dp.toPx(), points[i])
                                            drawCircle(bgColor, 2.5.dp.toPx(), points[i])
                                        }
                                    } else if (chartData.size == 1) {
                                        drawCircle(lineColor, 5.dp.toPx(), Offset(w / 2, h / 2))
                                        drawCircle(bgColor, 2.5.dp.toPx(), Offset(w / 2, h / 2))
                                    }
                                }

                                if (chartData.size > 1) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 36.dp, top = 300.dp - 20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        chartData.forEachIndexed { i, g ->
                                            if (i % maxOf(1, chartData.size / 5) == 0 || i == chartData.size - 1) {
                                                Text(
                                                    try {
                                                        LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                                            .format(DateTimeFormatter.ofPattern("MM/dd"))
                                                    } catch (_: Exception) { "" },
                                                    style = LocalAppTypography.current.labelSmall,
                                                    color = c.textSecondary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    grouped.forEach { (date, items) ->
                        stickyHeader(key = date) {
                            Text(
                                date,
                                style = LocalAppTypography.current.labelMedium,
                                color = c.textSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = spacing.xs),
                            )
                        }
                        items(items = items, key = { it.id }) { g ->
                            val label = DateUtils.growthTypeLabel(GrowthType.raw(g.type))
                            val valStr = String.format(Locale.US, "%.1f", g.value)
                            val dateStr = try {
                                LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
                                    .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
                            } catch (_: Exception) { "" }
                            val unitStr = when (g.type) {
                                GrowthType.HEIGHT -> "cm"
                                GrowthType.WEIGHT -> "kg"
                                GrowthType.HEAD -> "cm"
                            }
                            RecordCard(
                            onDelete = {
                                onDelete(g)
                                scope.launch {
                                    appSnackbar.showUndo(message = AppStrings.deletedGrowth) { onUndoDelete() }
                                }
                            },
                                onClick = { detailGrowth = g },
                                onLongClick = {
                                    editingGrowth = g
                                    showForm = true
                                },
                                modifier = Modifier.padding(bottom = spacing.sm),
                            ) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(shapes.large))
                                        .background(AppColorScale.fromSeed(c.primary).tintContainer(c)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        when (g.type) {
                                            GrowthType.HEIGHT -> "📏"
                                            GrowthType.WEIGHT -> "⚖️"
                                            GrowthType.HEAD -> "📐"
                                        },
                                        style = LocalAppTypography.current.titleLarge,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "$label $valStr $unitStr",
                                        style = LocalAppTypography.current.titleSmall,
                                        color = c.textPrimary,
                                    )
                                    Text(
                                        dateStr,
                                        style = LocalAppTypography.current.bodySmall,
                                        color = c.textSecondary,
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(spacing.sm)) }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = spacing.md, vertical = 12.dp),
            ) {
                AppButton(
                    onClick = {
                        editingGrowth = null
                        showForm = true
                    },
                    label = "${String.format(Locale.US, AppStrings.growthRecordTemplate, tabs[tab])}",
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    // 单击卡片 = 详情弹层
    detailGrowth?.let { g ->
        RecordDetailSheet(
            show = true,
            title = DateUtils.growthTypeLabel(GrowthType.raw(g.type)),
            emoji = when (g.type) { GrowthType.HEIGHT -> "📏"; GrowthType.WEIGHT -> "⚖️"; GrowthType.HEAD -> "📐" },
            tint = c.success,
            fields = growthDetailFields(g),
            onEdit = {
                detailGrowth = null
                editingGrowth = g
                showForm = true
            },
            onDelete = {
                onDelete(g)
                scope.launch {
                    appSnackbar.showUndo(message = AppStrings.deletedGrowth) { onUndoDelete() }
                }
            },
            onDismiss = { detailGrowth = null },
        )
    }

    if (showForm) {
        GrowthFormDialog(
            babyId = babyId,
            editEntity = editingGrowth,
            onDismiss = {
                showForm = false
                editingGrowth = null
            },
            onSave = { growth ->
                onSave(growth)
                showForm = false
                editingGrowth = null
            },
        )
    }

    DateTimeCascadeDialog(
        show = showDatePicker,
        initialDateTime = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 00:00",
        dateOnly = true,
        onConfirm = { dt ->
            selectedDate = LocalDate.parse(dt.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            showDatePicker = false
        },
        onDismiss = { showDatePicker = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthFormDialog(
    babyId: Int,
    editEntity: Growth? = null,
    onDismiss: () -> Unit,
    onSave: (Growth) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val isEdit = editEntity != null
    var type by remember { mutableStateOf(editEntity?.let { GrowthType.raw(it.type) } ?: "height") }
    var value by remember {
        mutableStateOf(
            editEntity?.value?.let {
                if (it == it.toLong().toDouble() && it == 0.0) "" else String.format(Locale.US, "%.1f", it)
            } ?: "",
        )
    }
    var measuredAt by remember {
        mutableStateOf(
            editEntity?.measuredAt?.let { ts ->
                try {
                    LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (_: Exception) {
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                }
            } ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        )
    }
    var note by remember { mutableStateOf(editEntity?.note ?: "") }

    val buildEntity = {
        if (isEdit) {
            editEntity.copy(
                type = GrowthType.fromRaw(type),
                value = value.toDoubleOrNull() ?: 0.0,
                measuredAt = measuredAt.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        } else {
            Growth(
                babyId = babyId,
                type = GrowthType.fromRaw(type),
                value = value.toDoubleOrNull() ?: 0.0,
                measuredAt = measuredAt.replace(" ", "T") + ":00",
                note = note.ifBlank { null },
            )
        }
    }

    AppFormSheet(
        title = if (isEdit) AppStrings.editGrowthLabel else AppStrings.recordGrowthLabel,
        onDismiss = onDismiss,
        onSave = { onSave(buildEntity()) },
        saveText = if (isEdit) AppStrings.updateLabel else AppStrings.save,
    ) {
        AppOptionChipRow(
            options = listOf("height" to AppStrings.growthOptionHeight, "weight" to AppStrings.growthOptionWeight, "head" to AppStrings.growthOptionHead),
            selectedKey = type,
            onSelect = { type = it },
            modifier = Modifier.padding(bottom = spacing.md),
        )

        AppInput(
            value = value,
            onValueChange = { value = it },
            label = AppStrings.detailValue,
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )

        AppDateTimeField(
            label = AppStrings.detailMeasuredAt,
            value = measuredAt,
            onPick = { measuredAt = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )

        AppInput(
            value = note,
            onValueChange = { note = it },
            label = AppStrings.noteOptional,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun whoReferenceLines(type: String, minVal: Double, maxVal: Double, range: Double): List<Double> {
    val median = when (type) {
        "height" -> 67.0
        "weight" -> 7.5
        "head" -> 43.0
        else -> return emptyList()
    }
    val offsets = listOf(0.10, 0.0, -0.10)
    return offsets.map { median * (1 + it) }
        .filter { it in (minVal - range * 0.2)..(maxVal + range * 0.2) }
}


/** 生长记录详情字段 */
private fun growthDetailFields(g: Growth): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val measured = try {
        java.time.LocalDateTime.parse(g.measuredAt, DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) { g.measuredAt }
    list += AppStrings.detailMeasuredAt to measured
    val unit = when (g.type) { GrowthType.HEIGHT -> "cm"; GrowthType.WEIGHT -> "kg"; GrowthType.HEAD -> "cm" }
    list += AppStrings.detailValue to String.format(java.util.Locale.US, "%.1f %s", g.value, unit)
    g.note?.takeIf { it.isNotBlank() }?.let { list += AppStrings.detailNote to it }
    return list
}
