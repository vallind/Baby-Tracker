# 设计系统升级 P2（能力阶段）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成设计系统升级 P2：密度变体（compact/comfortable/large 三档，设置页可切换）、无障碍语义基线（可交互组件 semantics + 装饰组件隔离 + 基线文档）、暗色/状态令牌测试。

**Architecture:** 沿用 P1 后的四层令牌结构。密度通过 `LocalAppDensity` + `BabyTrackerTheme(density)` 在注入层缩放 AppSpacing 并调整控件基准高度（零组件迁移成本）；a11y 按调研盘点逐组件补齐显式 semantics；令牌测试新增 `ComponentTokensStateAuditTest`。

**Tech Stack:** Kotlin 2.3.21 / Compose BOM 2026.05.01 / material3 / JUnit4 / 单模块 app（包根 `com.babytracker`）

**设计文档:** `docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md` 第四节（4.1 密度 / 4.2 a11y / 4.3 令牌测试）

## Global Constraints

- 每个任务结束必跑：`./gradlew assembleDebug` 与 `./gradlew testDebugUnitTest`，全绿才算完成；涉及 Compose/资源时补 `./gradlew lint`
- 注释与 Commit message 一律中文（AGENTS.md 第七节）
- 红线：百分比 `.coerceIn(0f, 1f)`；暗色只认 `theme.name == "night"`；`@Composable` 顶层；ViewModel 注册 `viewModel {}`/获取 `koinViewModel()`；改共享 API 先查调用方
- **token 桥接 M3，M3 不暴露给组件**：组件公开签名与语义参数不出现 M3 类型；`LocalAppDensity` 是自建类型
- 密度默认 comfortable；`AppDensity.fromKey` 未知 key 回退 Comfortable
- 密度三档数值（唯一依据）：
  - Compact: spacingScale = 0.85f, controlHeightDelta = -8.dp
  - Comfortable: spacingScale = 1.0f, controlHeightDelta = 0.dp
  - Large: spacingScale = 1.15f, controlHeightDelta = +8.dp
- a11y 组件分类基线见调研报告（`docs/superpowers/plans/2026-08-08-ds-upgrade-p2-a11y-survey.md` 已存于会话，实现时以本计划各任务清单为准）
- i18n：新用户可见文本必须写入 `AppStrings`；存量硬编码迁移仅限本计划列出的 2 处（AppTopBar 返回 CD、AppInput 密码 CD）
- 文档同步义务：design-system.md（密度）、project-structure.md（DensityController 等）、CHANGELOG（并入 1.8.0，无 Unreleased 小节）

---

### Task 1: AppDensity 令牌体系与密度注入

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt`（AppSpacing 附近加 scaled 函数；文件尾加 AppDensity/AppDensityTokens/LocalAppDensity）
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/Theme.kt`（BabyTrackerTheme 增加 density 参数与注入改造，L322-353 区域）
- Modify: `app/src/main/java/com/babytracker/MainActivity.kt`（传 density）
- Test: `app/src/test/java/com/babytracker/designsystem/theme/DensityTokensTest.kt`（新建）

**Interfaces:**
- Consumes: `AppSpacing`（8 字段 none/xxs/xs/sm/md/lg/xl/xxl）、`AppControlTokens`（small/medium/large）、`BabyTrackerTheme(theme, componentTokens?, content)`
- Produces:
  - `enum class AppDensity(val key: String, val label: String) { Compact("compact", "紧凑"), Comfortable("comfortable", "舒适"), Large("large", "宽松"); companion object { fun fromKey(key: String): AppDensity } }`
  - `@Immutable data class AppDensityTokens(val spacingScale: Float, val controlHeightDelta: Dp)` + `val AppDensity.tokens: AppDensityTokens`（扩展属性，三档数值见 Global Constraints）
  - `val LocalAppDensity = staticCompositionLocalOf { AppDensity.Comfortable }`
  - `fun AppSpacing.scaled(factor: Float): AppSpacing`（8 字段 × factor，保留一位小数用 `roundToInt().dp`？——用 `(value * factor).dp` 保留精度，`value` 是 Float，直接乘即可，`Dp` 支持小数）
  - `fun AppControlTokens.densityAdjusted(density: AppDensity): AppControlTokens`
  - `BabyTrackerTheme(theme, componentTokens?, density: AppDensity = LocalAppDensity.current, content)`

- [ ] **Step 1: 写失败测试**

新建 `DensityTokensTest.kt`：

```kotlin
package com.babytracker.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DensityTokensTest {

    @Test
    fun `AppSpacing scaled 应按系数缩放且 1 倍不变`() {
        val base = AppSpacing()
        assertEquals(0.dp, base.scaled(0.85f).none)
        assertEquals(base.xs.value * 0.85f, base.scaled(0.85f).xs.value, 0.01f)
        assertEquals(base.md, base.scaled(1.0f).md)
    }

    @Test
    fun `AppDensity 三档数值应唯一且有序`() {
        val compact = AppDensity.Compact.tokens
        val comfortable = AppDensity.Comfortable.tokens
        val large = AppDensity.Large.tokens
        assertTrue(compact.spacingScale < comfortable.spacingScale)
        assertTrue(comfortable.spacingScale < large.spacingScale)
        assertTrue(compact.controlHeightDelta < comfortable.controlHeightDelta)
        assertTrue(comfortable.controlHeightDelta < large.controlHeightDelta)
    }

    @Test
    fun `fromKey 应解析已知键并回退 Comfortable`() {
        assertEquals(AppDensity.Compact, AppDensity.fromKey("compact"))
        assertEquals(AppDensity.Comfortable, AppDensity.fromKey("comfortable"))
        assertEquals(AppDensity.Large, AppDensity.fromKey("large"))
        assertEquals(AppDensity.Comfortable, AppDensity.fromKey("unknown-key"))
    }

    @Test
    fun `densityAdjusted 应只调整 medium 高度`() {
        val base = AppControlTokens()
        assertEquals(base.medium.height - 8.dp, base.densityAdjusted(AppDensity.Compact).medium.height)
        assertEquals(base.medium.height, base.densityAdjusted(AppDensity.Comfortable).medium.height)
        assertEquals(base.medium.height + 8.dp, base.densityAdjusted(AppDensity.Large).medium.height)
        assertEquals(base.small.height, base.densityAdjusted(AppDensity.Compact).small.height)
    }
}
```

注意测试文件 import：`androidx.compose.ui.unit.dp`、`assertEquals` 用 `(Float, Float, delta)` 重载（Dp 的 value 是 Float）。若 `Dp` 比较有精度问题改用 `value` 比较。

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.DensityTokensTest"`
Expected: 编译失败（AppDensity/scaled 不存在）

- [ ] **Step 3: 实现令牌**

AppTokens.kt：
1. `AppSpacing` 数据类后追加扩展函数：
```kotlin
/** 按密度系数缩放全部间距（全局密度调整入口，零组件迁移成本） */
fun AppSpacing.scaled(factor: Float): AppSpacing = AppSpacing(
    none = none,
    xxs = xxs * factor,
    xs = xs * factor,
    sm = sm * factor,
    md = md * factor,
    lg = lg * factor,
    xl = xl * factor,
    xxl = xxl * factor,
)
```
（`Dp * Float` 运算符存在；`none=0.dp` 恒等。若 `Dp * Float` 编译报错用 `Dp(value * factor)` 构造。）

2. 文件尾（LocalAppControl 附近）追加：
```kotlin
/** 页面密度三档 */
enum class AppDensity(val key: String, val label: String) {
    Compact("compact", "紧凑"),
    Comfortable("comfortable", "舒适"),
    Large("large", "宽松"),
    ;
    companion object {
        fun fromKey(key: String): AppDensity =
            entries.firstOrNull { it.key == key } ?: Comfortable
    }
}

/** 密度档令牌：间距缩放系数 + 控件高度调整量 */
@Immutable
data class AppDensityTokens(
    val spacingScale: Float,
    val controlHeightDelta: Dp,
)

val AppDensity.tokens: AppDensityTokens
    get() = when (this) {
        AppDensity.Compact -> AppDensityTokens(spacingScale = 0.85f, controlHeightDelta = (-8).dp)
        AppDensity.Comfortable -> AppDensityTokens(spacingScale = 1.0f, controlHeightDelta = 0.dp)
        AppDensity.Large -> AppDensityTokens(spacingScale = 1.15f, controlHeightDelta = 8.dp)
    }

/** 密度调整控件基准（只动 medium 档，组件默认尺寸随密度变化） */
fun AppControlTokens.densityAdjusted(density: AppDensity): AppControlTokens {
    val delta = density.tokens.controlHeightDelta
    if (delta == 0.dp) return this
    return copy(medium = medium.copy(height = medium.height + delta))
}

/** 当前页面密度（默认舒适） */
val LocalAppDensity = staticCompositionLocalOf { AppDensity.Comfortable }
```

- [ ] **Step 4: 改造 BabyTrackerTheme 注入**

Theme.kt `BabyTrackerTheme` 签名增加 `density: AppDensity = LocalAppDensity.current`，L322-328 改为：

```kotlin
val densityTokens = density.tokens
val tokensSpacing = AppSpacing().scaled(densityTokens.spacingScale)
val tokensElevation = AppElevation()
val tokensOpacity = AppOpacity()
val tokensMotion = AppMotion()
val tokensShapes = AppShapes()
val tokensTypography = AppTypography()
val tokensControl = AppControlTokens().densityAdjusted(density)
```

CompositionLocalProvider 增加 `LocalAppDensity provides density,`。

⚠️ 注意：`LocalAppDensity` 默认值在函数签名默认参数里使用 `staticCompositionLocalOf` 的 current——Composable 函数默认参数可读 compositionLocal（合法）。`tokensSpacing` 缩放后间距可能变小数（如 16×0.85=13.6），`LocalAppSpacing provides tokensSpacing` 直接注入。

- [ ] **Step 5: MainActivity 传入 density**

MainActivity：
```kotlin
import com.babytracker.designsystem.theme.DensityController
import com.babytracker.designsystem.theme.AppDensity  // 若需要类型
private val densityController: DensityController by inject()
...
BabyTrackerTheme(theme, density = densityController.currentDensity) { ... }
```
（DensityController 在 Task 2 才创建——为保持本任务可编译，**本任务先不接线 MainActivity**，用默认 `LocalAppDensity.current`（Comfortable），Task 2 完成后再接线。本任务 Step 5 跳过。）

- [ ] **Step 6: 测试 + 编译**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.DensityTokensTest" && ./gradlew assembleDebug`
Expected: 全绿

- [ ] **Step 7: 提交**

```bash
git add app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt app/src/main/java/com/babytracker/designsystem/theme/Theme.kt app/src/test/java/com/babytracker/designsystem/theme/DensityTokensTest.kt
git commit -m "密度令牌体系：AppDensity 三档 + LocalAppDensity + AppSpacing 缩放注入，默认舒适档"
```

---

### Task 2: SettingsStore.density + DensityController + DI 接线

**Files:**
- Modify: `app/src/main/java/com/babytracker/core/settings/AppSettings.kt`（AppearanceSettings 加 density 字段）
- Create: `app/src/main/java/com/babytracker/designsystem/theme/DensityController.kt`
- Modify: `app/src/main/java/com/babytracker/core/di/Modules.kt`（single 注册）
- Modify: `app/src/main/java/com/babytracker/MainActivity.kt`（inject + 传 density——接上 Task 1 的 Step 5）

**Interfaces:**
- Consumes: `SettingsStore`、`AppSettings`/`AppearanceSettings`、`AppDensity.fromKey`（Task 1）
- Produces: `class DensityController(settingsStore)`：`var currentDensity by mutableStateOf(AppDensity.Comfortable)`（private set）、`fun switchDensity(density: AppDensity)`、订阅 `settingsStore.settings` 流

- [ ] **Step 1: AppearanceSettings 加字段**

AppSettings.kt 的 `AppearanceSettings`（含 themeName 等）增加：
```kotlin
val density: String = "comfortable",
```
⚠️ 改共享 API：`AppearanceSettings` 是 data class，加字段是向后兼容（默认值）。全局搜索 `AppearanceSettings(` 构造点确认无破坏（`rg -n "AppearanceSettings\(" app/src/main/java app/src/test`）。

- [ ] **Step 2: 确认解析逻辑测试覆盖**

DensityController 的密度解析逻辑（`AppDensity.fromKey`）已在 Task 1 的 `DensityTokensTest.fromKey 应解析已知键并回退 Comfortable` 覆盖（调生产代码）。流订阅与 DataStore 写入属集成逻辑，项目对 ThemeController 同类组件无 JVM 单测先例——本任务**不新增测试**，以编译 + 全量回归验证。

- [ ] **Step 3: 实现 DensityController**

仿 `ThemeController.kt`（同包同模式）：

```kotlin
package com.babytracker.designsystem.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.babytracker.core.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** 界面密度控制器 — 仿 ThemeController，订阅设置流并暴露当前密度 */
class DensityController(private val settingsStore: SettingsStore) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var currentDensity by mutableStateOf(AppDensity.Comfortable)
        private set

    init {
        scope.launch {
            settingsStore.settings.collectLatest { settings ->
                currentDensity = AppDensity.fromKey(settings.appearance.density)
            }
        }
    }

    fun switchDensity(density: AppDensity) {
        currentDensity = density
        scope.launch(Dispatchers.IO) {
            settingsStore.update { current ->
                current.copy(appearance = current.appearance.copy(density = density.key))
            }
        }
    }
}
```

- [ ] **Step 4: Modules.kt 注册**

`Modules.kt` 在 ThemeController 注册附近（L44 模式 `single { ThemeController(get()) }`）追加：
```kotlin
single { DensityController(get()) }
```

- [ ] **Step 5: MainActivity 接线**

`private val densityController: DensityController by inject()` + `BabyTrackerTheme(theme, density = densityController.currentDensity)`。

- [ ] **Step 6: 编译 + 全量测试**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿

- [ ] **Step 7: 提交**

```bash
git add app/src/main/java
git commit -m "密度设置持久化：AppearanceSettings.density + DensityController + DI 注册，MainActivity 接线"
```

---

### Task 3: 设置页密度选择 UI

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt`（设置项 + 底部弹层）
- Modify: `app/src/main/java/com/babytracker/designsystem/i18n/AppStrings.kt`（密度文案键）

**Interfaces:**
- Consumes: `DensityController`（Task 2）、`AppDensity`（Task 1）、`AppBottomSheet`、`AppStrings`
- Produces: `DensityPickerSheet(ctrl: DensityController, onDismiss: () -> Unit)`（仿 ThemePickerSheet：AppBottomSheet + AppDensity.entries 遍历 + AppCard 卡片选中态）

- [ ] **Step 1: AppStrings 补键**

AppStrings 增加（风格对齐现有键）：
```kotlin
const val densityLabel = "界面密度"
const val densityCompact = "紧凑"
const val densityComfortable = "舒适"
const val densityLarge = "宽松"
```
（若 AppStrings 用 object/class + companion const，按现有风格追加；`AppDensity.label` 已有「紧凑/舒适/宽松」——**优先复用 AppDensity.label**，AppStrings 只加 `densityLabel` 一个键，避免重复维护。确认 AppStrings 结构后决定：若 label 中文硬编码违反 i18n 约定，则把 label 也迁到 AppStrings 并在 AppDensity 引用——两者选一，以「AppDensity.label 直接复用 + AppStrings 仅加 densityLabel」为默认，若 review 提出 label 硬编码问题再迁。）

- [ ] **Step 2: 设置页加入口**

SettingsScreen 主题设置项附近加一行设置项（仿现有「选择主题」条目形态）：标题 `AppStrings.densityLabel`，值显示 `densityController.currentDensity.label`，点击弹 `DensityPickerSheet`。设置项所在的 Screen composable 需要 `densityController: DensityController` 参数——**先查 SettingsScreen 现有参数与调用方**（`rg -n "fun SettingsScreen" app/src/main/java` 与导航处），按现有注入方式传递（可能是参数 or koinViewModel）。**若 SettingsScreen 已有很多参数，则密度入口放 SettingsMenuScreen（若存在）**——以实际结构为准，迁移面最小为原则。

- [ ] **Step 3: DensityPickerSheet**

仿 ThemePickerSheet 结构（AppBottomSheet + 横向卡片）：

```kotlin
@Composable
fun DensityPickerSheet(ctrl: DensityController, onDismiss: () -> Unit) {
    val c = LocalAppColors.current
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elev = LocalAppElevation.current
    AppBottomSheet(show = true, onDismiss = onDismiss) {
        Column(Modifier.padding(spacing.md)) {
            Text(AppStrings.densityLabel, style = LocalAppTypography.current.titleLarge, modifier = Modifier.padding(bottom = 20.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppDensity.entries.forEach { density ->
                    val selected = ctrl.currentDensity == density
                    AppCard(
                        modifier = Modifier
                            .width(120.dp)
                            .height(96.dp)
                            .border(
                                if (selected) BorderStroke(2.dp, c.primary) else BorderStroke(1.dp, c.outline),
                                RoundedCornerShape(shapes.large),
                            )
                            .clickable { ctrl.switchDensity(density) },
                        elevation = elev.level1,
                    ) {
                        Box(Modifier.fillMaxSize().padding(12.dp)) {
                            Column {
                                Box(
                                    Modifier.size(36.dp)
                                        .clip(RoundedCornerShape(shapes.large))
                                        .background(if (selected) c.primary else c.surfaceVariant)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(density.label, style = LocalAppTypography.current.bodySmall, color = c.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
```
（imports：`androidx.compose.foundation.border`、`background`、`horizontalScroll`、`rememberScrollState`、`clip`、`size`、`Arrangement`、`BorderStroke`、`RoundedCornerShape`、`LocalAppColors`、`LocalAppTypography`、`LocalAppShapes`、`LocalAppElevation`、`AppBottomSheet`、`AppCard`、`AppStrings`、`DensityController`、`AppDensity`——以 ThemePickerSheet 现有 import 为模板。）

- [ ] **Step 4: 编译 + 全量测试 + lint**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew lint`
Expected: 全绿

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java
git commit -m "设置页新增界面密度选择（三档底部弹层），AppStrings 补文案"
```

---

### Task 4: 暗色 / 状态令牌测试（ComponentTokensStateAuditTest）

**Files:**
- Create: `app/src/test/java/com/babytracker/designsystem/theme/ComponentTokensStateAuditTest.kt`

**Interfaces:**
- Consumes: `AppComponentTokens.default(...)`（现签名：colors/spacing/shapes/typography/opacity/motion/elevation/control/darkTheme）、`AppColors.derive`、`AppComponentTokens` 各令牌组字段
- Produces: 无（纯测试）

- [ ] **Step 1: 写测试**

```kotlin
package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ComponentTokensStateAuditTest {

    private fun tokens(darkTheme: Boolean): AppComponentTokens {
        val colors = AppColors.derive(
            primary = Color(0xFF4A7DFF),
            surface = if (darkTheme) Color(0xFF18181B) else Color(0xFFFFFFFF),
            onSurface = Color(0xFF333333),
        )
        return AppComponentTokens.default(
            colors = colors,
            spacing = AppSpacing(),
            shapes = AppShapes(),
            typography = AppTypography(),
            opacity = AppOpacity(),
            motion = AppMotion(),
            elevation = AppElevation(),
            control = AppControlTokens(),
            darkTheme = darkTheme,
        )
    }

    @Test
    fun `暗色与亮色下按钮容器色应不同`() {
        val light = tokens(darkTheme = false)
        val dark = tokens(darkTheme = true)
        assertNotEquals(light.button.containerColor, dark.button.containerColor)
    }

    @Test
    fun `暗色与亮色下对话框容器色应不同`() {
        val light = tokens(darkTheme = false)
        val dark = tokens(darkTheme = true)
        assertNotEquals(light.dialog.containerColor, dark.dialog.containerColor)
    }

    @Test
    fun `含状态色字段的令牌组必须派生齐全`() {
        val t = tokens(darkTheme = false)
        val unchecked = listOf(
            t.button.disabledContainerColor,
            t.button.disabledContentColor,
            t.input.disabledContainerColor,
            t.input.disabledContentColor,
        )
        assertNotNull("状态色不得为 Unspecified", unchecked.all { it != Color.Unspecified })
    }
}
```

⚠️ **先查 AppComponentTokens 实际字段**：`val input: InputTokens` 是否存在？`input` 的字段名（disabledContainerColor? borderColor?）——以代码为准调整断言字段（`rg -n "data class InputTokens" -A 15 app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`）。若 dialog 令牌组无 containerColor 字段（如 dialog 用 color），按实际字段调整。测试必须真实断言（非空转）。

- [ ] **Step 2: 运行确认**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.ComponentTokensStateAuditTest"`
Expected: 先失败（若字段名不符编译失败）→ 修正字段名 → 全绿。若暗色差异断言失败说明 derive/default 逻辑有 bug，**停下向 controller 报告**（不要改产品逻辑掩盖）。

- [ ] **Step 3: 全量测试 + 提交**

Run: `./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/test/java/com/babytracker/designsystem/theme/ComponentTokensStateAuditTest.kt
git commit -m "令牌测试：暗色差异与状态色完整性审计（ComponentTokensStateAuditTest）"
```

---

### Task 5: 可交互组件语义补齐（核心缺口）

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/SegmentedControl.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/recordcard/RecordCard.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/rate/AppRate.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/slider/AppSlider.kt`（AppLabeledSlider）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/fab/Fab.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/bottomnav/BottomNav.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/timepicker/TimePickerLogic.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/datetimecascade/DateTimeCascade.kt`
- Test: `app/src/test/java/com/babytracker/designsystem/components/A11ySemanticsAuditTest.kt`（新建，静态断言）

**Interfaces:**
- Consumes: 各组件现有实现
- Produces: 各组件显式 semantics（见各 Step）

- [ ] **Step 1: SegmentedControl 加 role/selected**

`SegmentedControl.kt`：每个 segment 的 `Modifier.clickable` 前加：
```kotlin
.semantics {
    role = Role.Tab
    selected = segment 选中状态
    stateDescription = segment.label
}
```
（先读现文件确认 segment 渲染结构——`Modifier` 链组装位置与选中判断变量。）

- [ ] **Step 2: RecordCard 左滑删除可发现化**

`RecordCard.kt`：卡片本体 `combinedClickable` 的 Modifier 加：
```kotlin
.semantics {
    // 左滑删除对读屏不可执行，暴露自定义动作
    customActions = listOf(
        CustomAccessibilityAction(
            label = "删除记录",
            action = { /* 触发 onDelete 回调 */ true },
        ),
    )
}
```
背景删除 Icon（SwipeToDismissBox 内）改为 `Modifier.clearAndSetSemantics {}` 隔离（防常驻语义树）。**先读文件确认回调名**（onDelete? onDeleteRecord?）与 Icon 位置。

- [ ] **Step 3: AppRate 整体语义**

`AppRate.kt`：在评分容器 Row 上加：
```kotlin
.semantics {
    contentDescription = "评分 $value，共 5 星"
}
```
（`value` 为当前评分变量名以代码为准。）只读态 5 个独立 Icon 节点改为 `clearAndSetSemantics`（如只读分支可整体 `Modifier.semantics { contentDescription = ... }`——**先读文件**再定最小改动：只读分支 Row 上 `clearAndSetSemantics` + 设 contentDescription；可交互分支保持星号按钮 + 容器 Row 加 contentDescription）。

- [ ] **Step 4: AppLabeledSlider 合并 label**

`AppSlider.kt` 的 `AppLabeledSlider`：把 label 语义合并进滑块：
```kotlin
// label Text 与滑块合并朗读：滑块 Modifier 上加
.semantics { contentDescription = label }
```
（M3 Slider 自带 progressBarRangeInfo，合并 contentDescription 后朗读「<label>，50%」。需读文件确认 label 参数名。）

- [ ] **Step 5: AppFAB 双重朗读修复**

`Fab.kt` label 变体：内部 `Icon(icon, contentDescription = label)` → `contentDescription = null`（文本 label 已朗读）。紧凑变体保持透传。

- [ ] **Step 6: BottomNavBar icon CD 去重**

`BottomNav.kt`：`Icon(..., contentDescription = tab.label)` → `null`（M3 NavigationBarItem 内置 label 语义）。

- [ ] **Step 7: TimePickerLogic 滚轮语义**

`TimePickerLogic.kt`：滚轮 item 渲染处（LazyColumn item）加：
```kotlin
.semantics {
    // 滚轮值朗读：选中项带 selected 状态
    selected = isCurrent
    contentDescription = item 文本
}
```
（`isCurrent` 以代码实际选中判断为准。）⚠️ 若 item 是文本已朗读，则仅加 `selected = isCurrent`（最小）。**先读文件。**

- [ ] **Step 8: DateTimeCascade 日历格选中语义**

`DateTimeCascade.kt`：DayCell 的 clickable Box 加：
```kotlin
.semantics {
    role = Role.Button
    selected = day 是否选中
    stateDescription = if (选中) "已选中" else null
}
```
（先读文件确认 DayCell 渲染与选中判断。）

- [ ] **Step 9: 静态审计测试**

新建 `A11ySemanticsAuditTest.kt`：

```kotlin
package com.babytracker.designsystem.components

import org.junit.Test
import java.io.File

class A11ySemanticsAuditTest {

    private val root = File("src/main/java/com/babytracker/designsystem/components")

    private fun read(name: String): String =
        root.walkTopDown().first { it.name == name }.readText()

    @Test
    fun `自定义可交互组件必须带显式语义`() {
        assert(read("SegmentedControl.kt").contains("Role.Tab"))
        assert(read("SegmentedControl.kt").contains("selected ="))
        assert(read("RecordCard.kt").contains("customActions"))
        assert(read("TimePickerLogic.kt").contains("semantics"))
        assert(read("AppRate.kt").contains("contentDescription"))
        assert(read("AppSlider.kt").contains("contentDescription"))
    }
}
```
（路径用 `src/main/java/...`（CWD 是 app/ 模块，lessons #14 教训）；`TimePickerLogic.kt` 若语义加在 TimePicker.kt 里则断言对应文件——以实施为准微调。）

- [ ] **Step 10: 编译 + 全量测试**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿

- [ ] **Step 11: 提交**

```bash
git add app/src/main/java app/src/test/java/com/babytracker/designsystem/components/A11ySemanticsAuditTest.kt
git commit -m "可交互组件无障碍语义补齐：SegmentedControl/RecordCard/AppRate/滚轮/日历/去重修复"
```

---

### Task 6: 装饰组件语义隔离

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/BabyIllustration.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/BadgeIcon.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/EmptyState.kt`（仅 emoji Text）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/skeleton/Skeleton.kt`（可选）

**Interfaces:**
- Consumes: 各组件现状
- Produces: 装饰元素 `clearAndSetSemantics` / 语义化

- [ ] **Step 1: BabyIllustration**

整个组件根 Modifier 加 `Modifier.clearAndSetSemantics {}`（emoji 插图不被逐个朗读）。

- [ ] **Step 2: BadgeIcon**

角标内容语义化：`Text(badge)` 加 `Modifier.clearAndSetSemantics()`，组件根设 `semantics { contentDescription = "$badge 条未读" }`？——**先读文件**：BadgeIcon 是通用角标（数字），做通用处理 `clearAndSetSemantics {}`（数字对读屏无意义）即可，不猜文案。

- [ ] **Step 3: EmptyState emoji**

仅 emoji `Text(emoji, ...)` 加 `Modifier.clearAndSetSemantics {}`（title/subtitle 保留朗读）。

- [ ] **Step 4: 编译 + 测试 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/main/java/com/babytracker/designsystem/components/BabyIllustration.kt app/src/main/java/com/babytracker/designsystem/components/BadgeIcon.kt app/src/main/java/com/babytracker/designsystem/components/EmptyState.kt
git commit -m "装饰组件读屏隔离：BabyIllustration/BadgeIcon/EmptyState emoji 静默化"
```

---

### Task 7: i18n 迁移 + a11y 基线文档

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/topbar/TopBar.kt`（返回 CD）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/input/Input.kt`（密码切换 CD）
- Modify: `app/src/main/java/com/babytracker/designsystem/i18n/AppStrings.kt`
- Create: `docs/a11y-baseline.md`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`（若需补注释）

**Interfaces:**
- Consumes: AppStrings
- Produces: `docs/a11y-baseline.md` 基线文档（组件承诺语义表）

- [ ] **Step 1: AppStrings 补键**

```kotlin
const val back = "返回"
const val showPassword = "显示密码"
const val hidePassword = "隐藏密码"
```
（查现有键避免重复：`rg -n "返回|密码" AppStrings.kt`——若已有对应键则复用。）

- [ ] **Step 2: TopBar / Input 迁移**

TopBar.kt:57 `contentDescription = "返回"` → `contentDescription = AppStrings.back`（若 TopBar 在 designsystem 引用 AppStrings——AppStrings 在 designsystem/i18n 包内，同层可引用；确认 import）。Input.kt:70 同理。

- [ ] **Step 3: docs/a11y-baseline.md**

新建基线文档，结构：

```markdown
# 无障碍语义基线（a11y-baseline）

> 对应版本：随 P2 发布批次 · 更新日期：2026-08-08

## 原则
- 可交互组件：M3 包装组件（AppButton/AppSwitch/AppCheckbox/AppRadioButton/AppSlider/AppFilterChip/进度条/对话框/AppInput）依赖 M3 内置语义，作为基线锚点
- 自定义可交互组件（SegmentedControl/RecordCard/AppRate/TimePickerLogic/DateTimeCascade）必须显式提供 role/selected/stateDescription/customActions
- 纯装饰（BabyIllustration/BadgeIcon/EmptyState emoji/分割线/骨架屏）必须 clearAndSetSemantics 隔离
- 防重复朗读：文本标签与 icon 不得重复声明（icon contentDescription=null）

## 组件承诺表
| 组件 | 语义承诺 |
|---|---|
| AppButton | M3 内置（enabled 自动） |
| AppIconButton | 依赖调用方 contentDescription；漏传=无标签 |
| AppFAB | label 变体文本朗读，icon CD=null；紧凑变体依赖调用方 |
| SegmentedControl | Role.Tab + selected + stateDescription |
| RecordCard | customActions 暴露删除动作 |
| AppRate | 容器 contentDescription=「评分 x，共 5 星」 |
| AppLabeledSlider | label 合并进滑块语义 |
| TimePickerLogic 滚轮 | 选中项 selected 语义 |
| DateTimeCascade 日历 | 日期格 Role.Button + selected |
| BabyIllustration/BadgeIcon/EmptyState emoji | clearAndSetSemantics |
| ...（按实际实现补全） |

## 审计
- A11ySemanticsAuditTest（静态断言）守护自定义可交互组件语义；ThemeTokenizationStaticAuditTest 守护令牌合规
```

- [ ] **Step 4: 编译 + 全量测试 + lint**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew lint`
Expected: 全绿

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java docs/a11y-baseline.md
git commit -m "返回/密码提示文案迁移 AppStrings，新增无障碍语义基线文档"
```

---

### Task 8: 文档同步 + CHANGELOG

**Files:**
- Modify: `docs/design-system.md`（密度章节 + AppDensity）
- Modify: `docs/project-structure.md`（DensityController/DensityPickerSheet/新测试文件）
- Modify: `CHANGELOG.md`（并入 `### [1.8.0]` 小节，不新建 [Unreleased]）
- Modify: `docs/lessons.md`（如踩新坑）
- Modify: `docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md`（4.1/4.2/4.3 若与实现有出入）

**Interfaces:**
- Consumes: 全部前序任务产物

- [ ] **Step 1: design-system.md**

新增「密度」小节：AppDensity 三档（key/label/数值表）、LocalAppDensity、设置页入口、缩放机制（AppSpacing.scaled + AppControlTokens.densityAdjusted）。令牌清单更新（AppDensityTokens 为非组件令牌，不进 AppComponentTokens 计数）。

- [ ] **Step 2: project-structure.md**

设计系统模块新增文件：DensityController.kt、DensityPickerSheet（SettingsScreen 内）、DensityTokensTest/ComponentTokensStateAuditTest/A11ySemanticsAuditTest。

- [ ] **Step 3: CHANGELOG 并入 1.8.0**

在 `### [1.8.0] — 2026-08-08` 小节（P1 条目之后）追加「设计系统升级 P2」条目组：
- 密度变体：AppDensity 三档（紧凑/舒适/宽松），设置页可切换，AppSpacing 缩放注入
- 无障碍语义基线：自定义可交互组件 semantics 补齐（SegmentedControl/RecordCard/AppRate/滚轮/日历），装饰组件隔离，新增 docs/a11y-baseline.md
- 令牌测试：ComponentTokensStateAuditTest（暗色差异 + 状态色完整性）
- 文案迁移：返回/密码提示走 AppStrings

- [ ] **Step 4: spec 同步核对**

核对 spec 4.1/4.2/4.3 与实现：密度三档数值、a11y 范围、测试形态。有出入则更新 spec（先改设计文档再改代码的原则反向适用——实现已完成，spec 作为历史文档标注实际偏差）。

- [ ] **Step 5: 全量验证**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew lint`
Expected: 全绿

- [ ] **Step 6: 提交**

```bash
git add docs CHANGELOG.md
git commit -m "P2 文档同步：密度/无障碍基线/令牌测试入设计文档与 CHANGELOG（1.8.0）"
```

---

## P2 验收清单（全部任务完成后）

- [ ] `./gradlew assembleDebug` 通过
- [ ] `./gradlew testDebugUnitTest` 全绿（DensityTokensTest 4 项 + ComponentTokensStateAuditTest 3 项 + A11ySemanticsAuditTest + 既有 108 项）
- [ ] `./gradlew lint` 通过
- [ ] 密度三档可在设置页切换且 App 生效（LocalAppSpacing 缩放 + 控件高度调整）
- [ ] 全项目 `Modifier.semantics`/`clearAndSetSemantics` 出现在自定义可交互与装饰组件中（按 A11ySemanticsAuditTest 断言）
- [ ] 文档同步：design-system.md / project-structure.md / a11y-baseline.md（新增）/ CHANGELOG（1.8.0）
- [ ] 无新硬编码中文（AppTopBar/AppInput 已迁移 AppStrings）
