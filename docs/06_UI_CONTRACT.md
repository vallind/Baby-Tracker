# UI 约束与组件白名单

## 1. 组件白名单

所有 UI 实现**必须**使用以下白名单内的组件。禁止引入额外 UI 库。

### Jetpack Compose (必需)

```
Text, Row, Column, Box, Spacer
LazyColumn, LazyRow, items
Scaffold, TopAppBar, BottomNavigation
Card, OutlinedCard
Button, OutlinedButton, TextButton, IconButton
FloatingActionButton
OutlinedTextField
Switch, Checkbox
CircularProgressIndicator, LinearProgressIndicator
Icon, Image
AlertDialog, DatePickerDialog
ExposedDropdownMenuBox, DropdownMenuItem
Tab, TabRow, PrimaryTabRow
AssistChip, FilterChip
```

### Material Icons (白名单)

```
Icons.Default.Add
Icons.Default.Delete
Icons.Default.CheckCircle
Icons.Default.ArrowBack
Icons.Default.ChildCare
Icons.Default.Bedtime
Icons.Default.TrendingUp
Icons.Default.Shield
Icons.Default.MonitorHeart
Icons.Default.BabyChangingStation
Icons.Default.Inbox
Icons.Default.Settings
Icons.Default.Info
```

### 设计系统组件 (必需)

```
ParentingCard
ParentingButton
EmptyState
LoadingView
ErrorView
SectionTitle
```

## 2. UI 约束

### 禁止行为

- ❌ 不使用额外的 UI 库（除白名单外）
- ❌ 不直接使用 `Modifier.padding()` 写死边距（使用设计系统值）
- ❌ 不创建新的主题色值（使用 Design System 定义的色彩）
- ❌ 不直接使用 `dp` 值（使用 Design System 定义的间距常量）

### 必需行为

- ✅ 所有列表页必须有 Loading / Empty / Error 三态
- ✅ 使用 ParentingCard 替代 Card
- ✅ 使用 ParentingButton 替代 Button（全宽按钮时）
- ✅ 新增/编辑页面使用 TopAppBar + 返回按钮
- ✅ 数据操作成功后 `popBackStack()`

## 3. 命名规范

### Compose 函数命名

- Screen: `名词 + Screen`，如 `FeedingScreen`
- Composable 组件: `名词`，如 `ParentingCard`
- 新增页面: `Add + 名词 + Screen`，如 `AddFeedingScreen`

### 文件命名

- Screen: `XxxScreen.kt`
- ViewModel: `XxxViewModel.kt`
- UiState: `XxxUiState.kt`
- Event: `XxxEvent` (sealed interface, 定义在 ViewModel 文件中或同包下)

## 4. 状态管理规范

```
data class XxxUiState(
    val loading: Boolean = false,
    val records: List<Entity> = emptyList(),
    val error: String? = null
)

sealed interface XxxEvent {
    data class Action(val params) : XxxEvent
}

class XxxViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(XxxUiState(loading = true))
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<XxxEvent>()
    val events: SharedFlow<XxxEvent> = _events.asSharedFlow()

    fun onEvent(event: XxxEvent) { ... }
}
```
