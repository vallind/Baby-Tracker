# 色彩基座收敛 + 动效层 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 色彩基座中性化（背景/顶栏去彩色，主题只换强调色）+ 按压动效 + 页面转场，实现 Apple HIG × Linear 气质。

**Architecture:** Part A 色彩基座（4 文件纯色值改动）；Part B 动效层（新增 pressScale Modifier + 3 组件叠加 + 导航转场）。B1 的 pressScale 不消费指针事件，与现有 ripple 共存。

**Tech Stack:** Jetpack Compose + Material3 + navigation-compose 2.9.1

**Spec:** `docs/superpowers/specs/2026-07-31-color-base-motion-design.md`

**关键背景：**
- `instantComposable` 是刻意关闭转场的封装（AppNavigation.kt:104-112），本次按用户指示去除
- AppCard 目前无 onClick 参数（点击由调用方 Modifier.clickable 叠加）——B2 需要给它新增 onClick 参数
- `ThemeColors.derive` 的 bg 参数被 3 个主题显式传值（aurora=White / sunny=#FFFAF0 / morandi=#FAFAFA），需同步清理
- 其他页面（DevelopmentAssessment/Family/LogViewer/AiChat）的 primaryContainer 属选中态/小图标/气泡语义，**不在 A4 范围**（风格验证后再统一铺开）

---

### Task 1: A1 — ThemeColors.derive 背景中性化

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/Theme.kt`

- [ ] **Step 1: 修改 derive 默认背景（line 93-96 区域）**

```kotlin
            val resolvedPrimaryLight = primaryLight ?: primary.mix(white, 0.88f)
            val resolvedBg = bg ?: if (isDark) Color(0xFF12121F) else resolvedPrimaryLight
            val resolvedCard = card ?: if (isDark) Color(0xFF1E1E32) else white
            val resolvedPageBg = pageBg ?: resolvedBg
```

改为：

```kotlin
            val resolvedPrimaryLight = primaryLight ?: primary.mix(white, 0.88f)
            // 中性基座：背景固定中性灰阶，不再随主色染色（Apple HIG 克制原则）
            val resolvedBg = bg ?: if (isDark) Color(0xFF12121F) else Color(0xFFF5F7FA)
            val resolvedCard = card ?: if (isDark) Color(0xFF1E1E32) else white
            val resolvedPageBg = pageBg ?: resolvedBg
```

- [ ] **Step 2: 清理 3 个主题的显式 bg 传值（line 150-175 区域）**

```kotlin
        val aurora = AppTheme("aurora", ThemeColors.derive(
            primary = Color(0xFF7C6CF0), bg = Color.White, pageBg = Color(0xFFF8F5FF),
            accent = Color(0xFFFCD34D),
        ))
```
改为（删除 bg/pageBg，背景统一中性）：
```kotlin
        val aurora = AppTheme("aurora", ThemeColors.derive(
            primary = Color(0xFF7C6CF0),
            accent = Color(0xFFFCD34D),
        ))
```

```kotlin
        val sunny = AppTheme("sunny", ThemeColors.derive(
            primary = Color(0xFFF5A623), bg = Color(0xFFFFFAF0),
            accent = Color(0xFFE67A2E),
        ))
```
改为：
```kotlin
        val sunny = AppTheme("sunny", ThemeColors.derive(
            primary = Color(0xFFF5A623),
            accent = Color(0xFFE67A2E),
        ))
```

```kotlin
        val morandi = AppTheme("morandi", ThemeColors.derive(
            primary = Color(0xFFB0BEC5), bg = Color(0xFFFAFAFA),
            accent = Color(0xFFD0A878),
        ))
```
改为：
```kotlin
        val morandi = AppTheme("morandi", ThemeColors.derive(
            primary = Color(0xFFB0BEC5),
            accent = Color(0xFFD0A878),
        ))
```

- [ ] **Step 3: 自查** — `bg`/`pageBg` 参数保留在函数签名中（兼容），仅默认值与显式传值中性化；warm/night/pure 本就没传 bg，无需动。

---

### Task 2: A2 — AppBarTokens 亮色容器中性化

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`

- [ ] **Step 1: 修改 AppBarTokens.default（line 502-511 区域）**

```kotlin
            containerColor = if (darkTheme) colors.pageBackground else colors.primaryContainer,
```
改为：
```kotlin
            // 中性基座：顶栏与页面同色，无彩色顶栏（Apple 风格）；iconColor 保留主色作强调
            containerColor = colors.pageBackground,
```

同时删除 `darkTheme: Boolean = false` 参数？**不删**——参数保留兼容（AppComponentTokens.default 还传 darkTheme），只改内部实现。但 darkTheme 参数将不再被使用 → 编译警告。处理：保留参数但用 `@Suppress("UNUSED_PARAMETER")`？不，更干净：Kotlin 未使用参数不报错（只有 warning 级别，lint 可能报）。检查：`darkTheme` 是函数参数，未使用不会导致编译错误，lint 也不报（除非 UnusedParameter 规则启用）。**保留不动**，避免动签名（红线 #10）。

- [ ] **Step 2: 自查** — `AppBarTokens.default(colors, typography, control, darkTheme)` 签名不变，仅容器色实现改中性。

---

### Task 3: A3 — Gradients.pageHeader 灰调化

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/Gradients.kt`

- [ ] **Step 1: 修改 pageHeader（line 41-43 区域）**

```kotlin
    /** 首页顶部背景渐变（浅蓝 → 更浅蓝，营造柔和氛围） */
    fun pageHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.primaryContainer, c.pageBackground),
    )
```
改为：
```kotlin
    /** 首页顶部背景渐变（中性灰 → 页面背景，Apple 克制风格） */
    fun pageHeader(c: AppColors): Brush = Brush.verticalGradient(
        colors = listOf(c.bgHover, c.pageBackground),
    )
```

- [ ] **Step 2: 自查** — 其余渐变（overviewCard/iconBgWarm/reminderCard/diaperSummary/growthChart 等）按 spec 保留不动。

---

### Task 4: A4 — HomeScreen primaryContainer 大块中性化

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/home/HomeScreen.kt`

- [ ] **Step 1: AiAssistantEntryCard 整卡背景（line 115-120 区域）**

```kotlin
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.AiAssistant.route) },
        containerColor = colors.primaryContainer,
    ) {
```
改为（顺带迁移到 AppCard 的 onClick 参数，为 B2 的按压效果示范；AppCard onClick 参数在 Task 6 新增，若 Task 6 未完成会编译失败——**本 Task 只改 containerColor**，onClick 迁移放到 Task 6）：
```kotlin
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.AiAssistant.route) },
        containerColor = colors.bgHover,
    ) {
```

- [ ] **Step 2: BabyHeader 宝宝头像（line 193-201 区域）**

```kotlin
            Box(
                Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(shapes.full))
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("👶", style = typography.display)
            }
```
改为：
```kotlin
            Box(
                Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(shapes.full))
                    .background(c.bgHover),
                contentAlignment = Alignment.Center,
            ) {
                Text("👶", style = typography.display)
            }
```

- [ ] **Step 3: 自查** — HomeScreen 全文件 `primaryContainer` 应无残留（共 2 处已处理）。

---

### Task 5: B1 — 新增 PressFeedback.kt

**Files:**
- Create: `app/src/main/java/com/babytracker/designsystem/components/PressFeedback.kt`

- [ ] **Step 1: 创建文件（完整内容）**

```kotlin
package com.babytracker.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * 按压缩放反馈 — 按下缩至 0.97、松手弹簧回弹（Linear 风格微反馈）。
 *
 * 不消费指针事件，与 ripple 水波共存。用法：
 *   Modifier.pressScale().clickable { ... }
 */
@Composable
fun Modifier.pressScale(
    pressedScale: Float = 0.97f,
    pressDurationMillis: Int = 80,
    releaseSpring: SpringSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 500f),
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                scope.launch { scale.animateTo(pressedScale, tween(pressDurationMillis)) }
                waitForUpOrCancellation()
                scope.launch { scale.animateTo(1f, releaseSpring) }
            }
        }
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}
```

- [ ] **Step 2: 编译验证**（新文件风险高，单独验证）

```bash
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 6: B2 — 组件叠加 pressScale（Button/AppListItem/AppCard）

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/button/Button.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/section/Section.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/card/Card.kt`
- Modify: `app/src/main/java/com/babytracker/feature/home/HomeScreen.kt`

- [ ] **Step 1: Button.kt — 3 个按钮叠加（line 47、83、114 区域）**

PrimaryButton（line 47）：
```kotlin
        modifier = modifier.height(height),
```
改为：
```kotlin
        modifier = modifier.height(height).pressScale(),
```

SecondaryButton（line 83）：同上改 `modifier = modifier.height(height).pressScale(),`
AppTextButton（line 114）：`modifier = modifier,` 改为 `modifier = modifier.pressScale(),`

新增 import（Button.kt 顶部）：
```kotlin
import com.babytracker.designsystem.components.pressScale
```

- [ ] **Step 2: Section.kt — AppListItem 叠加（line 78-84 区域）**

```kotlin
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
```
改为：
```kotlin
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .then(if (onClick != null) Modifier.pressScale().clickable(onClick = onClick) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
```

新增 import：
```kotlin
import com.babytracker.designsystem.components.pressScale
```

- [ ] **Step 3: Card.kt — AppCard 新增 onClick 参数（向后兼容，默认 null 行为不变）**

完整改为：

```kotlin
package com.babytracker.designsystem.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.CardDefaults as AppCardDefaults
import com.babytracker.designsystem.components.pressScale

/**
 * 统一卡片组件 — 对标 Palette Card 组件，消费 AppComponentTokens.card
 *
 * 优先级模型：
 *   显式参数 > CardDefaults（令牌） > M3 默认值
 *
 * 用法：
 *   AppCard { Text("内容") }
 *   AppCard(cornerRadius = 12.dp, containerColor = Color.Red) { ... }
 *   AppCard(onClick = { ... }) { ... }   // 可点击卡片（内置按压反馈）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(
    cornerRadius: Dp = AppCardDefaults.cornerRadius(),
    elevation: Dp = AppCardDefaults.elevation(),
    containerColor: Color = AppCardDefaults.containerColor(),
    borderColor: Color = AppCardDefaults.borderColor(),
    borderWidth: Dp = AppCardDefaults.borderWidth(),
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val pressModifier = if (onClick != null) Modifier.pressScale() else Modifier

    Card(
        onClick = onClick,
        modifier = pressModifier.then(modifier).shadow(elevation = elevation, shape = shape),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        content = content,
    )
}
```

注意：M3 `Card(onClick: (() -> Unit)?, ...)` 重载要求 `@OptIn(ExperimentalMaterial3Api::class)`；`onClick = null` 时该重载渲染为非可点击卡片（与旧行为一致，只是多一层 ripple 未激活）。

- [ ] **Step 4: HomeScreen.kt — AiAssistantEntryCard 迁移到 onClick（line 115-120 区域）**

```kotlin
    AppCard(
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.AiAssistant.route) },
        containerColor = colors.bgHover,
    ) {
```
改为：
```kotlin
    AppCard(
        onClick = { navController.navigate(Screen.AiAssistant.route) },
        modifier = Modifier
            .padding(horizontal = spacing.md)
            .fillMaxWidth(),
        containerColor = colors.bgHover,
    ) {
```

检查后若 `clickable` import 在 HomeScreen 其他地方仍被使用则保留，否则清理（HomeScreen line 182 有 `Modifier.clickable(onClick = onClickProfile)`，import 仍需要）。

- [ ] **Step 5: 编译验证**

```bash
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 7: B3 — animateNumber spring 化

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/Animations.kt`

- [ ] **Step 1: 修改 animateNumber（line 54-62 区域）**

```kotlin
@Composable
fun animateNumber(target: Int): Int {
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600),
        label = "number",
    )
    return animated
}
```
改为：
```kotlin
@Composable
fun animateNumber(target: Int): Int {
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 350f),
        label = "number",
    )
    return animated
}
```

新增 import：
```kotlin
import androidx.compose.animation.core.spring
```

（tween import 若文件内 AnimatedListItem 仍用则保留——AnimatedListItem line 37-41 使用 tween，保留）

- [ ] **Step 2: 自查** — 仅 animateNumber 的 animationSpec 变化；tween import 仍被 AnimatedListItem 使用。

---

### Task 8: B4 — AppNavigation 恢复页面转场

**Files:**
- Modify: `app/src/main/java/com/babytracker/navigation/AppNavigation.kt`

- [ ] **Step 1: 替换 instantComposable 为 fadeComposable / slideComposable（line 67-112 区域）**

新增 import：
```kotlin
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
```

路由分类替换：
- 主 tab 级（9 个）：Home / Timeline / Feeding / Sleep / Growth / Vaccination / Health / Diaper / Stats / Settings → `fadeComposable`
- 层级 push（其余 15 个）：PreferenceSettings / DataSettings / SupportSettings / BabyManagement / BabyProfile / Backup / LogViewer / SyncSettings / Family / Message / DevelopmentAssessment / Reminder / Login / AiAssistant / AiSettings → `slideComposable`

辅助函数（替换原 instantComposable 定义）：

```kotlin
/**
 * 淡入淡出转场 — 主 tab 级页面切换（Apple 克制风格，150ms）。
 */
private fun NavGraphBuilder.fadeComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { fadeOut(tween(150)) },
        content = content,
    )
}

/**
 * 滑动转场 — 层级 push 页面（淡入 + 轻微右滑入，pop 反向）。
 */
private fun NavGraphBuilder.slideComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { it / 16 } },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { -it / 16 } },
        popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { it / 16 } },
        content = content,
    )
}
```

- [ ] **Step 2: 全部 25 条路由替换调用**（`instantComposable(` → `fadeComposable(` 或 `slideComposable(`，按上述分类）

- [ ] **Step 3: 删除 instantComposable 原定义及注释**

- [ ] **Step 4: 编译验证**

```bash
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 9: 全量验证

- [ ] **Step 1: 完整编译**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Lint**

```bash
./gradlew lint
```
Expected: 无新增 error/warning

---

### Task 10: CHANGELOG + 提交

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: CHANGELOG 添加版本条目（文件顶部 `### [1.8.0]` 之前）**

```markdown
### [1.9.0] — 2026-07-31

**精致感重塑 · 色彩基座 + 动效：**
- 色彩基座中性化：6 主题背景/顶栏统一中性灰阶，主题色只保留在按钮/图标/选中态等强调位置（Apple HIG 克制原则）
- `ThemeColors.derive` 默认背景固定中性灰阶（亮 #F5F7FA / 暗 #12121F），清理 aurora/sunny/morandi 的显式彩色背景
- AppBar 亮色容器改为页面同色，移除彩色顶栏；首页顶部渐变灰调化；首页 AI 入口卡/宝宝头像中性化
- 新增 `Modifier.pressScale()` 按压反馈（按下 0.97 缩放 + 弹簧回弹），应用于按钮/列表项/可点击卡片
- `AppCard` 新增 `onClick` 参数（可点击卡片，内置按压反馈，向后兼容）
- 数字跳动动画改为弹簧曲线；恢复页面转场（主 tab 淡入淡出 150ms、层级页滑动转场）
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/com/babytracker/designsystem/theme/Theme.kt app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt app/src/main/java/com/babytracker/designsystem/theme/Gradients.kt app/src/main/java/com/babytracker/designsystem/components/PressFeedback.kt app/src/main/java/com/babytracker/designsystem/components/button/Button.kt app/src/main/java/com/babytracker/designsystem/components/section/Section.kt app/src/main/java/com/babytracker/designsystem/components/card/Card.kt app/src/main/java/com/babytracker/designsystem/components/Animations.kt app/src/main/java/com/babytracker/navigation/AppNavigation.kt app/src/main/java/com/babytracker/feature/home/HomeScreen.kt CHANGELOG.md
git commit -m "feat: 色彩基座中性化 + 按压动效 + 页面转场（精致感重塑第二弹）"
```

---

## Self-Review

**Spec 覆盖检查：**
- A1（Theme.kt bg/pageBg 中性化）→ Task 1 ✅
- A2（AppBarTokens 亮色容器 → pageBackground）→ Task 2 ✅
- A3（Gradients.pageHeader 灰调）→ Task 3 ✅
- A4（HomeScreen primaryContainer 大块 → bgHover）→ Task 4 ✅
- A5（设置页图标容器保留）→ 不列入任务（spec 明确保留）✅
- B1（PressFeedback.kt 新增）→ Task 5 ✅
- B2（Button/AppListItem/AppCard 叠加）→ Task 6 ✅
- B3（animateNumber spring）→ Task 7 ✅
- B4（AppNavigation 转场，移除 instantComposable）→ Task 8 ✅
- 验证标准 → Task 9 ✅

**占位符扫描：** 全部步骤含完整代码或精确改动，无 TBD/TODO ✅

**类型一致性：**
- `Modifier.pressScale()` 在 Task 5 定义（@Composable 扩展），Task 6 三处引用，import 路径 `com.babytracker.designsystem.components.pressScale` 一致 ✅
- `AppCard(onClick: (() -> Unit)? = null, ...)` Task 6 Step 3 定义，Task 6 Step 4 调用 `AppCard(onClick = {...})` ✅
- `fadeComposable`/`slideComposable` Task 8 定义并被同 Task 替换调用 ✅
- 图标/颜色常量与现有 AppColors 字段一致（bgHover/pageBackground 均存在）✅

**边界确认：**
- AppBarTokens 的 `darkTheme` 参数保留不删（避免动签名）；Kotlin 未使用参数不报错 ✅
- Button.kt 的 `pressScale` 是 @Composable 扩展，在 @Composable 函数体内调用合法 ✅
- M3 `Card(onClick = null)` 与 `Card()` 视觉等价，默认行为不破坏 ✅
- HomeScreen 的 `clickable` import 被 line 182 继续使用，保留 ✅
- Animations.kt 的 `tween` import 被 AnimatedListItem 继续使用，保留 ✅
- 其余页面 primaryContainer 选中态/气泡不动（spec 范围外）✅
