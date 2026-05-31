# Baby Tracker - 育儿记录 App 设计文档

## 1. 概述

一款帮助父母记录宝宝日常（喂养、睡眠、生长、疫苗等）的 Android 原生 App。

## 2. 技术栈

- **UI**: Jetpack Compose + Material 3
- **导航**: Navigation Compose
- **分页**: Paging 3
- **图表**: Vico Chart
- **轻量存储**: DataStore
- **网络**: Retrofit 2.9.0 + OkHttp 4.12.0
- **图片**: Coil 2.6.0
- **数据库**: Room 2.6.1 + KSP
- **异步**: Kotlin 协程 + Flow
- **依赖注入**: Koin 3.5.6
- **架构**: MVVM (ViewModel + StateFlow)
- **构建**: Gradle 9.5.1 + AGP 8.9.3
- **语言**: Kotlin 1.9.22, Java 17
- **compileSdk / targetSdk**: 34, **minSdk**: 24

## 3. 页面清单

| Screen | 说明 |
|--------|------|
| HomeScreen | 首页 — 宝宝卡片、功能网格、今日统计、最近记录、待办提醒 |
| FeedingScreen | 喂养记录列表 |
| AddFeedingScreen | 新增喂养记录 |
| SleepScreen | 睡眠记录列表 |
| AddSleepScreen | 新增睡眠记录 |
| GrowthScreen | 生长记录（身高/体重/头围 Tab + 图表） |
| AddGrowthScreen | 新增生长测量 |
| AssessmentScreen | 发育评估问卷 |
| VaccineScreen | 疫苗管理（待接种/已接种/已过期） |
| HealthScreen | 健康档案（出生信息、过敏史、病史、用药、备注） |
| StatisticsScreen | 统计分析（喂养、睡眠、生长趋势、疫苗完成率） |
| MessageScreen | 消息中心 |
| ProfileScreen | 个人中心（头像、宝宝管理、提醒设置、数据导出等） |
| BabyInfoScreen | 宝宝资料编辑 |

## 4. 导航

```kotlin
sealed interface Route {
    data object Home : Route
    data object Feeding : Route
    data object AddFeeding : Route
    data object Sleep : Route
    data object AddSleep : Route
    data object Growth : Route
    data object AddGrowth : Route
    data object Assessment : Route
    data object Vaccine : Route
    data object Health : Route
    data object Statistics : Route
    data object Message : Route
    data object Profile : Route
    data object BabyInfo : Route
}
```

## 5. 首页 (HomeScreen)

```
┌────────────────────┐
│ 宝宝信息卡片        │
└────────────────────┘

┌────┬────┬────┬────┐
│喂养│睡眠│生长│评估│
├────┼────┼────┼────┤
│疫苗│健康│统计│更多│
└────┴────┴────┴────┘

今日统计

最近记录

待办提醒
```

`LazyColumn` 纵向排列五个 section items。

## 6. 功能模块详情

### 6.1 喂养 (Feeding)

- **记录列表**: 按时间倒序展示喂养记录（母乳/配方奶/辅食/水）
- **新增表单**: 类型下拉选择 + 用量输入
- **FeedingType**: `BREAST_MILK`, `FORMULA`, `SOLID_FOOD`, `WATER`
- FAB 进入新增页面

### 6.2 睡眠 (Sleep)

- **记录列表**: 卡片展示夜间/白天睡眠时长
- **新增表单**: DatePicker + TimePicker 选择起止时间
- 自动计算总时长

### 6.3 生长 (Growth)

- **三个 Tab**: 身高 / 体重 / 头围
- **图表**: Vico `CartesianChartHost` + `LineCartesianLayer` 展示趋势
- **新增表单**: 输入身高、体重、头围

### 6.4 发育评估 (Assessment)

- LazyColumn 展示评估问题卡片
- 维度：大运动、精细动作、语言、社交、认知

### 6.5 疫苗 (Vaccine)

- FilterChip 筛选：待接种 / 已接种 / 已过期
- ListItem 展示疫苗名称和推荐月龄

### 6.6 健康档案 (Health)

- 多行 OutlinedTextField：出生信息、过敏史、病史、用药记录、医生备注

### 6.7 统计分析 (Statistics)

- ElevatedCard 展示统计数据：本周喂养次数、本周睡眠时长、生长趋势、疫苗完成率

### 6.8 消息 (Message)

- LazyColumn + ListItem，展示标题和内容

### 6.9 个人中心 (Profile)

- ListItem 列表：头像、宝宝管理、提醒设置、数据导出、意见反馈、关于

### 6.10 宝宝资料 (BabyInfo)

- 表单：姓名、性别、出生日期、出生身高、出生体重、头像

## 7. 数据层

### 7.1 Room 实体

```kotlin
@Entity data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Int,
    val time: Long
)

@Entity data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long
)

@Entity data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val height: Float,
    val weight: Float,
    val headCircumference: Float,
    val date: Long
)
```

其他实体（疫苗、评估、消息、宝宝信息）在实施阶段补充。

### 7.2 DAO

每个 Entity 对应一个 DAO，提供标准的 CRUD + Flow 查询。

### 7.3 Repository

每个模块一个 Repository 接口 + 实现，封装 DAO 操作。

### 7.4 DataStore

用于存储用户偏好设置（提醒设置等）。

## 8. 依赖注入 (Koin)

```kotlin
val appModule = module {
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().feedingDao() }
    single<FeedingRepository> { FeedingRepositoryImpl(dao = get()) }
    viewModel { FeedingViewModel(repository = get()) }
    // ... 其他模块同理
}
```

## 9. 项目结构

```
com.parenting.app
├── app/
├── navigation/
├── ui/
│   ├── home/
│   ├── feeding/
│   ├── sleep/
│   ├── growth/
│   ├── assessment/
│   ├── vaccine/
│   ├── health/
│   ├── statistics/
│   ├── message/
│   ├── profile/
│   └── components/
├── domain/
├── data/
│   ├── room/
│   ├── datastore/
│   └── repository/
├── di/
└── theme/
```

## 10. 实施顺序

建议按依赖和复杂度分阶段实施：

1. **基建**: Room 数据库 + DAO + Entity + Koin 模块
2. **核心模块**: 喂养 → 睡眠 → 生长（三个最常用的记录功能）
3. **导航**: Navigation 框架 + 首页
4. **辅助模块**: 疫苗 → 健康档案 → 发育评估
5. **数据洞察**: 统计分析 + 图表
6. **社交与设置**: 消息 → 个人中心 → 宝宝资料
