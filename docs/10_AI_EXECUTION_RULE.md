# AI 辅助开发规范

## 1. 通用规则

### 1.1 代码生成

- 使用 Kotlin 1.9.22 语法，不引入实验性 API (除非 `@OptIn` 标注)
- 所有 Compose 函数使用 `@Composable` 注解
- ViewModel 继承 `androidx.lifecycle.ViewModel`
- 所有 DAO 方法标记 `suspend` 或返回 `Flow`
- Koin 注入使用 `single` / `viewModel`，不使用 `factory`

### 1.2 命名约定

- 包名: `com.example.myapp.*`
- 文件命名: PascalCase (如 `FeedingScreen.kt`)
- Compose 组件: PascalCase (如 `ParentingCard`)
- 函数/变量: camelCase (如 `onEvent`, `uiState`)

### 1.3 MVI 模式强制要求

每个功能模块必须包含：

```kotlin
// XxxUiState.kt
data class XxxUiState(val loading: Boolean, val records: List<Entity>, val error: String?)

// XxxViewModel.kt — sealed event + onEvent
sealed interface XxxEvent { ... }
class XxxViewModel : ViewModel() {
    fun onEvent(event: XxxEvent) { ... }
}
```

## 2. AI 开发流程

### 2.1 任务执行步骤

```
1. 阅读当前任务描述和文件列表
2. 检查依赖文件是否存在且符合预期
3. 生成代码（遵循 UI_CONTRACT 白名单）
4. 验证编译（运行 assembleDebug）
5. 若编译失败，修复后重复步骤 3-4
6. 提交 git commit
```

### 2.2 错误处理规则

- 所有 UseCase 必须 try-catch，失败时 emit `UiEvent.Error`
- ViewModel 不直接 try-catch，交给 UseCase
- UI 层通过 `when { state.loading / state.error / data.isEmpty() / else }` 处理

### 2.3 编译验证

每个任务完成后必须运行：

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

## 3. 提交规范

### 3.1 Commit Message 格式

```
类型: 简短描述

- 要点 1
- 要点 2
```

### 3.2 类型前缀

| 类型 | 用途 |
|------|------|
| feat | 新功能 |
| refactor | 重构 |
| fix | 修复 |
| test | 测试 |
| docs | 文档 |
| chore | 构建/依赖 |

## 4. 代码质量红线

- ❌ 不允许硬编码字符串（使用 string resource 或常量）
- ❌ 不允许 magic number（使用命名常量）
- ❌ 不允许未捕获的协程异常
- ❌ 不允许空列表时显示 Loading (即 Loading 必须有时效性)
- ❌ 不允许阻塞主线程
- ✅ 所有 Flow 必须通过 viewModelScope 管理
