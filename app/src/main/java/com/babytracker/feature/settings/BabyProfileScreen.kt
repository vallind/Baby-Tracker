package com.babytracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babytracker.core.domain.model.*
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.GrowthRepository
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.divider.AppDivider
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.designsystem.theme.LocalAppElevation
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.components.scaffold.AppScaffold
import com.babytracker.navigation.BabyManagement
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BabyProfileScreen(navController: NavController) {
    val c = LocalAppColors.current
    val typography = LocalAppTypography.current
    val spacing = LocalAppSpacing.current
    val elev = LocalAppElevation.current

    val babyCtrl: BabyController = koinInject()
    val babyRepo: BabyRepository = koinInject()
    val growthRepo: GrowthRepository = koinInject()

    val babies by babyRepo.watchAll().collectAsState(initial = emptyList())
    val baby = babies.find { it.id == babyCtrl.currentBabyId } ?: babies.firstOrNull()

    val growths by growthRepo.watchByBaby(baby?.id ?: 0).collectAsState(initial = emptyList())
    val activeGrowths = growths.filter { it.deletedAt == null }

    val latestHeight = activeGrowths.filter { it.type == GrowthType.HEIGHT }.maxByOrNull { it.measuredAt }
    val latestWeight = activeGrowths.filter { it.type == GrowthType.WEIGHT }.maxByOrNull { it.measuredAt }
    val latestHead = activeGrowths.filter { it.type == GrowthType.HEAD }.maxByOrNull { it.measuredAt }

    var showEdit by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (baby == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请先添加宝宝", color = c.textTertiary)
        }
        return
    }

    AppScaffold(
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(c.pageBackground)
                    .padding(vertical = spacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 2.1：删除头像上的假相机徽章（无换头像功能，避免假入口）
                    Box(
                        Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(AppColorScale.fromSeed(c.primary).tintContainer(c)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            baby.name.take(1),
                            style = typography.displayLarge,
                            color = c.primary,
                        )
                    }

                    Spacer(Modifier.height(spacing.md))

                    Text(
                        baby.name,
                        style = typography.headlineLarge,
                        color = c.textPrimary,
                    )

                    Spacer(Modifier.height(6.dp))

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
                            style = typography.titleMedium,
                        )
                        Spacer(Modifier.width(spacing.xs))
                        Text(
                            "${if (baby.gender == "男") "男宝" else "女宝"}",
                            style = typography.bodyLarge,
                            color = c.textSecondary,
                        )
                        if (ageText.isNotEmpty()) {
                            Text(" · ", style = typography.bodyLarge, color = c.textTertiary)
                            Text(ageText, style = typography.bodyLarge, color = c.textSecondary)
                        }
                    }
                }
            }

            SectionHeader("出生信息")

            AppCard(
                containerColor = c.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md),
                elevation = elev.level2,
            ) {
                Column(Modifier.padding(spacing.md)) {
                    InfoRow("出生日期", baby.birthDate)
                    BirthInfoDivider()
                    InfoRow("出生身高", baby.birthHeight?.let { "${it}cm" } ?: "未记录")
                    BirthInfoDivider()
                    InfoRow("出生体重", baby.birthWeight?.let { "${it}kg" } ?: "未记录")
                }
            }

            Spacer(Modifier.height(spacing.lg))

            SectionHeader("当前生长数据")

            AppCard(
                containerColor = c.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md),
                elevation = elev.level2,
            ) {
                Column(Modifier.padding(spacing.md)) {
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

            Spacer(Modifier.height(spacing.lg))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    variant = ButtonVariant.Text,
                    onClick = { navController.navigate(BabyManagement) },
                    label = "管理全部宝宝",
                    contentColor = c.textSecondary,
                )
                Text("·", color = c.textTertiary, style = typography.bodyMedium)
                AppButton(
                    variant = ButtonVariant.Text,
                    onClick = { showEdit = true },
                    label = "编辑资料",
                )
            }

            Spacer(Modifier.height(spacing.xl))
        }
    }

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

@Composable
private fun SectionHeader(title: String) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    Text(
        title,
        style = LocalAppTypography.current.bodyMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(start = spacing.md, bottom = spacing.sm),
    )
}

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
        Text(label, style = LocalAppTypography.current.bodyLarge, color = c.textPrimary)
        Text(
            value,
            style = LocalAppTypography.current.bodyLarge,
            color = c.textSecondary,
            textAlign = TextAlign.End,
        )
    }
}

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
        Text(label, style = LocalAppTypography.current.bodyLarge, color = c.textPrimary)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = LocalAppTypography.current.bodyLarge, color = c.textPrimary)
            if (date != null) {
                Text(date, style = LocalAppTypography.current.labelSmall, color = c.textTertiary)
            }
        }
    }
}

@Composable
private fun BirthInfoDivider() {
    val c = LocalAppColors.current
    AppDivider(
        color = c.divider,
        thickness = 0.5.dp,
    )
}

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
