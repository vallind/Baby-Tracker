# 设计系统升级 P3（门禁阶段）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成设计系统升级 P3：提取共享令牌审计检查器（Gradle 任务 + 单元测试双路复用）、注册 `themeTokenAudit` 独立 Gradle 任务、Detekt 集成与两条自定义规则（HardcodedColor / TokenBypass）。

**Architecture:** 检查器为纯 Kotlin（扫描文件文本 → 违规列表），放 app 主源码（`com.babytracker.core.util.TokenAuditChecker`），JUnit 测试直接调用；Gradle 任务用 `JavaExec`（classpath = main 输出 + debugRuntimeClasspath）跑检查器的 `main()` 入口。Detekt 自定义规则为独立模块 `:detekt-rules`（app 的依赖，`detektPlugins(project(":detekt-rules"))` 接入）——**Termux POC（Task 3）先行验证**，失败则降级为仅 themeTokenAudit 任务（设计文档 6.2 已定）。

**Tech Stack:** Kotlin 2.3.21 / AGP 9.2.1 / Gradle 9.5.1 / detekt（版本由 POC 确定）/ JUnit4 / 单模块 app + 新增 :detekt-rules 模块

**设计文档:** `docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md` 第六节（6.1 / 6.2）

## Global Constraints

- 每个任务结束必跑：`./gradlew assembleDebug` 与 `./gradlew testDebugUnitTest`，全绿才算完成
- 注释与 Commit message 一律中文（AGENTS.md 第七节）
- 检查器必须「单份代码双路复用」：Gradle 任务与 JUnit 测试不得各写一份扫描逻辑
- 红线：百分比 `.coerceIn(0f, 1f)`；`@Composable` 顶层；改共享 API 先查调用方；token 桥接 M3 原则（theme 层唯一豁免）
- **Termux 环境已知坑**：`/usr/bin/env` shebang 不可用（skill 脚本已适配）；`./gradlew` 命令较慢，给足超时
- 检查器违规规则集（与现有静态审计测试一致，5 条 + Gradle 全量扫描 2 条）：
  1. Defaults 文件必须包含 `LocalAppComponentTokens`
  2. Defaults 文件禁止 `Color.Black` / `Color.White` / `Color(0xFF`
  3. Defaults 文件禁止 import `LocalAppColors`
  4. 组件层（非 theme 目录）禁止 import M3 令牌类型（Typography/ColorScheme/Shapes）与 `MaterialTheme.typography/colorScheme/shapes`
  5. 新增组件 Defaults 必须注册进 AppComponentTokens（`val xxx:` 字段存在）
- 文档同步义务：design-system.md（门禁章节）、project-structure.md（detekt-rules 模块、TokenAuditChecker）、CHANGELOG（1.8.0）、spec 第六节偏差标注、lessons.md（新坑）
- 检查器产物属于开发工具，不参与运行时（R8 自动剔除未引用代码）

---

### Task 1: 共享检查器 TokenAuditChecker（纯 Kotlin + 单测）

**Files:**
- Create: `app/src/main/java/com/babytracker/core/util/TokenAuditChecker.kt`
- Create: `app/src/test/java/com/babytracker/core/util/TokenAuditCheckerTest.kt`
- Modify: 无（本任务不改现有审计测试——Task 2 迁移）

**Interfaces:**
- Consumes: 现有 `ThemeTokenizationStaticAuditTest` 的 5 条规则语义（读该文件对照）
- Produces:
  ```kotlin
  package com.babytracker.core.util

  /** 令牌审计违规项 */
  data class AuditViolation(val file: String, val rule: String, val detail: String)

  /**
   * 令牌化静态审计检查器 — 纯 Kotlin 文本扫描，Gradle 任务与 JVM 单测双路复用。
   * 规则编号与 ThemeTokenizationStaticAuditTest 对应。
   *
   * @param kotlinRoot 源码根目录（如 app/src/main/java）
   * @param themeRelDir theme 包相对路径（如 com/babytracker/designsystem/theme，M3 桥接豁免）
   * @param componentsRelDir components 包相对路径（如 com/babytracker/designsystem/components，Defaults 扫描范围）
   * @param componentTokensFile AppComponentTokens.kt 文件（注册字段校验）
   */
  fun audit(
      kotlinRoot: File,
      themeRelDir: String,
      componentsRelDir: String,
      componentTokensFile: File,
  ): List<AuditViolation>
```
  语义：`kotlinRoot` = 源码根（如 `app/src/main/java`），`themeRelDir` = theme 包相对路径（如 `com/babytracker/designsystem/theme`，用于豁免），`componentTokensFile` = AppComponentTokens.kt 路径。返回全部违规（规则 1-5 聚合）。

- [ ] **Step 1: 写失败测试**

新建 `TokenAuditCheckerTest.kt`，用 JUnit 临时目录构造样本：

```kotlin
package com.babytracker.core.util

import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TokenAuditCheckerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun write(relPath: String, content: String): File =
        File(tmp.root, relPath).apply { parentFile.mkdirs(); writeText(content) }

    @Test
    fun `违规样本应被全部检出`() {
        write(
            "com/babytracker/designsystem/components/bad/BadDefaults.kt",
            "package com.babytracker.designsystem.components.bad\n" +
                "import com.babytracker.designsystem.theme.LocalAppColors\n" +
                "object BadDefaults { val c = Color(0xFF000000) }\n",  // 无 LocalAppComponentTokens + 硬编码 + 引 LocalAppColors
        )
        write(
            "com/babytracker/feature/bad/BadScreen.kt",
            "package com.babytracker.feature.bad\n" +
                "import androidx.compose.material3.Typography\n",  // 组件层导入 M3 令牌
        )
        val componentTokens = write(
            "com/babytracker/designsystem/theme/AppComponentTokens.kt",
            "object AppComponentTokens { /* 无 divider 字段 */ }\n",
        )
        val violations = TokenAuditChecker.audit(
            kotlinRoot = tmp.root,
            themeRelDir = "com/babytracker/designsystem/theme",
            componentsRelDir = "com/babytracker/designsystem/components",
            componentTokensFile = componentTokens,
        )
        val ruleNames = violations.map { it.rule }
        assertTrue("Defaults 缺 LocalAppComponentTokens 未检出", ruleNames.contains("DefaultsMissingLocalAppComponentTokens"))
        assertTrue("Defaults 硬编码颜色未检出", ruleNames.contains("DefaultsHardcodedColor"))
        assertTrue("Defaults 引 LocalAppColors 未检出", ruleNames.contains("DefaultsImportsLocalAppColors"))
        assertTrue("组件层 M3 令牌未检出", ruleNames.contains("ComponentLayerM3Token"))
        assertTrue("新组件令牌未注册未检出", ruleNames.contains("ComponentTokensMissingRegistration"))
    }

    @Test
    fun `合规样本应零违规且 theme 层豁免`() {
        write(
            "com/babytracker/designsystem/components/good/GoodDefaults.kt",
            "package com.babytracker.designsystem.components.good\n" +
                "import com.babytracker.designsystem.theme.LocalAppComponentTokens\n" +
                "object GoodDefaults { fun color() = LocalAppComponentTokens.current.button.containerColor }\n",
        )
        write(
            "com/babytracker/designsystem/theme/Theme.kt",
            "package com.babytracker.designsystem.theme\n" +
                "import androidx.compose.material3.Typography\n",  // theme 层豁免
        )
        val componentTokens = write(
            "com/babytracker/designsystem/theme/AppComponentTokens.kt",
            "object AppComponentTokens { val divider: DividerTokens; val surface: SurfaceTokens; val snackbarHost: SnackbarHostTokens; val emptyState: EmptyStateTokens }\n",
        )
        val violations = TokenAuditChecker.audit(
            kotlinRoot = tmp.root,
            themeRelDir = "com/babytracker/designsystem/theme",
            componentsRelDir = "com/babytracker/designsystem/components",
            componentTokensFile = componentTokens,
        )
        assertTrue("合规样本出现违规: ${violations.joinToString { "${it.file}:${it.rule}" }}", violations.isEmpty())
    }
}
```

（规则名常量定义在检查器里，测试引用同名常量或字面量——以检查器实际常量名为准，保持一致。）

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.core.util.TokenAuditCheckerTest"`
Expected: 编译失败（TokenAuditChecker 不存在）

- [ ] **Step 3: 实现检查器**

`TokenAuditChecker.kt` 核心逻辑（从 ThemeTokenizationStaticAuditTest 平移 5 条规则，无业务改动）：

```kotlin
package com.babytracker.core.util

import java.io.File

data class AuditViolation(val file: String, val rule: String, val detail: String)

object TokenAuditChecker {

    const val RULE_DEFAULTS_MISSING_TOKENS = "DefaultsMissingLocalAppComponentTokens"
    const val RULE_DEFAULTS_HARDCODED_COLOR = "DefaultsHardcodedColor"
    const val RULE_DEFAULTS_IMPORTS_COLORS = "DefaultsImportsLocalAppColors"
    const val RULE_COMPONENT_M3_TOKEN = "ComponentLayerM3Token"
    const val RULE_TOKENS_MISSING_REGISTRATION = "ComponentTokensMissingRegistration"

    private val hardcodedColorPatterns = listOf(
        "Color.Black" to "hardcoded Color.Black",
        "Color.White" to "hardcoded Color.White",
        "Color(0xFF" to "hardcoded Color",
    )

    private val m3TokenImports = listOf(
        "import androidx.compose.material3.Typography",
        "import androidx.compose.material3.ColorScheme",
        "import androidx.compose.material3.Shapes",
    )

    fun audit(
        kotlinRoot: File,
        themeRelDir: String,
        componentsRelDir: String,
        componentTokensFile: File,
    ): List<AuditViolation> {
        val violations = mutableListOf<AuditViolation>()
        val componentsDir = File(kotlinRoot, componentsRelDir.replace('.', '/'))
        val defaultsFiles = if (componentsDir.exists()) {
            componentsDir.walkTopDown().filter { it.name.endsWith("Defaults.kt") }.toList()
        } else emptyList()
        // 防呆（lessons #14）：扫描为空必须报错，禁止静默假绿
        if (defaultsFiles.isEmpty()) {
            violations.add(AuditViolation(componentsDir.path, "ScanEmpty", "Defaults 扫描为空，路径可能漂移"))
        }
        for (file in defaultsFiles) {
            val text = file.readText()
            if (!text.contains("LocalAppComponentTokens")) {
                violations.add(AuditViolation(file.path, RULE_DEFAULTS_MISSING_TOKENS, "Defaults 未走组件令牌"))
            }
            for ((pattern, label) in hardcodedColorPatterns) {
                if (text.contains(pattern)) {
                    violations.add(AuditViolation(file.path, RULE_DEFAULTS_HARDCODED_COLOR, "包含 $label"))
                }
            }
            if (text.contains(Regex("import.*LocalAppColors"))) {
                violations.add(AuditViolation(file.path, RULE_DEFAULTS_IMPORTS_COLORS, "直接 import LocalAppColors"))
            }
        }
        val themeRelPath = themeRelDir.replace('.', '/')
        val scanned = kotlinRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".kt") }
            .filterNot { it.path.contains(themeRelPath) }
            .toList()
        for (file in scanned) {
            val text = file.readText()
            val hitM3Import = m3TokenImports.any { text.contains(it) }
            val hitMaterialTheme = text.contains("MaterialTheme.typography") ||
                text.contains("MaterialTheme.colorScheme") || text.contains("MaterialTheme.shapes")
            if (hitM3Import || hitMaterialTheme) {
                violations.add(AuditViolation(file.path, RULE_COMPONENT_M3_TOKEN, "组件层暴露 M3 令牌/主题类型"))
            }
        }
        val required = listOf("divider", "surface", "snackbarHost", "emptyState")
        if (componentTokensFile.exists()) {
            val text = componentTokensFile.readText()
            for (field in required) {
                if (!text.contains("val $field:")) {
                    violations.add(AuditViolation(componentTokensFile.path, RULE_TOKENS_MISSING_REGISTRATION, "缺少令牌字段 $field"))
                }
            }
        }
        return violations
    }
}
```

- [ ] **Step 4: 运行测试**

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.core.util.TokenAuditCheckerTest"`
Expected: 全绿（违规检出 + 合规零违规）

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/babytracker/core/util/TokenAuditChecker.kt app/src/test/java/com/babytracker/core/util/TokenAuditCheckerTest.kt
git commit -m "提取共享令牌审计检查器 TokenAuditChecker（纯 Kotlin，JVM 可单测）"
```

---

### Task 2: themeTokenAudit Gradle 任务 + 审计测试迁移到检查器

**Files:**
- Modify: `app/build.gradle.kts`（注册 JavaExec 任务）
- Modify: `app/src/test/java/com/babytracker/designsystem/theme/ThemeTokenizationStaticAuditTest.kt`（改用检查器）
- Test: 复用 Task 1 的 `TokenAuditCheckerTest` + 迁移后的静态审计测试

**Interfaces:**
- Consumes: `TokenAuditChecker.audit(...)`（Task 1）
- Produces: `./gradlew themeTokenAudit` 任务（违规时构建失败 + 打印违规清单）；静态审计测试单测继续全绿

- [ ] **Step 1: 注册 Gradle 任务**

`app/build.gradle.kts` 末尾（android 块外）追加：

```kotlin
// —— 令牌审计门禁：themeTokenAudit（共享 TokenAuditChecker，双路复用）——
val auditClasspath = configurations.create("auditClasspath") {
    extendsFrom(configurations.getByName("debugRuntimeClasspath"))
}

tasks.register<JavaExec>("themeTokenAudit") {
    group = "verification"
    description = "令牌化静态审计：扫描全部 Kotlin 源码，拦截 M3 令牌直用/硬编码颜色/令牌绕过"
    dependsOn("compileDebugKotlin")
    classpath = auditClasspath + sourceSets.getByName("main").output
    mainClass = "com.babytracker.core.util.TokenAuditCheckerKt"
    args(
        project.file("src/main/java").absolutePath,
        "com/babytracker/designsystem/theme",
        "com/babytracker/designsystem/components",
        project.file("src/main/java/com/babytracker/designsystem/theme/AppComponentTokens.kt").absolutePath,
    )
}
```

⚠️ 检查器需要 `main(args)` 入口函数（`fun main(args: Array<String>)`，从 args 解析 4 个参数并打印违规、非零退出）——**Task 1 的检查器文件加 main 入口**（`TokenAuditChecker.kt` 文件顶层 `fun main(args)`，或检查器内 `@JvmStatic main`；JavaExec mainClass 对应 `TokenAuditCheckerKt`）。main 逻辑：解析 args → 调用 audit → 违规非空打印并 `exitProcess(1)`。

（若 `sourceSets.getByName("main").output` 引用在 Kotlin DSL 有冲突，用 `sourceSets.main.get().output`——以编译为准。）

- [ ] **Step 2: 迁移静态审计测试**

`ThemeTokenizationStaticAuditTest.kt` 全部 5 个测试改为调用 `TokenAuditChecker.audit(...)` 并断言 `violations.isEmpty()`（保留 assertScanNonEmpty 防呆逻辑于测试，检查器已内置 ScanEmpty 违规——测试断言不变形）。删除测试内重复的扫描代码。测试仍需要本地路径（`src/main/java`）。

- [ ] **Step 3: 验证双路**

Run: `./gradlew themeTokenAudit`
Expected: 构建成功（当前代码无违规；若有存量违规需先修——P1/P2 已清，应无）

Run: `./gradlew testDebugUnitTest --tests "com.babytracker.designsystem.theme.ThemeTokenizationStaticAuditTest" --tests "com.babytracker.core.util.TokenAuditCheckerTest"`
Expected: 全绿

⚠️ 若 themeTokenAudit 检出存量违规（P1/P2 未清的），不要静默修改——先 `git stash` 无关改动，跑任务看违规列表，逐条确认是否真实违规后修复或向 controller 报告。

- [ ] **Step 4: 负向验证（门禁有效）**

临时在组件文件加一行违规（如 feature 某文件加 `import androidx.compose.material3.Typography`），跑 `./gradlew themeTokenAudit` 预期失败（BUILD FAILED 且打印违规路径），然后移除。**验证后必须还原**（`git checkout` 该文件）。

- [ ] **Step 5: 全量 + 提交**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿
```bash
git add app/build.gradle.kts app/src/test/java/com/babytracker/designsystem/theme/ThemeTokenizationStaticAuditTest.kt
git commit -m "注册 themeTokenAudit Gradle 门禁任务，静态审计测试迁移到共享检查器"
```

---

### Task 3: Detekt Termux POC（前置验证）

**Files:**
- Modify: `gradle/libs.versions.toml`（detekt 版本——POC 验证用）
- Modify: `app/build.gradle.kts`（临时加 detekt 插件）
- Create: `config/detekt/detekt.yml`（临时最小配置）
- 失败时还原以上全部

**Interfaces:**
- Produces: POC 结论（可行性 + 版本 + 自定义规则接入方式），记录到 POC 报告，供 Task 4/5 决策

- [ ] **Step 1: 查版本**

Run: `curl -s "https://plugins.gradle.org/m2/io/gitlab/arturbosch/detekt/detekt-gradle-plugin/maven-metadata.xml" | tail -5`（或网页查询），确认最新稳定版。兼容目标：Kotlin 2.3.21（detekt 官方标注兼容 Kotlin 版本）+ AGP 9.2.1 + Gradle 9.5.1。候选：1.24.x（已知 Kotlin 2.1）/ 2.x（若存在且标注 Kotlin 2.3）。**选择依据：官方兼容矩阵 + Termux 实跑结果。**

- [ ] **Step 2: 接入插件并跑通**

`libs.versions.toml` 加 detekt 版本 + `app/build.gradle.kts`（或根）加 `alias(libs.plugins.detekt) apply false` + `id("io.gitlab.arturbosch.detekt")`。`config/detekt/detekt.yml` 最小配置（只开基础规则）。Run: `./gradlew detekt`（Termux 下载插件 + 分析，给足超时）。

**通过标准**：任务能完整跑完（可大量存量 warning，但任务不 FAIL 崩溃）；报告实际行为。

- [ ] **Step 3: 自定义规则 POC（若 Step 2 通过）**

新建 `detekt-rules/` 独立模块（`settings.gradle.kts` include + 模块内 build.gradle.kts，依赖 `io.gitlab.arturbosch.detekt:detekt-api`），写一个**最小规则**（如 `NameStartsWithApp`：拦截类名不以 App 开头的组件，纯验证 API 链路），`app/build.gradle.kts` 加 `detektPlugins(project(":detekt-rules"))` + detekt 配置引用。Run: `./gradlew detekt` 确认自定义规则被加载执行（造一个违规样本触发）。

⚠️ **POC 失败判定**：插件无法解析/加载（与 Kotlin/AGP/Gradle 版本冲突且 2 个候选版本都失败）、规则 jar 无法构建、detektPlugins 接入失败。任一失败 → POC 失败。

- [ ] **Step 4: POC 结论记录**

写 POC 报告到 `/data/data/com.termux/files/home/vide/MyApp/.superpowers/sdd/2026-08-08-ds-upgrade-p3/task-3-poc-report.md`：版本选择、实跑输出摘要、自定义规则接入方式、**结论：可行 / 不可行 + 原因**。POC 通过则保留接入代码（作为 Task 4 基础），失败则**还原所有临时改动**（git checkout 涉及文件 + 删除临时模块）。

- [ ] **Step 5: 提交（通过时）或还原（失败时）**

通过：
```bash
git add settings.gradle.kts gradle/libs.versions.toml app/build.gradle.kts config/detekt detekt-rules
git commit -m "detekt Termux POC 通过：插件接入与自定义规则链路验证"
```
失败：
```bash
git checkout -- settings.gradle.kts gradle/libs.versions.toml app/build.gradle.kts
rm -rf config/detekt detekt-rules
git commit -m "detekt Termux POC 失败：记录原因并回滚（保留 themeTokenAudit 单任务方案）"
```
（回滚 commit 由 controller 指示执行与否——POC 失败时 controller 决策降级路线后再提交。）

- [ ] **Step 6: 回报**

回报状态 + POC 结论（可行/不可行 + 版本 + 原因）。controller 据此决策 Task 4/5 是否执行。

---

### Task 4: :detekt-rules 模块 + HardcodedColor 规则（POC 通过才执行）

**Files:**
- Modify: `settings.gradle.kts`（include ":detekt-rules"——若 POC 已加则确认）
- Modify: `detekt-rules/build.gradle.kts`、`detekt-rules/src/main/kotlin/...`
- Modify: `app/build.gradle.kts`（detekt 配置 + detektPlugins）
- Modify: `config/detekt/detekt.yml`
- Create: `detekt-rules/src/test/kotlin/...`（规则单测）
- 删除 POC 的最小规则，替换为正式规则

**Interfaces:**
- Consumes: detekt-api（POC 验证的版本）
- Produces: `HardcodedColorRule`（拦截 `Color(0xFF`、`Color.Black`、`Color.White` 在组件层/feature 的硬编码）——**排除范围**：designsystem/theme 的令牌定义文件（AppColors/derive 里有合法的 Color(0xFF 默认值）与 core 非 UI 层。规则判定基于 import + 调用：检测 `Color(0xFF`/`Color.Black`/`Color.White` 字面量出现于 `Color(` 调用参数（KtCallExpression 参数含 0xFF 字面量）或 `Color.Black/White` 属性引用（KtDotQualifiedExpression）。以「绑定 import」判断层：文件 import `androidx.compose.ui.graphics.Color` 且不在白名单路径（designsystem/theme 包）→ 命中。

- [ ] **Step 1: 写失败测试（规则单测）**

detekt-rules 模块测试（用 detekt 的 `RuleTestCase`）：
```kotlin
// HardcodedColorRuleTest.kt
class HardcodedColorRuleTest : RuleTest {
    override val rule = HardcodedColorRule()

    @Test
    fun `组件层 Color 字面量应命中`() {
        assertThat(
            """
            import androidx.compose.ui.graphics.Color
            val c = Color(0xFF000000)
            """.trimIndent(),
        ).hasLintViolation("Color(0xFF... 硬编码")
    }
    // 白名单：theme 包内令牌定义合法；Color.Black 命中；无 import Color 不命中
}
```
（detekt 测试 API 以所选版本为准——1.24 用 `RuleTest`/`assertThat(compileContent).hasLintViolation(...)`；2.x 可能不同。以 POC 实测 API 为准微调。）

- [ ] **Step 2: 实现规则**

`HardcodedColorRule` 继承 detekt `Rule`，visit KtCallExpression（`Color(0xFF`）+ KtDotQualifiedExpression（`Color.Black/White`），过滤：文件不含 `import androidx.compose.ui.graphics.Color` 则跳过；文件路径含 `designsystem/theme/` 则跳过（白名单）。`report(offset, "...")` 中文规则名与说明。

- [ ] **Step 3: detekt 配置**

`config/detekt/detekt.yml` 开 HardcodedColor 规则（active: true，severity）。`app/build.gradle.kts` 的 detekt 块确认 `detektPlugins(project(":detekt-rules"))`。

- [ ] **Step 4: 全量验证**

Run: `./gradlew detekt`（当前代码应无 HardcodedColor 违规——若有真实存量违规，确认后修或报告）+ `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿

- [ ] **Step 5: 提交**

```bash
git add detekt-rules config/detekt app/build.gradle.kts settings.gradle.kts
git commit -m "detekt 自定义规则 HardcodedColor：拦截组件层硬编码颜色，白名单 theme 令牌层"
```

---

### Task 5: TokenBypass 规则 + 单测（POC 通过才执行）

**Files:**
- Create: `detekt-rules/src/main/kotlin/.../TokenBypassRule.kt` + 测试

**Interfaces:**
- Consumes: detekt-api、TokenAuditChecker 规则语义（规则 2/3 的 AST 版）
- Produces: `TokenBypassRule`

- [ ] **Step 1: 写失败测试**

拦截样本：
1. Defaults 文件 import `LocalAppColors`（token 绕过：Defaults 直读核心语义令牌）——KtImportDirective 匹配
2. 组件代码直接 `MaterialTheme.colorScheme.X` / `MaterialTheme.typography.X`（组件层绕过 token 直用 M3 主题）——KtDotQualifiedExpression 匹配 `MaterialTheme.colorScheme/typography/shapes`
豁免：designsystem/theme 包路径

- [ ] **Step 2: 实现规则**

`TokenBypassRule`：规则 1 检查文件包路径含 `components` + 文件含 import LocalAppColors（Defaults 场景）；规则 2 检查 `MaterialTheme.colorScheme|typography|shapes` 属性访问（theme 包豁免）。`report(offset, "...")`。

- [ ] **Step 3: detekt.yml 配置 + 全量验证**

Run: `./gradlew detekt`（现有代码应无违规）+ `./gradlew assembleDebug && ./gradlew testDebugUnitTest`
Expected: 全绿

- [ ] **Step 4: 提交**

```bash
git add detekt-rules config/detekt
git commit -m "detekt 自定义规则 TokenBypass：拦截 Defaults 直读核心令牌与 M3 主题直用"
```

---

### Task 6: 文档同步 + CHANGELOG（含 POC 失败时的降级记录）

**Files:**
- Modify: `docs/design-system.md`（门禁章节：themeTokenAudit 任务 + detekt 规则说明）
- Modify: `docs/project-structure.md`（TokenAuditChecker、detekt-rules 模块、config/detekt）
- Modify: `CHANGELOG.md`（并入 `### [1.8.0]` 小节——无 [Unreleased] 惯例）
- Modify: `docs/superpowers/specs/2026-08-08-design-system-upgrade-design.md`（6.1/6.2 偏差标注）
- Modify: `docs/lessons.md`（新坑，如 detekt 版本兼容）
- Modify: `AGENTS.md`（开发命令表加 `./gradlew themeTokenAudit` 与 `./gradlew detekt`——**若用户确认门禁进入日常命令**；P3 完成后向用户提出，默认加入）

**Interfaces:**
- Consumes: Task 1-5 产物（或 POC 失败降级状态）

- [ ] **Step 1: 文档更新**

按「以代码实际为准」更新上述文档；CHANGELOG 1.8.0 追加「设计系统升级 P3」条目组：
- 共享令牌审计检查器 TokenAuditChecker + themeTokenAudit Gradle 门禁任务（扫描全部源码：M3 令牌直用/硬编码颜色/令牌绕过）
- 静态审计测试迁移到共享检查器（双路复用）
- detekt 集成与自定义规则（若 POC 通过）：HardcodedColor / TokenBypass；若失败记录降级决策

- [ ] **Step 2: spec 6.1/6.2 偏差标注**

对照实现：检查器签名、规则数、detekt 版本选择、POC 结果。有出入加 blockquote 标注（参考 P2 Task 8 模式）。

- [ ] **Step 3: 全量验证**

Run: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew themeTokenAudit && ./gradlew detekt`
Expected: 全绿（detekt 若 POC 失败则跳过）

- [ ] **Step 4: 提交**

```bash
git add docs CHANGELOG.md
git commit -m "P3 文档同步：令牌审计门禁与 detekt 规则入设计文档与 CHANGELOG（1.8.0）"
```

---

## P3 验收清单（全部任务完成后）

- [ ] `./gradlew assembleDebug` 通过
- [ ] `./gradlew testDebugUnitTest` 全绿（TokenAuditCheckerTest + 迁移后静态审计 + 既有 116 项）
- [ ] `./gradlew themeTokenAudit` 通过（门禁任务可用）
- [ ] 负向验证已做（临时违规 → 任务 FAIL → 还原）
- [ ] （POC 通过时）`./gradlew detekt` 通过且自定义规则单测全绿
- [ ] 检查器单份代码双路复用（Gradle 任务 + JUnit 测试不重复实现）
- [ ] 文档同步：design-system.md / project-structure.md / CHANGELOG（1.8.0）/ spec 偏差标注 / lessons.md
- [ ] AGENTS.md 开发命令表是否加入门禁命令（提交后向用户确认）
