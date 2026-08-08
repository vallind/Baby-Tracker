# 设计系统升级 P1（清理阶段）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成设计系统升级 P1：Typography 自建单体系统一、Button 家族收敛为 AppButton、新增 AppDivider/AppSurface/AppSnackbarHost、EmptyState 令牌化、feature 遗留清理。

**Architecture:** 保持「核心语义令牌 → 组件令牌 → XxxDefaults → 组件」四层不动。AppTypography 数据类扩展为 12 级自建单体系（`LocalAppTypography` 不再暴露 M3 `Typography` 类型）；Button 三函数 + PaiButton 收敛为单一 `AppButton(label, onClick, variant)`；新增组件均含 Tokens + Defaults。

**Tech Stack:** Kotlin 2.3.21 / Compose BOM 2026.05.01 / material3 / JUnit4 / 单模块 app（包根 `com.babytracker`）

## Global Constraints

- 每个任务结束必须跑：`./gradlew assembleDebug` 与 `./gradlew testDebugUnitTest`，全绿才算完成
- 涉及 Compose/资源时补跑 `./gradlew lint`
- 注释与 Commit message 一律中文（AGENTS.md 第七节）
- 所有 `@Composable` 定义在文件顶层（红线 6）；AlertDialog 平级 if（红线 7，本计划不新增弹窗）
- 百分比夹紧 `.coerceIn(0f, 1f)`（红线 1）；日期过滤 `.take(10)`（红线 2）
- 暗色只认 `theme.name == "night"`（红线 8）
- 改共享 API 前先查调用方（红线 10）；ViewModel 注册用 `viewModel {}`、获取用 `koinViewModel()`（红线 3/4）
- 组件签名约定：显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌；变体用枚举参数；颜色参数类型 `Color`，默认走令牌；组件内部不写死色值
- **token 桥接 M3，M3 不暴露给组件**：`designsystem/theme/` 内部允许持有/转换 M3 类型（private M3 Typography、toColorScheme 等）；`designsystem/components/` 与 `feature/` 禁止 import M3 令牌/主题类型（`androidx.compose.material3.Typography`、`ColorScheme`、`Shapes`、`MaterialTheme.typography/colorScheme/shapes`）；组件公开 API 不出现 M3 类型（M3 组件类受控包裹与 SnackbarHostState 状态类沿用既有模式，不在此限）
- 禁止给用户可见文本硬编码中文 → 新组件文案走 `AppStrings`
- 设计文档：`docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md`

---

### Task 1: AppTypography 扩展（新增 8 级，保留现有 7 级）

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt:192-216`
- Test: `app/src/test/java/com/babytracker/designsystem/theme/TypographyTokensTest.kt`（新建）

**Interfaces:**
- Consumes: 现有 `AppTypography` 数据类（7 级：display/headline/titleLarge/titleMedium/bodyLarge/bodyMedium/label）
- Produces: `AppTypography` 15 字段版本（新增 8 级，Task 3 删除 3 个裸字段后为 12 级）

新增字段与数值（自建风格插值，唯一性保证）：

```kotlin
displayLarge = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
headlineLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
```

注意：`headlineMedium`/`labelMedium` 与现有裸字段 `headline`(24sp)/`label`(12sp) 数值相同，Task 3 删除裸字段后无重复。

- [ ] **Step 1: 写失败测试**

新建 `app/src/test/java/com/babytracker/designsystem/theme/TypographyTokensTest.kt`：

```kotlin
package com.babytracker.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTokensTest {

    @Test
    fun `AppTypography 应包含全部 15 个字段`() {
        val t = AppTypography()
        val fields = listOf(
            t.displayLarge, t.display, t.headlineLarge, t.headline, t.headlineMedium,
            t.headlineSmall, t.titleLarge, t.titleMedium, t.titleSmall,
            t.bodyLarge, t.bodyMedium, t.bodySmall,
            t.label, t.labelMedium, t.labelSmall,
        )
        assertEquals(15, fields.size)
    }

    @Test
    fun `新增层级 fontSize 应有序且无越界`() {
        val t = AppTypography()
        val ordered = listOf(
            t.displayLarge.fontSize.value, t.display.fontSize.value,
            t.headlineLarge.fontSize.value, t.headline.fontSize.value,
            t.headlineMedium.fontSize.value, t.headlineSmall.fontSize.value,
            t.titleLarge.fontSize.value, t.titleMedium.fontSize.value,
            t.titleSmall.fontSize.value, t.bodyLarge.fontSize.value,
            t.bodyMedium.fontSize.value, t.bodySmall.fontSize.value,
            t.label.fontSize.value, t.labelMedium.fontSize.value,
            t.labelSmall.fontSize.value,
        )
        // M3 允许不同层级同字号（bodySmall 与 labelMedium 均为 12sp），只约束单调不增与两端极值
        assertTrue(ordered.zipWithNext().all { (a, b) -> a >= b })
        assertEquals(40f, ordered.first())
        assertEquals(11f, ordered.last())
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.TypographyTokensTest"`
Expected: 编译失败（`AppTypography` 无 `displayLarge` 等成员）

- [ ] **Step 3: 实现 — 扩展 AppTypography**

编辑 `AppTokens.kt` 192-216 行，把数据类扩为 15 字段（现有 7 字段**数值不动**，新增 8 级插在对应位置），并把注释 `// —— 排版令牌（5 组 7 级） ——` 改为 `// —— 排版令牌（自建 15 级，Task 3 收口为 12 级） ——`：

```kotlin
@Immutable
data class AppTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold,
    ),
    val display: TextStyle = TextStyle(
        fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold,
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headline: TextStyle = TextStyle(
        fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
    ),
    val headlineSmall: TextStyle = TextStyle(
        fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
    ),
    val titleSmall: TextStyle = TextStyle(
        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal,
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal,
    ),
    val bodySmall: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal,
    ),
    val label: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
    val labelMedium: TextStyle = TextStyle(
        fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
    val labelSmall: TextStyle = TextStyle(
        fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium,
    ),
)
```

⚠️ 注意：`headline`/`headlineMedium` 与 `label`/`labelMedium` 此时数值重复（M3 允许同字号，测试用 `>=` 容忍），Task 3 删除裸字段后无重复。

- [ ] **Step 4: 运行测试**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.TypographyTokensTest"`
Expected: 编译通过，3 项测试全绿（顺序断言用 `>=` 容忍等值）

- [ ] **Step 5: 编译验证**

Run: `./gradlew assembleDebug`
Expected: 成功

- [ ] **Step 6: 提交**

```bash
git add app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt app/src/test/java/com/babytracker/designsystem/theme/TypographyTokensTest.kt
git commit -m "Typography 令牌新增 8 级（自建单体系第一步），补层级一致性测试"
```

---

### Task 2: Theme.kt 双体系合一 + 全量 Typography 迁移

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/Theme.kt`（L186、L327、L350、L353、L357）
- Modify: 31 个 feature/DS 文件（见下清单）
- Modify: DS 内部 4 文件 13 处 `MaterialTheme.typography`

**Interfaces:**
- Consumes: Task 1 的 15 字段 `AppTypography`
- Produces: `LocalAppTypography`（类型 = `AppTypography`，不再暴露 M3 `Typography`）；`internalMaterialTypography`（private M3 Typography，仅 MaterialTheme 内部使用）

**字段映射表（迁移唯一依据）：**

| 原来源 | 原字段 | 新来源 | 新字段 |
|---|---|---|---|
| `LocalAppTypography`(M3) | displayLarge | `LocalAppTypography`(AppTypography) | displayLarge |
| `LocalAppTypographyStyle` | display | 同 | displayLarge |
| `LocalAppTypographyStyle` | headline | 同 | headlineMedium |
| `LocalAppTypography`(M3) | headlineLarge | 同 | headlineLarge |
| `LocalAppTypography`(M3) | headlineMedium | 同 | headlineMedium |
| `LocalAppTypography`(M3) | headlineSmall | 同 | headlineSmall |
| 任意 | titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall | 同 | 同名不变 |
| `LocalAppTypographyStyle` | label | 同 | labelMedium |
| `LocalAppTypography`(M3) | labelMedium | 同 | labelMedium |
| `LocalAppTypography`(M3) | labelSmall | 同 | labelSmall |

- [ ] **Step 1: 改造 Theme.kt**

1. L186：`val LocalAppTypography = compositionLocalOf { AppTypography() }`（去掉 `BabyTrackerTypography`）
2. L188-204：`BabyTrackerTypography`（M3 Typography 15 级）→ 改为 `private val internalMaterialTypography = Typography(...)`，**数值全部改为自建体系对应值**（displayLarge 40sp/headlineLarge 28sp/headlineMedium 24sp/headlineSmall 20sp/titleLarge 18sp/titleMedium 16sp/titleSmall 14sp/bodyLarge 15sp/bodyMedium 13sp/bodySmall 12sp/labelMedium 12sp/labelSmall 11sp；其余 M3 未用字段可留默认 `Typography()` 值——仅保证 MaterialTheme 内部一致性，对外不可见）。保留 `import androidx.compose.material3.Typography`（文件内使用合法，本文件是 designsystem 内部）
3. L327：`val tokensTypography = AppTypography()` 不变
4. **AppTokens.kt:340 删除** `val LocalAppTypographyStyle = staticCompositionLocalOf { AppTypography() }` 声明
5. L353：`LocalAppTypography provides tokensTypography,`（改用自建实例）
6. L357：`typography = internalMaterialTypography,`

- [ ] **Step 2: 迁移 17 个 M3 `LocalAppTypography` 文件**

这些文件的字段引用**同名可达**（titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/headlineLarge/headlineMedium/headlineSmall/labelMedium/labelSmall/displayLarge 全部存在于自建 15 级），**无需改动引用点**。唯一例外：`val typography = LocalAppTypography.current` 别名（如 LoginScreen.kt:33、SyncSettingsScreen.kt:39、BabyProfileScreen.kt:42）无需改动（类型变化自动适配）。本步只需**验证编译**，无源码改动。

```bash
./gradlew assembleDebug
```

Expected: 成功（若个别文件用了 M3 独有字段如 displayMedium，编译失败则在对应文件改为最近的层级，并记录到 commit message）

- [ ] **Step 3: 迁移 14 个 `LocalAppTypographyStyle` 文件**

文件清单（12 个 feature + 1 个 DS）：

```
feature/health/HealthScreen.kt
feature/ai/AiSettingsScreen.kt
feature/growth/GrowthScreen.kt
feature/message/MessageScreen.kt
feature/reminder/ReminderScreen.kt
feature/ai/AiChatScreen.kt
feature/home/HomeScreen.kt
feature/family/FamilyPage.kt
feature/timeline/TimelineScreen.kt
feature/vaccination/VaccinationListScreen.kt
feature/development/DevelopmentAssessmentScreen.kt
feature/stats/StatsScreen.kt
designsystem/components/markdown/AppMarkdownText.kt
```

每文件两步操作：
1. import 改：`import com.babytracker.designsystem.theme.LocalAppTypographyStyle` → `import com.babytracker.designsystem.theme.LocalAppTypography`
2. 全部 `LocalAppTypographyStyle.current.X` → `LocalAppTypography.current.Y`，按映射表改字段名（`label`→`labelMedium`、`headline`→`headlineMedium`、`display`→`displayLarge`；其余同名）。别名 `val typography = LocalAppTypographyStyle.current` → `val typography = LocalAppTypography.current`，别名后的 `typography.label` 等同样改字段名

用 sed 批量替换（每文件先 import 再字段）：

```bash
# 例：ReminderScreen.kt
sed -i 's/import com.babytracker.designsystem.theme.LocalAppTypographyStyle/import com.babytracker.designsystem.theme.LocalAppTypography/' \
  app/src/main/java/com/babytracker/feature/reminder/ReminderScreen.kt
sed -i 's/LocalAppTypographyStyle\.current/LocalAppTypography.current/g' \
  app/src/main/java/com/babytracker/feature/reminder/ReminderScreen.kt
sed -i 's/typography\.label\b/typography.labelMedium/g' \
  app/src/main/java/com/babytracker/feature/reminder/ReminderScreen.kt
```

⚠️ 手动核对每个文件的 `headline`/`display`/`label` 裸字段引用点（用 `rg -n "typography\.headline\b|typography\.display\b|typography\.label\b"` 查残留），`sed` 的 `\b` 在 BSD/GNU 行为一致，但**必须**用编译结果兜底。

- [ ] **Step 4: 迁移 DS 内部 13 处 `MaterialTheme.typography`**

文件与替换（`MaterialTheme.typography.X` → `LocalAppTypography.current.X`）：

```
components/dialog/AppFormSheet.kt:62  headlineSmall → headlineSmall
components/timepicker/TimePickerLogic.kt:164  titleLarge → titleLarge
components/datetimecascade/DateTimeCascade.kt:201,272  titleMedium → titleMedium
components/datetimecascade/DateTimeCascade.kt:299  bodySmall → bodySmall
components/datetimecascade/DateTimeCascade.kt:377  bodyMedium → bodyMedium
components/datetimecascade/DateTimeCascade.kt:418,449  labelSmall → labelSmall
components/datetimecascade/DateTimeCascade.kt:437  headlineMedium → headlineMedium
components/timepicker/TimePicker.kt:118,149  labelSmall → labelSmall
components/timepicker/TimePicker.kt:137  headlineMedium → headlineMedium
components/timepicker/TimePicker.kt:200  titleMedium → titleMedium
```

`MaterialTheme.typography.bodyMedium.copy(...)` 等带 copy 的调用保留 `.copy(...)`（如 DateTimeCascade.kt:377）。每个文件补充 import `com.babytracker.designsystem.theme.LocalAppTypography`；若 `MaterialTheme` import 因此变死 import 则删除。

- [ ] **Step 5: 编译 + 全量测试**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全部成功（若 `fontSize 不应重复` 测试失败，属预期，见 Task 1 说明）

- [ ] **Step 6: 提交**

```bash
git add app/src/main/java
git commit -m "Typography 双体系统一：LocalAppTypography 改供自建 AppTypography，迁移 31 文件与 13 处 M3 直用"
```

---

### Task 3: 删除裸字段与 LocalAppTypographyStyle（收口为 12 级）

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt:192-216`
- Modify: `app/src/test/java/com/babytracker/designsystem/theme/TypographyTokensTest.kt`

**Interfaces:**
- Consumes: Task 2 迁移后的代码（裸字段 0 引用）
- Produces: 最终 12 级 `AppTypography`：displayLarge/headlineLarge/headlineMedium/headlineSmall/titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/labelMedium/labelSmall

- [ ] **Step 1: 确认裸字段已 0 引用**

Run: `rg -n "typography\.display\b|typography\.headline\b|typography\.label\b|LocalAppTypographyStyle" app/src/main/java`
Expected: 无输出（有残留则回 Task 2 补迁移，不进入本任务）

- [ ] **Step 2: 删除裸字段**

AppTokens.kt 中删除 `display`、`headline`、`label` 三个字段定义，并把注释改为 `// —— 排版令牌（自建 12 级） ——`。

- [ ] **Step 3: 恢复严格测试断言**

`TypographyTokensTest.kt` 两个测试改为 12 字段清单（去掉 `t.display`/`t.headline`/`t.label`），顺序断言维持 `>=`（bodySmall 12sp 与 labelMedium 12sp 同为 12sp，允许相等），两端极值 `assertEquals(40f, ordered.first())` / `assertEquals(11f, ordered.last())` 不变。

- [ ] **Step 4: 编译 + 全量测试**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/babytracker/designsystem/theme/AppTokens.kt app/src/test/java/com/babytracker/designsystem/theme/TypographyTokensTest.kt
git commit -m "Typography 收口为自建 12 级，删除裸字段与 LocalAppTypographyStyle，禁暴露 M3 类型"
```

---

### Task 4: AppButton 收敛（替换 PrimaryButton/SecondaryButton/AppTextButton/PaiButton）

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`（ButtonTokens L24-57）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/button/Button.kt`（全量重写）
- Delete: `app/src/main/java/com/babytracker/designsystem/components/button/PaiButton.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/components/button/ButtonDefaults.kt`（新增 2 个函数）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`（注释索引）
- Modify: 14 个 feature 文件 58 处调用（见下清单）

**Interfaces:**
- Consumes: `AppButtonDefaults`（现有 10 函数）、`LocalAppColors`、`LocalAppComponentTokens`
- Produces:
  - `enum class ButtonVariant { Primary, Secondary, Text }`（替代 PaiButton.kt 的旧枚举）
  - `@Composable fun AppButton(label: String, onClick: () -> Unit, icon: ImageVector? = null, enabled: Boolean = true, variant: ButtonVariant = ButtonVariant.Primary, height: Dp = AppButtonDefaults.height(), cornerRadius: Dp = AppButtonDefaults.cornerRadius(), fontSize: TextUnit = AppButtonDefaults.fontSize(), fontWeight: FontWeight = AppButtonDefaults.fontWeight(), iconSize: Dp = AppButtonDefaults.iconSize(), containerColor: Color = AppButtonDefaults.containerColor(), contentColor: Color = AppButtonDefaults.contentColor(), disabledContainerColor: Color = AppButtonDefaults.disabledContainerColor(), disabledContentColor: Color = AppButtonDefaults.disabledContentColor(), modifier: Modifier = Modifier)`
  - ButtonTokens 新增字段：`secondaryContentColor: Color`（默认 `colors.primary`）、`textContentColor: Color`（默认 `colors.primary`）
  - ButtonDefaults 新增：`@Composable fun secondaryContentColor(): Color = LocalAppComponentTokens.current.button.secondaryContentColor`、`@Composable fun textContentColor(): Color = LocalAppComponentTokens.current.button.textContentColor`

- [ ] **Step 1: ButtonTokens 加字段**

AppComponentTokens.kt `ButtonTokens`（L24-57）增加两个字段与 default 派生：

```kotlin
val secondaryContentColor: Color,   // Secondary 变体内容色（描边色）
val textContentColor: Color,        // Text 变体内容色
```
default() 中：
```kotlin
secondaryContentColor = colors.primary,
textContentColor = colors.primary,
```

- [ ] **Step 2: ButtonDefaults 加函数**

`ButtonDefaults.kt` 末尾追加：
```kotlin
@Composable fun secondaryContentColor(): Color = LocalAppComponentTokens.current.button.secondaryContentColor
@Composable fun textContentColor(): Color = LocalAppComponentTokens.current.button.textContentColor
```

- [ ] **Step 3: 重写 Button.kt 为单一 AppButton**

删除三个旧函数，保留文件头注释风格，新实现（contentColor 等颜色参数默认 `Color.Unspecified`，表示未显式传参、按变体走令牌——与设计文档第五节约定一致）：

```kotlin
package com.babytracker.designsystem.components.button

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.ButtonDefaults as AppButtonDefaults

/** 按钮变体：Primary 填充 / Secondary 描边 / Text 文本 */
enum class ButtonVariant { Primary, Secondary, Text }

/**
 * 统一按钮 — 变体走枚举参数，替代 PrimaryButton/SecondaryButton/AppTextButton。
 *
 * 用法：
 *   AppButton(label = "保存", onClick = { ... })
 *   AppButton(label = "取消", variant = ButtonVariant.Secondary, onClick = { ... })
 *   AppButton(label = "复制", variant = ButtonVariant.Text, icon = Icons.Default.ContentCopy, onClick = { ... })
 */
@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    height: Dp = AppButtonDefaults.height(),
    cornerRadius: Dp = AppButtonDefaults.cornerRadius(),
    fontSize: TextUnit = AppButtonDefaults.fontSize(),
    fontWeight: FontWeight = AppButtonDefaults.fontWeight(),
    iconSize: Dp = AppButtonDefaults.iconSize(),
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    disabledContainerColor: Color = Color.Unspecified,
    disabledContentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    // Color.Unspecified 表示未显式传参，按变体走对应令牌；显式参数 > XxxDefaults 约定
    val resolvedContainer = if (containerColor == Color.Unspecified) AppButtonDefaults.containerColor() else containerColor
    val resolvedContent = when {
        contentColor != Color.Unspecified -> contentColor
        variant == ButtonVariant.Secondary -> AppButtonDefaults.secondaryContentColor()
        variant == ButtonVariant.Text -> AppButtonDefaults.textContentColor()
        else -> AppButtonDefaults.contentColor()
    }
    val resolvedDisabledContainer =
        if (disabledContainerColor == Color.Unspecified) AppButtonDefaults.disabledContainerColor() else disabledContainerColor
    val resolvedDisabledContent =
        if (disabledContentColor == Color.Unspecified) AppButtonDefaults.disabledContentColor() else disabledContentColor

    val content: @Composable () -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
        Text(label, fontSize = fontSize, fontWeight = fontWeight)
    }
    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.buttonColors(
                containerColor = resolvedContainer,
                contentColor = resolvedContent,
                disabledContainerColor = resolvedDisabledContainer,
                disabledContentColor = resolvedDisabledContent,
            ),
            content = content,
        )
        ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            modifier = modifier.height(height),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContent,
                disabledContentColor = resolvedDisabledContent,
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
            content = content,
        )
        ButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = ButtonDefaults.textButtonColors(
                contentColor = resolvedContent,
                disabledContentColor = resolvedDisabledContent,
            ),
            content = content,
        )
    }
}
```

- [ ] **Step 4: 删除 PaiButton.kt**

```bash
rm app/src/main/java/com/babytracker/designsystem/components/button/PaiButton.kt
```

- [ ] **Step 5: 迁移 58 处调用（14 个文件）**

调用文件清单与数量：AiChatScreen(10)、SettingsScreen(14)、FamilyPage(6)、VaccinationListScreen(5)、FeedingListScreen(3)、SleepListScreen(3)、DiaperListScreen(1)、LoginScreen(2)、HomeScreen(1)、GrowthScreen(1)、BabyProfileScreen(2)、SyncSettingsScreen(1)、TimelineScreen(1)、HealthScreen(0)。

替换规则（每个文件逐个处理）：

```bash
# 1. 先确认无位置参数调用（AppButton 的 label 是第一个参数，位置调用必须补参数名）
rg -n "PrimaryButton\([^=)]*," app/src/main/java/com/babytracker/feature  # 应无输出

# 2. 机械替换函数名（命名参数调用不受参数顺序影响）
sed -i 's/PrimaryButton(/AppButton(/g; s/SecondaryButton(/AppButton(/g; s/AppTextButton(/AppButton(/g' <每个文件>

# 3. Secondary/Text 变体必须显式加 variant 参数
#    SecondaryButton(onClick = X, label = Y) → AppButton(label = Y, variant = ButtonVariant.Secondary, onClick = X)
#    AppTextButton(onClick = X, label = Y) → AppButton(label = Y, variant = ButtonVariant.Text, onClick = X)
#    （逐个手动处理，不能用 sed 全量——variant 参数位置在 onClick 之后任意位置均可）
```

⚠️ Secondary/Text 的迁移是**逐文件手动**步骤，不能 sed 一把梭。每文件处理后用编译兜底。颜色参数映射：`SecondaryButton(color = X)` → `AppButton(variant = Secondary, contentColor = X)`；`AppTextButton(color = X)` → `AppButton(variant = Text, contentColor = X)`。

- [ ] **Step 6: 更新 AppComponents.kt 注释索引**

`// PrimaryButton 主按钮（填充）` 三行改为 `// AppButton        统一按钮（变体枚举）    (button/)`。

- [ ] **Step 7: 编译 + 全量测试**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿。若有漏网引用（`rg -n "PrimaryButton\(|SecondaryButton\(|AppTextButton\(|PaiButton\("` 应无输出），回 Step 5。

- [ ] **Step 8: 提交**

```bash
git add app/src/main/java
git commit -m "Button 家族收敛为 AppButton + ButtonVariant 枚举，迁移 58 处调用方，颜色改走 ButtonTokens"
```

---

### Task 5: 新增 AppDivider 并迁移 6 处 HorizontalDivider

**Files:**
- Create: `app/src/main/java/com/babytracker/designsystem/components/divider/Divider.kt`、`DividerDefaults.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`（新增 `DividerTokens` + `divider` 字段 + default 派生）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`（注释）
- Modify: 6 个 feature 文件

**Interfaces:**
- Consumes: `AppColors`、`AppComponentTokens`
- Produces: `AppDivider(modifier: Modifier = Modifier, color: Color = DividerDefaults.color(), thickness: Dp = DividerDefaults.thickness())`

- [ ] **Step 1: DividerTokens**

AppComponentTokens.kt 中（紧跟 TagTokens 后）新增：

```kotlin
// —— TT-033 分割线 ——
@Immutable
data class DividerTokens(
    val color: Color,
    val thickness: Dp,
) {
    companion object {
        fun default(colors: AppColors): DividerTokens = DividerTokens(
            color = colors.divider,
            thickness = 0.5.dp,
        )
    }
}
```

聚合类加 `val divider: DividerTokens` 并在 `AppComponentTokens.default()` 中加 `divider = DividerTokens.default(colors)`。检查现有聚合字段列表后按字母/编号位置插入。

- [ ] **Step 2: 组件与 Defaults**

`DividerDefaults.kt`：
```kotlin
@Immutable
object DividerDefaults {
    @Composable fun color(): Color = LocalAppComponentTokens.current.divider.color
    @Composable fun thickness(): Dp = LocalAppComponentTokens.current.divider.thickness
}
```

`Divider.kt`：
```kotlin
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color(),
    thickness: Dp = DividerDefaults.thickness(),
) {
    HorizontalDivider(modifier = modifier, color = color, thickness = thickness)
}
```
（HorizontalDivider 来自 `androidx.compose.material3.HorizontalDivider`）

- [ ] **Step 3: 迁移 6 处**

文件与替换（`c.divider`/`0.5.dp` 显式值可省略走令牌，但为减少视觉风险**保留显式参数**）：

```
settings/LogViewerScreen.kt:249  HorizontalDivider( → AppDivider(
timeline/TimelineScreen.kt:131   HorizontalDivider(color = c.divider, thickness = 0.5.dp) → AppDivider(color = c.divider, thickness = 0.5.dp)
settings/SettingsScreen.kt:397   HorizontalDivider( → AppDivider(
settings/BabyProfileScreen.kt:300 HorizontalDivider( → AppDivider(
family/FamilyPage.kt:362        HorizontalDivider(color = c.divider, thickness = 0.5.dp) → AppDivider(...)
home/HomeScreen.kt:396           HorizontalDivider(color = c.divider, thickness = 0.5.dp, modifier = ...) → AppDivider(...)
```

每文件：替换函数名 + 补 import `com.babytracker.designsystem.components.divider.AppDivider`；删除多余 M3 import（如 `androidx.compose.material3.HorizontalDivider`）若变死 import。

- [ ] **Step 4: 编译 + 测试 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/main/java
git commit -m "新增 AppDivider 组件（DividerTokens），迁移 6 处 HorizontalDivider"
```

---

### Task 6: 新增 AppSurface 并迁移 3 处 Surface

**Files:**
- Create: `app/src/main/java/com/babytracker/designsystem/components/surface/Surface.kt`、`SurfaceDefaults.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`（`SurfaceTokens`）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`
- Modify: 2 个 feature 文件（LogViewerScreen 2 处、SettingsScreen 1 处）

**Interfaces:**
- Produces: `AppSurface(modifier: Modifier = Modifier, color: Color = SurfaceDefaults.color(), shape: Shape = SurfaceDefaults.shape(), content: @Composable () -> Unit)`

- [ ] **Step 1: SurfaceTokens**

```kotlin
// —— TT-034 表面容器 ——
@Immutable
data class SurfaceTokens(
    val color: Color,
    val shape: Shape,
) {
    companion object {
        fun default(colors: AppColors, shapes: AppShapes): SurfaceTokens = SurfaceTokens(
            color = colors.surface,
            shape = shapes.scaled(shapes.medium),   // 卡片级圆角
        )
    }
}
```
聚合类加字段与 default 派生（`SurfaceTokens.default(colors, shapes)`）。

- [ ] **Step 2: 组件与 Defaults**

`SurfaceDefaults.kt`：
```kotlin
@Immutable
object SurfaceDefaults {
    @Composable fun color(): Color = LocalAppComponentTokens.current.surface.color
    @Composable fun shape(): Shape = LocalAppComponentTokens.current.surface.shape
}
```

`Surface.kt`：
```kotlin
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    color: Color = SurfaceDefaults.color(),
    shape: Shape = SurfaceDefaults.shape(),
    content: @Composable () -> Unit,
) {
    Surface(modifier = modifier, color = color, shape = shape, content = content)
}
```

- [ ] **Step 3: 迁移 3 处**

```
settings/LogViewerScreen.kt:177  Surface( → AppSurface(  （检查该处是否有 tonalElevation/border 参数——有则保留）
settings/LogViewerScreen.kt:258  Surface( → AppSurface(
settings/SettingsScreen.kt:520   Surface(color = c.primaryContainer, shape = RoundedCornerShape(shapes.medium)) { ... } → AppSurface(color = c.primaryContainer, shape = RoundedCornerShape(shapes.medium)) { ... }
```

每文件补 import；M3 `Surface` import 若变死则删除。

- [ ] **Step 4: 编译 + 测试 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/main/java
git commit -m "新增 AppSurface 组件（SurfaceTokens），迁移 3 处 M3 Surface"
```

---

### Task 7: 新增 AppSnackbarHost 并迁移 9 处 SnackbarHost

**Files:**
- Create: `app/src/main/java/com/babytracker/designsystem/components/snackbar/SnackbarHost.kt`、`SnackbarHostDefaults.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`（`SnackbarHostTokens`）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`
- Modify: 9 个 feature 文件（`snackbarHost = { SnackbarHost(snackbarHostState) }` → `snackbarHost = { AppSnackbarHost(snackbarHostState) }`）

**Interfaces:**
- Consumes: `SnackbarHostState`（M3）
- Produces: `AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier)`

- [ ] **Step 1: SnackbarHostTokens**

```kotlin
// —— TT-035 全局提示宿主 ——
@Immutable
data class SnackbarHostTokens(
    val containerColor: Color,
    val contentColor: Color,
    val cornerRadius: Dp,
    val elevation: Dp,
) {
    companion object {
        fun default(colors: AppColors, shapes: AppShapes, elevation: AppElevation): SnackbarHostTokens =
            SnackbarHostTokens(
                containerColor = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
                cornerRadius = shapes.scaled(shapes.small),
                elevation = elevation.medium,
            )
    }
}
```
聚合加字段与派生。`AppColors` 需有 `inverseSurface`/`inverseOnSurface`（39 字段含 inverseSurface、inverseOnSurface——已确认存在）。

- [ ] **Step 2: 组件与 Defaults**

`SnackbarHostDefaults.kt`：
```kotlin
@Immutable
object SnackbarHostDefaults {
    @Composable fun containerColor(): Color = LocalAppComponentTokens.current.snackbarHost.containerColor
    @Composable fun contentColor(): Color = LocalAppComponentTokens.current.snackbarHost.contentColor
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.snackbarHost.cornerRadius
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.snackbarHost.elevation
}
```

`SnackbarHost.kt`：
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SnackbarHostDefaults.containerColor(),
                contentColor = SnackbarHostDefaults.contentColor(),
                shape = RoundedCornerShape(SnackbarHostDefaults.cornerRadius()),
                tonalElevation = SnackbarHostDefaults.elevation(),
            )
        },
    )
}
```
⚠️ 确认 `androidx.compose.material3.Snackbar` 的 `tonalElevation` 参数可用（material3 最新版若改名用 `elevation`，以编译为准）。

- [ ] **Step 3: 迁移 9 处**

```
ai/AiChatScreen.kt:126
vaccination/VaccinationListScreen.kt:134
timeline/TimelineScreen.kt:106
sleep/SleepListScreen.kt:107
reminder/ReminderScreen.kt:71
health/HealthScreen.kt:114
growth/GrowthScreen.kt:93
feeding/FeedingListScreen.kt:97
diaper/DiaperListScreen.kt:103
```

每处：`SnackbarHost(snackbarHostState)` → `AppSnackbarHost(snackbarHostState)` + import。M3 SnackbarHost import 变死则删。

- [ ] **Step 4: 编译 + 测试 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/main/java
git commit -m "新增 AppSnackbarHost 组件（SnackbarHostTokens），迁移 9 处 M3 SnackbarHost"
```

---

### Task 8: EmptyState 令牌化

**Files:**
- Modify: `app/src/main/java/com/babytracker/designsystem/components/EmptyState.kt`
- Create: `app/src/main/java/com/babytracker/designsystem/components/EmptyStateDefaults.kt`
- Modify: `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt`（`EmptyStateTokens`）
- Modify: `app/src/main/java/com/babytracker/designsystem/components/AppComponents.kt`

**Interfaces:**
- Consumes: `AppButton`（Task 4）、`LocalAppColors`、`LocalAppTypography`（12 级）、`AppComponentTokens`
- Produces: `EmptyStateTokens(titleColor, subtitleColor, emojiSize, actionSpacing)` + `EmptyStateDefaults`（titleColor/subtitleColor/emojiSize/actionSpacing 4 函数），`EmptyState` 签名不变（10 个 feature 调用方零改动）

- [ ] **Step 1: EmptyStateTokens**

```kotlin
// —— TT-036 空状态 ——
@Immutable
data class EmptyStateTokens(
    val emojiSize: TextUnit,
    val titleColor: Color,
    val subtitleColor: Color,
    val actionSpacing: Dp,
) {
    companion object {
        fun default(colors: AppColors, spacing: AppSpacing): EmptyStateTokens = EmptyStateTokens(
            emojiSize = 56.sp,
            titleColor = colors.onSurface,
            subtitleColor = colors.textSecondary,
            actionSpacing = spacing.lg,
        )
    }
}
```
聚合类加字段与派生。

- [ ] **Step 2: EmptyStateDefaults + 重写 EmptyState**

`EmptyStateDefaults.kt`：4 个 `@Composable` 读取函数（emojiSize/titleColor/subtitleColor/actionSpacing）。

重写 `EmptyState.kt`（保留现有 API 签名与注释）：

```kotlin
package com.babytracker.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.button.AppButton
import com.babytracker.designsystem.theme.LocalAppTypography

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = EmptyStateDefaults.emojiSize())
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = LocalAppTypography.current.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = EmptyStateDefaults.titleColor(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = LocalAppTypography.current.bodySmall,
            color = EmptyStateDefaults.subtitleColor(),
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            AppButton(label = actionText, onClick = onAction)
        }
    }
}
```

（AppButton 默认高度 48dp 替代原 M3 Button 高度；圆角走 ButtonTokens。`actionSpacing` 字段本步先只做令牌定义与 Defaults，若视觉不变可用 `20.dp` 常量——为最小改动，本任务中 actionSpacing 令牌保留定义但不强制替换 20.dp 常量，避免无谓视觉抖动）

- [ ] **Step 3: 编译 + 测试 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/src/main/java
git commit -m "EmptyState 令牌化：新增 EmptyStateTokens，M3 Button/colorScheme 改走令牌体系"
```

---

### Task 9: AlertDialog 改造 + 死 import 清理

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt:690-720`
- Modify: `app/src/main/java/com/babytracker/feature/reminder/ReminderScreen.kt:10`（死 import）
- Modify: `app/src/main/java/com/babytracker/feature/timeline/TimelineScreen.kt:13`（死 import）

**Interfaces:**
- Consumes: `AppDialog`（现有签名 `AppDialog(show, title, text, content, confirmText, cancelText, confirmEnabled, onConfirm, onDismiss, ...)`）、`AppButton`（Task 4）
- Produces: 无（纯消费）

- [ ] **Step 1: NicknameEditDialog 迁移到 AppDialog**

`SettingsScreen.kt` 中 `NicknameEditDialog` 内的 M3 `AlertDialog(...)` 块替换为：

```kotlin
AppDialog(
    show = true,
    title = AppStrings.editNickname,
    content = {
        AppInput(
            value = input,
            onValueChange = { input = it },
            label = AppStrings.nicknameHint,
            placeholder = "输入你喜欢的昵称",
            modifier = Modifier.fillMaxWidth(),
        )
    },
    confirmText = AppStrings.save,
    cancelText = AppStrings.cancel,
    confirmEnabled = input.isNotBlank(),
    onConfirm = { onSave(input.trim()) },
    onDismiss = onDismiss,
)
```

⚠️ 行为变化：原 dismissButton 内的「清除」快捷按钮移除（AppDialog 无自定义 dismiss 插槽，遵循简单优先；用户仍可在输入框内手动清空）。`onClear` 参数与调用方同步删除；`AppStrings.cancel` 若不存在则查 `AppStrings` 现有键（如 `AppStrings.cancel` 或既有取消文案），没有则补充进 AppStrings。`AppTextButton` 的 import 与旧 AlertDialog import 一并清理。

- [ ] **Step 2: 死 import 清理**

```
ReminderScreen.kt:10   删除 import androidx.compose.material3.AlertDialog（确认无其他使用）
TimelineScreen.kt:13   删除 import androidx.compose.material3.rememberModalBottomSheetState
```

- [ ] **Step 3: 编译 + 全量测试 + lint**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew lint`
Expected: 全绿（lint 无新增告警）

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java
git commit -m "编辑昵称对话框迁移 AppDialog（移除清除快捷按钮），清理两处死 import"
```

---

### Task 10: 审计测试扩展 + 文档同步 + CHANGELOG

**Files:**
- Modify: `app/src/test/java/com/babytracker/designsystem/theme/ThemeTokenizationStaticAuditTest.kt`
- Modify: `docs/design-system.md`
- Modify: `AGENTS.md`（第八节）
- Modify: `docs/project-structure.md`（若组件目录有增删）
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: 全部前序任务产物
- Produces: 审计规则覆盖新组件；文档与 AGENTS.md 同步

- [ ] **Step 1: 扩展静态审计测试**

`ThemeTokenizationStaticAuditTest.kt`：

1. 修复头部注释漂移（`PaletteTheme.colors/componentThemes` 旧命名 → `LocalAppComponentTokens`）
2. 新增断言「新组件 Defaults 必须注册进 AppComponentTokens」：

```kotlin
@Test
fun `新组件 Defaults 应被 AppComponentTokens 覆盖`() {
    val componentTokensFile = File("app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt").readText()
    val required = listOf("divider", "surface", "snackbarHost", "emptyState")
    val missing = required.filterNot { componentTokensFile.contains("val $it:") }
    assert(missing.isEmpty()) { "AppComponentTokens 缺少组件令牌字段: $missing" }
}
```

3. 新增断言「M3 令牌类型不暴露给组件层（theme 层桥接豁免）」：

```kotlin
@Test
fun `组件层不应导入 M3 令牌与主题类型`() {
    // theme 层是唯一允许桥接 M3 的位置
    val root = File("app/src/main/java/com/babytracker")
    val m3TokenImports = listOf(
        "import androidx.compose.material3.Typography",
        "import androidx.compose.material3.ColorScheme",
        "import androidx.compose.material3.Shapes",
    )
    val violations = root.walkTopDown()
        .filter { it.isFile && it.name.endsWith(".kt") }
        .filterNot { it.path.contains("/designsystem/theme/") }
        .filter { file ->
            val text = file.readText()
            m3TokenImports.any { text.contains(it) } ||
                text.contains("MaterialTheme.typography") ||
                text.contains("MaterialTheme.colorScheme") ||
                text.contains("MaterialTheme.shapes")
        }
        .toList()
    assert(violations.isEmpty()) {
        "组件层暴露 M3 令牌/主题类型（token 只允许在 theme 层桥接 M3）:\n" +
            violations.joinToString("\n") { it.path }
    }
}
```

- [ ] **Step 2: 更新 docs/design-system.md**

- 顶部「对应版本」行更新为当前版本号 + 日期
- 组件用法速查：`PrimaryButton(...)`/`SecondaryButton(...)`/`AppTextButton(...)` 三行合并为 `AppButton(label = "保存", onClick = { ... }, variant = ButtonVariant.Secondary) // 统一按钮（Primary/Secondary/Text 变体枚举）`
- 新增 `AppDivider`/`AppSurface`/`AppSnackbarHost` 三行速查
- 令牌清单：`AppComponentTokens` 聚合字段 19 个 → 23 个（+ divider/surface/snackbarHost/emptyState，以代码为准核对）
- Typography：注明「自建 12 级：displayLarge/headlineLarge/headlineMedium/headlineSmall/titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/labelMedium/labelSmall」

- [ ] **Step 3: 更新 AGENTS.md 第八节**

`对应关系` 中 `Button → PrimaryButton/SecondaryButton，TextButton → AppTextButton` 改为 `Button/OutlinedButton/TextButton → AppButton（variant 枚举 Primary/Secondary/Text）`。

- [ ] **Step 4: 更新 CHANGELOG.md**

按提交批次追加条目（在 `[Unreleased]` 下，版本发布批次统一挂版本号），内容涵盖：Typography 自建 12 级单体系、AppButton 收敛、3 个新组件、EmptyState 令牌化、AlertDialog 迁移、死 import 清理。

- [ ] **Step 5: 全量验证**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew lint`
Expected: 全绿

- [ ] **Step 6: 提交**

```bash
git add app/src/test docs AGENTS.md CHANGELOG.md
git commit -m "审计测试覆盖新组件与 M3 Typography 禁入规则，同步设计系统文档与 AGENTS 映射"
```

---

## P1 验收清单（全部任务完成后）

- [ ] `./gradlew assembleDebug` 通过
- [ ] `./gradlew testDebugUnitTest` 全绿（TypographyTokensTest 3 项 + 扩展后的静态审计 5 项 + 既有测试）
- [ ] `rg -n "PrimaryButton\(|SecondaryButton\(|AppTextButton\(|PaiButton\("` 无输出
- [ ] `rg -n "MaterialTheme\.typography|MaterialTheme\.colorScheme"` 仅剩 designsystem 内部受控位置（无）
- [ ] `rg -n "LocalAppTypographyStyle|BabyTrackerTypography"` 无输出
- [ ] `rg -n "HorizontalDivider\(|material3.Surface|SnackbarHost\("` feature 下无输出
- [ ] 文档同步：design-system.md / AGENTS.md / CHANGELOG.md / project-structure.md
- [ ] 设计文档 `docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md` 的 3.1-3.5 全部落地（若实现与设计有出入，先改设计文档再改代码）

## 备注（P2/P3 衔接）

- P2 密度变体的 LocalAppSpacing 缩放机制不受本计划影响（AppSpacing 结构未动）
- P3 themeTokenAudit 检查器将从 Task 10 的静态审计测试提取共享逻辑
- 若本计划执行中发现 lessons.md 未覆盖的新坑（如双体系并存、PaiButton 死代码），按 AGENTS.md 第九节在提交时追加条目
