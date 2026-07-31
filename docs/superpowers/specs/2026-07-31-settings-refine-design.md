# 设置页精致感重塑 — 设计方案

> 日期：2026-07-31 | 状态：已确认 | 范围：单页面示范（设置页主屏）

## 背景与目标

用户反馈整体观感"精致感缺失"。经对比 HeroUI v3 与 Miuix 后明确：

- **设计目标**：精美、优雅、高级、克制、现代、精致、可维护、可扩展
- **视觉关键词**：Apple HIG 的克制与空间感 × Linear 的清晰与效率 × Ant Design 的精细化 × Material 3 的系统性
- 明确**不追 MIUI 风**（不做 squircle/毛玻璃/Monet）

选择「单页面示范」策略：先以设置页 SettingsScreen 做全链路示范，验证风格后再铺开。

## 现状诊断

设置页精致感缺失的直接原因：

1. **排版层级弱**：行标题仅 bodyMedium(13sp)，分组标题 label(12sp)，主次不分
2. **emoji 图标**：👶🎨🔒 等文本 emoji 在不同设备渲染不一致，显得廉价
3. **间距节奏挤**：分组间距仅 12dp，无 Apple 式留白；退出登录区与正文无隔离
4. **硬编码值**：多处 `Spacer(Modifier.height(12.dp))` 不走 spacing 令牌
5. 双系统问题（AppTypography 7 级 vs M3 13 级并存）本次**不动**，仅页面内统一引用 AppTypography 单源

## 改动范围

- 仅 `app/src/main/java/com/babytracker/feature/settings/SettingsScreen.kt` 中设置页主屏 UI
- 涉及函数：`SettingsScreen`（主体）、`UserInfoCard`、`SettingsSectionTitle`、`SettingsRow`、退出登录区
- **不改**任何共享 API（AppTypography / AppComponentTokens / 组件文件）、其他页面、其他屏幕（BackupScreen 等不在本次范围）

## 具体设计

### 1. 排版统一（AppTypography 7 级单源）

| 元素 | 现状 | 改为 |
|---|---|---|
| 行标题 | `bodyMedium`(13sp) | `titleMedium`(16sp Medium) |
| 行副标题 | `bodySmall`(12sp) | `bodyMedium`(13sp) |
| 用户卡显示名 | `titleMedium`(16sp) | `titleLarge`(18sp SemiBold) |
| 分组标题 | `label`(12sp Medium) + textSecondary | 保持 |
| 用户卡副文案 | `bodySmall` | `bodyMedium` |

- 全部通过 `LocalAppTypographyStyle.current` 取
- 不再引用 M3 `BabyTrackerTypography`（`LocalAppTypography`）的字段

### 2. emoji → 矢量图标

保留 40dp 圆角容器 + primaryContainer 背景，图标 24dp 主色（沿用现有 SettingsRow leadingContent 结构，仅把 Text(emoji) 换成 Icon）：

| 行 | 原 emoji | 新图标（Icons.Default.*） |
|---|---|---|
| 宝宝管理 | 👶 | `ChildCare` |
| 家庭与账号 | 👨👩👧 | `People` |
| 提醒设置 | 🔔 | `Notifications` |
| 使用偏好 | 🎨 | `Palette` |
| 数据与同步 | 🔒 | `Cloud` |
| 帮助与关于 | ❓ | `HelpOutline` |

（material-icons-extended 已依赖，可直接使用）

### 3. 间距节奏

| 位置 | 现状 | 改为 |
|---|---|---|
| 页面顶部 | 12dp（硬编码） | `spacing.md`(16dp) |
| 分组间 | 12dp（硬编码） | 20dp |
| 退出登录上方 | `spacing.md`(16dp) | 24dp（危险操作隔离） |
| 分组标题下 | `spacing.sm`(8dp) | 保持 |
| divider 缩进 | `spacing.md`(16dp) | 保持 |
| divider 厚度 | 0.5dp | 保持 |

### 4. 明确不做

- 不扩展 AppTypography 字段
- 不加动效（按压/弹簧等后续阶段）
- 不改组件文件（SettingsRow 为页面私有函数，允许在页面内改）
- 不碰 BackupScreen / BabyManagementScreen / ThemePickerSheet 等其他屏幕
- 不修双系统根因（另立专项）

## 验证标准

- `./gradlew assembleDebug` 编译通过
- `./gradlew lint` 无新增问题
- 设置页视觉检查：行标题主次分明、图标统一、分组留白明显、无 emoji 残留（设置页主屏范围内）
