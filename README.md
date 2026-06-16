# 宝宝记录 — Baby Tracker (Android)

Android 原生宝宝护理记录 App。Jetpack Compose + Material 3，MVVM + Koin + Room。

## 功能

| 模块 | 说明 |
|---|---|
| 首页 | 渐变宝宝头部 220dp · 4 列宫格 · 3 等分概览 |
| 喂养 | 时间轴样式 · 左时间+彩色圆点+右卡片 |
| 睡眠 | 蓝渐变 180dp 顶卡 · 入睡/起床/小睡统计 |
| 生长 | 圆角 Tab · Canvas 折线图 · WHO 参考线 |
| 疫苗 | 接种计划/记录双 Tab · 红/绿状态标签 |
| 健康 | 列表式 · emoji + 标题 + 日期 |
| 统计 | 4 个 110dp 卡片 + 迷你趋势条 |
| 我的 | 80dp 头像 · 功能区/系统区 · 主题切换 · 备份 |

## 设计系统

```kotlin
// DesignTokens.kt 统一管理
DT.bg         = #F8F9FC
DT.primary    = #6C8DFF
DT.cardRadius = 24.dp
DT.pageMargin = 20.dp
```

5 套主题（纯净/极光/暖宝/极夜/莫兰迪）+ 自定义主色。

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room 2.6.1 + KSP |
| DI | Koin 3.5.6 |
| 架构 | MVVM + ViewModel + StateFlow |
| 异步 | Kotlin Coroutines + Flow |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 图片 | Coil 2.6.0 |
| 构建 | Gradle 9.5.1 + AGP 8.9.3, Java 17, SDK 34 |

## 构建

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew lint
```

## 项目结构

```
app/src/main/java/com/babytracker/
├── BabyTrackerApp.kt
├── MainActivity.kt
├── core/
│   ├── theme/           # DT + 5主题 + ThemeController
│   ├── database/        # Room 实体 + DAO + AppDatabase
│   ├── di/              # Koin 模块
│   ├── backup/          # 备份管理器
│   └── util/            # 日期工具
├── data/repository/     # 6个 Repository
└── ui/
    ├── navigation/      # 路由
    ├── home/            # 首页
    ├── feeding/         # 喂养
    ├── sleep/           # 睡眠
    ├── growth/          # 生长
    ├── vaccination/     # 疫苗
    ├── health/          # 健康
    ├── stats/           # 统计
    └── settings/        # 设置
```

## 数据库

7 张 Room 实体表：babies, feedings, sleeps, growths, vaccinations, health_records, backup_config。DAO 通过 Flow 暴露数据。

## 许可

MIT
