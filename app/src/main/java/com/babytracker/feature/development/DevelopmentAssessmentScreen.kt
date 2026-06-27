package com.babytracker.feature.development

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.database.entity.BabyEntity
import com.babytracker.designsystem.theme.DT
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalThemeColors
import com.babytracker.designsystem.theme.ThemeColors
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.domain.model.AssessmentItem
import com.babytracker.core.domain.model.DevelopmentAssessment
import com.babytracker.designsystem.components.EmptyState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

// —— 5 项能力元数据 ——
private data class AbilityMeta(
    val title: String,
    val icon: ImageVector,
    val score: (DevelopmentAssessment) -> Int,
)

private val ABILITIES: List<AbilityMeta> = listOf(
    AbilityMeta("大运动", Icons.AutoMirrored.Filled.DirectionsRun) { it.grossMotor },
    AbilityMeta("精细动作", Icons.Default.PanTool) { it.fineMotor },
    AbilityMeta("语言", Icons.Default.RecordVoiceOver) { it.language },
    AbilityMeta("社交", Icons.Default.Group) { it.social },
    AbilityMeta("认知", Icons.Default.Psychology) { it.cognitive },
)

// 评分 → UI 文案 / 颜色
private fun scoreLabel(score: Int): String = when (score) {
    0 -> "未观察"
    1 -> "落后"
    2 -> "正常"
    3 -> "超前"
    else -> "--"
}

private fun scoreColor(score: Int, c: ThemeColors): Color = when (score) {
    0 -> c.textHint
    1 -> c.warning
    2 -> c.success
    3 -> c.danger
    else -> c.textHint
}

/** 各能力在不同评分下的简短描述（UI 展示用）。 */
private fun abilityDescription(title: String, score: Int): String = when (score) {
    0 -> "尚未观察 $title 表现"
    1 -> "$title 略低于月龄水平，建议关注"
    2 -> "$title 符合月龄水平"
    3 -> "$title 超前于月龄，表现突出"
    else -> "--"
}

/** 由 [BabyEntity.birthDate] 计算当前月龄（Int 月份）。 */
private fun babyAgeMonths(birthDate: String): Int {
    return try {
        val birth = LocalDate.parse(birthDate.take(10))
        val p = Period.between(birth, LocalDate.now())
        (p.years * 12 + p.months).coerceAtLeast(0)
    } catch (_: Exception) {
        0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevelopmentAssessmentScreen(navController: NavController) {
    val c = LocalThemeColors.current
    val babyRepo: BabyRepository = koinInject()
    val babyCtrl: BabyController = koinInject()
    val viewModel: DevelopmentAssessmentViewModel = koinViewModel()
    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()
    val state by viewModel.state.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(baby?.id) {
        if (baby != null) viewModel.load(baby.id)
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("发育评估", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.primaryLight,
                    titleContentColor = c.textPrimary,
                    navigationIconContentColor = c.textPrimary,
                ),
            )
        },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "👶",
                title = "还没有添加宝宝",
                subtitle = "请先在设置中添加宝宝信息",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.bg),
        ) {
            // —— 顶部宝宝信息区（浅蓝渐变 + 圆形头像）——
            BabyHeader(baby)

            val latest = state.latest

            if (latest == null) {
                Spacer(Modifier.height(DT.cardGap.dp))
                EmptyState(
                    emoji = "📝",
                    title = "还没有发育评估记录",
                    subtitle = "评估宝宝 5 项能力发展，了解成长进度",
                    actionText = "开始评估",
                    onAction = { showForm = true },
                )
            } else {
                Spacer(Modifier.height(DT.cardGap.dp))
                AssessmentSummaryCard(latest)
                Spacer(Modifier.height(DT.cardGap.dp))
                AssessmentItemsSection(latest)
            }

            Spacer(Modifier.height(DT.cardGap.dp))
            NextAssessmentHint(latest)

            Spacer(Modifier.height(DT.cardGap.dp))
            ReassessButton(onClick = { showForm = true })

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showForm && baby != null) {
        AssessmentFormDialog(
            babyId = baby.id,
            babyAgeMonths = babyAgeMonths(baby.birthDate),
            onDismiss = { showForm = false },
            onSubmit = { assessment ->
                viewModel.insert(assessment)
                showForm = false
            },
        )
    }
}

// —— 顶部宝宝信息区 ——
@Composable
private fun BabyHeader(baby: BabyEntity) {
    val c = LocalThemeColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = DT.pageMargin.dp),
    ) {
        Row(Modifier.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .shadow(elevation = DT.cardElevation.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Gradients.primary(c)),
                contentAlignment = Alignment.Center,
            ) {
                Text(baby.name.take(1), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    baby.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "月龄 ${babyAgeMonths(baby.birthDate)} 个月",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }
    }
}

// —— 最近一次评估概览卡 ——
@Composable
private fun AssessmentSummaryCard(latest: DevelopmentAssessment) {
    val c = LocalThemeColors.current
    val cardShape = RoundedCornerShape(DT.cardRadiusLg.dp)
    val dateText = remember(latest.assessDate) {
        latest.assessDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    Card(
        Modifier
            .padding(horizontal = DT.pageMargin.dp)
            .fillMaxWidth()
            .shadow(elevation = DT.cardElevation.dp, shape = cardShape),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = c.card),
    ) {
        Column(Modifier.padding(DT.cardInnerPadding.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("最近评估", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                Spacer(Modifier.weight(1f))
                Text(dateText, fontSize = 12.sp, color = c.textSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text("评估时月龄 ${latest.babyAgeMonths} 个月", fontSize = 12.sp, color = c.textSecondary)
            if (latest.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(latest.note, fontSize = 13.sp, color = c.textSecondary)
            }
        }
    }
}

// —— 5 项能力评估卡片 ——
@Composable
private fun AssessmentItemsSection(latest: DevelopmentAssessment) {
    val items: List<AssessmentItem> = ABILITIES.mapIndexed { i, meta ->
        AssessmentItem(
            title = meta.title,
            icon = meta.icon,
            score = meta.score(latest),
            description = abilityDescription(meta.title, meta.score(latest)),
        )
    }
    Column(Modifier.padding(horizontal = DT.pageMargin.dp)) {
        items.forEachIndexed { index, item ->
            AssessmentItemCard(item = item, useAccent = index % 2 == 1)
            if (index != items.lastIndex) Spacer(Modifier.height(DT.cardGapSm.dp))
        }
    }
}

@Composable
private fun AssessmentItemCard(item: AssessmentItem, useAccent: Boolean) {
    val c = LocalThemeColors.current
    val tint = if (useAccent) c.accent else c.primary
    val cardShape = RoundedCornerShape(DT.cardRadius.dp)
    val statusColor = scoreColor(item.score, c)
    Card(
        Modifier
            .fillMaxWidth()
            .shadow(elevation = DT.cardElevation.dp, shape = cardShape),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = c.card),
    ) {
        Row(
            Modifier.padding(DT.cardInnerPadding.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧图标背景
            Box(
                Modifier
                    .size(DT.iconBgSize.dp)
                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = item.title, tint = tint, modifier = Modifier.size(DT.iconSize.dp))
            }
            Spacer(Modifier.width(12.dp))
            // 中间标题 + 描述
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(item.description, fontSize = 12.sp, color = c.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            // 右侧状态标签
            ScoreTag(text = scoreLabel(item.score), color = statusColor)
        }
    }
}

@Composable
private fun ScoreTag(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(DT.chipRadius.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// —— 下次评估提示 ——
@Composable
private fun NextAssessmentHint(latest: DevelopmentAssessment?) {
    val c = LocalThemeColors.current
    val nextDateText = remember(latest?.assessDate) {
        val base = latest?.assessDate ?: LocalDateTime.now()
        base.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    Box(Modifier.padding(horizontal = DT.pageMargin.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DT.cardRadius.dp))
                .background(c.accent.copy(alpha = 0.10f))
                .padding(DT.cardInnerPadding.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = c.accent, modifier = Modifier.size(DT.iconSize.dp))
            Spacer(Modifier.width(10.dp))
            Text("下次评估时间：1 个月后（$nextDateText）", fontSize = 13.sp, color = c.textPrimary)
        }
    }
}

// —— 重新评估按钮（胶囊 + 主色渐变）——
@Composable
private fun ReassessButton(onClick: () -> Unit) {
    val c = LocalThemeColors.current
    val shape = RoundedCornerShape(DT.buttonRadius.dp)
    Box(
        Modifier
            .padding(horizontal = DT.pageMargin.dp)
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(Gradients.primary(c))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "重新评估",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// —— 评估表单（ModalBottomSheet：5 项打分 + 备注）——
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssessmentFormDialog(
    babyId: Int,
    babyAgeMonths: Int,
    onDismiss: () -> Unit,
    onSubmit: (DevelopmentAssessment) -> Unit,
) {
    val c = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 5 项评分初始值（默认 2=正常）
    val scores = remember {
        mutableStateListOf(2, 2, 2, 2, 2)
    }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.card,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DT.pageMargin.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("发育评估", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text("为宝宝 5 项能力打分（未观察/落后/正常/超前）", fontSize = 13.sp, color = c.textSecondary)
            Spacer(Modifier.height(16.dp))

            ABILITIES.forEachIndexed { index, meta ->
                ScoreSelector(
                    title = meta.title,
                    icon = meta.icon,
                    selected = scores[index],
                    onSelect = { scores[index] = it },
                    useAccent = index % 2 == 1,
                )
                if (index != ABILITIES.lastIndex) Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DT.inputRadius.dp),
                singleLine = false,
                minLines = 2,
                maxLines = 4,
            )

            Spacer(Modifier.height(20.dp))
            val shapeBtn = RoundedCornerShape(DT.buttonRadius.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shapeBtn)
                    .background(Gradients.primary(c))
                    .clickable {
                        onSubmit(
                            DevelopmentAssessment(
                                babyId = babyId,
                                assessDate = LocalDateTime.now(),
                                babyAgeMonths = babyAgeMonths,
                                grossMotor = scores[0],
                                fineMotor = scores[1],
                                language = scores[2],
                                social = scores[3],
                                cognitive = scores[4],
                                note = note.trim(),
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("保存评估", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScoreSelector(
    title: String,
    icon: ImageVector,
    selected: Int,
    onSelect: (Int) -> Unit,
    useAccent: Boolean,
) {
    val c = LocalThemeColors.current
    val tint = if (useAccent) c.accent else c.primary
    val options = listOf(0 to "未观察", 1 to "落后", 2 to "正常", 3 to "超前")
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(DT.iconBgSize.dp)
                    .clip(RoundedCornerShape(DT.iconBgRadius.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(DT.iconSize.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                val isSelected = selected == value
                val chipColor = if (isSelected) scoreColor(value, c) else c.divider
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(DT.chipRadius.dp))
                        .background(if (isSelected) chipColor else chipColor.copy(alpha = 0.25f))
                        .clickable { onSelect(value) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else c.textSecondary,
                    )
                }
            }
        }
    }
}
