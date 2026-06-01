# 版本变更记录

## [Unreleased]

### Added
- 项目脚手架：Compose + Material 3 + MVVM + Koin + Room + Retrofit 初始化
- AGENTS.md：技术栈约束与文档规范
- build.sh：编译脚本
- 设计文档 v1：宝宝记录 App 完整设计（14页面）
- 设计文档 v2：根据架构审阅反馈重写数据库模型、新增 BabyEntity、设计系统、4里程碑规划
- 实施计划：35+ 任务覆盖模块化、UseCase 层、MVI 模式、测试
- 产品文档：README / PRD / 用户流程 / UI设计 / 设计系统 / UI约束 / 架构 / 数据库 / 实施计划 / AI规则 / 变更记录

### Changed
- 技术栈升级：Gradle 8.5 → 9.5.1, AGP 8.5 → 8.9.3
- README 与 AGENTS.md 中文化
- 数据库设计：所有 Entity 增加 babyId，新增 BabyEntity/VaccineEntity/HealthProfileEntity/ReminderEntity
- 架构设计：引入 MVI (sealed UiEvent + onEvent) + UseCase 层
- 模块化：拆分为 `:core:data` + `:core:designsystem` + `:app`

### Infrastructure
- Room 7 表 + DAO + Database
- Koin DI 全链路注册
- DataStore (Theme/Notification 偏好)
- WorkManager (每日提醒)
- Navigation Compose + sealed Route
- Design System (6 个通用组件)
- 单元测试 + Compose UI 测试
