# MyApp — 宝宝记录 (Android)

Android 原生宝宝护理记录 App。Jetpack Compose + Material 3（完整 M3 主题系统），MVVM + Koin + Room。

## 技术栈（约束）

| 层面 | 选型 | 说明 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | full ColorScheme / Typography / Shapes |
| 导航 | Navigation Compose | 11 条路由 |
| 数据库 | Room 2.6.1 + KSP | 8 张表，Migration 增量升级 |
| 异步 | Kotlin Coroutines + Flow | — |
| DI | Koin 3.5.6 | — |
| 架构 | MVVM（ViewModel + StateFlow） | 2 个 ViewModel |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 | WebDAV 备份 |
| 文件 | DocumentFile 1.0.1 | SAF 目录选择 |
| 图片 | Coil 2.6.0 | — |
| 构建 | Gradle 9.5.1 + AGP 8.9.3 | — |
| 编译 | Kotlin 1.9.24, Java 17, SDK 34, minSdk 24 | — |

## 项目结构

```
app/src/main/java/com/babytracker/
├── BabyTrackerApp.kt           # Application + Koin 启动
├── MainActivity.kt             # Compose Activity（dynamicColor=false）
├── core/
│   ├── theme/
│   │   ├── DesignTokens.kt     # DT 布局令牌 + 6 个 AppTheme + ThemeColors
│   │   ├── Theme.kt            # MaterialTheme 封装 + ColorScheme/Typography/Shapes
│   │   └── ThemeController.kt  # SharedPreferences 主题持久化
│   ├── database/
│   │   ├── Entities.kt         # 8 个实体
│   │   ├── dao/Daos.kt         # 8 个 DAO
│   │   └── AppDatabase.kt      # Room DB（Migration 1→2）
│   ├── di/Modules.kt           # Koin appModule + databaseModule
│   ├── backup/BackupManager.kt # 本地备份 + SAF 目录备份 + JSON 导入数据
│   └── util/DateUtils.kt       # 日期格式化 + 时长（支持秒级）
├── data/repository/Repositories.kt  # 7 个 Repository 接口 + 实现
└── ui/
    ├── navigation/AppNavigation.kt   # 路由定义（sealed class Screen）
    ├── home/                   # HomeScreen + HomeViewModel（含今日概览/最近记录/功能宫格）
    ├── feeding/                # 喂养时间轴 + 记录弹窗 + 长按删除
    ├── sleep/                  # 睡眠列表 + 深色头卡 + 进度条 + 记录弹窗 + 长按删除
    ├── diaper/                 # 尿布列表（新）+ 记录弹窗 + 长按删除
    ├── growth/                 # 生长 Tab + Canvas 曲线图 + 记录弹窗 + 长按删除
    ├── vaccination/            # 疫苗列表 + 状态标签 + 记录弹窗 + 长按删除
    ├── health/                 # 健康列表 + 分类 + 记录弹窗 + 长按删除
    ├── stats/                  # 统计卡片 + MiniLineChart
    ├── settings/               # 我的 + 宝宝管理 + 备份 + 主题切换
    └── components/             # BabyIllustration 组件
```

## 设计规范

### Material 3（完整主题系统）

| 要素 | 说明 |
|---|---|
| ColorScheme | 20+ 色槽，`AppTheme.toColorScheme()` 映射 |
| Typography | 13 级（displayLarge → labelSmall） |
| Shapes | 5 级（4dp → 24dp），`MaterialTheme.shapes.*` |
| Dynamic Colors | `dynamicColor=false`，使用 6 个自定义主题 |

### 主题

| 名称 | 主色 | 键值 |
|---|---|---|
| 纯净蓝 pure | #2563EB | `"pure"` |
| 极光紫 aurora | #7C6CF0 | `"aurora"` |
| 暖阳粉 warm | #FF8A80 | `"warm"` |
| 阳光黄 sunny | #F59E0B | `"sunny"` |
| 暗夜深 night | #5C6BC0 | `"night"` |
| 莫兰迪 morandi | #B0BEC5 | `"morandi"` |

### 布局令牌（DT）

```
页面边距: 20dp, 卡片间距: 16dp, 卡片圆角: 8dp
按钮圆角: 8dp, 输入框圆角: 8dp, 图标尺寸: 22dp, 图标背景: 40dp
```

### 配色映射

```
c.primary      → MaterialTheme.colorScheme.primary
c.primaryLight → MaterialTheme.colorScheme.primaryContainer
c.bg           → MaterialTheme.colorScheme.background
c.card         → MaterialTheme.colorScheme.surface
c.cardBorder   → MaterialTheme.colorScheme.outlineVariant
c.textPrimary  → MaterialTheme.colorScheme.onSurface
c.textSecondary→ MaterialTheme.colorScheme.onSurfaceVariant
c.pink/yellow/cyan/green → 无 M3 等价色，保留自定义
```

## 数据库

| 表 | 实体 | DAO |
|---|---|---|
| babies | BabyEntity | BabyDao |
| feedings | FeedingEntity | FeedingDao |
| sleeps | SleepEntity | SleepDao |
| growths | GrowthEntity | GrowthDao |
| vaccinations | VaccinationEntity | VaccinationDao |
| health_records | HealthRecordEntity | HealthRecordDao |
| diapers | DiaperEntity | DiaperDao |
| backup_config | BackupConfigEntity | BackupConfigDao |

Migration：`fallbackToDestructiveMigration()` → 正式 `Migration(1, 2)` 增量升级。

## 页面列表

| 路由 | 页面 | 说明 |
|---|---|---|
| `/` | 首页 | 底部 Tab：首页/统计/我的 |
| `/feeding` | 喂养 | 时间轴 + 记录弹窗 + 长按删除 |
| `/sleep` | 睡眠 | 深色头卡 + 列表 + 记录弹窗 + 长按删除 |
| `/diaper` | 尿布 | 列表 + 记录弹窗 + 长按删除 |
| `/growth` | 生长 | Tab + Canvas 图表 + 记录弹窗 + 长按删除 |
| `/vaccination` | 疫苗 | 状态标签 + 记录弹窗 + 长按删除 |
| `/health` | 健康 | 分类列表 + 记录弹窗 + 长按删除 |
| `/stats` | 统计 | 2×3 卡片 + MiniLineChart |
| `/settings` | 我的 | 设置列表 + 主题切换 + 状态栏适配 |
| `/settings/babies` | 宝宝信息 | 编辑/删除 + 表单弹窗 |
| `/settings/backup` | 备份 | 目录选择 + 本地备份 + WebDAV |

### 交互规范

- 所有列表页**长按卡片** → 确认删除
- 所有弹窗 `skipPartiallyExpanded = true` 全展开
- 时间输入统一 `yyyy-MM-dd HH:mm` 格式
- 列表**跨日**显示日期分隔标签；**同日**隐藏

## 构建

```bash
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Release APK
./gradlew lint                # 静态检查
```

环境变量：
```bash
ANDROID_AAPT2_OVERRIDE=/data/data/com.termux/files/home/android-sdk/build-tools/34.0.0/aapt2
```

## 文档规范

- 文档统一使用中文
- commit message 使用中文
