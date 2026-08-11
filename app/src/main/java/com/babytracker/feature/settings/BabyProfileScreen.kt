package com.babytracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.navigation.Navigator
import com.babytracker.core.domain.model.*
import com.babytracker.core.util.BabyController
import com.babytracker.core.util.DateUtils
import com.babytracker.core.data.repository.BabyRepository
import com.babytracker.core.data.repository.GrowthRepository
import io.elyon.kmp.basic.ButtonDefaults
import io.elyon.kmp.basic.Card
import io.elyon.kmp.basic.CardDefaults
import io.elyon.kmp.basic.HorizontalDivider
import io.elyon.kmp.basic.Icon
import io.elyon.kmp.basic.IconButton
import io.elyon.kmp.basic.Scaffold
import io.elyon.kmp.basic.Text
import io.elyon.kmp.basic.TextButton
import io.elyon.kmp.basic.TopAppBar
import io.elyon.kmp.theme.ElyonTheme
import com.babytracker.navigation.Route
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BabyProfileScreen(navigator: Navigator) {
    val c = ElyonTheme.colorScheme
    val typography = ElyonTheme.textStyles

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
            Text("请先添加宝宝", color = c.onSurfaceVariantSummary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "宝宝信息",
                color = c.primaryContainer,
                titleColor = c.onPrimaryContainer,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
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
                    .background(c.background)
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                style = typography.headline1,
                                color = c.primary,
                            )
                        }
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c.primary)
                                .border(2.dp, c.surface, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        baby.name,
                        style = typography.headline2,
                        color = c.onSurface,
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
                            "${if (baby.gender == "男") "男宝" else "女宝"}",
                            style = typography.body1,
                            color = c.onSurfaceVariantSummary,
                        )
                        if (ageText.isNotEmpty()) {
                            Text(" · ", style = typography.body1, color = c.onSurfaceVariantSummary)
                            Text(ageText, style = typography.body1, color = c.onSurfaceVariantSummary)
                        }
                    }
                }
            }

            SectionHeader("出生信息")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp,
                colors = CardDefaults.defaultColors(color = c.surface),
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

            SectionHeader("当前生长数据")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 16.dp,
                colors = CardDefaults.defaultColors(color = c.surface),
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

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = "管理全部宝宝",
                    onClick = { navigator.navigate(Route.BabyManagement) },
                    colors = ButtonDefaults.textButtonColors(
                        textColor = c.onSurfaceVariantSummary,
                    ),
                )
                Text("·", color = c.onSurfaceVariantSummary, style = typography.body2)
                TextButton(
                    text = "编辑资料",
                    onClick = { showEdit = true },
                )
            }

            Spacer(Modifier.height(32.dp))
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
    Text(
        title,
        style = ElyonTheme.textStyles.body2,
        color = ElyonTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    val c = ElyonTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = ElyonTheme.textStyles.body1, color = c.onSurface)
        Text(
            value,
            style = ElyonTheme.textStyles.body1,
            color = c.onSurfaceVariantSummary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun GrowthValueRow(label: String, value: String, date: String?) {
    val c = ElyonTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = ElyonTheme.textStyles.body1, color = c.onSurface)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = ElyonTheme.textStyles.body1, color = c.onSurface)
            if (date != null) {
                Text(date, style = ElyonTheme.textStyles.footnote2, color = c.onSurfaceVariantSummary)
            }
        }
    }
}

@Composable
private fun BirthInfoDivider() {
    HorizontalDivider(
        color = ElyonTheme.colorScheme.dividerLine,
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
