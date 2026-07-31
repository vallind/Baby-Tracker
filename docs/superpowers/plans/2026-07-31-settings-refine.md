# 设置页精致感重塑 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重塑设置页主屏排版/图标/间距，作为精致感示范（Apple HIG × Linear × AntD × M3）。

**Architecture:** 只改 `SettingsScreen.kt`。`SettingsRow` 是共享函数（4 文件 25 处调用），按红线 #10 不动它，新增页面私有 `SettingsIconRow` 承载新样式；排版统一引用 `LocalAppTypographyStyle`（AppTypography 7 级单源），不再引用 M3 `BabyTrackerTypography` 字段。

**Tech Stack:** Jetpack Compose + Material3 + material-icons-extended（已依赖）

**Spec:** `docs/superpowers/specs/2026-07-31-settings-refine-design.md`

---

### Task 1: 新增 SettingsIconRow 并切换主屏 6 行

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt`

`SettingsRow`（line 413-446）是共享公开函数（SyncSettingsScreen/SettingsMenuScreen/AiSettingsScreen 也在用），**保持不动**。在其下方新增私有 `SettingsIconRow`，支持矢量图标 + 升级排版（行标题 `titleMedium` 16sp、副标题 `bodyMedium` 13sp、图标 24dp 主色、40dp primaryContainer 容器）。

- [ ] **Step 1: 在 SettingsRow 函数后（line 446 之后）新增 SettingsIconRow**

```kotlin
/**
 * 设置行（图标版）— 精致感示范：
 * 矢量图标 + titleMedium 行标题 + bodyMedium 副标题。
 * 共享 SettingsRow 保持不动，等风格验证后统一迁移。
 */
@Composable
private fun SettingsIconRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current
    AppListItem(
        leadingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(shapes.large))
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        headlineContent = {
            Text(label, style = LocalAppTypographyStyle.current.titleMedium, color = c.textPrimary)
        },
        supportingContent = subtitle?.let {
            {
                Text(it, style = LocalAppTypographyStyle.current.bodyMedium, color = c.textSecondary)
            }
        },
        trailingContent = trailing ?: {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        },
        onClick = onClick,
    )
}
```

- [ ] **Step 2: 主屏 6 处 SettingsRow 调用切换为 SettingsIconRow（line 137-187）**

第一处（宝宝与家庭组，line 137-162）：

```kotlin
            SettingsCard {
                SettingsIconRow(
                    icon = Icons.Default.ChildCare,
                    label = "宝宝管理",
                    subtitle = "资料、成长信息与宝宝切换",
                    onClick = { navController.navigate(Screen.BabyManagement.route) },
                )
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Default.People,
                    label = "家庭与账号",
                    subtitle = if (isLoggedIn) "成员管理与账号信息" else "登录后与家人共享记录",
                    onClick = {
                        navController.navigate(
                            if (isLoggedIn) Screen.Family.route else Screen.Login.route
                        )
                    },
                )
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Default.Notifications,
                    label = "提醒设置",
                    subtitle = "喂养、睡眠与护理提醒",
                    onClick = { navController.navigate(Screen.Reminder.route) },
                )
            }
```

第二处（更多设置组，line 167-188）：

```kotlin
            SettingsCard {
                SettingsIconRow(
                    icon = Icons.Default.Palette,
                    label = "使用偏好",
                    subtitle = "主题与 AI 助手",
                    onClick = { navController.navigate(Screen.PreferenceSettings.route) },
                )
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Default.Cloud,
                    label = "数据与同步",
                    subtitle = "云同步、备份与隐私",
                    onClick = { navController.navigate(Screen.DataSettings.route) },
                )
                SettingsDivider()
                SettingsIconRow(
                    icon = Icons.Default.HelpOutline,
                    label = "帮助与关于",
                    subtitle = "问题反馈、运行日志与版本信息",
                    onClick = { navController.navigate(Screen.SupportSettings.route) },
                )
            }
```

图标对应：宝宝管理→`ChildCare`、家庭与账号→`People`、提醒设置→`Notifications`、使用偏好→`Palette`、数据与同步→`Cloud`、帮助与关于→`HelpOutline`（均来自 `androidx.compose.material.icons.filled.*`，extended 已依赖）。

- [ ] **Step 3: 确认无编译依赖问题**

`SettingsIconRow` 是私有函数仅本文件使用；`ImageVector` 需确认 import：文件已 import `androidx.compose.material.icons.Icons`，但需新增 `androidx.compose.ui.graphics.vector.ImageVector`。在 import 区（line 33 附近 `import androidx.compose.ui.graphics.Color` 后）加：

```kotlin
import androidx.compose.ui.graphics.vector.ImageVector
```

（`Icon` 已在 line 28 `import androidx.compose.material3.Icon`，`Icons.Default.*` 已通过 line 58 `import androidx.compose.material.icons.filled.*` 覆盖。）

---

### Task 2: 排版统一（UserInfoCard + SettingsSectionTitle）

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt`

- [ ] **Step 1: UserInfoCard 头像首字母换 AppTypography（line 282）**

```kotlin
                Text(
                    displayName.take(1).ifEmpty { "?" },
                    style = LocalAppTypographyStyle.current.headline,
                    fontWeight = FontWeight.Bold,
                    color = c.onPrimary,
                )
```

（原 `LocalAppTypography.current.headlineSmall` 24sp → `LocalAppTypographyStyle.current.headline` 24sp，等值替换，消除 M3 引用）

- [ ] **Step 2: UserInfoCard 显示名升级 titleLarge（line 291-297）**

```kotlin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = LocalAppTypographyStyle.current.titleLarge,
                        color = c.textPrimary,
                    )
```

（原 `titleMedium + fontWeight = SemiBold` → `titleLarge`(18sp SemiBold)，去掉冗余 fontWeight）

- [ ] **Step 3: UserInfoCard 副文案升级 bodyMedium（line 311-318）**

```kotlin
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = if (isLoggedIn && displayAccount != null) {
                        if (nickname != null) "账号: ${displayAccount.take(8)}…" else "ID: ${displayAccount.take(8)}…"
                    } else "点击登录账号",
                    style = LocalAppTypographyStyle.current.bodyMedium,
                    color = c.textTertiary,
                    maxLines = 1,
                )
```

（原 `LocalAppTypography.current.bodySmall` → `bodyMedium`）

- [ ] **Step 4: SettingsSectionTitle 换 AppTypography（line 341-346）**

```kotlin
    Text(
        title,
        style = LocalAppTypographyStyle.current.label,
        color = c.textSecondary,
        modifier = Modifier.padding(bottom = spacing.sm),
    )
```

（原 `LocalAppTypography.current.labelMedium`(12sp Medium) → `LocalAppTypographyStyle.current.label`(12sp Medium)，等值替换）

---

### Task 3: 间距节奏

**Files:**
- Modify: `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt`

- [ ] **Step 1: 页面顶部间距（line 115）**

```kotlin
            Spacer(Modifier.height(spacing.md))
```

（原硬编码 `12.dp` → `spacing.md`(16dp)）

- [ ] **Step 2: 两组分组间距（line 134、line 164）**

```kotlin
            Spacer(Modifier.height(20.dp))
```

（原 `12.dp` → 20dp，Apple 分组留白；此值不在 AppSpacing 内，属页面级语义间距，注释说明即可）

- [ ] **Step 3: 退出登录区上方（line 190-191）**

```kotlin
            Spacer(Modifier.height(spacing.lg))
```

（现状已是 `spacing.lg`(24dp) = 设计目标，**无需改动**，仅确认）

---

### Task 4: 编译验证

- [ ] **Step 1: 编译**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL（无报错）

- [ ] **Step 2: Lint**

```bash
./gradlew lint
```

Expected: 无新增 error/warning（或仅有与本次改动无关的存量问题）

---

### Task 5: CHANGELOG + 提交

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 1: CHANGELOG 添加版本条目（文件顶部 `### [1.7.8]` 之前）**

```markdown
### [1.8.0] — 2026-07-31

**精致感重塑示范（设置页）：**
- 设置页主屏行从 emoji 文本改为矢量图标（ChildCare/People/Notifications/Palette/Cloud/HelpOutline），保留 40dp 主色容器
- 行标题升级为 titleMedium(16sp)、副标题升级为 bodyMedium(13sp)，用户卡显示名升级 titleLarge(18sp)
- 分组间距 12dp → 20dp、页面顶部 12dp → 16dp，均走间距令牌
- 主屏排版全部收敛到 AppTypography 7 级单源，消除 M3 BabyTrackerTypography 混用
- 新增页面私有 `SettingsIconRow`，共享 `SettingsRow` 保持不动（风格验证后再统一迁移）
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt CHANGELOG.md
git commit -m "feat: 设置页精致感重塑示范 — 矢量图标/排版层级/间距节奏"
```

---

## Self-Review

**Spec 覆盖检查：**
- 排版统一（spec §1）→ Task 1（SettingsIconRow 内 titleMedium/bodyMedium）+ Task 2（UserInfoCard/SectionTitle）✅
- emoji → 图标（spec §2）→ Task 1 Step 2（6 行 + 图标映射表）✅
- 间距节奏（spec §3）→ Task 3 ✅
- 明确不做（spec §4）→ 计划未触碰 AppTypography/组件文件/其他屏幕 ✅
- 验证（spec 验证标准）→ Task 4 ✅

**占位符扫描：** 全部步骤含完整代码，无 TBD/TODO ✅

**类型一致性：** `SettingsIconRow(icon: ImageVector, ...)` 在 Task 1 定义并被 Task 1 Step 2 调用；`LocalAppTypographyStyle.current.titleMedium/bodyMedium/titleLarge/headline/label` 均存在于 AppTypography（AppTokens.kt line 194-216）✅。图标名 `ChildCare/People/Notifications/Palette/Cloud/HelpOutline` 均为 material-icons-extended 标准图标 ✅。

**边界确认：** `UserInfoCard`(line 243)、`SettingsSectionTitle`(line 338)、`SettingsDivider`(line 402)、`SettingsCard`(line 397) 均为本文件私有或仅主屏使用；`SettingsRow` 共享不动 ✅。`BackupScreen`/`BabyManagementScreen`/`ThemePickerSheet` 均在计划外 ✅。
