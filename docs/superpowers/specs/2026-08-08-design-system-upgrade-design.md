# 设计系统与组件全面升级设计文档

> 日期：2026-08-08 · 版本目标：1.8.x（版本号在发布批次统一提升）· 状态：已确认

## 一、背景与目标

当前 DS 已具备成熟的三层令牌架构（核心语义令牌 AppTokens → 组件令牌 AppComponentTokens → XxxDefaults 桥接 → Xxx 组件），但存在三类缺口：

1. **遗留 M3 直用**：feature 层 1 处原生 AlertDialog、9 处 M3 SnackbarHost、3 处 Surface、6 处 HorizontalDivider、2 处死 import；DS 内部 EmptyState 无令牌、13 处 `MaterialTheme.typography`、4 处 `MaterialTheme.colorScheme`、Button 次组件颜色绕过组件令牌。
2. **能力缺口**（对标 `docs/Palette组件库设计深度分析报告.md` P1/P2 路线图）：Typography 仅 7 级、无密度变体、无障碍语义无基线、暗色/状态令牌无测试。
3. **门禁缺口**：令牌合规仅靠 1 个静态审计测试，无独立 Gradle 任务、无自定义 Detekt 规则。

**目标**：增量增强现有架构（不推倒重来），完成清理 + 能力补全 + 自动化门禁，分 3 阶段执行，每阶段独立验收。

## 二、总体原则

1. **架构不动**：保持「核心语义令牌 → 组件令牌 → Defaults 桥接 → 组件」四层结构。
2. **API 统一**：新增组件遵循统一签名约定（见第五节）；变体用枚举参数表达，不拆新组件函数、不堆布尔参数。
3. **不盲目扩展数量**：只加实际使用的令牌字段与层级（Typography 12 级字段集由实际使用盘点得出，无预判性扩展）。
4. **红线**：触碰共享 API 前全局搜索调用方（红线 10）；暗色只认 `theme.name == "night"`（红线 8）；百分比夹紧 `coerceIn`（红线 1）；日期过滤 `take(10)`（红线 2）。
5. **每阶段验收**：`./gradlew assembleDebug` + `testDebugUnitTest` 全绿 + lint（涉及 Compose/资源时）+ 文档同步 + CHANGELOG 按批次累积。
6. **token 桥接 M3，M3 不暴露给组件**（用户原则）：
   - **theme 层可以桥接 M3**：`designsystem/theme/` 内部允许持有/转换 M3 类型（如 private M3 `Typography` 供 `MaterialTheme` 使用、`toColorScheme()`、`MaterialTheme(typography=...)` 的桥接），这是 token 体系与 M3 的唯一接触点。
   - **组件层不暴露 M3**：`designsystem/components/` 与 `feature/`（含 navigation/core 的 UI 代码）禁止直接 import/使用 M3 的令牌与主题类型——`androidx.compose.material3.Typography`、`ColorScheme`、`Shapes`、`MaterialTheme.typography`/`MaterialTheme.colorScheme`/`MaterialTheme.shapes`。组件只能通过 `LocalApp*` 令牌访问视觉参数。
   - **边界说明**：M3 组件类（`Button`/`OutlinedTextField` 等）在 DS 组件内部受控包裹是允许的（AppButton 包 material3.Button 等），但组件的**公开 API 签名不出现 M3 类型**；M3 状态类（如 `SnackbarHostState`）沿用既有 `AppSnackbar(hostState)` 模式，不扩大解释。

## 三、P1 清理（阶段 1）

### 3.1 Typography 自建单体系统一（不暴露 M3）

**现状（自审核实）**：存在双体系——
- `LocalAppTypography`（Theme.kt:186）当前提供 **M3 `Typography` 类**（`BabyTrackerTypography`，15 级），17 个文件使用；
- `LocalAppTypographyStyle`（AppTokens.kt:194）提供**自建 `AppTypography` 数据类**（7 级），14 个文件使用；
- 两套字号数值不一致（titleLarge 18sp vs 22sp、bodyLarge 15sp vs 16sp、bodyMedium 13sp vs 14sp），同名字段不同页面渲染字号不同。

**目标（用户决策：自建、不暴露 M3）**：统一到自建 `AppTypography` 数据类单体系，对外（designsystem 之外的任何代码）不暴露 M3 `Typography` 类型。

**字段全集（按实际使用盘点，12 级，每级均有真实使用点）**：
`displayLarge`、`headlineLarge`、`headlineMedium`、`headlineSmall`、`titleLarge`、`titleMedium`、`titleSmall`、`bodyLarge`、`bodyMedium`、`bodySmall`、`labelMedium`、`labelSmall`
（原始盘点含裸 `display`/`headline`/`label` 共 15 级，Task 2 分别映射到 `displayLarge`/`headlineMedium`/`labelMedium`，Task 3 删除裸字段，收敛为 12 级）

**实施**：
1. `AppTypography` 数据类扩展为上述 12 级；现有 7 级数值保持不变（避免 14 个文件视觉回归），新增 5 级在现有数值间按自建风格插值（数值表在实施计划中确定）。
2. `LocalAppTypography` 改为提供 `AppTypography` 数据类；删除 `LocalAppTypographyStyle`（统一单一入口）；删除 `BabyTrackerTypography`（M3 Typography 对象）。
3. `MaterialTheme(typography = ...)` 所需 M3 Typography 在 Theme.kt 内部**私有**构造（从自建数值映射），不导出。
4. **迁移 31 个文件**：17 个 `LocalAppTypography`（M3）文件 + 14 个 `LocalAppTypographyStyle` 文件 → 统一 `LocalAppTypography.current`。因字段全部同名，迁移 = 改 import / 别名来源，style 引用点基本零改动（`typography.label`、`typography.titleLarge` 等不变）。
5. DS 内部 13 处 `MaterialTheme.typography` → `LocalAppTypography.current`（TimePicker、TimePickerLogic、AppFormSheet、DateTimeCascade；字段在 12 级中全部存在，纯替换）。
6. 组件令牌 8 处 `typography.label/titleLarge/bodyLarge/bodyMedium` 引用（AppComponentTokens.kt:46 等）在 12 级下继续有效，无需改动。
7. 快照：`AppDefaults.kt` 无 typography 字段（已核实），无需改动；`docs/design-system.md` 更新。
8. P3 门禁新增规则：禁止在 designsystem 之外 import `androidx.compose.material3.Typography`（不暴露 M3 落地为审计规则）。

### 3.2 Button 家族收敛为 AppButton + 变体枚举

- 新增单一入口：`AppButton(label, onClick, variant = ButtonVariant.Primary, ...)`。
- `ButtonVariant` 枚举：`Primary / Secondary / Text`（现有三个函数一一对应）。
- 删除 `PrimaryButton`、`SecondaryButton`、`AppTextButton` 三个旧函数与 `PaiButton` 简化工厂（PaiButton 收口进 AppButton 默认参数）。
- **迁移 58 处调用方**（PrimaryButton 27 / AppTextButton 22 / SecondaryButton 9，14 个 feature 文件 + DS 内部 Button.kt/PaiButton.kt）。
- 次要颜色修复：Secondary/Text 变体颜色从「直接 `LocalAppColors.current.primary`」改为走 `ButtonTokens` 字段（新增 `secondaryContainerColor`、`textColor` 等）。
- 同步更新 `docs/design-system.md` 对应关系表与 `AGENTS.md` 第八节（`Button → AppButton`，原 PrimaryButton/SecondaryButton/AppTextButton 条目废弃）。

### 3.3 新组件（3 个）

每个组件均满足「两次规则 + 令牌封装」标准，含 `XxxTokens`（注册进 `AppComponentTokens`）+ `XxxDefaults` + 组件本体，更新 `docs/design-system.md`：

| 组件 | 令牌 | 封装要点 | 迁移量 |
|---|---|---|---|
| `AppSnackbarHost` | `SnackbarHostTokens` | 容器色/圆角/elevation，配合现有 `AppSnackbar` 逻辑类 | 9 处 SnackbarHost |
| `AppDivider` | `DividerTokens` | 颜色/厚度 | 6 处 HorizontalDivider |
| `AppSurface` | `SurfaceTokens` | surface 色/形状 | 3 处 Surface |

### 3.4 DS 内部遗留修复

- `EmptyState`：补 `EmptyStateTokens + EmptyStateDefaults`，内部 M3 Button 改用 AppButton、`MaterialTheme.colorScheme` 改用 `LocalAppColors`。
- 4 处 `MaterialTheme.colorScheme` → `LocalAppColors`（EmptyState、DateTimeCascade、TimePicker）。
- 修复 `ThemeTokenizationStaticAuditTest` 头部注释漂移（旧 Palette 命名）。

### 3.5 feature 层迁移

- `SettingsScreen:698` 编辑昵称 AlertDialog → `AppDialog`（内部已用 AppInput/AppTextButton，外壳替换）。
- 清理死 import：`ReminderScreen:10`、`TimelineScreen:13`。

## 四、P2 能力（阶段 2）

### 4.1 密度变体（compact / comfortable / large）

- 新增 `AppDensity` 枚举 + `AppDensityTokens`（三档：间距缩放系数 + 控件触控基准）。
- `LocalAppDensity`（`staticCompositionLocalOf`，默认 comfortable）。
- **零迁移机制**：`BabyTrackerTheme` 依据 density 对 `AppSpacing` 缩放后注入 `LocalAppSpacing`，全部组件自动生效；控件高度走 `AppControlTokens` 密度档（compact 40dp / comfortable 44dp / large 48dp+）。
- 存储与切换：`SettingsStore` 新增 `density` 字段；新建 `DensityController`（仿 ThemeController 模式：订阅流 + `mutableStateOf` + `switchDensity`），以 `single` 注册进 `core/di/Modules.kt`（与 ThemeController 注册方式一致，见 Modules.kt:44）。
- UI：设置页新增「界面密度」入口（三选一，复用 ThemePickerSheet 形态模式）。
- 组件 API：任何组件若需按密度区分，用枚举参数（如 `density: AppDensity`），不新增函数。

> **实现偏差标注（Task 8 回填）：** 三档具体数值落地为 `compact 0.85f / comfortable 1.0f / large 1.15f`（spacingScale）+ `controlHeightDelta ±8dp`（AppTokens.kt:390-392）。控件高度实际实现比计划更保守：`AppControlTokens.densityAdjusted(density)` **仅调整 medium 档高度**（48dp 基准 → 40/48/56dp，不按 spec 的 44dp 舒适档），small/large 档保持基准值（DensityTokensTest 断言）。存储字段实际落在 `AppSettings.appearance.density`（DataStore 聚合设置，非独立 SettingsStore 字段）。

### 4.2 无障碍语义基线

- 盘点 63 个组件分两类处理：
  - **可交互组件**（AppButton/AppIconButton/AppChip/AppSwitch/AppRadioButton/AppSlider/AppFAB/AppTopBar/SegmentedControl 等）：补 `contentDescription`/`stateDescription`/`role`/`disabled`/`toggleableState`。
  - **纯装饰组件**（BadgeIcon/BabyIllustration 等）：`clearAndSetSemantics` 隔离读屏。
- 新增 `docs/a11y-baseline.md` 基线文档。
- 审计测试新增断言：可交互组件必须含 semantics。

> **实现偏差标注（Task 8 回填）：** 实际实现按「M3 内置锚点 / 自定义显式承诺 / 装饰隔离」三类落位（详见 docs/a11y-baseline.md）。两点与计划有出入：① SegmentedControl 未加 `stateDescription`（与内部 Text 双读，实现时移除以避免重复朗读，commit df6a447/0a921d5）；② 自定义组件语义实际覆盖 SegmentedControl（Role.Tab+selected）、RecordCard（customActions）、AppRate/AppLabeledSlider（contentDescription）、TimePickerLogic 滚轮与 DateTimeCascade 日历（selected）、FAB/BottomNav（图标 contentDescription=null 防双读）。审计测试落地为 `A11ySemanticsAuditTest`（静态断言 8 处组件语义）。

### 4.3 暗色 / 状态令牌测试

新增 `ComponentTokensStateAuditTest`：

- **暗色差异**：`AppComponentTokens.default(colors, darkTheme = true/false)` 下关键 container/content 色对必须存在差异或显式不变，且符合可读性预期。
- **状态色完整性**：含状态色字段的令牌组（Button/Input/Select 等）必须派生齐全（disabled/hover/focus/error 无 `Color.Unspecified`）。

> **实现偏差标注（Task 8 回填）：** 测试形态与计划一致，共 3 项断言：暗色差异 2 项（按钮禁用容器色与主容器色恒定、对话框与输入框容器色）+ 状态色完整性 1 项。另新增 `DensityTokensTest` 4 项覆盖 4.1 密度缩放纯函数。

## 五、统一签名约定（新组件 + 收敛组件）

```
显式参数 > XxxDefaults > 组件令牌 > 核心语义令牌 > 回退值
```

1. 组件唯一入口函数名 `AppXxx(...)`；多形态用 `variant: XxxVariant` 枚举参数，不拆函数。
2. 默认值全部来自 `XxxDefaults`，不内联写死；Defaults 引用 `LocalAppComponentTokens`。
3. 颜色参数接受 `Color` 类型（默认 `Color.Unspecified` 表示走令牌），组件内部不写死色值。
4. 尺寸/间距参数默认走令牌（`Dp.Unspecified` 或令牌值），不用魔法数。
5. 无内部交互状态的组件不写 Logic；需要时用现有 Hooks 桥接模式。
6. 文件内 `@Composable` 全部顶层定义（红线 6）。
7. **组件公开签名禁止 M3 类型**：`Typography`/`ColorScheme`/`Shapes` 等 M3 令牌类型不出现在组件参数与返回值中（见总体原则 6）；桥接只发生在 `designsystem/theme/`。

## 六、P3 门禁（阶段 3）

### 6.1 themeTokenAudit 独立 Gradle 任务

- 提取 `ThemeTokenizationStaticAuditTest` 扫描逻辑为共享纯 Kotlin 检查器（JVM 可单测），Gradle 任务与单元测试双路复用同一份代码。
- 注册 `./gradlew themeTokenAudit`，扫描 `app/src/main/java` 全部 `.kt`，拦截：
  - `MaterialTheme.typography` / `MaterialTheme.colorScheme` 直接使用（Defaults 桥接层豁免白名单）
  - 硬编码 `Color(0xFF` / `Color.Black` / `Color.White`
  - `isSystemInDarkTheme()`
  - Defaults 文件 import `LocalAppColors`
  - designsystem 之外 import `androidx.compose.material3.Typography`（不暴露 M3 落地）

> **实现偏差标注（Task 6 回填）：** 检查器落地为 `TokenAuditChecker.audit(kotlinRoot, themeRelDir, componentsRelDir, componentTokensFile)` 四参签名（计划未定义签名，见 TokenAuditChecker.kt:42）。规则实际 5 条：Defaults 走组件令牌 / Defaults 硬编码颜色 / Defaults 引 LocalAppColors / 组件层 M3 令牌（含 `MaterialTheme.shapes` 与 M3 `Typography`/`ColorScheme`/`Shapes` import）/ 新组件令牌注册（divider/surface/snackbarHost/emptyState 硬校验）；`isSystemInDarkTheme()` 拦截未落地为独立规则（该模式已在早前批次清零，审计重点转向 M3 令牌/颜色/注册），另加 `ScanEmpty` 防呆（扫描为空或 AppComponentTokens.kt 缺失必报违规，lessons #14/#17）。门禁任务用 JavaExec 注册（mainClass=`TokenAuditCheckerKt`，group=verification，dependsOn compileDebugKotlin，违规 exitProcess(1)），classpath 直接引用 `debugCompileClasspath`——AGP 9 移除 sourceSets 容器、新建配置 `extendsFrom` 不继承变体属性（lessons #17）。静态审计测试 `ThemeTokenizationStaticAuditTest` 迁移为调用共享检查器，新增 `TokenAuditCheckerTest`（拦截样本/白名单样本/ScanEmpty 防呆 3 项）。

### 6.2 自定义 Detekt 规则

- 新增 detekt 插件（版本与 Kotlin 2.3.21 兼容）+ 独立规则模块产规则 jar。
- 两条自定义规则：`HardcodedColor`（硬编码颜色字面量）、`TokenBypass`（默认参数绕过组件令牌的颜色引用）。
- **前置 POC**：Termux 环境下验证 detekt 插件加载与规则 jar 构建链路；POC 失败则降级为仅 6.1 单任务方案，并在文档记录原因。

> **实现偏差标注（Task 6 回填）：** detekt 版本落地为 **1.23.8**（gradle/libs.versions.toml，POC 验证与 Kotlin 2.3.21 兼容，插件与 detekt-api/test 同版本）；Termux POC **通过**，未触发降级路径。实现约束：`config/detekt/detekt.yml` 采用**显式枚举**方案——detekt 1.23 移除 `@ActiveByDefault` 语义，独立 config 文件整体替换默认配置，规则集/规则未显式列出的一律不激活（ruleset 级 active 不级联）；`buildUponDefaultConfig` 在 Termux 上分析 184 个文件超 20 分钟不结束，显式枚举实测 ~9 秒（naming 整体关闭 + potential-bugs 逐条对齐 1.23.8 默认激活 + 自定义规则集逐条列出）。规则实现与计划语义一致、判定层更细：`HardcodedColor` 以「绑定 import」判定——命中 `Color(0xFF...)` 调用与 `Color.Black`/`Color.White` 属性（含全限定写法），豁免未 import compose `Color` 的文件与路径含 `designsystem/theme` 的令牌定义层（通配 import 不识别，已知盲区）；`TokenBypass` 命中 components 包 `Defaults.kt` 文件 import `LocalAppColors`（形态 1）与组件层 `MaterialTheme.colorScheme|typography|shapes` 直用（形态 2），豁免 theme 桥接层。`./gradlew detekt` 为 report-only（`ignoreFailures=true`，只报告不阻断），存量 24 处 `HardcodedColor` 违规（components 7 + feature 17，`Color.White.copy(alpha=...)` 等无令牌等价物）列为已知债务。规则单测：HardcodedColorRuleTest / TokenBypassRuleTest（detekt-test + assertj-core）；改规则后须 `./gradlew --stop` 再跑 detekt（ClassLoaderCache，lessons #18）；Rule 子类 `issue` 声明须在任何自定义 init 块之前（lessons #19）。

## 七、测试与验收

- 每阶段收尾必跑：`./gradlew assembleDebug`、`./gradlew testDebugUnitTest`（全绿）、`./gradlew lint`（涉及 Compose/资源/Manifest/API 时）。
- 新增逻辑补测试：
  - P1：AppTypography 12 级字段全量/数值一致性测试、3 个新组件的静态审计断言（纳入审计检查器）、AppButton 变体视觉差异断言（Paparazzi 如可行）、58 处调用方迁移后编译通过即回归验证。
  - P2：密度缩放纯函数测试、暗色/状态令牌测试（4.3）、a11y 审计断言。
  - P3：检查器单测（拦截样本 + 白名单样本，含"禁止 import M3 Typography"规则）。
- 禁止镜像测试：测试必须调用生产代码。
- 文档同步义务：`docs/design-system.md`（新组件/密度/Typography/AppButton）、`docs/project-structure.md`（DensityController、规则模块等）、`docs/a11y-baseline.md`（新增）、`AGENTS.md`（第八节 Button 对应关系）、CHANGELOG.md（按提交批次累积，版本发布时统一提升）。
- 经验闭环：每阶段完成后检查 lessons.md 是否需追加条目。

## 八、阶段划分与依赖

| 阶段 | 内容 | 依赖 |
|---|---|---|
| P1 清理 | Typography 12 级、AppButton 收敛、3 新组件、EmptyState 令牌化、AlertDialog/死 import 迁移 | 无（先行，打通全量回归） |
| P2 能力 | 密度变体、a11y 基线、暗色/状态令牌测试 | P1（Typography/组件结构就绪后做） |
| P3 门禁 | themeTokenAudit 任务、Detekt 规则 | P1/P2（检查器需覆盖全部组件） |

每阶段独立计划（writing-plans）、独立验收、独立提交批次（CHANGELOG 挂版本时统一）。
