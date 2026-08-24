# P0 边界收敛 + P1 低风险整理 实施方案（v4 · 已批准执行）

> 状态：**已批准** 评审总评：架构方向 9/10 · 复杂度控制 9/10 · 可执行性 8.5/10。**Batch 1 开始执行。**
>
> v3 → v4 修订（执行前 4 处调整，已全部采纳）：

| # | v3 表述 | v4 修订 |
|---|---|---|
| 1 | Feature ──→ Repository（未区分契约/实现） | **Feature 只依赖 Repository 契约（interface）**，不直接依赖具体实现/数据源；实现类由 Koin 绑定在 interface 后面。不加层，只是明确依赖方向 |
| 2 | 「每普通 Feature 恰好三文件」 | **三文件是默认模板，不是死规则**：仅当真实复杂度出现时才增加文件（Settings/AiChat 等天然多文件不算违规） |
| 3 | Route = DI + Navigation + VM 装配，示例里 Route 做 babyCtrl 解析 + LaunchedEffect 加载 | **Route 是组合根，不是业务逻辑容器**：当前宝宝解析、数据加载全部进 ViewModel；Route 只做 DI → VM → 导航回调 → Screen |
| 4 | Batch 6 迁移含「孤儿清理 DELETE」 | **迁移默认不做不可逆数据删除**：六表先统计孤儿数，任一非 0 → 迁移失败（抛异常），人工决定后再迁；8→9 前不自动删任何行 |
| 5 | 每批 versionCode +1 / versionName 升 minor | **同一功能批次内版本号只升一次**：Batch 1 起 versionName 2.2.0 → 2.3.0（整个重构波次一个 minor）；versionCode 仅产生可发布构建时递增；批次内提交均为 `refactor:` 中文 commit |
| 6 | 「任一 VM 超 ~800 行必须拆」 | **800 行只是警戒线不是规则**：按职责拆（是否存在多个互不相关的状态生命周期） |

> v1 → v2 修订对照（避免伪 MVI / God VM / escape hatch / Robolectric / CASCADE / 时间模型拆分）见下方附录 A。

---

## 0. 核心架构（三层原则，十原则已写入 AGENTS.md）

```
┌──────────────────────────────┐
│        Design System         │   UI 怎么长得好看、统一、可复用
│  Theme / Token / Components  │   不知道 Feature / Repository / Navigation / Koin
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│           Feature            │   页面显示什么 + 用户做了什么
│  Route / ViewModel / Screen  │   Route=组合根；VM=业务状态；Screen=纯 UI
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│      Repository 契约         │   数据从哪里来、存到哪里去
│       (interface)            │   Feature 只依赖契约
└──────────────┬───────────────┘
               │
     ┌─────────┴─────────┐
     ↓                   ↓
  Repository 实现     （Koin 绑定在 interface 后面）
     │
  Room / Supabase
```

**Navigation 是「页面之间怎么走」的胶水，不是第四层业务架构；DI/Koin 是基础设施，不算一层。**

十原则要点（全文见 AGENTS.md「核心架构」）：DS 三不知 / Screen 三禁 / VM 禁 UI lambda·禁 NavController / **Route 是组合根不承载业务逻辑** / **Feature 依赖 Repository 契约不依赖具体实现** / UI 临时状态留 Screen、业务状态进 VM / 除非真实复杂度否则不增加架构层。

```text
❌ 禁止预先引入：UseCase / DomainService / DataSource / Mapper / Interactor / Presenter /
   Reducer / Coordinator / GenericRepository / 全局 Destination 抽象 / 全量 Event 总线 / 四层 Model
```

**Feature 默认模板**：Route（组合根）/ Screen（纯 UI）/ ViewModel（业务，UiState 同文件）三文件；**默认形态，不是死规则**。

---

## 1. Batch 1：DesignSystem 边界（执行中）

### 1.1 BottomNav 拆两层

- `designsystem/components/navigation/AppNavigationBar.kt`（纯 UI，零业务依赖）：

```kotlin
data class AppNavigationItem(val label: String, val icon: ImageVector, val badgeCount: Int = 0)

@Composable
fun AppNavigationBar(
    items: List<AppNavigationItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

胶囊裁切 + 暖阴影 + 无障碍语义留在 DS 层（lessons #21/#15）；`BottomBarDefaults.kt` 平移进 `components/navigation/`。

- `navigation/AppBottomBar.kt`（App 层壳）：

```kotlin
@Composable
fun AppBottomBar(navController: NavController) {
    val messageRepo: MessageRepository = koinInject()   // 业务依赖只允许出现在这里
    val unreadCount by messageRepo.watchUnreadCount().collectAsState(initial = 0)
    // 5 Tab：Home/Timeline/Stats/Message/Settings，selected 由 hasRoute 匹配
    AppNavigationBar(items = ..., selectedIndex = ..., onItemClick = { idx -> navController.navigateToRoot(...) })
}
```

- 删除 `designsystem/components/bottomnav/BottomNav.kt`；
- 调用点 9 处（Home/Sleep/Feeding/Diaper/Growth/Stats/Timeline/Message/Settings）全部替换；
- Tab 文案迁入 `AppStrings`（现为硬编码中文）。

### 1.2 ThemeController / DensityController 迁出 designsystem

- 文件平移至 `core/settings/`（与 SettingsStore 同包）；类名/API 不变；
- 调用方（执行时全量 grep）：MainActivity / core/di/Modules.kt / SettingsScreen.kt / SettingsMenuScreen.kt / 相关测试。

### 1.3 守护测试（本批落地）

静态审计测试（沿用 TokenAuditChecker 模式，注意 lessons #24 路径比较坑）：designsystem 源码禁止 import `com.babytracker.core` / `com.babytracker.feature` / `com.babytracker.navigation` / `androidx.navigation` / `koin`。DS 边界从此由测试守门。

---

## 2. Batch 2：Route / Screen 拆分 · 已有 VM 的 11 屏（结构性）

**Route 是组合根，不是业务逻辑容器** —— 当前宝宝解析、数据加载全部在 ViewModel：

```kotlin
// ── ViewModel：业务状态 + 数据加载 + 当前宝宝（组合进构造）──
class HomeViewModel(
    private val feedingRepo: FeedingRepository,   // 依赖契约，不依赖实现
    private val sleepRepo: SleepRepository,
    private val diaperRepo: DiaperRepository,
    private val babyCtrl: BabyController,          // 宝宝选择/当前宝宝
) : ViewModel() {
    val state: StateFlow<HomeUiState>
    // init 内监听 babyCtrl.currentBabyId，自动加载 —— Route 无需 LaunchedEffect
}

// ── Route：组合根，只做 DI + VM 装配 + 导航回调映射 ──
@Composable
fun HomeRoute(navController: NavController) {
    val viewModel: HomeViewModel = koinViewModel()

    HomeScreen(
        state = viewModel.state.collectAsState().value,
        baby = viewModel.currentBaby,
        onSeeAll = { navController.navigate(Timeline) },
        onOpenAi = { navController.navigate(AiAssistant) },
        onOpenProfile = { navController.navigate(BabyProfile) },
    )
}

// ── Screen：纯 UI ──
@Composable
fun HomeScreen(
    state: HomeUiState,
    baby: Baby?,
    onSeeAll: () -> Unit,
    onOpenAi: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- 方法回调而非 Event 全量化；UI 临时状态（sheet 展开、picker 显隐）留 Screen；
- 不建全局 sealed Destination；
- 11 屏清单：Home（FeatureGrid / AiAssistantEntryCard 的 navController 改回调）、Stats、Timeline、Message、Development、Reminder、AiChat、AiSettings、Family、Login、SyncSettings；
- 验收：Home + 1 个记录屏 Paparazzi 样板（fake state + `{}` 回调），脱离 NavHost/Koin/Room/Supabase 运行。

---

## 3. Batch 3：六个记录页建 VM（Feeding / Sleep / Growth / Vaccination / Health / Diaper）

按 §0 默认模板三文件落地（UiState 与 VM 同文件）。**不强行 VM 化**：只有把 Screen 现有逻辑（watch 订阅/筛选/删除撤销/表单提交）收进 VM 是明确收益时才收；纯 UI 状态一律留 Screen。

```kotlin
data class FeedingUiState(
    val records: List<Feeding> = emptyList(),
    val filterDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val undoPending: UndoInfo? = null,     // 只有数据，没有回调
    val editing: Feeding? = null,          // 非空 = 编辑中
)
data class UndoInfo(val recordId: Int)

class FeedingViewModel(
    private val repository: FeedingRepository,   // 契约
    private val babyCtrl: BabyController,        // 当前宝宝进 VM，Route 不解析
) : ViewModel() {
    fun load(babyId: Int)
    fun onDateChange(date: LocalDate?)
    fun delete(id: Int)                    // 删 → state.undoPending → UI 弹 Snackbar
    fun undoDelete()
    fun save(draft: FeedingDraft)
    fun edit(record: Feeding)
    fun dismissEdit()
}
```

- 状态归属标准（评审采纳）：**刷新页面以后还应该存在的状态 → VM；只是当前 UI 怎么展示 → Screen**（`showDatePicker`/`expanded`/`selectedTab` 都是 Screen 的）；
- 迁移来源：六屏现有 watchByBaby 订阅、日期筛选、delete→snackbar→undo、表单提交、SharedPreferences koinInject（FeedingListScreen L364 / SleepListScreen L339）收编进各自 VM；
- BabyProfile / LogViewer 并入本批末尾；每屏独立提交，提交时全绿。

---

## 4. Batch 4：Settings 拆解

```
feature/settings/
├── SettingsScreen.kt          # 只留 Section 编排（< 500 行）
├── SettingsComponents.kt      # SettingsRow / Card / Divider / SectionTitle / ThemeDots
├── BabyManagementScreen.kt    # 独立文件（含两个对话框）
├── BackupScreen.kt            # 独立文件
└── ViewModels:
    ├── SettingsViewModel.kt       # 薄：只编排设置主页自己的展示状态
    ├── BabyManagementViewModel.kt # 宝宝增删改 + 出生日计算
    └── BackupViewModel.kt         # BackupManager 全部逻辑
```

- 业务归属：AuthService/FamilyService（登录态/家庭）；BabyRepository + BabyManagementViewModel（宝宝）；ThemeController/DensityController（主题/密度，Batch 1 已迁）；SyncViewModel（同步，不动）；BackupManager + BackupViewModel（备份）；
- **拆分判断按职责，不按行数**：VM 是否拥有多个互不相关的状态生命周期？是 → 拆。800 行只是警戒线；
- `SettingsMenuScreen` 双 `koinViewModel<SettingsViewModel>()` 实例问题：本批评估，状态不跨屏则维持现状。

---

## 5. Batch 5：Token 单一来源 + AppButton 收紧

```kotlin
private fun AppTypography.toMaterialTypography(): Typography = Typography(/* 12 级逐项映射，数值已一致，零视觉变化 */)
// 数值等价映射（保持 M3 默认形不变）：extraSmall←small(12) small←medium(16) medium←large(20)
// large←largeIncreased(24) extraLarge←extraLarge(32)
private fun AppShapes.toMaterialShapes(): Shapes = Shapes(/* 上表 */)

MaterialTheme(
    colorScheme = colorScheme,
    typography = tokensTypography.toMaterialTypography(),
    shapes = tokensShapes.toMaterialShapes(),
    content = content,
)
```

- 删除 `internalMaterialTypography` + `BabyTrackerShapes`；DS 组件全走 LocalApp*，零 UI 回归；
- AppButton 收紧为语义 API（无 escape hatch）：

```kotlin
enum class ButtonSize { Small, Medium, Large }   // 映射 AppControlTokens.small/medium/large

@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,        // 复现现状 48dp
    modifier: Modifier = Modifier,
)
```

- 删除 11 个裸覆盖参数（AppButtonStyle 方案废弃）；约 47 处调用点迁移；静态审计测试守门；
- 文档：design-system.md 优先级模型更新 + 组件令牌计数口径一致化。

---

## 6. Batch 6：Room baby_id FK + Index（Migration 8→9）

- 六张表（feedings / sleeps / growths / vaccinations / health_records / diapers）补 `FOREIGN KEY(baby_id) REFERENCES babies(id)`（**不带 CASCADE**）+ `Index(baby_id)`；babies 补 familyId 索引；
- 12 步重建（SQLite 无法 ALTER 加 FK）：建新表（含 FK）→ INSERT SELECT → DROP → RENAME → CREATE INDEX；
- **迁移默认不做不可逆数据删除（评审核心修正）**：

```sql
-- 迁移第一步（任何 DDL 之前）：六表逐一统计孤儿数
SELECT COUNT(*) FROM feedings      WHERE baby_id NOT IN (SELECT id FROM babies);
SELECT COUNT(*) FROM sleeps        WHERE baby_id NOT IN (SELECT id FROM babies);
-- ... 六表全部统计
```

  - 全部为 0 → 正常执行迁移；
  - 任一非 0 → **迁移失败（migrate() 抛异常），绝不自动 DELETE**；孤儿数据可能来自旧版 bug / 同步顺序 / 备份恢复 / 家庭数据迁移，是否清理由人工决定（恢复 baby 或明确清理）；
- 时间列 TEXT 原样搬移（本批不碰时间模型）；验证 = SQL 预演脚本 + 真机冒烟；不引入 Robolectric；
- 版本 8 → 9；明确不做：时间模型、复合索引设计（留 P2-A）。

---

## 7. P2-A（单独立项，不在本方案执行）

时间模型统一（String → Long + Domain 类型化 + Sync/Backup/Stats/Timeline/Home 调用面）。**String 时间虽不漂亮但能正常工作**，与 FK/Index 缺失（结构性问题）是两个不同等级的问题。若未来执行，按 DB → Repository 映射 → Domain → VM → UI → Sync → Backup 逐层迁移，不一次全仓库替换。

---

## 8. 执行批次与版本

| 批 | 内容 | 依赖 | 风险 | 验收 |
|---|---|---|---|---|
| Batch 1（已完成 ✅） | DS 边界（BottomNav 拆层 + Controller 迁 core/settings + 审计守门） | 无 | 低 | 编译+测试全绿（唯一红 = 既有 Batch 2 守门 ScreenBoundaryAuditTest）+ DS 边界审计通过 |
| Batch 2（已完成 ✅） | 11 个已有 VM 屏 Route/Screen 拆分 + Paparazzi 样板 | 1 | 低 | 编译+测试全绿（唯一红 = 既有 Batch 2 守门，已收敛至 Batch 3/4 范围）+ Home Screen 测试脱离 Koin/Room 运行 |
| Batch 3（已完成 ✅） | 六记录屏建 VM + 状态迁移（逐屏提交） | 2 | 中 | 每屏提交全绿 |
| Batch 4（已完成 ✅） | Settings 拆文件 + BackupVM / BabyManagementVM / 薄 SettingsVM | 3 | 中 | 全绿 + 按职责无 God VM；ScreenBoundaryAuditTest 首次全绿 |
| Batch 5（已完成 ✅） | Token 桥接 + AppButton 收紧 | 无（可与 4 并行） | 低～中 | 全绿 + 审计守门（AppButtonApiAuditTest） |
| Batch 6（已完成 ✅） | Room FK + Index（Migration 8→9） | 5 | 中 | 孤儿预检 + v9 schema 交叉核对 + 预演脚本 + 全绿（真机冒烟待用户） |

**版本规则**（AGENTS.md §七 已同步更新）：整个重构波次作为一个发布批次，**versionName 只升一次**（2.2.0 → 2.3.0，Batch 1 首个提交时）；批次内后续提交不升版本号；versionCode 仅在产生可发布构建时递增。每批：`assembleDebug` + `testDebugUnitTest` 全绿 → `refactor:` 中文 commit → CHANGELOG 汇聚到 2.3.0 小节 → 文档同步（§9）。

**为什么 FK/Index 必须现在做、时间模型必须缓**：前者是结构完整性问题（高频过滤字段无索引 + 引用完整性缺失）；后者是一次数据模型重构，横跨全链路，边界收敛完成前动手只会互相污染。

---

## 9. 文档同步映射

| 改了什么 | 文档 |
|---|---|
| 新包/新文件（navigation/AppBottomBar、feature/*/Route、core/settings 迁入） | `docs/project-structure.md` |
| AppButton 参数、Token 桥接、计数口径 | `docs/design-system.md` |
| 六表 FK/Index、版本 8→9 | `docs/data-architecture.md`、`docs/room-supabase-architecture.md` |
| sync_metadata 无改动（确认迁移不触碰） | `docs/sync-architecture.md` |
| 架构十原则 + 版本规则（已完成） | `AGENTS.md` |
| 本次方案记录 | 本文档 |

---

## 附录 A：v1 → v2 修订对照

| # | v1 原案 | v2 修订 | 理由 |
|---|---|---|---|
| 1 | 全部 UI 行为塞进 `sealed interface XxxEvent` | 取消 Event 全量化：Screen 收逐动作方法回调 | 避免 MVVM → 伪 MVI |
| 2 | `uiState` / `event` 独立文件 | UiState 与 ViewModel 同文件 | 不制造文件碎片 |
| 3 | Delete Event 携带 `onUndo: () -> Unit` | VM 不持有 UI lambda；撤销走 `state.undoPending` | VM 不知道 UI 回调 |
| 4 | Settings 全部业务收进 SettingsViewModel | 按业务状态拆（BackupVM / BabyManagementVM 独立） | 防止 God Screen → God VM |
| 5 | `AppButtonStyle` escape hatch | 去掉。AppButton 只留 variant + size | 抽象后置 |
| 6 | MIGRATION_8_9 一锅端 | 只做 FK + Index；时间模型拆出为 P2-A | 问题等级不同 |
| 7 | FK 带 ON DELETE CASCADE | FK 不带 CASCADE | soft delete / tombstone 链路 |
| 8 | `shell.theme` 新包 | Controller 迁入 core/settings | 不新建包再塞业务 |
| 9 | 强推 Robolectric 迁移测试 | 不引入。SQL 预演脚本 + 真机冒烟 | 不为迁移测试强行上基建 |

## 10. 不做清单

MVVM（StateFlow 方法回调）/ Koin / Room / Supabase / Navigation Compose / SyncCursor / WorkManager / AI 适配器 / 多模块 / Hilt 保留不动；不建 shell 包；不引入上述禁止清单中的任何预置抽象；不为迁移测试引入 Robolectric；不提前设计 escape hatch；不建全局 Destination 抽象。