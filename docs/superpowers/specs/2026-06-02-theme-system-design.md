# 主题系统完整化 — 设计文档

日期：2026-06-02
项目：MyApp 宝宝记录

## 背景

当前所有 UI 文件通过 `DT` (DesignTokens) 静态 object 引用颜色：

```kotlin
object DT {
    val primary = Color(0xFF6C8DFF)
    val bg = Color(0xFFF8F9FC)
    // ... 全部是静态常量
}
```

`ThemeController` 能切换 `AppTheme` 状态，但 `Theme.kt` 只将 `colors.primary` 传给 Material color scheme。其余颜色（bg、card、textPrimary、textSecondary、divider）在所有 UI 文件中直接引用 `DT.bg` 等静态常量，不随主题变化。

效果：切换主题只有主色变化，背景/卡片/文字色不变，夜间主题等于没实现。

## 目标

1. 颜色随 `AppTheme` 动态变化（5 套主题全部颜色生效）
2. 可视化主题选择器 UI（取代循环切换）
3. 主题偏好持久化（重启后保持）

## 方案

### 1. CompositionLocal 注入

`Theme.kt` 新增 `LocalThemeColors`，在 `BabyTrackerTheme` 中注入当前 `ThemeColors`：

```kotlin
val LocalThemeColors = compositionLocalOf { ThemeColors(...) }

@Composable
fun BabyTrackerTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val colors = theme.colors
    CompositionLocalProvider(LocalThemeColors provides colors) {
        MaterialTheme(
            colorScheme = if (theme.name == "night")
                darkColorScheme(primary = colors.primary)
            else lightColorScheme(primary = colors.primary),
            content = content,
        )
    }
}
```

### 2. 全量颜色替换规则

所有 UI 文件中的颜色引用按以下规则替换：

| 原引用 | 替换为 | 说明 |
|---------|--------|------|
| `DT.bg` | `c.bg` | 页⾯背景 |
| `DT.card` | `c.card` | 卡片背景 |
| `DT.textPrimary` | `c.textPrimary` | 主文字 |
| `DT.textSecondary` | `c.textSecondary` | 辅助文字 |
| `DT.divider` | `c.divider` | 分割线 |

以下保持 `DT.xxx` 不变（仅几何常量）：

- `DT.cardRadius` / `DT.pageMargin` / `DT.buttonRadius` / `DT.inputRadius`
- `DT.iconSize` / `DT.iconBgSize` / `DT.appBarHeight` / `DT.cardGap`

**所有色值**（含 primaryLight、success、warning、danger、pink、blue、green、yellow、purple、cyan、textHint、tagBg、tagText）均加入 `ThemeColors`，随主题变化。

每个 Composable 顶层获取：

```kotlin
@Composable
fun SomeScreen(...) {
    val c = LocalThemeColors.current
    // c.bg, c.card, c.textPrimary, c.divider ...
}
```

### 3. 主题持久化

使用 `SharedPreferences`，`ThemeController` 在构造时读取、切换时写入：

```
KEY = "theme_name", default = "pure"

init:  sp.getString(KEY, "pure") → currentTheme
switchTheme(name): sp.edit().putString(KEY, name).apply() + currentTheme = ...
```

Koin 模块注入 `SharedPreferences` 实例。

### 3.5 Expanded ThemeColors

`ThemeColors` 扩展为包含所有 DT 色值：

```kotlin
data class ThemeColors(
    val primary: Color,
    val bg: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val primaryLight: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pink: Color,
    val blue: Color,
    val green: Color,
    val yellow: Color,
    val purple: Color,
    val cyan: Color,
    val textHint: Color,
    val tagBg: Color,
    val tagText: Color,
)
```

每个 `AppTheme` preset 定义所有字段。夜间主题各颜色调暗适配深色背景。

### 4. 可视化主题选择器

替换 `SettingsScreen.showThemePicker()` 的循环切换逻辑为 `ModalBottomSheet`：

```
点击"主题模式" → ModalBottomSheet
  ├── Text("选择主题", 居中)
  └── LazyRow(horizontalArrangement = spacedBy(12dp))
        └── for each theme in AppTheme.all:
              Card(120×84dp, RoundedCornerShape(16dp))
                .background(theme.colors.bg)
                .border(选中: 3dp DT.primary, else 1dp DT.divider)
                .clickable { switchTheme(theme.name) }
                internal:
                  Box(40dp圆角, theme.colors.primary)  // 主色预览
                  Spacer(8dp)
                  Text(theme.name)
                  if 选中: Icon(check, DT.primary)
```

## 波及范围

| 文件 | 改动 |
|------|------|
| `core/theme/Theme.kt` | 加 CompositionLocal + 全量 provider |
| `core/theme/ThemeController.kt` | 加 SP 持久化 |
| `core/di/Modules.kt` | 加 SharedPreferences 提供 |
| `ui/settings/SettingsScreen.kt` | 可视化选择器 UI；移除旧 showThemePicker |
| 所有 10 个 UI Screen 文件 | `DT.bg`/`DT.card`/`DT.textPrimary`/`DT.textSecondary`/`DT.divider` → `c.xxx` |

## 不包含在 Phase 1 的内容

- 其他模块的 CRUD 表单（Phase 2）
- 数据驱动页面（Phase 3）
- 设置页其他菜单项（Phase 4）
- 备份系统修复（Phase 5）
