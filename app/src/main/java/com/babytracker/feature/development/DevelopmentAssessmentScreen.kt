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
import com.babytracker.core.domain.model.Baby
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.ScoreSelectorOptionColors
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.scoreselector.AppScoreSelector
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.sheet.AppBottomSheet
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.core.domain.model.AssessmentItem
import com.babytracker.core.domain.model.DevelopmentAssessment
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.topbar.AppTopBar
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
fun DevelopmentAssessmentScreen(
    state: DevelopmentAssessmentUiState,
    baby: Baby?,
    onBack: () -> Unit,
    onSubmitAssessment: (DevelopmentAssessment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    var showForm by remember { mutableStateOf(false) }

    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = "发育评估", onBack = onBack)
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
            // 宝宝摘要行（2.1 C3：替代自建渐变头部，与其他页面头部体系统一）——G3 收编为调用点内联组合
            AppCard(modifier = Modifier.padding(horizontal = spacing.md).fillMaxWidth()) {
                Row(
                    Modifier.padding(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(c.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("\uD83D\uDC76", style = LocalAppTypography.current.headlineSmall)
                    }
                    Spacer(Modifier.width(spacing.md))
                    Column {
                        Text(
                            baby.name,
                            style = LocalAppTypography.current.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        )
                        Spacer(Modifier.height(spacing.xxs))
                        Text(
                            babyAgeDetail(baby.birthDate),
                            style = LocalAppTypography.current.bodyMedium,
                            color = c.textSecondary,
                        )
                    }
                }
            }

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
                onSubmitAssessment(assessment)
                showForm = false
            },
        )
    }
}

@Composable
private fun AssessmentItemsSection(latest: DevelopmentAssessment) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    Column(Modifier.padding(horizontal = spacing.md)) {
        abilities().forEachIndexed { index, meta ->
            // 能力项行：徽章 + 标题 + 评分胶囊 + 描述，G3 收编为循环内 AppCard 组合
            val score = meta.score(latest)
            val statusColor = scoreColor(score)
            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(horizontal = spacing.md, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppEmojiBadge(emoji = meta.emoji, tint = meta.bgColor)
                        Spacer(Modifier.width(12.dp))
                        Row(
                            Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(meta.title, style = LocalAppTypography.current.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                            Spacer(Modifier.width(10.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(shapes.full))
                                    .background(AppColorScale.fromSeed(statusColor).tintContainer(c))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(scoreLabel(score), style = LocalAppTypography.current.labelMedium, fontWeight = FontWeight.SemiBold, // 胶囊文字取强调档，保证浅底上的对比度
                                color = AppColorScale.fromSeed(statusColor).accentContent(c))
                            }
                        }
                        Spacer(Modifier.width(spacing.xs))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(spacing.sm))
                    Text(abilityDescription(meta.title, score), style = LocalAppTypography.current.bodyMedium.copy(lineHeight = 20.sp), color = c.textSecondary)
                }
            }
            if (index != abilities().lastIndex) Spacer(Modifier.height(spacing.sm))
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

// 评分选择条已收编为设计系统 AppScoreSelector；分数 Int ↔ key 映射与四档中文 label
// 为本页存量私有映射，保留在 feature 层不动文案体系。
private val SCORE_OPTIONS: List<Pair<String, String>> = listOf(
    "0" to "未观察",
    "1" to "落后",
    "2" to "正常",
    "3" to "超前",
)

@Composable
private fun ScoreSelector(
    title: String,
    icon: ImageVector,
    selected: Int,
    onSelect: (Int) -> Unit,
    useAccent: Boolean,
) {
    val c = LocalAppColors.current
    AppScoreSelector(
        title = title,
        icon = icon,
        options = SCORE_OPTIONS,
        selectedKey = selected.toString(),
        onSelect = { key -> onSelect(key.toInt()) },
        useAccent = useAccent,
        subtitle = abilityDescription(title, selected),
        // 四档各自语义色（未观察灰 / 落后琥珀 / 正常绿 / 超前蓝）——保真源码逐档配色
        optionColors = mapOf(
            "0" to ScoreSelectorOptionColors(c.textDisabled, c.textPrimary),
            "1" to ScoreSelectorOptionColors(c.warning, c.onWarning),
            "2" to ScoreSelectorOptionColors(c.success, c.onSuccess),
            "3" to ScoreSelectorOptionColors(c.primary, c.onPrimary),
        ),
    )
}
