package com.babytracker.feature.development

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.domain.model.Baby
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.AppColors
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.util.BabyController
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.domain.model.AssessmentItem
import com.babytracker.core.domain.model.DevelopmentAssessment
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter

private data class AbilityMeta(
    val title: String,
    val emoji: String,
    val bgColor: Color,
    val icon: ImageVector,
    val score: (DevelopmentAssessment) -> Int,
)

@Composable
private fun abilities(): List<AbilityMeta> {
    val c = LocalAppColors.current
    return listOf(
        AbilityMeta("大运动", "\uD83C\uDFC3", c.warning, Icons.AutoMirrored.Filled.DirectionsRun) { it.grossMotor },
        AbilityMeta("精细动作", "\u270B", c.danger, Icons.Default.PanTool) { it.fineMotor },
        AbilityMeta("语言能力", "\uD83D\uDCAC", c.primary, Icons.Default.RecordVoiceOver) { it.language },
        AbilityMeta("社交能力", "\uD83E\uDD1D", c.success, Icons.Default.Group) { it.social },
        AbilityMeta("认知能力", "\uD83E\uDDE0", c.secondary, Icons.Default.Psychology) { it.cognitive },
    )
}

private fun scoreLabel(score: Int): String = when (score) {
    0 -> "未观察"
    1 -> "落后"
    2 -> "正常"
    3 -> "超前"
    else -> "--"
}

@Composable
private fun scoreColor(score: Int): Color {
    val c = LocalAppColors.current
    return when (score) {
        0 -> c.textDisabled
        1 -> c.warning
        2 -> c.success
        3 -> c.primary
        else -> c.textDisabled
    }
}

private fun abilityDescription(title: String, score: Int): String = when (title) {
    "大运动" -> when (score) {
        0 -> "尚未观察大运动表现"
        1 -> "大运动略低于月龄水平，建议关注"
        2 -> "能独立站稳，偶尔能跑几步"
        3 -> "跑跳自如，运动能力突出"
        else -> "--"
    }
    "精细动作" -> when (score) {
        0 -> "尚未观察精细动作表现"
        1 -> "精细动作略低于月龄水平，建议关注"
        2 -> "会用小勺吃饭"
        3 -> "精细操作能力超前，灵活协调"
        else -> "--"
    }
    "语言能力" -> when (score) {
        0 -> "尚未观察语言能力表现"
        1 -> "语言发展略低于月龄水平，建议关注"
        2 -> "会说简单词语"
        3 -> "语言表达丰富，词汇量超前"
        else -> "--"
    }
    "社交能力" -> when (score) {
        0 -> "尚未观察社交能力表现"
        1 -> "社交互动略低于月龄水平，建议关注"
        2 -> "会与人互动、分享玩具"
        3 -> "社交能力强，善于互动分享"
        else -> "--"
    }
    "认知能力" -> when (score) {
        0 -> "尚未观察认知能力表现"
        1 -> "认知发展略低于月龄水平，建议关注"
        2 -> "能认识常见物品"
        3 -> "认知超前，学习能力强"
        else -> "--"
    }
    else -> when (score) {
        0 -> "尚未观察 $title 表现"
        1 -> "$title 略低于月龄水平，建议关注"
        2 -> "$title 符合月龄水平"
        3 -> "$title 超前于月龄，表现突出"
        else -> "--"
    }
}

private fun babyAgeDetail(birthDate: String): String {
    return try {
        val birth = LocalDate.parse(birthDate.take(10))
        val p = Period.between(birth, LocalDate.now())
        val parts = mutableListOf<String>()
        if (p.years > 0) parts.add("${p.years}岁")
        if (p.months > 0) parts.add("${p.months}个月")
        parts.add("${p.days}天")
        parts.joinToString("")
    } catch (_: Exception) {
        "未设置"
    }
}

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
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
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

    AppScaffold(
        topBar = {
            AppTopBar(title = "发育评估", onBack = { navController.popBackStack() })
        },
    ) { padding ->
        if (baby == null) {
            EmptyState(
                emoji = "\uD83D\uDC76",
                title = "还没有添加宝宝",
                subtitle = "请先在设置中添加宝宝信息",
                modifier = Modifier.padding(padding),
            )
            return@AppScaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(c.pageBackground),
        ) {
            BabyHeader(baby)

            Spacer(Modifier.height(spacing.md))

            val latest = state.latest

            if (latest == null) {
                EmptyState(
                    emoji = "\uD83D\uDCDD",
                    title = "还没有发育评估记录",
                    subtitle = "评估宝宝 5 项能力发展，了解成长进度",
                    actionText = "开始评估",
                    onAction = { showForm = true },
                )
            } else {
                AssessmentItemsSection(latest)

                Spacer(Modifier.height(spacing.md))

                BottomActionRow(latest = latest, onReassess = { showForm = true })
            }

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

@Composable
private fun BabyHeader(baby: Baby) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = spacing.md),
    ) {
        Row(
            Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("\uD83D\uDC76", style = LocalAppTypography.current.displayLarge)
            }
            Spacer(Modifier.width(spacing.md))
            Column {
                Text(
                    baby.name,
                    style = LocalAppTypography.current.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    babyAgeDetail(baby.birthDate),
                    style = LocalAppTypography.current.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun AssessmentItemsSection(latest: DevelopmentAssessment) {
    val spacing = LocalAppSpacing.current
    Column(Modifier.padding(horizontal = spacing.md)) {
        abilities().forEachIndexed { index, meta ->
            AssessmentItemCard(
                emoji = meta.emoji,
                bgColor = meta.bgColor,
                title = meta.title,
                score = meta.score(latest),
                description = abilityDescription(meta.title, meta.score(latest)),
            )
            if (index != abilities().lastIndex) Spacer(Modifier.height(spacing.sm))
        }
    }
}

@Composable
private fun AssessmentItemCard(
    emoji: String,
    bgColor: Color,
    title: String,
    score: Int,
    description: String,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val statusColor = scoreColor(score)
    AppCard(
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = spacing.md, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, style = LocalAppTypography.current.titleLarge)
                }
                Spacer(Modifier.width(12.dp))
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = LocalAppTypography.current.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(shapes.full))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(scoreLabel(score), style = LocalAppTypography.current.labelMedium, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }
                Spacer(Modifier.width(spacing.xs))
                Text("\u203A", style = LocalAppTypography.current.titleLarge, color = c.textTertiary)
            }
            Spacer(Modifier.height(spacing.sm))
            Text(description, style = LocalAppTypography.current.bodyMedium.copy(lineHeight = 20.sp), color = c.textSecondary)
        }
    }
}

@Composable
private fun BottomActionRow(latest: DevelopmentAssessment, onReassess: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val nextDateText = remember(latest.assessDate) {
        latest.assessDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    Row(
        Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("下次评估时间", style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
            Spacer(Modifier.height(spacing.xxs))
            Text("1个月后（$nextDateText）", style = LocalAppTypography.current.bodyLarge.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
        }
        Spacer(Modifier.width(spacing.md))
        Box(
            Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(shapes.full))
                .background(Gradients.primary(c))
                .clickable(onClick = onReassess)
                .padding(horizontal = spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "重新评估",
                color = c.onPrimary,
                style = LocalAppTypography.current.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssessmentFormDialog(
    babyId: Int,
    babyAgeMonths: Int,
    onDismiss: () -> Unit,
    onSubmit: (DevelopmentAssessment) -> Unit,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val scores = remember {
        mutableStateListOf(2, 2, 2, 2, 2)
    }
    var note by remember { mutableStateOf("") }

    AppBottomSheet(
        show = true,
        onDismiss = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("发育评估", style = LocalAppTypography.current.titleLarge, fontWeight = FontWeight.Bold, color = c.textPrimary)
            Spacer(Modifier.height(spacing.xs))
            Text("为宝宝 5 项能力打分（未观察/落后/正常/超前）", style = LocalAppTypography.current.bodyMedium, color = c.textSecondary)
            Spacer(Modifier.height(spacing.md))

            abilities().forEachIndexed { index, meta ->
                ScoreSelector(
                    title = meta.title,
                    icon = meta.icon,
                    selected = scores[index],
                    onSelect = { scores[index] = it },
                    useAccent = index % 2 == 1,
                )
                if (index != abilities().lastIndex) Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(spacing.md))
            AppInput(
                value = note,
                onValueChange = { note = it },
                label = "备注（可选）",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
            val shapeBtn = RoundedCornerShape(shapes.large)
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
                Text("保存评估", color = c.onPrimary, style = LocalAppTypography.current.titleMedium, fontWeight = FontWeight.SemiBold)
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
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val tint = if (useAccent) c.warning else c.primary
    val options = listOf(
        0 to "未观察",
        1 to "落后",
        2 to "正常",
        3 to "超前",
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = LocalAppTypography.current.bodyLarge, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Text(abilityDescription(title, selected), style = LocalAppTypography.current.labelMedium, color = c.textSecondary)
            }
        }
        Spacer(Modifier.height(spacing.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            options.forEach { (value, label) ->
                val isSelected = selected == value
                val chipColor = scoreColor(value)
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(shapes.medium))
                        .background(if (isSelected) chipColor else chipColor.copy(alpha = 0.2f))
                        .clickable { onSelect(value) }
                        .padding(vertical = spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = LocalAppTypography.current.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else c.textSecondary,
                    )
                }
            }
        }
    }
}
