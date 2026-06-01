# 开发计划

实施计划详见：`docs/superpowers/plans/2026-06-01-baby-tracker-implementation.md`

## 里程碑总览

| 里程碑 | 任务数 | 核心交付 | 预计工时 |
|--------|--------|---------|---------|
| M0: 架构前置 | 1 | 模块化拆分 | 0.5天 |
| M1: 基础能力 | 18 | Room/Koin/Nav/DesignSystem + 首页/喂养/睡眠/生长 | 5-7天 |
| M2: 健康管理 | 6 | 疫苗/健康档案/WorkManager/DataStore | 4-5天 |
| M3: 数据分析 | 2 | 统计分析页面 | 3天 |
| M4: 产品化 | 4 | 设置/关于/路由集成 | 2天 |
| M5: 测试 | 3 | DAO/ViewModel/UI 测试 | 2天 |

## 任务依赖关系

```
M0 (模块化)
 └── M1 (基础能力)
      ├── M2 (健康管理)
      │    └── M3 (数据分析)
      └── M4 (产品化)
           └── M5 (测试)
```

## 关键技术决策

| 决策 | 内容 |
|------|------|
| MVI | sealed UiEvent + onEvent() + UseCase |
| 事件总线 | GlobalEventBus (SharedFlow) |
| 依赖注入 | Koin single/viewModel |
| 状态管理 | StateFlow<UiState> + collectAsState |
| 测试框架 | JUnit + Mockito + Compose Test |
