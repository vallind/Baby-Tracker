# 育儿助手 App 设计文档 v2

> 基于 v1 设计 + 代码审阅反馈修订
> 更新日期：2026-06-01

## 1. 项目概述

### 项目名称

育儿助手（Parenting Assistant）

### 产品定位

面向 0-6 岁儿童家庭的成长记录与健康管理工具。

### 核心功能

- 喂养记录
- 睡眠记录
- 生长发育跟踪
- 疫苗管理
- 健康档案管理
- 提醒管理
- 数据统计分析

### 产品目标

建立宝宝完整成长档案，实现：
- 数据记录简单化
- 成长趋势可视化
- 疫苗提醒自动化
- 健康信息数字化

---

## 2. 用户角色

### 宝妈

22~40 岁，记录日常育儿数据、查看成长情况、获取提醒。

### 宝爸

22~45 岁，共享宝宝数据、查看统计分析、接收提醒通知。

### 祖辈照护者

50~70 岁，简单记录、查看提醒、操作方便。

---

## 3. 信息架构

### 页面结构（14 个页面）

```
首页
├── 喂养记录
├── 睡眠记录
├── 生长记录（含图表）
├── 发育评估
├── 健康档案
├── 统计分析
├── 疫苗接种
├── 提醒中心
├── 消息中心
├── 我的
├── 宝宝信息
├── 设置
└── 关于我们
```

### 底部导航（5 个 Tab）

| Tab | 页面 |
|-----|------|
| 首页 | 宝宝卡片 + 功能宫格 + 今日概览 + 最近记录 |
| 记录 | 喂养/睡眠/生长 快捷入口 |
| 统计 | 统计分析（日/周/月/年） |
| 消息 | 消息中心 |
| 我的 | 宝宝管理/设置/导出/关于 |

---

## 4. 页面设计

### 4.1 首页

- **宝宝卡片**: 头像、姓名、年龄（月龄）、当前身高体重
- **功能宫格**: 喂养/睡眠/生长/疫苗/健康/评估 快捷入口
- **今日概览**: 喂养次数、睡眠时长
- **最近记录**: 最近 3 条各类型记录摘要

### 4.2 喂养记录

- **记录类型**: 母乳 / 配方奶 / 辅食 / 饮水
- **字段**: 时间、类型、用量(ml/g)、备注
- **操作**: 新增 / 编辑 / 删除
- **展示**: 按日期分组 LazyColumn，支持删除

### 4.3 睡眠记录

- **字段**: 开始时间、结束时间、时长、类型（夜间睡眠/白天小睡）
- **操作**: 新增 / 编辑 / 删除
- **展示**: 按日期分组 LazyColumn，显示时长

### 4.4 生长记录

- **数据项**: 身高(cm)、体重(kg)、头围(cm)、BMI（自动计算）
- **图表**: Vico 折线图，Tab 切换身高/体重/头围
- **操作**: 新增 / 编辑 / 删除

### 4.5 发育评估

- **维度**: 大运动 / 精细动作 / 语言能力 / 社交能力 / 认知能力
- **输出**: 正常 / 需关注 / 建议复查（V1 手动评估，V2 算法辅助）
- **展示**: 各维度进度条 + 总体评估卡片

### 4.6 健康档案

- **出生信息**: 出生身高、出生体重、胎龄、分娩方式
- **过敏史**: 过敏原列表
- **既往病史**: 疾病名称 + 时间 + 备注
- **就诊记录**: 医院、科室、诊断、日期
- **用药记录**: 药品名、剂量、用药时间

### 4.7 统计分析

- **维度**: 日 / 周 / 月 / 年
- **指标**: 喂养次数趋势、睡眠时长趋势、身高/体重增长曲线、疫苗完成率
- **展示**: Vico 图表 + 汇总数据卡片

### 4.8 疫苗接种

- **状态**: 待接种 / 已接种 / 已过期
- **信息**: 疫苗名称、针次、建议接种时间、实际接种时间、状态
- **功能**: 完成登记、查看历史

### 4.9 提醒中心

- **类型**: 疫苗提醒 / 体检提醒 / 用药提醒
- **状态**: 开启 / 关闭 / 已过期
- **实现**: WorkManager + 通知

### 4.10 消息中心

- **类型**: 系统通知 / 服务通知
- **功能**: 未读统计、消息详情、消息删除

### 4.11 我的

- **模块**: 宝宝管理（多宝宝切换）、提醒设置、数据导出、隐私设置、帮助反馈、关于我们

### 4.12 宝宝信息

- **基础信息**: 头像（Coil）、姓名、性别、出生日期
- **出生数据**: 出生身高、出生体重
- **当前数据**: 当前身高、当前体重、头围

### 4.13 设置

- **主题**: 浅色/深色/跟随系统（DataStore）
- **通知**: 疫苗提醒开关、体检提醒开关
- **数据管理**: 导出 / 清除所有数据

### 4.14 关于我们

- 应用版本、开发者信息、开源许可

---

## 5. 数据模型

### 5.1 BabyEntity

```kotlin
@Entity(tableName = "babies")
data class BabyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,                  // MALE / FEMALE
    val birthday: Long,
    val avatar: String?,                 // URI
    val birthHeight: Float,              // cm
    val birthWeight: Float,              // kg
    val note: String?
)
```

### 5.2 FeedingEntity

```kotlin
@Entity(tableName = "feeding_records")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: String,                    // BREAST_MILK / FORMULA / SOLID_FOOD / WATER
    val amount: Int,                     // ml or g
    val unit: String,                    // "ml" / "g"
    val note: String?,
    val createdAt: Long
)
```

### 5.3 SleepEntity

```kotlin
@Entity(tableName = "sleep_records")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val startTime: Long,
    val endTime: Long,
    val type: String                     // NIGHT / NAP
)
```

### 5.4 GrowthEntity

```kotlin
@Entity(tableName = "growth_records")
data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val height: Float,                   // cm
    val weight: Float,                   // kg
    val headCircumference: Float,        // cm
    val recordDate: Long
)
```

### 5.5 VaccineEntity

```kotlin
@Entity(tableName = "vaccines")
data class VaccineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val name: String,
    val dose: Int,                       // 第几针
    val plannedDate: Long,               // 建议接种日期
    val completedDate: Long?,            // 实际接种日期（null = 未接种）
    val status: String                   // PENDING / COMPLETED / EXPIRED
)
```

### 5.6 HealthProfileEntity

```kotlin
@Entity(tableName = "health_profiles")
data class HealthProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val allergies: String?,              // JSON array string
    val medicalHistory: String?,         // JSON array string
    val doctorNotes: String?
)
```

### 5.7 ReminderEntity

```kotlin
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long,
    val type: String,                    // VACCINE / CHECKUP / MEDICATION
    val title: String,
    val scheduledAt: Long,
    val enabled: Boolean
)
```

### 5.8 关联关系

所有记录通过 `babyId` 关联到 BabyEntity。V1 默认使用 single baby（id=1），数据库设计保留多宝宝扩展性。

---

## 6. 架构设计

### 6.1 整体架构（MVVM）

```
[UI] Compose Screen <--> ViewModel (StateFlow)
                                |
[Data] Repository <--> Room DAO <--> Database
                       DataStore (Preferences)
                       WorkManager (Background Tasks)
```

### 6.2 包结构

```
com.example.myapp/
├── MyApp.kt                       // Application
├── MainActivity.kt                // Single Activity
├── di/
│   └── AppModule.kt               // Koin DI
├── data/
│   ├── room/
│   │   ├── AppDatabase.kt
│   │   ├── BabyEntity.kt
│   │   ├── BabyDao.kt
│   │   ├── FeedingEntity.kt
│   │   ├── FeedingDao.kt
│   │   ├── SleepEntity.kt
│   │   ├── SleepDao.kt
│   │   ├── GrowthEntity.kt
│   │   ├── GrowthDao.kt
│   │   ├── VaccineEntity.kt
│   │   ├── VaccineDao.kt
│   │   ├── HealthProfileEntity.kt
│   │   ├── HealthProfileDao.kt
│   │   ├── ReminderEntity.kt
│   │   └── ReminderDao.kt
│   ├── repository/
│   │   ├── BabyRepository.kt
│   │   ├── FeedingRepository.kt
│   │   ├── SleepRepository.kt
│   │   ├── GrowthRepository.kt
│   │   ├── VaccineRepository.kt
│   │   ├── HealthRepository.kt
│   │   └── ReminderRepository.kt
│   └── datastore/
│       ├── ThemePreference.kt
│       └── NotificationPreference.kt
├── ui/
│   ├── designsystem/
│   │   ├── ParentingCard.kt
│   │   ├── ParentingButton.kt
│   │   ├── EmptyState.kt
│   │   ├── LoadingView.kt
│   │   ├── SectionTitle.kt
│   │   └── ErrorView.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── feeding/
│   │   ├── FeedingScreen.kt
│   │   ├── AddFeedingScreen.kt
│   │   ├── FeedingViewModel.kt
│   │   └── FeedingUiState.kt
│   ├── sleep/
│   │   ├── SleepScreen.kt
│   │   ├── AddSleepScreen.kt
│   │   ├── SleepViewModel.kt
│   │   └── SleepUiState.kt
│   ├── growth/
│   │   ├── GrowthScreen.kt
│   │   ├── AddGrowthScreen.kt
│   │   ├── GrowthViewModel.kt
│   │   └── GrowthUiState.kt
│   ├── vaccine/
│   │   ├── VaccineScreen.kt
│   │   ├── VaccineViewModel.kt
│   │   └── VaccineUiState.kt
│   ├── health/
│   │   ├── HealthScreen.kt
│   │   ├── HealthViewModel.kt
│   │   └── HealthUiState.kt
│   ├── stats/
│   │   ├── StatsScreen.kt
│   │   ├── StatsViewModel.kt
│   │   └── StatsUiState.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── about/
│       └── AboutScreen.kt
├── navigation/
│   ├── Route.kt
│   └── AppNavGraph.kt
├── worker/
│   └── ReminderWorker.kt
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

### 6.3 Route 设计

```kotlin
sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Feeding : Route("feeding")
    data object AddFeeding : Route("add_feeding")
    data object Sleep : Route("sleep")
    data object AddSleep : Route("add_sleep")
    data object Growth : Route("growth")
    data object AddGrowth : Route("add_growth")
    data object Vaccine : Route("vaccine")
    data object AddVaccine : Route("add_vaccine")
    data object Health : Route("health")
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object About : Route("about")
}
```

使用方式：`navController.navigate(Route.Feeding.route)`。

### 6.4 UiState 模式

每个页面 ViewModel 暴露统一的 UiState：

```kotlin
data class FeedingUiState(
    val loading: Boolean = false,
    val records: List<FeedingEntity> = emptyList(),
    val error: String? = null
)
```

ViewModel 通过 `combine` 或 `map` 转换 Flow → StateFlow<UiState>。

### 6.5 设计系统（Design System）

统一组件放在 `ui/designsystem/`，包括：

| 组件 | 用途 |
|------|------|
| `ParentingCard` | 统一卡片容器，圆角 24dp，白色背景 |
| `ParentingButton` | 统一按钮，高度 52dp |
| `EmptyState` | 空列表占位 |
| `LoadingView` | 加载指示器 |
| `ErrorView` | 错误提示 + 重试 |
| `SectionTitle` | 区块标题 |

---

## 7. 数据流

```
用户操作 → ViewModel (方法调用)
    ↓
viewModelScope.launch { repository.method() }
    ↓
Repository → DAO → Room (suspend)
    ↓
Room Flow 自动通知 → StateFlow<UiState>
    ↓
Compose collectAsState() → UI 更新
```

偏好设置走 DataStore：

```
ViewModel → DataStore Preference → Flow<T> → StateFlow → UI
```

后台提醒走 WorkManager：

```
WorkManager (PeriodicWorkRequest) → ReminderWorker
    ↓
NotificationManager → 通知栏
```

---

## 8. UI 设计规范

### 色彩

| Token | 值 |
|-------|-----|
| 主色 | #6C8DFF |
| 背景色 | #F8F9FD |
| 卡片背景 | #FFFFFF |
| 成功 | #4CAF50 |
| 警告 | #FF9800 |
| 错误 | #F44336 |

### 尺寸

| Token | 值 |
|-------|-----|
| 圆角 | 24dp |
| 按钮高度 | 52dp |
| 图标尺寸 | 24dp |
| 页面边距 | 16dp |
| 卡片间距 | 12dp |

### Material 3

使用 Material 3 + Dynamic Colors（Android 12+），定义 fallback 配色方案匹配上方色彩规范。

---

## 9. 基础设施

### 依赖

```kotlin
// Navigation
implementation("androidx.navigation:navigation-compose:$navVersion")

// Room
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")

// Koin
implementation("io.insert-koin:koin-android:$koinVersion")
implementation("io.insert-koin:koin-androidx-compose:$koinVersion")

// DataStore
implementation("androidx.datastore:datastore-preferences:$datastoreVersion")

// Vico Chart
implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")

// Coil
implementation("io.coil-kt:coil-compose:$coilVersion")

// WorkManager
implementation("androidx.work:work-runtime-ktx:$workVersion")
```

### DataStore 实现

- `ThemePreference`：主题模式（浅色/深色/系统）
- `NotificationPreference`：各类型提醒开关
- 通过 Koin `single` 注入

### WorkManager 实现

- `ReminderWorker`：检查待办提醒 → 发送通知
- 每日一次的 PeriodicWorkRequest
- 在 AppModule 中初始化

---

## 10. 里程碑规划

### Milestone 1：基础能力（~30 个任务）

**目标**: 可用的核心记录功能

**覆盖页面**: 首页 / 喂养 / 睡眠 / 生长（含图表）/ 宝宝信息

**基础设施**: Room 全部 Entity + DAO + Database、Koin 全部 Repository + ViewModel、Route 全部定义、Design System 组件、导航框架

**预计工时**: 5~7 天

### Milestone 2：健康管理（~10 个任务）

**目标**: 疫苗 + 健康档案 + 提醒

**覆盖页面**: 疫苗接种 / 健康档案 / 提醒中心

**基础设施**: WorkManager、DataStore

**预计工时**: 4~5 天

### Milestone 3：数据分析（~5 个任务）

**目标**: 统计 + 图表

**覆盖页面**: 统计分析

**预计工时**: 3 天

### Milestone 4：产品化（~5 个任务）

**目标**: 设置 + 导出 + 收尾

**覆盖页面**: 设置 / 我的 / 关于我们 / 消息中心（骨架）

**预计工时**: 3 天

---

## 11. 技术约束

| 项目 | 版本 |
|------|------|
| Kotlin | 1.9.22 |
| AGP | 8.9.3 |
| Gradle | 9.5.1 |
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 24 |
| Java | 17 |
| Compose BOM | 2024.02.00 |

---

## 12. 后续展望（V2 范围）

- 多宝宝支持（数据库已预留 `babyId`）
- 家庭共享与云同步
- PDF/Excel 成长报告导出
- AI 成长分析与智能建议
- 医生协同功能
