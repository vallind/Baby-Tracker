# 设置页新增日志查看器 — 实施计划

## 步骤清单

### 1. 添加 Timber 依赖

**`gradle/libs.versions.toml`** — 追加：
```toml
# ── Logging ──
timber = "5.0.1"

timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
```

**`app/build.gradle.kts`** — 追加 `implementation(libs.timber)`

---

### 2. 新建 `core/util/LogTree.kt`

- `LogBuffer` — 线程安全的环形缓冲区，存储 `LogEntry`（timestamp, level, tag, message），上限 1000 条，提供 `getEntries()` / `clear()`
- `AppLogTree` — 继承 `Timber.Tree`，同时写入 Logcat + LogBuffer + 文件（`$filesDir/logs/`）

---

### 3. 修改 `BabyTrackerApp.kt`

```kotlin
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        Timber.plant(AppLogTree(this))
    }
}
```

---

### 4. 新建 `feature/settings/LogViewerScreen.kt`

- `@Composable fun LogViewerScreen(navController: NavController)`
- 使用 `AppScaffold` + `AppTopBar("日志查看", onBack)`
- 操作栏：过滤输入 + 刷新/清除/自动滚动按钮
- LazyColumn 显示日志条目，按级别着色
- LaunchedEffect 定期刷新

---

### 5. 修改 `navigation/AppNavigation.kt`

- 添加 `object LogViewer : Screen("/settings/logviewer")`
- 添加 `composable(Screen.LogViewer.route) { LogViewerScreen(navController) }`

---

### 6. 修改 `feature/settings/SettingsScreen.kt`

FunctionGrid 添加 `FunctionGridItem("📋", "日志查看", onClick = { navController.navigate(Screen.LogViewer.route) })`

---

### 7. 日志埋点（10 处）

| 文件 | 埋点位置 |
|---|---|
| `SyncEngine.push()` | 循环开头 + catch 块 |
| `SyncEngine.pull()` | 开头 + catch 块 |
| `FamilyService.joinFamily()` | RPC 后 |
| `FamilyService.loadMyFamilies()` | 返回后 |
| `SettingsViewModel.tryAutoSync()` | 结束 |
| `SettingsViewModel` family collect | 切换时 |
| `AuthService observeAuthState()` | 变化时 |
| `AuthService signUp/signIn` | 结果 |

---

### 8. 验证

```bash
./gradlew assembleDebug
```
