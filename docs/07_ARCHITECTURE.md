# 架构设计

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  Screen ──collectAsState──▶ ViewModel (StateFlow)       │
│                              │ onEvent()                 │
├─────────────────────────────────────────────────────────┤
│                  Domain Layer (UseCase)                  │
│  AddXxxUseCase / DeleteXxxUseCase                       │
│  GlobalEventBus (SharedFlow) ──▶ Snackbar / Toast        │
├─────────────────────────────────────────────────────────┤
│                   Data Layer                             │
│  Repository ──▶ DAO ──▶ Room Database                   │
│  DataStore (Preferences)                                 │
│  WorkManager (Background Tasks)                          │
└─────────────────────────────────────────────────────────┘
```

## 2. 分层职责

### UI Layer

- Screen：Composable 函数，只做布局和事件绑定
- ViewModel：持有 `StateFlow<UiState>`，接收 `onEvent(UiEvent)`，通过 UseCase 执行操作
- 不直接操作 Repository 或 DAO

### Domain Layer

- UseCase：单一职责的操作单元，封装业务逻辑
- `operator fun invoke()` 约定
- 通过 `GlobalEventBus` 广播操作结果（成功/失败）

### Data Layer

- Entity：Room 数据映射
- DAO：数据库访问接口（Flow 查询 + suspend 写入）
- Repository：DAO 的封装层，提供统一的数据访问入口
- DataStore：键值对偏好存储

## 3. MVI 数据流

```
用户操作 → Screen.onClick
         → ViewModel.onEvent(UiEvent)
         → UseCase.invoke() [suspend]
         → Repository → DAO → Room [suspend]
         → Room Flow 自动通知
         → ViewModel 更新 StateFlow<UiState>
         → Screen collectAsState() 重组
```

## 4. 依赖注入 (Koin)

```
AppModule (单模块提供全部依赖)
├── single: Database, DAOs, Repositories, UseCases, Preferences
└── viewModel: 所有 ViewModel
```

## 5. 模块化结构

```
:app (Application)
  ├── :core:data (Room Entity/DAO/Database, Repository, DataStore, UseCase)
  └── :core:designsystem (UI 组件、主题)
```

### 模块依赖

```
:app → :core:data
:app → :core:designsystem
:core:data → (无模块依赖)
:core:designsystem → (无模块依赖)
```

## 6. 关键设计决策

| 决策 | 选择 | 理由 |
|------|------|------|
| DI 框架 | Koin | 轻量，无编译时代码生成，单模块友好 |
| 状态管理 | MVI (sealed UiEvent) | 单向数据流，可追踪，易测试 |
| 数据库 | Room + KSP | 官方推荐，编译期 SQL 校验 |
| 异步 | Kotlin Coroutines + Flow | Compose 原生支持，Room 集成 |
| 后台任务 | WorkManager | 兼容 API 24+，系统级调度 |
| 图表 | Vico | Compose 原生图表库，M3 主题 |
