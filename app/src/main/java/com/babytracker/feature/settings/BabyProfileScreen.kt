package com.babytracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babytracker.core.domain.model.*
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.navigation.Screen
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 宝宝信息页面 — 头像 + 基本信息 + 出生数据 + 当前生长数据。
 *
 * 从 SettingsScreen "宝宝信息"入口进入，展示当前宝宝的完整档案。
 * 右上角编辑按钮打开 BabyFormDialog 修改基本资料。
 */
@Composable
fun BabyProfileScreen(navController: NavController) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current

    val babyCtrl: BabyController = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val growthRepo: GrowthRepository = koinInject()

    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()

    // 生长记录
    val growths by growthRepo.watchByBaby(baby?.id ?: 0).collectAsState(initial = emptyList())
    val activeGrowths = growths.filter { it.deletedAt == null }

    // 各类最新记录
    val latestHeight = activeGrowths.filter { it.type == GrowthType.HEIGHT }.maxByOrNull { it.measuredAt }
    val latestWeight = activeGrowths.filter { it.type == GrowthType.WEIGHT }.maxByOrNull { it.measuredAt }
    val latestHead = activeGrowths.filter { it.type == GrowthType.HEAD }.maxByOrNull { it.measuredAt }

    // 编辑弹窗
    var showEdit by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (baby == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请先添加宝宝", color = c.textTertiary)
        }
        return
    }

    Scaffold(
        containerColor = c.pageBackground,
        topBar = {
            AppTopBar(
                title = "宝宝信息",
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ═══════════════════════════════════════════
            //  头像 + 姓名 + 性别 · 年龄
            // ═══════════════════════════════════════════
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.pageBackground)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 头像
                    Box(
                        Modifier.size(88.dp),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Box(
                            Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(c.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                baby.name.take(1),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.primary,
                            )
                        }
                        // 相机图标
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c.primary)
                                .border(2.dp, c.surface, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📷", fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 姓名
                    Text(
                        baby.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                    )

                    Spacer(Modifier.height(6.dp))

                    // 性别 · 年龄
                    val birthDate = try {
                        LocalDate.parse(baby.birthDate)
                    } catch (_: Exception) { null }
                    val ageText = birthDate?.let { DateUtils.monthAge(it) } ?: ""

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            if (baby.gender == "男") "👦" else "👧",
                            fontSize = 16.sp,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${if (baby.gender == "男") "男宝" else "女宝"}",
                            fontSize = 14.sp,
                            color = c.textSecondary,
                        )
                        if (ageText.isNotEmpty()) {
                            Text(" · ", fontSize = 14.sp, color = c.textTertiary)
                            Text(ageText, fontSize = 14.sp, color = c.textSecondary)
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            //  出生信息卡片
            // ═══════════════════════════════════════════
            SectionHeader("出生信息")

            AppCard(
                cornerRadius = shapes.medium,
                containerColor = c.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("出生日期", baby.birthDate)
                    BirthInfoDivider()
                    InfoRow("出生身高", baby.birthHeight?.let { "${it}cm" } ?: "未记录")
                    BirthInfoDivider()
                    InfoRow("出生体重", baby.birthWeight?.let { "${it}kg" } ?: "未记录")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════
            //  当前生长数据卡片
            // ═══════════════════════════════════════════
            SectionHeader("当前生长数据")

            AppCard(
                cornerRadius = shapes.medium,
                containerColor = c.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    GrowthValueRow(
                        label = "当前身高",
                        value = latestHeight?.let { "${it.value}cm" } ?: "未记录",
                        date = latestHeight?.measuredAt?.let { formatMeasuredAt(it) },
                    )
                    BirthInfoDivider()
                    GrowthValueRow(
                        label = "当前体重",
                        value = latestWeight?.let { "${it.value}kg" } ?: "未记录",
                        date = latestWeight?.measuredAt?.let { formatMeasuredAt(it) },
                    )
                    BirthInfoDivider()
                    GrowthValueRow(
                        label = "头围",
                        value = latestHead?.let { "${it.value}cm" } ?: "未记录",
                        date = latestHead?.measuredAt?.let { formatMeasuredAt(it) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════════
            //  操作入口
            // ═══════════════════════════════════════════
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { navController.navigate(Screen.BabyManagement.route) }) {
                    Text("管理全部宝宝", color = c.textSecondary, fontSize = 13.sp)
                }
                Text("·", color = c.textTertiary, fontSize = 13.sp)
                TextButton(onClick = { showEdit = true }) {
                    Text("编辑资料", color = c.primary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── 编辑弹窗 ──
    if (showEdit) {
        BabyFormDialog(
            baby = baby,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                coroutineScope.launch {
                    babyRepo.update(updated)
                }
                showEdit = false
            },
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  子组件
// ═══════════════════════════════════════════════════════════

/** 区块标题 */
@Composable
private fun SectionHeader(title: String) {
    val c = LocalAppColors.current
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = c.textSecondary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

/** 信息行：标签（左）+ 值（右），用于出生信息 */
@Composable
private fun InfoRow(label: String, value: String) {
    val c = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = c.textPrimary)
        Text(
            value,
            fontSize = 14.sp,
            color = c.textSecondary,
            textAlign = TextAlign.End,
        )
    }
}

/** 生长数据行：标签（左）+ 值 + 日期（右，两行） */
@Composable
private fun GrowthValueRow(label: String, value: String, date: String?) {
    val c = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = c.textPrimary)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            if (date != null) {
                Text(date, fontSize = 11.sp, color = c.textTertiary)
            }
        }
    }
}

/** 分割线 */
@Composable
private fun BirthInfoDivider() {
    val c = LocalAppColors.current
    HorizontalDivider(
        color = c.divider,
        thickness = 0.5.dp,
    )
}

/** 格式化测量日期为简短显示 */
private fun formatMeasuredAt(isoString: String): String {
    return try {
        val dt = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (_: Exception) {
        try {
            val d = LocalDate.parse(isoString.take(10))
            d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (_: Exception) { "" }
    }
}
