# 宝宝记录 — Baby Tracker (Android)

Android 原生宝宝护理记录 App。Jetpack Compose + Material 3，MVVM + Koin + Room。

[![Android CI](https://github.com/vallind/Baby-Tracker/actions/workflows/android-ci.yml/badge.svg)](https://github.com/vallind/Baby-Tracker/actions/workflows/android-ci.yml)

## 功能

| 模块 | 说明 |
|---|---|
| 首页 | 渐变宝宝头部 · 今日概览（数字动画）· 最近记录 · 功能宫格 |
| 喂养 | 时间轴样式 · 左时间+彩色圆点+右卡片 · 长按触觉反馈删除 · leadingIcon+校验 |
| 睡眠 | 蓝渐变顶卡 · 今日夜间睡眠 · 进度条渐变 · 双向 clamp |
| 生长 | 圆角 Tab · Canvas 折线图 · 渐变填充 · 动态 Y 轴刻度 · WHO 参考虚线 |
| 疫苗 | 接种计划/记录双 Tab · 一键生成接种计划 · DatePicker |
| 健康 | 列表式 · emoji + 标题 + 日期 · DatePicker · 空状态引导 |
| 统计 | 4 个卡片 + 迷你趋势线 |
| 我的 | 80dp 头像 · 功能区/系统区 · 主题切换 · 备份 |

## 设计系统

```kotlin
// DesignTokens.kt 统一管理几何常量
DT.pageMargin = 20
DT.cardGap = 16
DT.cardRadius = 8
DT.buttonRadius = 8
DT.iconSize = 22
DT.iconBgSize = 40
DT.appBarHeight = 56

// Theme.kt — M3 Shapes 层级
small      = 16dp  // 列表 Card
medium     = 20dp  // 容器 Card
large      = 28dp  // 弹窗
extraLarge = 32dp

// Gradients.kt — 渐变 Brush 工具
Gradients.primary(c)        // 主色横向渐变
Gradients.primarySoft(c)    // 主色到背景的纵向渐变（首页头部）
Gradients.chartArea(c)      // 图表下方填充渐变
Gradients.progress(c)       // 进度条渐变
```

6 套主题（纯净/极光/暖阳/阳光黄/暗夜/莫兰迪）+ 自定义主色。AppBar 统一使用 `primaryContainer` 主题色化。

## 技术栈

| 层面 | 选型 |
|---|---|---|
| UI | Jetpack Compose + Material 3 |
| 启动屏 | androidx.core:core-splashscreen 1.2.0 |
| 导航 | Navigation Compose 2.9.1 |
| 数据库 | Room 2.8.4 + KSP 2.3.9 |
| DI | Koin 4.2.1（ViewModel 用 `viewModel { }` + `koinViewModel()`） |
| 架构 | MVVM + ViewModel + StateFlow |
| 异步 | Kotlin Coroutines 1.11.0 + Flow |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0 |
| 图片 | Coil 2.7.0 |
| 构建 | Gradle 9.5.1 + AGP 9.2.1, Java 21, SDK 36 |
| 发布 | R8 minify + resource shrinking + 自定义 ProGuard 规则 |

## 构建

```bash
./gradlew assembleDebug
./gradlew assembleRelease   # 启用 R8 + 资源压缩
./gradlew lint
```

GitHub Actions 会在默认分支和 Pull Request 上自动执行：

```bash
./gradlew --no-daemon --continue testDebugUnitTest lintDebug assembleDebug
```

验证失败时上传测试与 Lint 报告；成功时提供保留 7 天的 Debug APK。Termux 的 AAPT2
覆盖路径属于本机配置，请按 [Termux AAPT2 说明](docs/aapt2-termux-fix.md) 写入用户级
`$HOME/.gradle/gradle.properties`，不要写入项目配置。

日常开发从 `codex/<任务名>` 功能分支提交 Pull Request 到受保护的 `rerr`。本地优先运行
相关测试或 Kotlin 编译，Pull Request 由 CI 完成全量测试、Lint 和 APK 构建；CI 通过后
才能合并。模拟器验证仅在明确需要 UI 或真机流程验收时执行。

## 项目结构

```
app/src/main/java/com/babytracker/
├── BabyTrackerApp.kt
├── MainActivity.kt              # SplashScreen 安装
├── core/
│   ├── theme/                   # DT + 6 主题 + ThemeController + Gradients
│   ├── database/                # Room 实体 + DAO + AppDatabase
│   ├── di/Modules.kt            # Koin 模块（viewModel { } 注册）
│   ├── backup/BackupManager.kt  # 备份管理器
│   └── util/                    # DateUtils (含 safeParse) + VaccineSchedule + BabyController
├── data/
│   ├── repository/Repositories.kt  # 7 个 Repository 接口 + 实现
│   └── database/mapper/Mappers.kt  # Entity ↔ Domain Model 映射（占位）
├── domain/
│   └── model/Models.kt          # Domain Model + 枚举（占位）
└── ui/
    ├── navigation/              # 路由
    ├── home/                    # 首页
    ├── feeding/                 # 喂养
    ├── sleep/                   # 睡眠
    ├── growth/                  # 生长
    ├── vaccination/             # 疫苗
    ├── health/                  # 健康
    ├── stats/                   # 统计
    ├── settings/                # 设置
    └── components/              # EmptyState / HapticExtensions / BabyIllustration
```

## 数据库

8 张 Room 实体表：babies, feedings, sleeps, growths, vaccinations, health_records, diapers, backup_config。DAO 通过 Flow 暴露数据。

## 许可

MIT
