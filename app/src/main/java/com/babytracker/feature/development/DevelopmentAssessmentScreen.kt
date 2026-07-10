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

// —— 5 项能力元数据（emoji + 配色）——
private data class AbilityMeta(
    val title: String,
    val emoji: String,
    val bgColor: Color,
    val icon: ImageVector, // 用于评估表单
    val score: (DevelopmentAssessment) -> Int,
)

private val ABILITIES: List<AbilityMeta> = listOf(
    AbilityMeta("大运动", "🏃", Color(0xFFFF8C00), Icons.AutoMirrored.Filled.DirectionsRun) { it.grossMotor },
    AbilityMeta("精细动作", "✋", Color(0xFFE91E63), Icons.Default.PanTool) { it.fineMotor },
    AbilityMeta("语言能力", "💬", Color(0xFF2196F3), Icons.Default.RecordVoiceOver) { it.language },
    AbilityMeta("社交能力", "🤝", Color(0xFF4CAF50), Icons.Default.Group) { it.social },
    AbilityMeta("认知能力", "🧠", Color(0xFF9C27B0), Icons.Default.Psychology) { it.cognitive },
)

// 评分 → UI 文案
private fun scoreLabel(score: Int): String = when (score) {
    0 -> "未观察"
    1 -> "落后"
    2 -> "正常"
    3 -> "超前"
    else -> "--"
}

// 评分 → 颜色
private fun scoreColor(score: Int): Color = when (score) {
    0 -> Color(0xFF9E9E9E)
    1 -> Color(0xFFFF9800)
    2 -> Color(0xFF4CAF50)
    3 -> Color(0xFF5B7CFF)
    else -> Color(0xFF9E9E9E)
}

/** 各能力在不同评分下的描述（仿自然语言）。 */
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

/** 由 [Baby.birthDate] 计算详细年龄 "X岁X个月X天"。 */
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

/** 由 [Baby.birthDate] 计算当前月龄（Int 月份）。 */
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
                emoji = "👶",
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
            // —— 顶部宝宝信息区（与首页一致的卡通风格）——
            BabyHeader(baby)

            Spacer(Modifier.height(16.dp))

            val latest = state.latest

            if (latest == null) {
                EmptyState(
                    emoji = "📝",
                    title = "还没有发育评估记录",
                    subtitle = "评估宝宝 5 项能力发展，了解成长进度",
                    actionText = "开始评估",
                    onAction = { showForm = true },
                )
            } else {
                // 5 项能力卡片
                AssessmentItemsSection(latest)

                Spacer(Modifier.height(16.dp))

                // 底部：下次评估时间 + 重新评估按钮
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

// —— 顶部宝宝信息区（与首页一致：卡通 👶 头像 + 渐变背景）——
@Composable
private fun BabyHeader(baby: Baby) {
    val c = LocalAppColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Gradients.pageHeader(c))
            .padding(horizontal = 16.dp),
    ) {
        Row(
            Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 卡通宝宝头像
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("👶", fontSize = 36.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    baby.name,
                    style = LocalAppTypography.current.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    babyAgeDetail(baby.birthDate),
                    style = LocalAppTypography.current.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }
    }
}

// —— 5 项能力评估卡片（彩色圆形 emoji + 标题行 + 描述）——
@Composable
private fun AssessmentItemsSection(latest: DevelopmentAssessment) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        ABILITIES.forEachIndexed { index, meta ->
            AssessmentItemCard(
                emoji = meta.emoji,
                bgColor = meta.bgColor,
                title = meta.title,
                score = meta.score(latest),
                description = abilityDescription(meta.title, meta.score(latest)),
            )
            if (index != ABILITIES.lastIndex) Spacer(Modifier.height(8.dp))
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
    val statusColor = scoreColor(score)
    AppCard(
        cornerRadius = 12.dp,
        elevation = 2.dp,
        containerColor = c.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 左侧：彩色圆形 emoji
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                // 中间：标题 + 状态标签
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(scoreLabel(score), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }
                Spacer(Modifier.width(4.dp))
                // 右侧箭头
                Text("›", fontSize = 20.sp, color = c.textTertiary)
            }
            Spacer(Modifier.height(8.dp))
            // 描述文字
            Text(description, fontSize = 13.sp, color = c.textSecondary, lineHeight = 20.sp)
        }
    }
}

// —— 底部操作栏：下次评估时间（左）+ 重新评估按钮（右）——
@Composable
private fun BottomActionRow(latest: DevelopmentAssessment, onReassess: () -> Unit) {
    val c = LocalAppColors.current
    val nextDateText = remember(latest.assessDate) {
        latest.assessDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧提示
        Column(Modifier.weight(1f)) {
            Text("下次评估时间", fontSize = 13.sp, color = c.textSecondary)
            Spacer(Modifier.height(2.dp))
            Text("1个月后（$nextDateText）", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
        }
        Spacer(Modifier.width(16.dp))
        // 右侧重新评估按钮
        Box(
            Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Gradients.primary(c))
                .clickable(onClick = onReassess)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "重新评估",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
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
    val c = LocalAppColors.current
    // 5 项评分初始值（默认 2=正常）
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
                .padding(horizontal = 16.dp)
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
            AppInput(
                value = note,
                onValueChange = { note = it },
                label = "备注（可选）",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))
            val shapeBtn = RoundedCornerShape(12.dp)
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
    val c = LocalAppColors.current
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                Text(abilityDescription(title, selected), fontSize = 11.sp, color = c.textSecondary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                val isSelected = selected == value
                val chipColor = scoreColor(value)
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) chipColor else chipColor.copy(alpha = 0.2f))
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
