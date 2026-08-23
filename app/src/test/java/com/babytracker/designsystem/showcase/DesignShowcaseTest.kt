package com.babytracker.designsystem.showcase

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.core.domain.model.BreastSide
import com.babytracker.core.domain.model.Diaper
import com.babytracker.core.domain.model.DiaperType
import com.babytracker.core.domain.model.Feeding
import com.babytracker.core.domain.model.FeedingType
import com.babytracker.core.domain.model.Sleep
import com.babytracker.core.domain.model.SleepType
import com.babytracker.designsystem.components.EmptyState
import com.babytracker.designsystem.components.SegmentedControl
import com.babytracker.designsystem.components.badge.AppEmojiBadge
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.components.button.ButtonVariant
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.components.chip.AppChip
import com.babytracker.designsystem.components.chip.AppFilterChip
import com.babytracker.designsystem.components.input.AppInput
import com.babytracker.designsystem.components.recordcard.RecordCard
import com.babytracker.designsystem.components.statcell.StatCell
import com.babytracker.designsystem.components.summarycard.AppSummaryCard
import com.babytracker.designsystem.components.switchcontrol.AppSwitch
import com.babytracker.designsystem.components.topbar.AppTopBar
import com.babytracker.designsystem.theme.AppTheme
import com.babytracker.designsystem.theme.AppTypography
import com.babytracker.designsystem.theme.BabyTrackerTheme
import com.babytracker.designsystem.theme.Gradients
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppTypography
import com.babytracker.designsystem.theme.accentContent
import com.babytracker.designsystem.theme.tintContainer
import com.babytracker.feature.home.RecentRecordsSection
import com.babytracker.feature.home.TodayOverviewCard
import com.babytracker.feature.stats.MiniBarChart
import com.babytracker.feature.stats.MiniLineChart
import org.junit.Rule
import org.junit.Test

// ═══════════════════════════════════════════════════════════════
//  设计系统截图 Showcase — 用 Paparazzi 渲染真实主题与组件，
//  供视觉走查（recordPaparazzi 后查看 snapshots/images）。
//
//  中文字体：Noto Sans SC 三字重子集（见 res/font，约 700KB）放 main 资源，
//  真实引用 com.babytracker.R.font；通过嵌套 MaterialTheme/LocalAppTypography 覆盖字体。
// ═══════════════════════════════════════════════════════════════

private val showcaseFontFamily = FontFamily(
    Font(com.babytracker.R.font.noto_sc_regular, FontWeight.Normal),
    Font(com.babytracker.R.font.noto_sc_medium, FontWeight.Medium),
    Font(com.babytracker.R.font.noto_sc_bold, FontWeight.Bold),
)

@Composable
private fun appTypographyWithFont(): AppTypography {
    val base = LocalAppTypography.current
    fun withFont(s: TextStyle) = s.copy(fontFamily = showcaseFontFamily)
    return AppTypography(
        displayLarge = withFont(base.displayLarge),
        headlineLarge = withFont(base.headlineLarge),
        headlineMedium = withFont(base.headlineMedium),
        headlineSmall = withFont(base.headlineSmall),
        titleLarge = withFont(base.titleLarge),
        titleMedium = withFont(base.titleMedium),
        titleSmall = withFont(base.titleSmall),
        bodyLarge = withFont(base.bodyLarge),
        bodyMedium = withFont(base.bodyMedium),
        bodySmall = withFont(base.bodySmall),
        labelMedium = withFont(base.labelMedium),
        labelSmall = withFont(base.labelSmall),
    )
}

@Composable
private fun m3TypographyWithFont(): Typography {
    val base = MaterialTheme.typography
    fun withFont(s: TextStyle) = s.copy(fontFamily = showcaseFontFamily)
    return Typography(
        displayLarge = withFont(base.displayLarge),
        headlineLarge = withFont(base.headlineLarge),
        headlineMedium = withFont(base.headlineMedium),
        headlineSmall = withFont(base.headlineSmall),
        titleLarge = withFont(base.titleLarge),
        titleMedium = withFont(base.titleMedium),
        titleSmall = withFont(base.titleSmall),
        bodyLarge = withFont(base.bodyLarge),
        bodyMedium = withFont(base.bodyMedium),
        bodySmall = withFont(base.bodySmall),
        labelMedium = withFont(base.labelMedium),
        labelLarge = withFont(base.labelLarge),
        labelSmall = withFont(base.labelSmall),
    )
}

/** Showcase 宿主：真实主题 + 中文字体覆盖（仅截图测试使用） */
@Composable
private fun ShowcaseHost(dark: Boolean = false, content: @Composable () -> Unit) {
    BabyTrackerTheme(theme = if (dark) AppTheme.night else AppTheme.pure) {
        CompositionLocalProvider(LocalAppTypography provides appTypographyWithFont()) {
            val m3 = m3TypographyWithFont()
            MaterialTheme(typography = m3) { content() }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
    ) {
        androidx.compose.material3.Text(
            text,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = LocalAppColors.current.textSecondary,
        )
    }
}

class DesignShowcaseTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sampleFeedings = listOf(
        Feeding(babyId = 1, type = FeedingType.BREAST, durationMin = 15, breastSide = BreastSide.BOTH, amountMl = 90, timestamp = "2026-08-23T08:15:00"),
        Feeding(babyId = 1, type = FeedingType.FORMULA, amountMl = 120, brand = "爱他美", timestamp = "2026-08-23T11:30:00"),
        Feeding(babyId = 1, type = FeedingType.FOOD, foodName = "南瓜泥", amountG = 60, timestamp = "2026-08-23T13:00:00"),
    )
    private val sampleSleeps = listOf(
        Sleep(babyId = 1, type = SleepType.NIGHT, startTime = "2026-08-22T21:00:00", endTime = "2026-08-23T07:00:00"),
        Sleep(babyId = 1, type = SleepType.NAP, startTime = "2026-08-23T09:30:00", endTime = "2026-08-23T10:15:00"),
    )
    private val sampleDiapers = listOf(
        Diaper(babyId = 1, type = DiaperType.WET, timestamp = "2026-08-23T09:10:00"),
        Diaper(babyId = 1, type = DiaperType.POOP, timestamp = "2026-08-23T14:40:00"),
    )

    @Test
    fun home_light() {
        paparazzi.snapshot(name = "home_light") {
            ShowcaseHost(dark = false) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(LocalAppColors.current.pageBackground)
                        .verticalScroll(rememberScrollState()),
                ) {
                    AppTopBar(title = "首页", showBack = false)
                    TodayOverviewCard(
                        feedCount = 6, breastFeedCount = 3, formulaCount = 3, formulaTotalMl = 360,
                        sleepHours = "9.5小时", diaperCount = 5,
                    )
                    Spacer(Modifier.height(12.dp))
                    RecentRecordsSection(items = sampleFeedings + sampleSleeps + sampleDiapers)
                    Spacer(Modifier.height(12.dp))
                    AppSummaryCard(
                        emoji = "🌙", title = "夜间睡眠", value = "10小时",
                        subtitle = "21:00-07:00", gradient = Gradients.sleep(LocalAppColors.current),
                    )
                }
            }
        }
    }

    @Test
    fun components_light() {
        paparazzi.snapshot(name = "components_light") {
            ShowcaseHost(dark = false) {
                val c = LocalAppColors.current
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(c.pageBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("按钮（胶囊）")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppButton(label = "主要按钮", onClick = {}, modifier = Modifier.weight(1f))
                        AppButton(label = "次要", variant = ButtonVariant.Secondary, onClick = {}, modifier = Modifier.weight(1f))
                    }
                    SectionTitle("统计格")
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            StatCell(value = "8", label = "喂养次数", unit = "次", modifier = Modifier.weight(1f))
                            StatCell(value = "320", label = "配方奶", unit = "ml", modifier = Modifier.weight(1f))
                            StatCell(value = "9.5h", label = "睡眠时长", modifier = Modifier.weight(1f))
                        }
                    }
                    SectionTitle("输入框（填充式）")
                    AppInput(value = "", onValueChange = {}, label = "宝宝姓名", placeholder = "请输入姓名", modifier = Modifier.fillMaxWidth())
                    AppInput(value = "120", onValueChange = {}, label = "奶量 (ml)", leadingIcon = { androidx.compose.material3.Text("🍼") }, modifier = Modifier.fillMaxWidth())
                    SectionTitle("筛选胶囊")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFilterChip(selected = true, onClick = {}, label = "全部", modifier = Modifier.weight(1f))
                        AppFilterChip(selected = false, onClick = {}, label = "已接种", modifier = Modifier.weight(1f))
                        AppFilterChip(selected = false, onClick = {}, label = "待接种", modifier = Modifier.weight(1f))
                    }
                    SectionTitle("标签与徽章")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppChip(label = "已完成")
                        AppChip(label = "紧急", backgroundColor = c.dangerScale.tintContainer(c), textColor = c.dangerScale.accentContent(c))
                        AppEmojiBadge(emoji = "🍼", tint = c.danger)
                        AppEmojiBadge(emoji = "🌙", tint = c.secondary)
                        AppSwitch(checked = true, onCheckedChange = {})
                    }
                    SectionTitle("分段控件")
                    SegmentedControl(labels = listOf("日", "周", "月"), selectedIndex = 1, onSelect = {}, modifier = Modifier.fillMaxWidth())
                    SectionTitle("记录卡片")
                    RecordCard(onDelete = {}, modifier = Modifier.fillMaxWidth(), accentColor = c.danger) {
                        AppEmojiBadge(emoji = "🤱", tint = c.danger)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            androidx.compose.material3.Text("母乳", style = LocalAppTypography.current.titleSmall, color = c.textPrimary)
                            androidx.compose.material3.Text("双侧 · 15分钟", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                        androidx.compose.material3.Text("08:15", style = LocalAppTypography.current.labelMedium, color = c.textTertiary)
                    }
                    SectionTitle("分区渐变摘要卡")
                    AppSummaryCard(emoji = "🧷", title = "今日尿布", value = "6 次", subtitle = "💧3 · 💩2 · 🔄1", gradient = Gradients.diaper(c))
                    AppSummaryCard(emoji = "📏", title = "生长记录", value = "72.5cm", subtitle = "本月 +1.8cm", gradient = Gradients.growth(c))
                    SectionTitle("迷你图表")
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            MiniBarChart(listOf(3f, 5f, 2f, 6f, 4f, 7f, 5f), Modifier.fillMaxWidth().height(56.dp), barColor = c.danger)
                            Spacer(Modifier.height(12.dp))
                            MiniLineChart(listOf(10f, 12f, 11f, 14f, 15f), Modifier.fillMaxWidth().height(56.dp))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun components_dark() {
        paparazzi.snapshot(name = "components_dark") {
            ShowcaseHost(dark = true) {
                val c = LocalAppColors.current
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(c.pageBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("暗色：按钮与输入")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppButton(label = "主要按钮", onClick = {}, modifier = Modifier.weight(1f))
                        AppButton(label = "次要", variant = ButtonVariant.Secondary, onClick = {}, modifier = Modifier.weight(1f))
                    }
                    AppInput(value = "宝宝", onValueChange = {}, label = "宝宝姓名", modifier = Modifier.fillMaxWidth())
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            StatCell(value = "8", label = "喂养", unit = "次", modifier = Modifier.weight(1f))
                            StatCell(value = "9.5h", label = "睡眠", modifier = Modifier.weight(1f))
                            StatCell(value = "6", label = "尿布", unit = "次", modifier = Modifier.weight(1f))
                        }
                    }
                    SectionTitle("暗色：记录卡与徽章")
                    RecordCard(onDelete = {}, modifier = Modifier.fillMaxWidth(), accentColor = c.tertiary) {
                        AppEmojiBadge(emoji = "🧷", tint = c.tertiary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            androidx.compose.material3.Text("换尿布", style = LocalAppTypography.current.titleSmall, color = c.textPrimary)
                            androidx.compose.material3.Text("小便", style = LocalAppTypography.current.bodySmall, color = c.textSecondary)
                        }
                        androidx.compose.material3.Text("09:10", style = LocalAppTypography.current.labelMedium, color = c.textTertiary)
                    }
                    SectionTitle("暗色：渐变摘要卡")
                    AppSummaryCard(emoji = "🌙", title = "夜间睡眠", value = "10小时", subtitle = "21:00-07:00", gradient = Gradients.sleep(c))
                    AppSummaryCard(emoji = "⏰", title = "提醒", value = "3 条", subtitle = "疫苗 / 体检 / 用药", gradient = Gradients.reminder(c))
                    SectionTitle("暗色：空状态")
                    EmptyState(emoji = "🍼", title = "还没有添加宝宝", subtitle = "点击下方按钮，记录宝宝成长的每一个瞬间")
                }
            }
        }
    }
}
