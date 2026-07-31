# 色彩基座收敛 + 动效层 — 设计方案

> 日期：2026-07-31 | 状态：已确认 | 类型：🔴 重大重构（改色系系统 + 导航层）

## 背景与目标

设置页精致感示范已完成（1.8.0），但用户反馈"整体气质不对"。诊断结论：**气质根源在色彩基座**——当前 6 套全彩主题将背景/顶栏/卡片全部染色，违背目标风格（Apple HIG 克制 × Linear 清晰 × AntD 精细化 × M3 系统性）。

用户决策：
- **中性基座 + 主题只换强调色**（大面中性灰阶，主题色只出现在小面强调）
- **基座 + 动效一起做**（动效不依赖基座，可并行）
- **去除低端性能限制**（推翻 `instantComposable` 无转场决策，恢复页面转场）

## 现状诊断（彩色感来源）

1. `ThemeColors.derive`（Theme.kt:94）：`bg = primary.mix(white, 0.88f)` — 6 主题背景全带色
2. `AppBarTokens.default`（AppComponentTokens.kt:507）：亮色 `containerColor = colors.primaryContainer` — 全页面顶栏是主题色淡彩
3. `Gradients.pageHeader`（Gradients.kt:41）：首页顶部 `primaryContainer → pageBackground` 彩色氛围渐变
4. 各页面 primaryContainer 大面积背景块（HomeScreen:197 等）
5. `AppNavigation.kt`：`instantComposable` 关闭全部页面转场（低端性能决策）

## 改动范围

### Part A：色彩基座（5 项）

| # | 文件 | 改动 |
|---|---|---|
| A1 | `app/src/main/java/com/babytracker/designsystem/theme/Theme.kt` | `ThemeColors.derive` 中 `bg`/`pageBg` 中性化：亮色固定 `Color(0xFFF5F7FA)`、暗色 `#121212`（与 `AppColors.derive.pageBackground` 同源），不再 `primary.mix(white, 0.88f)` |
| A2 | `app/src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt` | `AppBarTokens.default` 亮色 `containerColor = colors.pageBackground`（与页面同色，Apple 无顶栏色块风格）；iconColor 保留 primary、titleColor 保持 |
| A3 | `app/src/main/java/com/babytracker/designsystem/theme/Gradients.kt` | `pageHeader`：`primaryContainer → pageBackground` 改为 `bgHover → pageBackground`（灰调）；`overviewCard` 保留 primary 渐变（CTA 级强调）；其余渐变不动 |
| A4 | `app/src/main/java/com/babytracker/feature/home/HomeScreen.kt` 等页面 primaryContainer 大块 | 逐个检查，大面积（>60dp 见方或整卡背景）改 `bgHover` 中性淡灰；小面积图标容器/选中态保留 |
| A5 | 设置页 40dp 图标容器（SettingsScreen.kt SettingsIconRow） | **保留** primaryContainer（小面积强调，符合"主题只换强调色"） |

### Part B：动效层（4 项，含导航层）

| # | 文件 | 改动 |
|---|---|---|
| B1 | 新增 `app/src/main/java/com/babytracker/designsystem/components/PressFeedback.kt` | `Modifier.pressScale()`：按下 0.97 缩放 + 松手 `spring(dampingRatio = 0.7f, stiffness = 500f)` 回弹，与 ripple 叠加。需处理按下/抬起状态、快速连点（松手立即恢复） |
| B2 | `button/Button.kt`、`section/Section.kt`（AppListItem 内部 clickable 处）、`card/Card.kt`（onClick 时） | clickable 叠加 `pressScale()` — 全 App 按钮/卡片/列表项获得按压质感 |
| B3 | `components/Animations.kt` | `animateNumber`：`tween(600)` → spring 回弹数字跳动 |
| B4 | `navigation/AppNavigation.kt` | 移除 `instantComposable` 无转场限制。主 tab 级路由（Home/Timeline/Feeding/Sleep/Growth/Vaccination/Health/Diaper/Stats/Settings 及弹层类）：`fadeIn(150) + fadeOut(150)`（时长取 `AppMotionDuration.fast`）；层级 push 路由（其余）：`fadeIn(150) + slideInHorizontally(initialOffsetX = { it / 16 })`，pop 反向。保留 `instantComposable` 的封装结构改为两个辅助函数 `fadeComposable` / `slideComposable` |

## 明确不做

- 毛玻璃（第三阶段可选）
- 删除/改名 6 个主题（纯蓝/极光紫/暖阳粉/阳光黄/暗夜深/莫兰迪）
- 改 AppColors 39 字段结构、不改 AppTypography
- 不新增组件（pressScale 是 Modifier 扩展，非视觉形态组件，不适用两次规则）
- 不做列表滚动弹性/overscroll（Android 默认已有一级弹性，避免过度）

## 验证标准

- `./gradlew assembleDebug` 编译通过
- `./gradlew lint` 无新增问题
- 真机/模拟器检查：
  - 6 个主题下背景/顶栏均为中性灰阶，主题色仅出现在按钮/图标/选中态/渐变 CTA
  - 按压按钮/卡片/列表项有 0.97 缩放 + 回弹质感
  - 底部导航切换页面 fade 转场、进入子页面有滑动转场
  - 首页顶部渐变不再"粉彩"感
