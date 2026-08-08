# 宝宝记录 — Baby Tracker (Android)

Android 原生宝宝护理记录 App。Jetpack Compose + Material 3，MVVM + Koin + Room + Supabase 家庭同步。

## 功能

| 模块 | 说明 |
|---|---|
| 首页 | 宝宝头部 · 今日概览（数字动画）· 最近记录 · 功能宫格 |
| 喂养/睡眠/尿布 | 时间轴样式 · 计时器（自动填入时长）· 左滑删除 + 撤销 |
| 生长 | 圆角 Tab · Canvas 折线图 · WHO 参考虚线 |
| 疫苗 | 接种计划/记录双 Tab · 一键生成计划 · 四态状态胶囊 |
| 健康/发育/提醒 | 分类记录 · 发育评估 · 提醒中心 |
| 统计 | 日/周/月/年周期 · 对比 · 柱状图/折线图 |
| AI 助手 | 多供应商（OpenAI Responses / 兼容协议）· 流式回答 · 思考过程 · 本地会话历史 · 快捷分析 · 安全校验层 |
| 家庭共享 | 创建/加入家庭（邀请码 RPC）· 成员管理 · 宝宝数据云同步 |
| 同步 | 手动/自动/退后台/周期（WorkManager）· Realtime 增量 · sync_version 游标 · 重试退避 |
| 消息中心 | 互动/系统/服务通知分类（仅本机，不同步） |
| 设置 | 主题（6 套 + 自定义主色）· 同步策略 · 备份（本地/SAF/WebDAV）· 日志查看器 |

## 设计系统

`designsystem/` 自建设计系统：

```kotlin
// AppTokens.kt — 核心语义令牌
AppColors.light()/dark()          // 39 字段，AppColors.derive(primary) 自动派生
LocalAppSpacing.current           // 0/2/4/8/16/24/32/48 间距令牌
LocalAppShapes.current            // 圆角令牌 + radiusScale 全局缩放

// AppComponentTokens.kt — 组件令牌（21+ 种）
AppComponentTokens.default(colors) // 从 AppColors 自动派生组件颜色

// 组件（designsystem/components/）
AppCard / AppTopBar / PrimaryButton / AppInput / AppDialog /
AppConfirmDialog / AppBottomSheet / AppIconButton / AppRadioButton /
AppSwitch / AppChip / AppSlider / RecordCard / AppMarkdownText ...
```

6 套主题（纯净/极光/暖阳/阳光黄/暗夜/莫兰迪）+ 自定义主色。业务代码禁止直接使用原生 M3 组件（见 AGENTS.md 红线与 docs/design-system.md）。

## 技术栈

| 层面 | 选型 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 启动屏 | androidx.core:core-splashscreen 1.2.0 |
| 导航 | Navigation Compose 2.9.1（25+ 路由，无动画跳转） |
| 数据库 | Room 2.8.4 + KSP 2.3.9（version 8，15 张表，exportSchema 开启） |
| DI | Koin 4.2.1（ViewModel 用 `viewModel { }` + `koinViewModel()`） |
| 架构 | MVVM + ViewModel + StateFlow |
| 异步 | Kotlin Coroutines + Flow |
| 网络 | Retrofit 3.0.0 + OkHttp 5.4.0（WebDAV）+ Supabase Kotlin BOM 3.6.0 |
| 图片 | Coil 2.7.0 |
| 构建 | Gradle + AGP, Java 17, compileSdk 36, minSdk 24 |

## 构建

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest   # 单元测试（改动后必跑）
./gradlew assembleRelease     # 启用 R8 + 资源压缩
./gradlew lint
```

## 项目结构

```
app/src/main/java/com/babytracker/
├── designsystem/             # 设计系统：主题令牌 + 组件 + Hooks + AppStrings
├── core/                     # 业务基础设施
│   ├── ai/                   # AI 配置/供应商适配/安全校验
│   ├── auth/                 # 登录（AuthService）
│   ├── backup/               # 备份（BackupManager）
│   ├── database/             # Room（Entities/Daos/AppDatabase v8）
│   ├── data/                 # Repository + FamilyService
│   ├── di/                   # Koin Modules
│   ├── domain/               # Domain Models
│   ├── settings/             # AppSettings（DataStore）
│   ├── sync/                 # SyncEngine/SyncTrigger/RealtimeManager/SyncWorker
│   └── util/                 # 工具类
├── feature/                  # 16 个业务模块（home/feeding/sleep/diaper/growth/
│                             #   vaccination/health/stats/timeline/message/
│                             #   development/reminder/settings/ai/auth/family）
└── navigation/               # AppNavigation.kt（25 条路由）
```

详细索引见 `docs/project-structure.md`。

## 数据库

Room version 8，15 张 @Entity：9 张同步业务表（babies/feedings/sleeps/growths/vaccinations/health_records/diapers/development_assessments/reminders）+ messages（仅本机）+ backup_config + sync_metadata（10 业务字段 + 唯一索引）+ sync_cursors + ai_conversations/ai_messages（仅本机）。

## 许可

MIT
