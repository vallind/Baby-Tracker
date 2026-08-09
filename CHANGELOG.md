## 更新日志
遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 改版规范，无 Unreleased 部分。
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

### [1.8.0] — 2026-08-08

**设计系统升级 P1（Typography 单体系 + 组件收敛 + 令牌化收尾）：**
- Typography 双体系统一：LocalAppTypography 改供自建 AppTypography，新增 8 级补齐至自建 12 级单体系（displayLarge/headlineLarge/headlineMedium/headlineSmall/titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/labelMedium/labelSmall），删除裸字段与 LocalAppTypographyStyle，禁止组件层暴露 M3 令牌类型（仅 theme 层桥接），迁移 31 文件与 13 处 M3 直用
- Button 家族收敛为 AppButton + ButtonVariant 枚举（Primary/Secondary/Text），迁移 58 处调用方，颜色改走 ButtonTokens
- 新增 AppDivider 组件（DividerTokens），迁移 6 处 HorizontalDivider
- 新增 AppSurface 组件（SurfaceTokens），迁移 3 处 M3 Surface
- 新增 AppSnackbarHost 组件（SnackbarHostTokens），迁移 9 处 M3 SnackbarHost
- EmptyState 令牌化：新增 EmptyStateTokens，M3 Button/colorScheme 改走令牌体系
- AlertDialog 迁移：编辑昵称对话框迁移 AppDialog（移除清除快捷按钮），清理两处死 import
- 遗留清理：TimePicker/DateTimeCascade 两处 MaterialTheme.colorScheme 改走 LocalAppColors
- 审计测试扩展：静态审计新增「新组件 Defaults 应被 AppComponentTokens 覆盖」与「组件层不应导入 M3 令牌与主题类型」两条规则，修复头部注释漂移
- 文档同步：design-system.md 令牌清单/组件速查/Typography 12 级更新，AGENTS.md 组件映射行更新

**设计系统升级 P2（密度变体 + 无障碍语义基线 + 令牌测试）：**
- 密度变体：新增 AppDensity 三档（紧凑 0.85x / 舒适 1.0x / 宽松 1.15x，控件 medium 档 48dp 基准 ±8dp），LocalAppDensity 默认舒适档，设置页「界面密度」可切换（DensityPickerSheet），AppSpacing.scaled 缩放注入 + AppControlTokens.densityAdjusted 控件高度调整，全组件零迁移生效；AppSettings.appearance.density 持久化 + DensityController（仿 ThemeController）注册
- 无障碍语义基线：自定义可交互组件 semantics 补齐（SegmentedControl Role.Tab/selected、RecordCard customActions、AppRate/AppLabeledSlider contentDescription、TimePickerLogic 滚轮 selected 合并、DateTimeCascade 日历日期格 Role.Button+selected、FAB/BottomNav 图标去重不重复朗读），装饰组件读屏隔离（BabyIllustration/BadgeIcon/EmptyState emoji 等 clearAndSetSemantics），新增 docs/a11y-baseline.md 与 A11ySemanticsAuditTest
- 令牌测试：新增 ComponentTokensStateAuditTest（暗色差异 + 状态色完整性）
- 文案迁移：返回/密码提示走 AppStrings（AppTopBar 返回、AppInput 密码可见性）

**设计系统升级 P3（令牌审计门禁 + detekt 集成）：**
- 提取共享令牌审计检查器 `TokenAuditChecker`（纯 Kotlin 文本扫描，`core/util/TokenAuditChecker.kt`）：5 条规则（Defaults 走组件令牌/Defaults 硬编码颜色/Defaults 引 LocalAppColors/组件层 M3 令牌/新组件令牌注册）+ `ScanEmpty` 防呆（扫描为空或 AppComponentTokens.kt 缺失必报违规，禁止路径漂移静默假绿，lessons #14/#17）
- 注册 `themeTokenAudit` Gradle 门禁任务（JavaExec，group verification，入口 TokenAuditCheckerKt）：扫描全部源码拦截 M3 令牌直用/硬编码颜色/令牌绕过，违规即 FAIL；classpath 直接引用 `debugCompileClasspath`（AGP 9 无 sourceSets 容器，lessons #17）
- 静态审计测试迁移到共享检查器（双路复用）：`ThemeTokenizationStaticAuditTest` 改为调用 `TokenAuditChecker.audit()`，新增 `TokenAuditCheckerTest`（拦截/白名单/防呆样本），Gradle 任务与 JUnit 不再各自实现
- detekt 1.23.8 集成（Termux POC 通过）：新增 `:detekt-rules` 模块产规则 jar（ServiceLoader 注册，detektPlugins 接入），自定义规则 `HardcodedColor`（拦截 `Color(0xFF...)`/`Color.Black`/`Color.White`，白名单 theme 令牌层与未 import compose Color 的文件）与 `TokenBypass`（拦截 Defaults 直读 LocalAppColors 与组件层 M3 主题直用，豁免 theme 桥接层），各带规则单测；`./gradlew detekt` 为 report-only（ignoreFailures=true），`config/detekt/detekt.yml` 采用显式枚举方案（detekt 1.23 移除 @ActiveByDefault；buildUponDefaultConfig 在 Termux 上全量规则超 20 分钟，枚举实测 ~9 秒）
- 存量债务登记：24 处 `HardcodedColor` 存量违规（components 7 + feature 17，如 `Color.White.copy(alpha=...)` 等无令牌等价物的写法）与 9 处 `ImplicitDefaultLocale`（内置规则告警，如未显式传 Locale 的 toLowerCase/toUpperCase 等，真实存量债）列为已知债务，报告不阻断，待后续批次清理

**测试盲区补齐（按 AGENTS.md 测试纪律）：**
- 提取 StatsViewModel 聚合逻辑为 internal 顶层纯函数（aggregateStats 及 8 个辅助函数），新增 `StatsLogicTest` 14 项：周期边界（周日起始/月初/年初/翻页）、跨午夜睡眠时长、反向睡眠钳制、解析失败睡眠、喂养计数与图表桶一致性回归（1.7.10）、周/日分桶落点、对比文案、最新生长值、空数据
- 提取 TimelineViewModel 记录映射为 internal 顶层函数 `toTimelineItems`，重写 `TimelineViewModelTest` 调用生产代码（消除镜像测试），并新增短时间戳 substring 越界回归测试（1.7.9）
- `FamilyIsolationTest` 版本断言按新版本规范更新：app 版本必须存在于 CHANGELOG（不再要求等于最新条目，允许未发布的批次条目）

**文档体系修订（全部文档增加版本日期头，对齐代码实际状态）：**
- 重写 `docs/sync-architecture.md`：游标分页机制（sync_version 替代 lastSyncAt）、9 张同步表（messages 摘除）、sync_metadata 10 字段 + 唯一索引、markRetry 指数退避、push 远端 LWW 预检、家庭驱动链（替代 ensureFamily）、1.7.7~1.7.11 修复项
- 重写 `docs/room-supabase-architecture.md`：v8/15 实体/14 DAO/7 迁移/exportSchema、INSERT IGNORE、频道名 db-changes-$familyId、loadMyFamilies 已删、messages 不参与同步
- 重写 `docs/data-architecture.md`：v8/15 表清单（补 sync_cursors + AI 两表）、"待实施多家庭改造"整节标注已落地
- 重写 `docs/architecture.md`：25 条路由、12 ViewModel、游标机制、RLS 表清单
- 修订 `docs/project-structure.md`：16 模块、25+ 路由、15 表、Java 17、derive TODO 标注
- 修订 `docs/design-system.md`：derive{} 标注未实现（TT-032）、组件速查补全、showUndo 语义修正
- 修订 `README.md`：功能表补 AI/家庭/同步、目录结构、数据库、测试命令
- 所有文档统一加 `> 最后更新：2026-08-08 · 对应版本：1.7.11` 头，防止再次漂移

**AGENTS.md 修订（依据 agents.md 开放规范与 Claude Code 写作建议）：**
- 精简目录结构章节，改为指向 docs/project-structure.md，消除文档漂移
- 开发命令补充 `testDebugUnitTest`，新增测试纪律章节（何时必写测试、禁止镜像测试、已知盲区清单）
- 工作流准则明确"不清楚"判定边界（数据模型/同步/跨模块 API 必须请示，纯 UI 直接干）
- 红线修订：百分比夹紧补充 coerceAtLeast 单用；今日日期过滤升级为 take(10) 精确比较；AlertDialog 平级增加 DS 迁移完成后的自动失效条款
- 变更分级：共享 API 方法签名变更（哪怕只加参数）升为 🔴 必须确认
- 版本规范修订：CHANGELOG 按批次累积，版本号仅在发布时提升，消除逐提交升版本的通胀
- 新增 DS 组件缺失决策路径、i18n 硬编码禁令、lessons.md 闭环义务、文档同步义务表
- 参考文档补全 sync/data/architecture 等 4 份文档

### [1.7.11] — 2026-08-08

**低风险修复与死代码清理：**
- 修复日志查看器 LazyColumn key 碰撞（同一毫秒完全相同的日志会触发 IllegalArgumentException 崩溃）：LogEntry 新增递增序号 seq，列表 key 改为按序号
- 修复 push 的 LWW 比较与 pull 游标推进对 updatedAt/sync_version 的 JSON 解析脆弱性，统一 JsonPrimitive 安全读取，避免远端返回字符串时 LWW 退化为本地无条件覆盖
- 删除 SyncEngine EntityDao 从未被调用的 updateLocal 字段（9 张表共 9 处 lambda）
- 删除已迁移到 sync_cursors 表的旧游标 DAO 方法（watchPendingCount/watchPendingCountByFamily/getLastSyncAt/updateLastSyncAt/clearLastSyncAt）
- 删除无调用方的 MessageRepository.watchByType/MessageDao.watchByType、FamilyService.loadMyFamilies

**验证：** `assembleDebug` 与 `testDebugUnitTest` 全部通过；Android 应用版本更新为 1.7.11

### [1.7.10] — 2026-08-08

**同步稳定性修复：**
- 修复"立即同步"按钮在连续两次同步结果相同时卡死在"同步中..."的问题（StateFlow 去重不重发），改为自增 runId 复位
- 修复 markExistingPending 部分表标记失败后仍写入一次性标记、存量数据永久不进入同步队列的问题，失败时保留重试机会
- 修复防抖/推送期间新增变更事件缓冲溢出被丢弃后不补推的问题，推送后复查 pending 余量自动补推
- 修复 push/pull 的 check-then-act 并发守卫竞态，新增 pushPullMutex 统一互斥（fullSync 持锁顺序固定，无死锁）
- 修复备份还原清空 sync_metadata 后一次性标记已置位、还原数据永不自动上行的问题，还原成功后清除标记
- 修复 AuthService 登录态监听协程无 SupervisorJob/异常兜底、流异常后监控永久失效的问题
- 修复 joinFamily 邀请码与云端存储大小写不一致时误报"加入家庭失败"的问题

**备份解析与统计一致性：**
- BackupManager 新增 optStr 安全读取（org.json 对 JSONObject.NULL 返回字符串 "null"），替换全部 33 处 optString 调用
- 统计页喂养次数改为以图表可落入的桶求和为准，消除解析失败记录"数字有、柱子无"的矛盾

**验证：** `assembleDebug` 与 `testDebugUnitTest` 全部通过；Android 应用版本更新为 1.7.10

### [1.7.9] — 2026-08-08

**崩溃与稳定性修复：**
- 修复删除记录后点"撤销"必然崩溃的问题：软删除后撤销用原 id 重新 INSERT 触发主键冲突，8 个页面（喂养/睡眠/尿布/生长/健康/疫苗/提醒/时间线）统一改为清除删除标记后 UPDATE
- 修复时间线/首页从云端同步或备份还原的空时间戳触发 `substring` 下标越界崩溃的问题，统一长度保护
- 修复 NetworkMonitor 离线时误报"在线"导致同步门禁失效的问题，改为无网络即离线（fail-closed）
- 修复 RealtimeManager 切换家庭重订阅时旧频道收集协程永不取消的泄漏，并重抛 CancellationException 保持取消传播
- 修复 Realtime DELETE 事件与 applyRemoteChange 的 JSON 解析遗留 `removeSurrounding` 模式（JsonNull 变字符串 "null"），改用 JsonPrimitive 安全读取

**设计系统合规清理：**
- AppDialog 新增 content 插槽与 confirmEnabled 参数，宝宝表单、创建/加入家庭对话框迁移
- PrimaryButton 新增容器/内容颜色覆盖参数，备份恢复红色按钮迁移
- AppTextButton 新增 icon 参数，邀请码复制按钮迁移
- 迁移 LogViewerScreen 的 7 处 MaterialTheme.typography、SyncSettingsScreen 的 2 处 M3 RadioButton、AiChatScreen 的 3 处 M3 IconButton
- 清理 SettingsScreen 等文件 8 个未使用 M3 import

**验证：** `assembleDebug` 与 `testDebugUnitTest` 全部通过；Android 应用版本更新为 1.7.9

### [1.7.8] — 2026-07-24

**同步修复：**
- 修复 `pendingChange` 用 `REPLACE` 插入 sync_metadata 时静默变更行 id，导致 push 中 `markSynced` 空匹配、pending 状态丢失的问题。改为先 `updatePending` 更新已有行状态为 pending，无匹配再 INSERT
- `applyRemoteChange` 落地远程变更时先查 `sync_metadata` 是否存在同 `(tableName, localId)` 行，有则 `updateByTableAndId` 保留原行 id，避免与 push 的 `markSynced` 冲突
- `sync_metadata.insert` 改为 `IGNORE`，不再 `REPLACE`，防止行 id 被静默变更
- 新增 `SyncTriggerTest` 14 项静态分析测试，验证所有 Repository 的增/改/删操作均调用 `pendingChange` 且 `insert` 使用 `IGNORE`

### [1.7.7] — 2026-07-24

**同步引擎稳定性修复：**
- 修复 `applyRemoteChange` 用 `OnConflictStrategy.REPLACE` 插入 sync_metadata 时静默删除已有行，导致正在进行的 push 持有旧 id 调用 markSynced 空匹配、pending 状态永久丢失的问题。改为先查存在性，有则 UPDATE 保留原行 id
- 修复 `RealtimeManager` 超时后 `channel` 引用未保留导致失去 SDK 自动重连能力的问题

**功能 Bug 修复：**
- 修复首页睡眠统计漏掉午睡（SleepType.NAP）和跨午夜睡眠的问题
- 修复 `TableLogic.sortedData` 反射排序数字列被字符串化（"10" < "9"）的问题，改为数值优先比较
- 修复 `FormLogic.errors` 以错误消息字符串自身作为 Map 键导致字段级错误查询无效的问题

**构建与测试基础设施：**
- 启用 Room `exportSchema = true` 并配置 `room.schemaLocation`，为后续迁移自动测试奠定基础；生成并提交 version 8 schema JSON

**代码质量清理：**
- 移除 `LoginScreen` 死代码（未使用的 `passwordVisible`/`focusManager` 及 4 个未使用 import）
- 移除 `BabyTrackerTheme` 未使用的 `dynamicColor` 参数
- 标记 `AppComponentTokens` 中待实现的 `derive {}` 注释为 TODO
- 移除 `ButtonLogic` 未使用的 `isPressed` 公开暴露
- 修复 `AppInput` 密码切换 TODO 壳，新增 `passwordVisible`/`onPasswordToggle` 参数
- 修复 `AppFAB` icon-only 模式缺 `contentDescription` 的问题，新增可选参数
- 删除完全死代码 `AppLayers.kt`（Z 轴层级系统，零调用）
- `LogBuffer` 新增 `entries: SharedFlow`，`LogViewerScreen` 从每秒轮询改为 Flow 驱动
- `StatsViewModel.aggregate` 提取 `inRange` / `sleepDurationMinutes` / `latestGrowth` 辅助函数，消除非局部返回和 7 处重复 `safeParse`，从 127 行减至 70 行

**喂奶/睡眠计时器优化：**
- 喂奶和睡眠表单点击保存时自动结束计时并填入时长/结束时间，无需手动点"结束计时"
### [1.7.6] — 2026-07-24

**统计页面 P0 可用性修复：**
- 修复零值柱状图仍绘制圆角路径导致的底部彩色碎片，并为折线端点预留安全边距
- 统计页新增加载、失败重试和整页空数据状态，单项无记录时展示明确说明
- 周期切换继续保留日期范围和导航状态；Android 应用版本更新为 1.7.6

### [1.7.5] — 2026-07-24

**设置中心统一数据模型：**
- 新增单一 `AppSettings` 聚合主题、同步、AI 与日志偏好，使用 kotlinx.serialization JSON 存入 Jetpack DataStore
- 所有设置通过 DataStore `updateData` 原子变换更新，并从旧 SharedPreferences 一次性无损迁移
- `SettingsViewModel` 精简为 `settings` 与 `updateSettings()`，同步执行和运行状态拆入独立 `SyncViewModel`
- 一级、二级与三级设置菜单统一使用 `AppListItem` 槽位模式并嵌套在 `AppCardGroup` 中
- 增加设置 JSON 往返和未知字段兼容测试；Android 应用版本更新为 1.7.5

### [1.7.4] — 2026-07-24

**“我的”页面信息架构重构：**
- 一级页面聚焦账号概览、宝宝与家庭，并将零散设置归并为三个清晰分类
- 新增“使用偏好”“数据与同步”“帮助与关于”三个二级页面
- AI、同步、备份和运行日志作为三级功能页保留，减少主页面信息拥挤
- 移除尚未实现的“我的收藏”占位入口，日志开关改用设计系统组件

### [1.7.3] — 2026-07-24

**AI 助手 V2 第四阶段：**
- 最后一轮回答支持重新生成，旧回答会先移除并继续经过上下文构建与安全校验
- 支持编辑最后一个问题并回退整轮问答，修改后由用户主动重新发送
- 回答依据改为可展开的记录事实、AI 解读和行动建议三层说明
- 新增本地历史保存中、已保存和保存失败状态，失败不影响当前回答查看
- 编辑、重试、停止和重新生成按原会话覆盖保存，避免重复消息和并行请求
- 补充问答轮次修订测试与第四阶段 PRD；Android 应用版本更新为 1.7.3

### [1.7.2] — 2026-07-24

**AI 助手 V2 第三阶段：**
- 新增本地 AI 会话历史，支持继续对话、搜索、删除和开始新对话
- Room 数据库升级到 8，新增会话与消息表，并保留已有宝宝和业务记录
- 会话按家庭与宝宝双重隔离，只保存在本机，不加入 Supabase 同步或 Realtime
- 保存回答、思考内容、模型信息、引用范围与安全状态，思考内容不进入后续模型上下文
- 补充标题生成和历史搜索测试；Android 应用版本更新为 1.7.2

### [1.7.1] — 2026-07-24

**AI 助手 V2 第二阶段：**
- 快捷分析新增 7、14、30 天周期选择，并展示本期与前一等长周期
- 睡眠、喂养、健康和综合概览按严格时间边界生成两期记录摘要
- 综合概览支持跨类别同步变化解释，并明确禁止把相关变化表述为因果关系
- 切换周期后重新校验当前宝宝数据，发送前再次读取记录，不缓存家庭、宝宝或业务摘要
- 补充周期边界、空本期和健康分类对比测试；Android 应用版本更新为 1.7.1

### [1.7.0] — 2026-07-24

**AI 助手 V2 第一阶段：**
- AI 助手空态集中提供最近睡眠、近期喂养、健康整理和近期概览四类快捷分析
- 快捷卡展示数据可用状态，选择后只填入可编辑的问题草稿，不会自动产生模型调用
- 新增可移除的会话分析范围，后续追问沿用同一上下文，明确来源优先于问题关键词分类
- 无记录或近期数据未开启时阻止空分析请求；发送前重新读取当前宝宝数据
- 切换宝宝、家庭、账号或清除会话时清理分析上下文，不修改业务页面、导航、数据库和后端
- 更新 V2 第一阶段 PRD 与上下文单元测试；Android 应用版本更新为 1.7.0

### [1.6.9] — 2026-07-24

**AI 回答安全校验：**
- 所有完整回答在展示完成态前检查危险用药剂量、擅自停换药、确定性诊断和绝对安全保证
- 健康与高风险问题先缓冲完整回答再展示，避免流式正文绕过本地安全校验
- 紧急或高风险回答缺少就医指引时，由应用补充不可被模型省略的固定安全提示
- 未通过校验的回答替换为本地安全说明并清除思考过程，新增安全规则回归测试

### [1.6.8] — 2026-07-24

**AI 思考过程显示：**
- 解析并保留 OpenAI 兼容协议返回的 `reasoning_content`，支持 DeepSeek 流式与非流式思考内容
- 支持 Responses 协议的推理摘要，并按官方格式请求自动摘要
- 回答生成时实时展示思考过程，完成后可独立展开或收起，不会混入最终回答和后续对话上下文
- 修正 OpenAI 兼容协议的推理强度参数位置，新增两类协议的思考内容解析测试

### [1.6.7] — 2026-07-24

**AI 流式回答显示修复：**
- 流式正文增长时自动滚动到真正的回答末尾，不再停留在长回答开头
- 收到正文后隐藏重复的“AI 正在回答”提示，避免同时出现两种生成状态
- 参考范围、免责声明和复制按钮仅在回答完成或手动停止后显示
- 新增流式消息完成状态回归测试，并通过模拟器长回答实测

### [1.6.6] — 2026-07-23

**AI 模型能力与生成设置：**
- 助手设置新增对话上下文轮数、最大输出 Token 预设与自定义输入
- 新增流式输出、思考模式、推理强度和温度设置，并根据当前模型能力动态开放
- 后端运行配置新增模型能力声明，只提供功能提示和自动建议值，不限制上下文或 Token 数量
- OpenAI 兼容协议和 Responses 协议新增 SSE 流式解析及供应商参数映射
- 模型选项切换时自动过滤不受支持的生成参数，旧版后端配置安全降级为基础问答
- 精简对话体验主页面，不再让 Markdown、自动滚动和复制提示占据核心模型设置位置
- 新增能力配置、无 Token 上限、流式响应解析和生成参数测试

### [1.6.5] — 2026-07-23

**AI Markdown 与助手设置：**
- 设计系统新增安全的 `AppMarkdownText` 通用组件，支持标题、强调、列表、引用、代码和链接
- Markdown 图片只展示替代文字，HTML 按纯文本处理，不执行内容或加载模型提供的远程资源
- 新增独立助手设置页，可从 AI 页面和“我的”进入
- 支持按家庭保存默认模型，并配置回答详细程度、语气和行动清单
- 支持近期记录总开关及喂养、睡眠、尿布、生长、健康五类独立数据开关
- 支持推荐问题、自动滚动、Markdown 渲染和复制反馈等对话偏好
- 强化事实安全 Prompt，禁止编造疫苗、用药和护理记录，禁止主动提供具体药物或消毒剂方案
- 新增 Markdown、安全降级、回答偏好和上下文开关测试；Android 应用版本同步更新为 1.6.5

### [1.6.4] — 2026-07-23

**AI 问答交互与上下文稳定性：**
- AI 回答新增一键复制，并通过设计系统 Snackbar 明确反馈复制结果
- 多轮上下文改为按完整问答轮次和字符预算截断，避免出现孤立回答或请求体无限增长
- 无可用缓存时区分未登录、未选云端家庭、家庭验证中、家庭未验证、未选择宝宝和模型配置不可用
- 保留当前用户与家庭范围内的有效 AI 配置缓存，临时网络波动不阻断离线已缓存配置
- 新增完整轮次、字符预算和孤立回答回归测试；通过 1.3 倍字体与模拟器真实回答验证
- Android 应用版本同步更新为 1.6.4

### [1.6.3] — 2026-07-23

**AI 育儿安全层：**
- 新增紧急、高风险和需要关注三级本地规则，模型返回前立即展示独立风险卡
- 紧急症状固定提示立即拨打 120 或前往急诊；三月龄以下发热、误食药物等优先建议尽快就医
- 识别“没有抽搐”“no seizure”等否定表达，避免只按关键词产生风险误报
- 安全 Prompt 根据本地风险等级约束模型回答，禁止确定性诊断、儿童剂量计算和延误就医的居家建议
- 新增紧急症状、月龄发热、误食、需关注、否定表达和 Prompt 单元测试
- Android 应用版本同步更新为 1.6.3

### [1.6.2] — 2026-07-23

**AI 问题相关的宝宝近期记录：**
- 根据问题本地分类睡眠、喂养、尿布、生长和健康场景，无法分类时只使用宝宝档案
- 睡眠读取最近 7 天并结合最近 3 天喂养，喂养和尿布场景读取最近 3 天摘要
- 生长最多读取最近 90 天 3 条记录，健康最多读取最近 30 天 10 条记录且不发送备注和医生姓名
- AI 回答下方展示本次参考的数据类别与时间范围，无记录和数值 0 保持明确区分
- 新增问题分类、时间范围和记录聚合单元测试；不修改 Room 表结构和同步流程
- Android 应用版本同步更新为 1.6.2

### [1.6.1] — 2026-07-23

**AI 育儿助手基础问答：**
- 首页新增 AI 育儿助手入口和聊天页面，支持均衡/深度模型选择、纯文本多轮问答、停止与失败重试
- 问答固定使用当前宝宝昵称、月龄和性别作为最小上下文，宝宝切换时取消请求并清空当前会话
- 区分认证、余额、请求参数、限流、服务异常和网络错误，安全日志不记录问题、回答或 API Key
- 修复 Android 15 及以上版本 RSA-OAEP 的 MGF1 摘要授权不兼容，并在设备密钥轮换后自动刷新配置
- Android 应用版本同步更新为 1.6.1

### [1.6.0] — 2026-07-22

**AI 多供应商运行基础：**
- 新增应用级 AI 配置协调器，仅在 Supabase 用户与家庭均通过验证后刷新远端配置
- 使用 Android Keystore 生成设备 RSA 密钥，供应商凭据只缓存设备公钥加密后的密文
- 支持 OpenAI Responses 与 OpenAI-compatible Chat 协议，以及超时、429、5xx 的跨供应商降级
- 新增 `ai-bootstrap` Edge Function，校验家庭成员关系并按设备公钥封装多供应商凭据
- 新增配置校验与响应解析单元测试；不修改 Room 表结构，不保存或同步 AI 聊天记录
- Android 应用版本同步更新为 1.6.0

### [1.5.19] — 2026-07-22

**P0 问题修复：**
- 修复健康页已接种数量始终为 0 的问题
- 提醒完成与启用状态变更现在会更新时间戳并加入家庭同步队列
- 站内信新增与删除不再错误写入家庭同步元数据

**同步延迟与退后台分离：**
- 切后台同步从同步延迟选项中独立为单独的开关，与新增记录防抖推送互不干涉
- 删除 SyncDelay.ON_EXIT 枚举值，同步延迟选项只控制新增记录后的防抖延迟
- 修复开启退出时同步后新增记录自动推送被错误关闭的问题
- Android 应用版本同步更新为 1.5.19

### [1.5.18] — 2026-07-22

**多账号与多家庭数据隔离：**
- 新增账号专属家庭会话状态，区分离线缓存与 Supabase 已验证会话，过期请求不能覆盖新账号
- 云同步、后台 Worker、Realtime 和手动同步统一要求已验证用户与家庭成员关系
- 家庭选择改为按用户缓存，旧全局家庭 ID 仅在服务端成员关系确认后迁移
- 宝宝列表严格按当前家庭查询，不再把无归属宝宝混入任意家庭；修改与删除保留原家庭归属
- 无归属宝宝保留在“本机数据”模式，只有用户选择目标家庭并确认后才迁移宝宝及全部记录
- 首次同步扫描不再自动认领本机数据，pending 初始化标记改为按家庭保存
- 新增家庭状态、同步门禁、查询隔离和显式迁移静态测试

### [1.5.17] — 2026-07-22

**同步系统稳定性与启动速度优化：**
- 持久化上次家庭/用户/标记状态到 SharedPreferences，重启不重复跑 markExistingPending
- 自动触发按 syncDelay 设置防抖（2s/5s/10s/30s），ON_EXIT 模式退后台才触发
- SyncWorker 改用 KoinComponent 注入，删除 koin-androidx-workmanager 依赖
- WorkManager 恢复自动初始化（ContentProvider），不再手动 initialize
- NetworkMonitor 改用 registerDefaultNetworkCallback，修复启动时无法获取当前网络状态
- WorkManager/SyncTrigger/ProcessLifecycleOwner 延后到首帧之后启动，不阻塞 onCreate
- 自动触发/退后台/网络恢复改为 fullSync（推+拉），新增记录保持 push-only
- 启动时 auth 首次发射跳过触发，避免与 family 观察器重合
- 删除冗余 observeAuth 登录触发（有家庭必然触发 observeFamily）

### [1.5.16] — 2026-07-21

**应用级自动同步体系：**
- 新增 SyncConfig / SyncSettings 支持四种同步配置，SharedPreferences 持久化 + StateFlow 响应式
- 新增 SyncTrigger 实时同步触发器：监听待同步数量、配置、登录、网络，按延迟策略防抖触发 push
- 新增 SyncWorker 后台周期同步，WorkManager 管理，设备重启后持续有效
- 新增 NetworkMonitor 共享网络状态服务，支持在线/非计费网络双检测
- wifiOnly 统一约束实时同步、后台同步、退出同步所有入口
- 自动同步从关闭切到开启时立即触发一次推送
- 新增 SyncSettingsScreen 同步设置子页面，自动同步/同步延迟/后台同步/仅 Wi‑Fi 四项配置
- 主设置页增加同步设置入口并动态显示状态摘要
- SyncMetadataDao 新增 watchPendingCountByFamily 按家庭过滤待同步数
- SettingsViewModel 重构：注入 SyncSettings/NetworkMonitor，移除旧 tryAutoSync 逻辑
- manualSync 删除 markExistingPending 调用，避免每次手动同步重复标记存量
- 新增 lifecycle-process 依赖，应用进入后台触发 ON_EXIT 模式同步
- 禁用 WorkManager 自动初始化，改为 Koin 就绪后手动初始化

### [1.5.15] — 2026-07-21

**宝宝查询严格匹配家庭，同步元数据写入固化家庭归属：**
- BabyRepository 查询改为按当前家庭过滤，兼容无归属存量宝宝
- 新建/更新宝宝时注入当前家庭 ID，记录类通过 BabyDao 从宝宝推导家庭
- sync_metadata 写入时立即固化 familyId，避免 pending 变更无法被 push 拾取
- 修复 SettingsViewModel 两次 tryAutoSync 重复触发问题，合并 auth + network 为单 collector

### [1.5.14] — 2026-07-21

**回滚自动同步策略，保留后端 schema 兼容：**
- 删除 SyncCoordinator / SyncSettings / SyncWorker / SyncChangeTracker 等应用级自动同步组件
- 同步回归手动触发：SettingsViewModel.manualSync() 直接调用 SyncEngine.fullSync()
- 保留后端兼容：SyncCursorEntity 表、SyncMetadataEntity 新字段、MIGRATION_6_7、SyncCursorDao
- 保留数据库 schema：BabyEntity.familyId、BackupManager 导入导出新字段
- 修复 autoSyncMutex 声明在 init 块之后导致的 NPE 闪退

### [1.5.13] — 2026-07-21

**新增可配置同步策略并补齐退后台触发：**
- 设置页新增自动同步、写入延迟、后台周期和仅非计费网络选项，并保留不受自动策略限制的手动同步入口
- 写入同步支持立即、2/5/10/30 秒防抖或退到后台时推送，后台周期支持关闭、30 分钟、1 小时和 2 小时
- WorkManager 根据设置动态更新联网约束和周期任务，失败最多重试 5 次；应用退到后台可安排一次仅推送任务
- 自动同步关闭或当前网络不符合限制时取消 Realtime 订阅，重新开启或网络恢复后自动续接
- 存量数据对账改为按家庭仅执行一次，失败时保留重试机会，避免每次同步重复扫描
- WorkManager 升级至 2.11.2，新增进程生命周期接入及同步配置回归测试，27 项单元测试和 Debug 构建验证通过

### [1.5.12] — 2026-07-21

**补齐应用级自动同步、家庭隔离和失败可见性：**
- 新增应用级 `SyncCoordinator`，登录、家庭切换、网络恢复和本地 pending 变更均可触发同步，不再依赖进入设置页
- 接入 WorkManager，每 15 分钟在联网条件下执行后台同步，作为进程退出后的兜底
- 取消未归属数据自动绑定当前家庭；宝宝查询改为严格匹配家庭，新同步元数据在写入时固化家庭归属
- 存量同步扫描按宝宝所属家庭筛选，已有同步元数据根据宝宝关系修正归属，避免切换家庭串数据
- 推送和拉取返回逐表/逐记录失败信息，设置页区分成功、部分失败和完全失败
- 整页远程数据全部落库后才推进游标，失败记录使用指数退避并由后台任务继续重试
- 新增同步策略行为测试和应用生命周期静态审计，完整单元测试与 Debug 构建验证通过

### [1.5.11] — 2026-07-21

**重构家庭数据同步并修复生产 Supabase 安全边界：**
- 生产端 9 张同步表新增单调递增 `sync_version`、家庭复合索引和旧写入拦截，增量拉取改用“家庭 + 表”独立服务端游标
- Realtime 增加服务端 `family_id` 过滤，消息中心退出云同步；生产发布范围同步收紧并加入家庭成员变更
- Room 升级到版本 7，宝宝记录增加家庭归属，同步元数据增加唯一约束、重试退避、错误信息与独立游标表
- 推送前检测远端版本，远端较新时优先落地；分页拉取仅在整页成功应用后推进游标，避免失败漏数
- 修复提醒完成/启用状态未进入待同步队列、断网重连被用户去重逻辑跳过等问题
- 生产端移除危险通用写入 RPC、收紧家庭创建与成员权限，并软删除 2 条无家庭归属的历史喂养记录以保留审计数据
- 新增同步安全边界静态回归测试，`testDebugUnitTest` 与 `assembleDebug` 验证通过

### [1.5.10] — 2026-07-18

**AuthService init 优先从 SP 缓存恢复登录态，消除启动时"未登录"闪烁：**
- 恢复顺序改为：SP 缓存 → Supabase 异步恢复 → sessionStatus 监听
- SP 有 `userId` 时立即设 `_currentUser`，不等 Supabase 验证
- `sessionStatus` 确认后持续更新缓存，下次启动直接恢复
- 断网时 SP 缓存正常工作，不再闪"点击登录"

### [1.5.9] — 2026-07-18

**修复增量拉取永远漏记录（`push` 覆写 `updatedAt` + 锚点仅同步时推进）：**
- `push()` 将记录 `updatedAt` 覆写为当前时间戳上传 Supabase，无论记录何时创建，对方增量查询必定命中
- `pull()` / `push()` / `fullSync()` 仅在同步到数据时推进 `lastSyncAt`，无数据同步不涨锚点
- 修复：A 离线多日间 B 多次同步，锚点保持旧值，A 上线推送后 B 一次拉取即可全部命中

### [1.5.8] — 2026-07-18

**修复增量拉取漏记录（`updateLastSyncAt` WHERE 条件导致未来时间戳阻塞）：**
- `updateLastSyncAt` 的 `WHERE lastSyncAt < :now` 条件使写入过未来时间戳的行永不更新
- `MAX(lastSyncAt)` 永久返回未来值，后续所有增量拉取 `updatedAt >= 未来时间戳` 过滤掉所有正常记录
- 修复：去掉 WHERE 条件，每次无条件更新全部行

**修复 collector null 发射误清 `currentFamilyId` + `tryAutoSync` 去重：**
- collector 的 `newId=null` 不再覆盖 `syncEngine.currentFamilyId`
- 缓存 + Supabase 确认双发 auth 导致重复 `tryAutoSync`，增加 `lastAutoSyncUserId` 去重

### [1.5.7] — 2026-07-18

**断网保持登录态和家庭信息：**
- `AuthService` 登录/注册时缓存 `userId` 到 SharedPreferences，`init` 时 Supabase 验证失败用缓存兜底
- `sessionStatus` 监听器只在无缓存时清空登录态，断网保留缓存值
- `observeAuthState()` 改为直接暴露 `_currentUser` 流，不再映射 Supabase 原始流
- `ensureFamily()` 已优先从 SP 恢复家庭 ID，断网不依赖网络请求
- 新增 `Auth: init: cached session uid=...`、`Auth: sessionStatus: ...` 日志，可追踪登录态延迟

### [1.5.6] — 2026-07-18

**修复同步链路竞态条件（全量拉取退化为增量、重复全量、网络延迟）：**
- `currentFamily` collector 初始 null 发射不再覆盖 `lastSyncedFamilyId`，避免误判为家庭切换触发多余全量拉取
- `resetLastSync()` 从协程内移到 collector 中同步执行，确保 `tryAutoSync()` 的 `fullSync()` 读到已清零的锚点
- `fullSync()` 末尾兜底调用 `updateLastSyncAt(now)`，保证无待推送时锚点也能推进
- `ensureFamily()` 优先检查 SharedPreferences 本地持久化，避免每次同步都等 Supabase 网络请求

### [1.5.5] — 2026-07-18

**修复并发触发两次全量同步的重复请求问题：**
- `fullSync()` 新增 `Mutex.withLock` 互斥保护，避免 `tryAutoSync` 和 `currentFamily` collector 同时调用时重复拉取

### [1.5.4] — 2026-07-18

**今日概览和统计分析页喂养统计改为自适应展示：**
- 今日概览：有母乳记录显示`母乳 N次`，有配方显示`配方 Xml`，均无则显示`喂养 N次`
- 统计分析：喂养卡片同步自适应，母乳和配方分别独立展示

### [1.5.3] — 2026-07-18

**修复疫苗列表卡片连在一块的问题：**
- `VaccinationListScreen` 的 `LazyColumn` 缺少 `verticalArrangement`，卡片之间无间距
- 新增 `Arrangement.spacedBy(spacing.sm)` 恢复卡片间距

### [1.5.2] — 2026-07-18

**`jsonStr()` 增加防御性清洗，避免 JSON `null` 被解析为字符串 `"null"`：**
- 从 Supabase 拉取记录时，`JsonNull` 可能被底层库序列化为字符串 `"null"`，导致空字段显示 `"null"` 文本
- `jsonStr()` 返回值增加 `if (value == "null") null` 防御判断

### [1.5.1] — 2026-07-18

**修复全量同步时 push 覆盖拉取锚点导致互相看不到对方记录：**
- `SyncEngine.fullSync()` 原来先 `push()` 后 `pull()`，`push()` 末尾调用 `updateLastSyncAt(now)` 把增量锚点设成当前时间，导致后续 `pull()` 只拉取 `updatedAt >= now` 的记录——所有历史数据全部跳过
- 修复：互换顺序，先 `pull()` 拉取（此时 `lastSyncAt` 还是 null/上一个锚点），再 `push()` 推送本地变更

### [1.5.0] — 2026-07-18

**启用 coreLibraryDesugaring 修复 445 个 NewApi lint 错误：**
- `minSdk = 24` 但大量使用 `java.time.*` API（要求 API 26+），未启用 desugaring 导致 Lint 失败
- `gradle/libs.versions.toml` 新增 `desugar_jdk_libs` 依赖
- `app/build.gradle.kts` 开启 `isCoreLibraryDesugaringEnabled` + 添加 `coreLibraryDesugaring` 依赖

**修复消息中心每次清空后重新播种演示数据的 bug：**
- `MessageViewModel.seedDemoIfEmpty()` 在用户清空所有消息后重进页面会再次插入 6 条演示消息，用户误判为"每天生成虚假通知"
- 移除 `seedDemoIfEmpty()` 及其调用，消息中心不再自动生成演示数据

**修复时间选择器滚轮无法选中边缘值（0/23/0/59）：**
- 首尾填充 halfVisible 个 Spacer 使边缘值能滚动到视口正中央
- Spacer 显式指定不冲突的 key 避免与值 key 冲突
- 添加 lastSnappedValue 防止吸附后循环触发
- 吸附时跳过 Spacer 只考虑真实值

**修复喂养计时器开始时间未持久化：**
- 点击开始计时时保存当前时间字符串到 SharedPreferences
- 重新进入表单时从 SharedPreferences 恢复开始时间
- 结束计时时更新时间戳字段为计时启动精确时间

**设置页新增日志查看器 + Timber 日志系统：**
- 接入 Timber 日志库，自定义 AppLogTree 将日志写入内存环形缓冲区（1000 条）和文件（自动轮转）
- 设置页功能网格新增"日志查看"入口，进入后实时显示应用日志
- 日志查看页支持过滤、清除、自动滚动、级别着色、一键复制到剪贴板
- SyncEngine / FamilyService / SettingsViewModel / AuthService 关键路径埋点，便于排查同步问题

**修复：messages 表被错误加入同步流程导致 RLS 42501 错误：**
- messages 是用户私有数据，不从 SyncEngine push/pull/markExistingPending 中同步
- 新增 markExistingPending 中清理孤立 pending 记录的机制（DELETE orphaned sync_metadata）
- 日志页复制按钮，方便分享日志分析

**修复：ensureFamily() 多家庭场景返回错误家庭 ID：**
- 原来取 `families.firstOrNull()?.id`（列表第一个），改为 `familyService.currentFamily.value?.id`（实际当前家庭）
- 新增 lessons.md 第 5 条教训记录

**修复：JSON null 字段被解析为字符串"null"的全局问题：**
- `JsonNull.toString()` 返回字符串 `"null"`，导致所有可空字段（如 brand/note 等）拉到空值时变成字符串 "null"
- 新增 `jsonStr()` 安全辅助函数，通过 `(json[key] as? JsonPrimitive)?.content` 规避此问题
- 替换所有 parse* 函数中的 100+ 处字段解析调用

**日志系统改进：开关控制 + 移入设置区域：**
- 设置页新增"日志记录"行，带 Switch 开关，关闭时不写入内存/文件（仅输出 logcat）
- 默认关闭，开启后才抓取日志，减少性能开销
- 从顶部功能网格移入设置区域，与主题/隐私等设置项平级
- 开关状态通过 SharedPreferences 持久化，App 重启后保持

**日期选择器统一 + RecordCard 迁移：**
- Feeding/Sleep/Diaper/Health 列表日期筛选从 M3 DatePickerDialog 替换为 DateTimeCascadeDialog（dateOnly 模式）
- GrowthScreen 日历按钮接入日期筛选（之前是空实现）
- DateTimeCascadeDialog 新增 dateOnly 参数，跳过时间选择步骤
- ReminderScreen: AppCard + longPressDeletable → RecordCard（左滑删除 + Snackbar 撤销）
- VaccinationListScreen: AppCard + combinedClickable → RecordCard（左滑删除，点击/长按编辑）

**计时器重构：开始时间不再被覆盖：**
- 睡眠/母乳喂养"开始计时"不再重置表单的开始时间字段
- 退出重进时恢复用户手动填写的开始时间（不再显示当前时间）
- "结束计时"只设结束时间，不再覆盖用户手动设置的开始时间
- 计时器持久化新增 `*_timer_form_start_time` 字段

**家庭共享：当前家庭持久化：**
- FamilyService 通过 SharedPreferences 持久化当前选中家庭 ID
- 切换家庭、创建/加入家庭时自动保存选中状态
- 重启 App 后自动恢复上次选中的家庭
- 退出所有家庭时自动清理持久化记录

**全局圆角令牌升级 + 卡片大圆角风格：**
- AppShapes 按 M3 规范扩展为 10 级（新增 largeIncreased/extraLarge/extraLargeIncreased/extraExtraLarge）
- 卡片圆角从 8dp → 20dp（largeIncreased），边框去除（borderWidth = 0dp）
- BorderContainer 同步改为 20dp 圆角、无边框
- 所有 29 处 AppCard 调用移除显式 cornerRadius 覆盖，统一继承令牌默认值
- TimePicker/DatePicker/DateTimeCascade 圆角从 medium*2 改用 large（保持 16dp 不变）
- 同步清理各页面因 cornerRadius 移除变成未使用的 shapes 变量和 import

**计时器状态持久化：**
- 母乳/睡眠计时器状态通过 SharedPreferences 持久化
- 退出页面再进入时自动恢复计时器状态（运行中/已停止）
- 计时器状态提升到 Screen 层级，关闭表单后重新打开可继续计时

**新增母乳/睡眠计时器功能：**
- 母乳记录：新增"开始计时"/"结束计时"按钮，计时结束自动填入时长（分钟）
- 睡眠记录：新增"开始计时"/"结束计时"按钮，计时结束自动填入开始/结束时间
- 计时过程中实时显示经过时间（MM:SS 格式）

**修复 AppInput 文字显示不全：**
- `AppInput` 使用 `.height(height)` 强制固定高度，导致文字内容被裁剪
- 改为 `.defaultMinSize(minHeight = height)`，保持最小高度的同时允许自适应内容
- `InputTokens.height` 从 `control.medium.height`(48dp) 调整为 56dp（Material 3 标准输入框高度）

**修复 AppTextButton / SecondaryButton 默认字体颜色在亮色主题下不可见：**
- `AppTextButton` 和 `SecondaryButton` 原使用 `ButtonTokens.contentColor`(= `colors.onPrimary`，亮色=白色)
- 白色文字在白色 `AppBottomSheet` 背景上不可见
- 修复：新增 `color: Color` 参数，默认值改为 `colors.primary`（品牌蓝）

**组件默认样式补齐 + Feature 层强制使用 Design System：**
- 新增 5 个 Defaults 文件：IconButtonDefaults、ScaffoldDefaults、SheetDefaults、SectionHeaderDefaults、SegmentedControlDefaults
- 新增 2 组组件令牌（SheetTokens、SegmentedControlTokens），已注册到 AppComponentTokens
- 更新 8 个组件使用 Defaults：IconButton、AppScaffold、AppBottomSheet、SegmentedControl、SectionHeader、AppConfirmDialog、AppFormSheet、SkeletonLoader
- AppFormSheet 移除已废弃的 DT 引用，改用 SheetDefaults
- 全部 14 个 feature Screen 文件完成 Design System 迁移：
  - Scaffold → AppScaffold、Card → AppCard、Button → PrimaryButton、TextButton → AppTextButton
  - IconButton → AppIconButton、OutlinedTextField → AppInput、OutlinedButton → SecondaryButton
  - ModalBottomSheet → AppBottomSheet、AlertDialog → AppConfirmDialog、Switch → AppSwitch
  - CircularProgressIndicator → AppCircularProgress
  - MaterialTheme.typography → LocalAppTypography、DT.* 废弃常量 → 硬编码值
  - import androidx.compose.material3.* 改为具体导入

**主题系统重构：ThemeColors 迁入 Theme.kt + derive() 派生模式：**
- AppTheme / ThemeColors 从 `DesignTokens.kt` 迁入 `Theme.kt`，职责归位（DesignTokens 仅保留已弃用的 DT 常量）
- 新增 `ThemeColors.derive(primary, isDark, ...)` 工厂函数：从主色 + 少量种子自动派生全部 29 个颜色字段
- 内置 `Color.mix()`（线性混合）和 `Color.desaturate()`（去饱和）辅助方法，支持 `isDark` 亮暗双分支自动切换
- 6 套主题从 ~240 行硬编码 6×29 字段精简为每行 1-5 个 `derive()` 调用，净减 ~195 行
- 各主题只需传入主色与差异种子即可（如 `night` 仅需 `primary + isDark=true`）

**修复同步引擎删除链路（两处 bug）：**
- Bug 1：`SyncEngine.applyRemoteChange()` upsert 删除记录时，本地已有同 uuid 记录 → Room 尝试 INSERT 同 uuid → `UNIQUE constraint` 冲突静默丢弃，B 侧收到 duplicate 行但无 `deletedAt`，记录残留
- 修复：upsert 时按 uuid 查现有记录，存在则 update 覆盖，不存在才 insert
- Bug 2：`BabyRepository.cascadeSoftDelete()` 用 raw SQL 更新子记录 `deletedAt`，绕过 `syncMeta.pendingChange()`，级联删除的子记录从不上行，B 侧滞留
- 修复：级联删除前先用 `query()` 收集待删子集（id + uuid），SQL 执行后逐条标记 `syncMeta.pendingChange()`
- 新增文档：`docs/sync-architecture.md`（同步完整链路文档）、`docs/data-architecture.md`（数据架构文档）

**去除自动创建默认家庭，新增家庭持久化离线兜底：**
- `SettingsViewModel.ensureFamily()` 不再自动创建"我的家庭"，家庭需用户主动创建或加入
- 新增三层回退：内存 → Supabase API → SharedPreferences（离线/App 重启后仍可恢复上次使用的家庭 ID）
- `isNewFamily` 同步触发条件放宽，首次创建家庭后也能正确触发全量同步

**修复删除撤销闪退 + RecordCard 手势冲突：**
- `AppSnackbar.showUndo` 改为 suspend 函数，由调用方在 `scope.launch` 内顺序调用，delete → snackbar 不再并发
- `RecordCard` 恢复 `combinedClickable`，避免 `pointerInput` + `detectTapGestures` 与 `SwipeToDismissBox` 的手势冲突

**消除重复代码（DAO + Screen + Repository）：**
- DAO 所有 Flow 查询统一加 `deletedAt IS NULL` 过滤，修复软删除后记录仍显示的问题
- AGENTS.md 新增红线条目「改共享 API 不查调用方」，防止盲改全局接口
- Screen Snackbar：7 个 Screen 的删除-撤销代码统一改用 `AppSnackbar.showUndo()`，每处 8 行缩为 3 行
- Repository：提取 `SyncMetadataDao.pendingChange()` 扩展函数，30 处 `SyncMetadataEntity(...)` 调用统一为 `syncMeta.pendingChange(table, id, uuid, updatedAt)`

**备份还原改为完全还原（ID 保持不变）：**
- 之前还原时所有实体 `id=0` 让 Room 重新生成，导致 ID 全部改变，SyncEngine 无法匹配已有记录
- 修复：`doRestore()` 改为先清空所有表 → 按 JSON 中原始 ID 插入，`babyId` 无需重映射
- 补齐所有 10 个实体的 `toJson()` / `parse*()` 中 `uuid`/`updatedAt`/`deletedAt` 字段导出导入，使 SyncEngine 还原后能正确识别已同步记录
- 构建：`db.withTransaction` 事务保护，先删后插，重置自增序列防止未来 ID 冲突

**备份还原补齐 3 张缺失的表（消息/发育评估/提醒）：**
- Bug：`MessageEntity`、`DevelopmentAssessmentEntity`、`ReminderEntity` 未纳入备份导出和还原，导致还原后丢失消息、发育评估、提醒数据
- 修复：在 `exportAll()` 和 `doRestore()` 中新增这三张表的完整导出/导入逻辑，包括 `toJson()` 和 `parse*()` 方法
- 顺带修复：`doRestore()` 中 `parseBabies` 硬编码 `id=0` 导致 `prefix` 始终为 `"baby_0"`，无法匹配 JSON 中实际 `"baby_{origId}_*"` 键名，所有子记录（喂养/睡眠等）还原时被丢弃。改为保留原始 ID 做 prefix 匹配

**修复家庭页面无法显示当前选中家庭：**
- Bug：`FamilyViewModel.init` 始终 `currentFamily = families.firstOrNull()`，切换家庭后再次进入页面显示错误家庭
- 修复：优先读取 `FamilyService.currentFamily` 中已选中的家庭

**修复加入家庭后成员新增记录无法实时同步：**
- Bug：`RealtimeManager.subscribeAll()` 切换家庭时未先断开旧频道，旧频道仍然存活在 Supabase 服务端，新成员的新记录通过 Realtime 推送时路由到旧频道/旧 RLS 上下文，导致接收不到
- 修复：`subscribeAll()` 在同一协程内先执行 `disconnect()` 断开旧频道，再创建新频道，保证 JWT/RLS 上下文刷新后重新订阅

**修复加入家庭后完整链路断裂（3 处连环 bug）：**
- Bug 1：`FamilyService.joinFamily()` 加入后 `_currentFamily` 保留旧值（`loadMyFamilies` 只在 null 时设置），导致 `SettingsViewModel` 不触发同步
- Bug 2：`FamilyViewModel.selectFamily()` 只改 UI 层 `uiState.currentFamily`，未同步到 `FamilyService._currentFamily`，两个状态源分叉，`SettingsViewModel` 感知不到切换
- Bug 3：`SyncEngine.pull()` 不按 `family_id` 过滤，仅靠 RLS——多家庭用户的 pull 会混合所有家庭数据
+- 修复：`FamilyService` 新增 `selectFamily()` 方法供外部同步全局状态；`joinFamily()` 加入成功后按邀请码匹配自动切换到新家庭
+- 修复：`FamilyViewModel.selectFamily()` 调用 `familyService.selectFamily()` 桥接两个状态源
+- 修复：`SyncEngine.pull()` 添加显式 `eq("family_id", currentFamilyId)` 过滤，且 `currentFamilyId == null` 时拒绝拉取

**修复加入家庭后成员之间宝宝信息不同步的问题：**
+- 根因 1：加入家庭后 `SettingsViewModel` 未触发 `fullSync()`，`currentFamilyId` 更新了但数据没有拉取
+- 根因 2：增量 `pull()` 使用全局 `lastSyncAt` 过滤 (`gte("updatedAt", lastSyncAt)`)，加入新家庭时历史数据（`updatedAt` 早于该时间戳）被跳过
+- 修复：`SyncEngine` 新增 `resetLastSync()` 清除全局同步锚点；`SyncMetadataDao` 新增 `clearLastSyncAt()` SQL
+- 修复：`SettingsViewModel` 监听 `familyService.currentFamily` 变更，检测到切换到新家庭时自动 `resetLastSync()` + `fullSync()`，确保拉取新家庭全部历史数据
+- 修复：切换家庭后自动重新订阅 Realtime（RLS 上下文已变化）

**修复创建者看不到新成员加入的问题：**
+- RealtimeManager 新增 `family_members` 表订阅，新成员加入/角色变更/移除时通过 `SharedFlow` 实时推送
+- FamilyViewModel 监听 `familyMembersChanged` 事件，自动刷新家庭列表和成员列表
+- FamilyViewModel.init 现在也会加载成员列表（之前只加载家庭不加载成员）

**修复加入家庭"邀请码无效"问题：**
+- 根因：`families` 表 RLS 策略 `is_family_member(id)` 阻止非成员查询邀请码，形成闭环死锁
+- 修复：`joinFamily()` 改为调用 PostgreSQL `join_family(invite_code)` SECURITY DEFINER 函数（RPC），该函数绕过 RLS 完成查找+加入
+- 同时统一将邀请码转大写

**移除一级页面 TopBar 返回箭头：**
+- Timeline（记录）、Stats（统计分析）、Message（消息中心）均为一级 Tab 页面，去掉 TopBar 的 `onBack` 返回箭头

**修复底部导航首页点击无效问题：**
+- BottomNavBar 去掉 `if (currentRoute != tab.route)` 守卫，改为始终执行 navigate（`launchSingleTop` 已防重复），防止 currentRoute 误判导致点击无响应
+- HomeScreen FeatureGrid 导航统一为 `popUpTo(startDestination, saveState)` + `launchSingleTop` + `restoreState`，与 BottomNavBar 保持一致，消除回退栈状态不一致

**AppFormSheet：统一表单底部弹层组件：**
+- 新增 `AppFormSheet` 组件（`designsystem/components/dialog/`）：封装 ModalBottomSheet + 标题 + 保存按钮，中间 content 插槽由业务填充
+- 迁移 `FeedingFormDialog` / `SleepFormDialog` / `DiaperFormDialog` / `HealthFormDialog` / `GrowthFormDialog` → 每个减 ~15 行样板代码
+- 统一按钮形状 `DT.buttonRadius`、间距 `DT.pageMargin`，消除各 FormDialog 间的形状/间距差异

**首页宝宝资料跳转 & 账户昵称支持：**
+- 首页顶端 "宝宝资料" 点击改为跳转 `BabyProfileScreen`（宝宝信息详情页），原跳转为宝宝管理列表
+- AuthService 新增昵称功能：`nickname` StateFlow、`setNickname()`、`clearNickname()`，通过 SharedPreferences 持久化
+- 设置页 UserInfoCard 显示昵称（优先于账户名），已登录时名称旁显示 ✏️ 编辑图标
+- 新增 `NicknameEditDialog`：输入昵称 / 清除 / 保存，Toast 确认保存
+- AppStrings 新增 `nickname`、`editNickname`、`nicknameHint`、`nicknameSaved`

**修复记录页面闪退：LazyColumn key 冲突导致崩溃：**
+- 修复：`items(key = { it.id })` → `items(key = { "${it.recordType}-${it.id}" })`，因为不同 Room 表各自从 1 自增，喂养/睡眠/尿布等记录的 id 会重复，导致 LazyColumn 检测到重复 key 直接崩溃
+- 重命名 `forEach` 解构变量 `items` → `records` / 列表项参数 `item` → `record`，消除名称遮蔽歧义

**记录（Timeline）页面重做：**
+- 替换自定义渐变 Header 为 AppTopBar 统一导航栏
+- 用 LazyColumn 替换 Column+verticalScroll，支持大量记录的高性能滚动
+- 优化日期分组标题：相对日期 + 记录次数胶囊标签
+- 类型筛选时无数据展示专用空态（带对应 emoji）
+- diaer 颜色改用 c.tertiary（青绿），与尿布页面统一
+- 底部留白 80dp 避免 FAB 遮挡最后一条记录

**首页八宫格调整：尿布更换替换提醒中心，前置到生长记录前：**
+- 🧷 尿布更换替换 🔔 提醒中心，排在生长记录前面（喂养 → 睡眠 → 尿布 → 生长 → 发育 → 疫苗 → 健康 → 统计）

**修复尿布页面负 padding 闪退风险 + 睡眠大卡恢复间距：**
+- 尿布今日汇总大卡 + 换尿布详情卡移出 LazyColumn，在外层 Column 中用 `.padding(horizontal = DT.pageMargin.dp)` 正常布局，消除 `(-DT.pageMargin).dp` 负 padding 的布局崩溃风险
+- 睡眠夜间大卡添加 `padding(horizontal = DT.pageMargin.dp)` 恢复左右 20dp 间距

**尿布更换 UI 重做：参照睡眠页面：**
+- 顶部改用 AppTopBar（标题+返回+日历图标），替换旧版渐变 Header
+- 新增日期选择器行（今天/昨天/明天 + 完整日期 + 下拉箭头），支持 DatePickerDialog
+- 今日汇总大卡：青绿色渐变背景（Gradients.diaperSummary），展示总次数+分类计数（小便/大便/混合）
+- 换尿布详情卡片：小便/大便/混合 三列 emoji 统计（DiaperStatCell）
+- 记录列表按时间倒序排列，RecordCard + 类型色条（小便蓝/大便橙/混合红）
+- FAB 替换为底部固定按钮（"+ 记录尿布"），添加 BottomNavBar
+- 新增 Gradients.diaperSummary 渐变（tertiary 青绿色调）

**修复睡眠页面闪退：**
+- 移除负 padding 方案，改为将夜间睡眠大卡 + 睡眠详情卡移出 LazyColumn，在外层 Column 中用 `.padding(horizontal = DT.pageMargin.dp)` 正常布局
+- LazyColumn 只负责小睡记录列表，消除负 padding 导致的布局崩溃风险

**生长记录 UI 重做：匹配设计图：**
+- 顶部改用 AppTopBar（标题+返回+日历图标），Tab 切换区（身高/体重/头围）纯净无渐变
+- Hero 大数值卡：居中展示最新数值+单位（48sp），附测量日期
+- 正常范围卡片：独立展示当前 Tab 对应的正常范围值
+- 折线图保留 WHO 参考虚线+面积填充动画
+- FAB 替换为底部固定按钮（"+ 记录身高/体重/头围" 动态文字），添加 BottomNavBar

**睡眠记录 UI 重做：匹配设计图：**
+- 顶部改用 AppTopBar（标题+返回+日历图标），替换旧版渐变 Header
+- 新增日期选择器行（今天/昨天/明天 + 完整日期 + 下拉箭头），支持 DatePickerDialog
+- 夜间睡眠大卡：紫色渐变背景（Gradients.sleepHeader），展示时长+时间范围
+- 睡眠详情卡片：入睡时间 / 起床时间 / 夜醒次数 三列网格
+- 小睡列表：RecordCard + accent 橙黄色条，展示时间段+时长
+- FAB 替换为底部固定按钮（"+ 记录睡眠"），添加 BottomNavBar
+- 按日期过滤记录，仅显示当天数据

**喂养记录 UI 重做：匹配设计图：**
+- 顶部改用 AppTopBar（标题+返回+日历图标），替换旧版渐变 Header
+- 新增日期选择器行（今天/昨天/明天 + 完整日期 + 下拉箭头），支持 DatePickerDialog
+- 列表改为时间轴布局：左侧时间+彩色圆点+竖线，右侧 RecordCard + accent 色条
+- 喂养类型颜色映射：母乳=红 / 配方=蓝 / 辅食=橙 / 饮水=青
+- FAB 替换为底部固定按钮（"+ 记录喂养"），居底不遮挡内容
+- 按日期过滤记录，仅显示当天数据

**我的页面 UI 重做：匹配设计图：**
+- 去掉旧版渐变 Header + 直接设置列表布局
+- AppTopBar（"我的"标题）+ 用户信息卡片（渐变圆形头像 + 名称 + ID + 右箭头，已登录跳转家庭共享）
+- 常用功能宫格（4列）：宝宝管理 / 我的收藏 / 数据导出 / 提醒设置
+- 设置列表重构：主题模式（含当前主题名） / 隐私设置 / 帮助与反馈 / 关于我们（版本号）
+- 退出登录按钮移至底部独立区域，带同步状态提示行
+- SettingsRow 扩展支持 subtitle 参数 + 右箭头图标

**消息中心 UI 重做：匹配设计图：**
+- 去掉渐变 Header + Tab 栏，改用 AppTopBar（含"全部已读"） + 分类概览三角宫格卡片
+- 分类概览卡：互动消息(蓝💬) / 系统通知(蓝🔔) / 服务通知(紫⭐)，选中时蓝色实心反转，未读角标红点数字
+- 点击分类概览卡筛选对应类型消息，再次点击取消筛选显示全部
+- 消息列表改为全量混合展示（不再分 Tab），按时间倒序
+- 互动消息：彩色圆形头像（首字母哈希配色） + 用户名 + 时间(右侧) + 内容
+- 系统通知：蓝色铃铛圆角图标 + 标题 + 内容 + 时间
+- 服务通知：紫色星标圆角图标 + 标题 + 内容 + 时间
+- ViewModel 简化为返回全量消息，Screen 层自行用 filterType 做分类筛选
+- AppTopBar 新增 actions 参数支持右侧操作区

**疫苗接种页 UI 重做：匹配设计图：**
+- Segment 标签页 UI 简化：去掉渐变背景 Header，改为简洁文字 + 下划线指示器
+- 新增胶囊筛选栏：全部 / 待接种 / 已接种 / 已过期，各胶囊独立配色，选中实心 + 白色文字
+- 全部 = 显示所有匹配 tab 的疫苗；待接种 = PENDING + 未过期；已接种 = DONE；已过期 = PENDING + scheduledDate < today（动态计算）
+- 疫苗卡片重新设计：Row1 = 名称(粗体) + 剂次(灰) + 状态标签(胶囊)；Row2 = 建议月龄(左) + 建议日期(右)
+- 建议月龄从 scheduledDate - birthDate 动态计算（出生时/X月龄/X岁X个月）
+- 状态标签支持四态：未接种(黄) / 已接种(绿) / 已过期(红) / 已跳过(灰)
+- 空态文案按筛选条件动态变化（例如选「已过期」时显示"暂无过期疫苗"）
+- 长按卡片 = 删除（替代原 RecordCard 的 onDelete）

**健康档案页 UI 重做：匹配设计图：**
+- 顶部渐变标题区移除，纯卡片列表，无 BabyHeader
+- 卡片改为紧凑摘要行：彩色圆形 emoji（紫色🍼/黄🤧/绿📋/红🏥/蓝💊/橙💉/橙红📋）+ 标题 + 副标题 + › 箭头
+- 新增「疫苗接种记录」卡片：读取 Vaccination 数据，显示"已接种X针"，点击跳转疫苗接种列表页
+- 分类精简为 7 项：出生信息 → 过敏史 → 既往病史 → 就诊记录 → 用药记录 → 疫苗接种记录 → 医生备注（去掉体检记录和备注）
+- 分类卡片点击展开/收起内嵌记录列表，展开时带 AnimatedVisibility 过渡动画
+- 就诊/用药卡片副标题显示"X条记录"计数，单值卡片显示最新内容
+- 表单默认分类从 allergy 改为 birth_info，分类选项与列表顺序一致

**发育评估页 UI 重做：匹配设计图：**
+- 首页「发育评估」宫格项导航修正：Screen.Growth → Screen.DevelopmentAssessment
+- BabyHeader：首字母圆形头像 → 卡通 👶 头像（品牌色浅底圆形 72dp），年龄从"月龄 X 个月"改为"X岁X个月X天"详细格式
+- 能力卡片：Material Icons 改为彩色圆形 emoji（🏃✋💬🤝🧠），5 项各自独立配色（橙/粉/蓝/绿/紫），标题行内嵌胶囊状态标签 + › 箭头，新增自然语言描述
+- 去掉 AssessmentSummaryCard 概览卡片，简化页面结构
+- 底部操作栏：下次评估时间（左）+ 胶囊重新评估按钮（右）同一行布局
+- 评估表单 ScoreSelector 增加当前能力描述文字，评分选项配色对齐主卡片

**首页 UI 重做：更贴合设计图：**
+- BabyHeader：右侧改为卡通 👶 头像（品牌色浅底圆形），姓名+年龄/月龄行，宝宝资料链接
+- FeatureGrid：8 功能项名称改为设计图文本（喂养记录/睡眠记录/生长记录/发育评估/疫苗接种/健康档案/统计分析/提醒中心），新增 🔔🧠 图标
+- TodayOverviewCard：去掉 emoji 图标，改为大数字+单位+标签纯文字三列布局，新增「今日概览」标题
+- RecentRecordsSection：标题使用 Row+TextButton 替代 SectionHeader，圆角改为 shapes.medium
+- HomeViewModel sleepHours 格式从 "XhYmin" 改为 "X时Y分"

**统计页重做：日/周/月/年周期 + 对比 + 柱状图：**
- StatsScreen UI 重做：SegmentedControl 切换周期、DateRangeNav 翻页导航、四张统计卡（喂养柱状图、睡眠柱状图、身高折线图、体重折线图）
- 修正卡片布局：2x2 网格 → 纵向堆叠列表，卡片内图标+标题左对齐、数值右对齐、图表下方
- 新增 MiniBarChart 组件（柱顶圆角使用 shapes.extraSmall 令牌）

**宝宝信息页面：独立档案页 + 生长数据展示：**
- 新增 BabyProfileScreen：头像（含相机图标）+ 姓名 + 性别 · 年龄 + 出生信息卡片 + 当前生长数据卡片（身高/体重/头围，含测量日期）
- 从 GrowthRepository 实时读取最新身高/体重/头围记录
- 右上角编辑按钮可修改宝宝基本资料（复用 BabyFormDialog）
- 新增 Screen.BabyProfile 路由 `/settings/baby/profile`，SettingsScreen "宝宝信息" 跳转至档案页
- 底部 "管理全部宝宝" 入口导航至 BabyManagementScreen 列表管理

**Phase 7：RLS 家庭隔离策略升级：**
- Supabase 10 张业务表增加 `family_id UUID` 列（外键关联 families 表）
- 删除旧 `TO authenticated` 宽松策略，替换为 `is_family_member(family_id)` 按家庭隔离
- SyncEngine 新增 `currentFamilyId` 属性 + `injectFamilyId()` 方法，push 时自动注入
- SettingsViewModel 监听 FamilyService.currentFamily 自动同步 family_id 到 SyncEngine
- 退出登录时清空 SyncEngine.currentFamilyId

**Supabase Phase 6：家庭共享：**
- Supabase 建表：profiles（用户档案）、families（家庭 + 6位邀请码）、family_members（成员关系 owner/member）
- 新增 join_family RPC 函数 + is_family_member 辅助函数 + 三表 RLS 策略
- 新增 FamilyService：创建家庭、通过邀请码加入、成员列表查询（Supabase API）
- 新增 FamilyViewModel + FamilyPage UI：空态提示、家庭详情、邀请码复制、成员列表、家庭切换
- 导航新增 /settings/family 路由，Settings 页添加"👨‍👩‍👧 家庭共享"入口（登录后可见）
- DI 注册 FamilyService + FamilyViewModel

**修复 Supabase 注册 & 设置优化：**
- 新增 SettingsViewModel：管理同步状态（syncState / connectionState / isLoggedIn），监听登录态自动启动/停止 Realtime 订阅
- SettingsScreen 接入 ViewModel：登录后显示云同步状态行（连接状态指示点 + 状态文本 + "立即同步"按钮）
- RealtimeManager DELETE 事件实现：通过 uuid 查找并软删除本地记录（softDeleteByUuid）
- SyncEngine applyRemoteChange 完善 LWW 冲突处理：按 uuid 查找本地记录，比较 updatedAt 决定是否覆盖
- 10 个 DAO 补充 getByUuid + softDeleteByUuid 方法，SyncMetadataDao 补充 getByRemoteUuid
- EntityDao 内部类新增 getByUuid 参数
- 清理 SupabaseProvider 过时 TODO 注释
- DI 注册 SettingsViewModel

**Supabase Phase 2：核心同步引擎 & Realtime 订阅：**
- Repository 层自动填充同步字段：insert 时生成 UUID + updatedAt，update 时刷新 updatedAt，delete 改为软删除（设 deletedAt）
- 补充 AppMessage / DevelopmentAssessment / Reminder domain model 的 uuid/updatedAt/deletedAt 字段及 mapper
- 新增 SyncEngine：双向增量同步（push/pull），Postgrest upsert + 增量过滤（gte updatedAt），冲突策略 Last-Write-Wins
- 新增 RealtimeManager：订阅 Supabase 10 张业务表的 INSERT/UPDATE/DELETE 变更，自动回写本地 Room
- 新增 syncModule DI 注册：SupabaseClient / AuthService / SyncEngine / RealtimeManager
- MessageDao 补充 getById + update 方法

**领域模型升级 & Repository 重构：**
- Domain Models 新增 uuid / updatedAt / deletedAt 字段，与 Room Entity 对齐
- Mappers 更新 7 对 toDomain()/toEntity()，完整映射新字段
- Repository 接口返回值从 Entity 改为 Domain Model，Impl 层统一做 Entity→Domain 映射
- VaccineSchedule.createForBaby 返回值改为 `List<Vaccination>`（Domain Model），状态使用 VaccinationStatus 枚举
- ViewModel/Screen 层适配：import 从 entity 改为 domain.model，字符串比较改为枚举比较

**Settings 页面 DS 组件合规修复：**
- ThemePickerSheet: 原生 Card → AppCard
- SettingsCard: 原生 Card → AppCard
- BabyManagementScreen: CenterAlignedTopAppBar → AppTopBar, FloatingActionButton → AppFAB, AlertDialog → AppConfirmDialog
- BackupScreen: CenterAlignedTopAppBar → AppTopBar, 恢复确认 AlertDialog ×2 → AppConfirmDialog
- BabyFormDialog 添加 @OptIn 注解（表单对话框保留原生 AlertDialog，DS 无对应组件）
- 清理无用 import（ArrowBack 等）

**文档：Supabase 后端方案补充：**
- 补充 `profiles` 和 `family_members` 表的 RLS 策略（第三节）
- 新增第六节"删除策略（Soft Delete）"：所有业务表增加 `deleted_at` 字段，30 天保留期
- 修正第五节 Realtime 订阅代码的协程收集问题（`subscribeRealtime` 接收 `CoroutineScope` 参数）
- 认证方案调整为"不强制登录，本地优先"：只保留账户名+密码，去掉手机号 OTP 和微信登录
- 全文"邮箱"改为"账户"，Supabase Auth 底层用 email 字段存账户名，关闭邮箱验证

**Supabase 后端集成 Phase 1（基础架构）：**
- 添加 supabase-kt BOM 3.6.0 + Ktor 3.5.1 依赖（libs.versions.toml + build.gradle.kts）
- Room Migration 5→6：10 张业务表增加 uuid / updatedAt / deletedAt 字段
- 新增 sync_metadata 表（SyncMetadataEntity + SyncMetadataDao），跟踪同步状态
- 新增 SupabaseProvider 单例（Postgrest + Auth + Realtime + Storage）
- 新增 AuthService：账户名+密码注册/登录/退出，sessionStatus 状态监听
- 新增 LoginScreen + LoginViewModel：账户登录/注册页面
- AppNavigation 新增 /login 路由
- Settings 新增"登录账户"入口（显示登录状态）
- Koin DI 新增 syncModule，注册 SupabaseClient / AuthService / LoginViewModel
- 数据库 module 补充 SyncMetadataDao

### [1.4.3] — 2026-06-28

**AGENTS.md 重构：**
- 从 219 行精简为 ~120 行地图式结构，遵循"地图而非百科"原则
- 详细内容拆分至 `docs/design-system.md` 和 `docs/project-structure.md`

**修复状态栏颜色不一致：**
- 状态栏底色从 `background` 改为 `primaryContainer`（即 `primaryLight`），与所有页面顶部区域颜色统一

**性能优化 + 视觉改进 + 记录页闪退修复：**

**性能优化：**
- 7 个列表屏幕 `Column+verticalScroll` → `LazyColumn` + `stickyHeader`（Feeding/Sleep/Diaper/Growth/Vaccination/Timeline/Health）
- `groupBy` / `filter` 加 `remember` 缓存，不再每次重组重算
- TimelineViewModel 加 `distinctUntilChanged`，相同数据不触发全量重建
- StatsViewModel 日统计从 O(N×365) 嵌套循环改为 `groupBy{date}` O(N+365)

**记录页闪退修复：**
- LazyColumn + `weight(1f)` + `contentPadding(bottom=80dp)` 组合导致划到底闪退
- 回退到 `Column+verticalScroll` 稳定版，保留其他功能

**视觉改进：**
- 日期头显示相对时间 + 计数：`今天 · 5次`（`DateUtils.relativeDate()`）
- RecordCard 加 `accentColor` 参数，左侧 3dp 类型色条（drawBehind 绘制，无布局影响）
- TimelineScreen 按记录类型传色：喂养橙/睡眠紫/尿布蓝/生长绿/健康蓝灰
- 首页"查看全部"导航修复：FeedingScreen → TimelineScreen

**删除体验统一：**
- 所有屏幕卡片编辑改为长按触发（`onClick` → `onLongClick`），Vaccination 保持点击
- 左滑删除统一弹出 `AppConfirmDialog` 确认后执行（`SwipeToDeleteContainer` / `SwipeToEditDeleteContainer`）
- 红色背景裁剪：滑动容器外层加 `Box.clip(RoundedCornerShape(DT.cardRadius.dp))`，解决红色超出卡片边界的问题
- 卡片间距外移：卡片内部 `padding(vertical=4dp)` 移到 `SwipeToDeleteContainer` 外层 modifier，避免红色填充卡片间隙
- 移除 Feeding/Sleep/Diaper/Growth/Vaccination/Health/Timeline 共 7 个屏幕的长按删除逻辑，仅保留左滑删除（已含确认弹窗）

**记录页直接编辑：**
- TimelineScreen 点击卡片直接弹出编辑表单（不再跳转功能页），支持喂养/睡眠/尿布/生长/健康
- TimelineViewModel 新增 `findXxx(id)` / `updateXxx(entity)` 方法供直接编辑使用

**单元测试：**
- 新增 `DateUtilsTest`（相对日期 4 场景）
- 新增 `TimelineViewModelTest`（数据映射 + find/sort 验证）

**Warning 清零：**
- 移除 DT `@Deprecated` 注解（227 warnings）
- `centerAlignedTopAppBarColors` → `topAppBarColors`（7 处）
- `Icons.Outlined.List/Message` → `Icons.AutoMirrored.Outlined.*`
- `Icons.Filled.DirectionsRun` → `Icons.AutoMirrored.Filled.DirectionsRun`
- `statusBarColor` deprecation 压制
- 4 个 ViewModel 加 `@OptIn(ExperimentalCoroutinesApi::class)`
- BackupManager 移除多余 `?.` 和 `?:` dead code

**Palette 令牌系统完整实施（Round 5）：**
- **核心令牌升级**：`AppColors` 从 16 字段扩展到 39 字段（含 textPrimary/textSecondary/textTertiary/textDisabled、pageBackground/surfaceElevated、bgHover/bgPressed/bgSelected/bgDisabled、divider/overlay/shadow 等全部语义状态色）；`AppSemanticColors` 移除，所有语义色合并入 `AppColors`
- **derive() 升级**：HSL 色相位移覆盖全部 39 个字段，支持 `accent`/`accentLight` 参数；内置 LRU 缓存 12 条
- **组件令牌扩展**：`AppComponentTokens` 从 9 种 → 21 种令牌（新增 Select/SelectionControl/Switch/Table/Dialog/Menu/Tag/Progress/Steps/Pagination/Slider/Rate），全部带 `@Immutable`
- **颜色派生**：`AppComponentTokens.default(colors)` 从 `AppColors` 自动派生 7 个组件的颜色令牌
- **控件尺寸令牌**：`AppControl` → `AppControlTokens` + `ControlSizeTokens`，提供 small/medium/large 三档预设
- **Default 快照同步**：`AppDefaults` 同步更新七类令牌快照（8 级间距 / 6 级阴影 / 7 级透明 / 6 级圆角 / 动效令牌）
- **Logic 解耦**：新增 `ButtonLogic`（isPressed/isLoading/防抖）、`FormLogic`（fields/errors/touched/submitting）、`TableLogic`（sorting/selection/pagination），纯 Kotlin 可 JVM 单测
- **作用域桥接**：`rememberButtonLogic`/`rememberFormLogic`/`rememberTableLogic` 接受 `CoroutineScope?`，为 null 时 `consoleWarn` 提示传入 viewModelScope
- **PaiButton 工厂**：PRIMARY/SECONDARY/TEXT 三变体简化工厂
- **静态审计测试**：`ThemeTokenizationStaticAuditTest` 验证所有 Defaults 文件路由正确性
- **组件脚手架**：`scripts/generate-component.sh` 一键生成 Xxx.kt / XxxDefaults.kt / XxxLogic.kt / XxxLogicTest.kt

**组件目录化 + 语义色阶补齐 + Typography 令牌化：**
- **组件目录化**：14 个组件各自独立目录，`AppComponentDefaults.kt` 拆分为 9 个独立 `XxxDefaults.kt` 各归其位
- **新增组件**：`AppSwitch`（主题化 Switch，颜色自动跟随主题）、`AppScaffold`（Scaffold 包装，自动 `containerColor = c.bg`）
- **深色令牌**：`AppComponentTokens.dark()` 自动暗色变体，`BabyTrackerTheme` 根据 `theme.name == "night"` 自动选择
- **DT 弃用**：`DesignTokens.DT` 加 `@Deprecated`，组件级 8 处引用已迁移至 XxxDefaults/inline dp
- **语义色阶**：`ThemeColors` 新增 `borderHover` `borderFocus` `textDisabled` `bgHover` `bgPressed`，6 主题全部补全
- **Typography 令牌**：新增 `LocalAppTypography` CompositionLocal，4 个组件从 `MaterialTheme.typography` 迁移至令牌读取

**设计系统骨架（参照 Palette 令牌驱动架构）：**

**Round 1 — 核心令牌 + 组件层：**
- **核心令牌层**：新建 `AppTokens.kt`（Spacing 6 级 / Elevation 4 级 / Opacity 5 级 / Motion 3 级 / Shapes 6 级）+ 5 个 CompositionLocal
- **组件令牌层**：新建 `AppComponentTokens.kt`（9 组：Card / AppBar / Button / Input / Chip / Fab / BottomBar / ListItem / Skeleton）
- **组件 Defaults 层**：新建 `AppComponentDefaults.kt`（9 个对象，对标 Palette 的 XxxDefaults 模式）
- **Theme.kt 升级**：`BabyTrackerTheme` 注入全部 7 个 CompositionLocal
- **新增组件**：`AppCard` / `AppTopBar` / `PrimaryButton`+`SecondaryButton`+`AppTextButton` / `AppChip` / `SectionHeader`+`AppListItem` / `AppInput` / `AppFAB`
- **文件整理**：`AnimatedListItem`/`animateNumber` → `Animations.kt`；`SwipeToDeleteContainer` 系列 → `SwipeContainers.kt`；`HapticExtensions.kt` 精简

**Round 2 — 对标 Palette 完整结构补全：**
- **i18n**：新建 `core/i18n/AppStrings.kt`（60+ 中文文案集中管理，对标 PaiStrings）
- **hooks**：新建 `core/hooks/Hooks.kt`（`useDebounce` / `useState` / `useLatestState`，对标 Palette hooks）
- **快照**：新建 `core/util/AppDefaults.kt`（非 Composable Token 快照，对标 PaiDefaults）
- **foundation**：新建 `foundation/border/BorderContainer.kt` + `foundation/layout/CenterVerticallyRow.kt`
- **新组件**：`AppConfirmDialog`（确认删除）/ `AppSnackbar`（撤销 Snackbar）/ `AppIconButton`（主题化图标）/ `AppBottomSheet`（表单弹层）
- **导出索引**：新建 `ui/components/AppComponents.kt`（对标 Palette Pai.kt 桶文件）

**Round 4 — 设计系统统一迁移至 `designsystem/`：**
- `core/components/` + `core/theme/` + `core/hooks/` + `core/i18n/` + `core/foundation/` + `core/util/AppDefaults` → `designsystem/`
- 包路径统一为 `com.babytracker.designsystem.*`

**最终目录结构：**
```
core/theme/     — 主题入口（7 文件）
core/tokens →   AppTokens / AppComponentTokens / AppComponentDefaults / DesignTokens
core/hooks/     — 轻量 Hooks（useDebounce/useState/useLatestState）
core/i18n/      — 国际化文案（AppStrings）
core/util/      — 工具 + AppDefaults 快照
foundation/     — 基础组件（BorderContainer/CenterVerticallyRow）
ui/components/  — 19 个 Composable 文件 + 导出索引
```

**优先级模型（对标 Palette 五级）：**
```
显式参数 > Defaults 参数 > 组件令牌 > 语义令牌 > 硬编码回退
```
- **TimelineViewModel**：新增 `undoLastDelete()` 方法暂存最近删除实体用于撤销

### [1.4.2] — 2026-06-28

**DatePicker & TimePicker TDesign 风格重构：**
- 移除 M3 DatePickerDialog + AlertDialog 原生样式，改为 TDesign 风格自定义底部面板
- 日历面板：月/年标题 + 左右箭头切换 + 日期网格（圆形品牌色选中标记 + 今天品牌色文字）
- 时间面板：滚轮式时/分选择器（中间行品牌色高亮背景 + 上下分隔线 + 拖拽吸附）
- 级联流程：先选日期 → "下一步" → 选时间 → "确认"，统一在一个底部面板内完成
- 提取 WheelPicker 为独立共享组件（timepicker 包内 public，供 TimePickerDialog 和 DateTimeCascadeDialog 共用）
+- 修复滚轮选择器边界值重复显示（小时 23 重复三次、分钟 0 重复三次），改为 null 占位替代 coerceIn
+- 修复小时/分钟 range 错误（0..24→0..23、0..60→0..59）
+- 修复确认按钮点击后窗口不消失（onConfirm 后追加 onDismiss）
+- WheelPicker → AppWheelPicker → TimePickerLogic（符合命名规范），文件名同步重命名
- 令牌扩展：DatePickerTokens/TimePickerTokens 新增 toolbarHeight/toolbarTextColor/dividerColor/selectedBackgroundColor 等 TDesign 风格字段

### [1.4.1] — 2026-06-28

**修复底部导航栏图标显示不全：**
- BottomBarTokens 高度 64dp → 80dp、图标 22dp → 24dp，与 M3 NavigationBar 默认值对齐，解决图标被裁剪问题
- NavigationBar 添加 navigationBarsPadding() 防止系统手势条遮挡

**修复主题切换后组件颜色不跟随变化：**
- LocalAppComponentTokens 从 staticCompositionLocalOf 改为 compositionLocalOf，确保主题切换时可靠重组
- BabyTrackerTheme.resolvedColors 从硬编码 AppColors.light()/dark() 改为基于 theme.colors 调用 AppColors.derive()，使暖阳粉/极光紫等主题的组件令牌颜色正确跟随

**设计令牌参照 shadcn/ui 样式升级：**
- 组件令牌补 contentColor：CardTokens/DialogTokens 新增 contentColor，与 containerColor 成对
- 圆角体系：AppShapes 新增 radiusScale 全局缩放 + scaled() 方法，所有组件 cornerRadius 标注与 shapes 对应关系
- ButtonTokens.contentColor 默认值从 Color.Transparent 修正为 c.onPrimary
- AppColors.derive 重构为语义派生（参照 PaletteColors）：输入 primary/surface/onSurface/border 4 个基础色，自动推导全部 39 字段
- light()/dark() 改用 derive 实现，消除冗余手写值
- 文档：AGENTS.md 补充令牌设计约定，design-system.md 补充 surface/foreground 配对 + 圆角派生说明

**shadcn 风格视觉调优：**
- 色彩：亮色主色 #4285F4→#3B82F6(blue-500)，背景 #E6F0FF→#F8FAFC(slate-50)，边框 #E0EAF5→#E4E4E7(zinc-200)，success/warning/danger 同步对齐
- 暗色：surface #1E1E32→#18181B(zinc-900)，background #12121F→#09090B(zinc-950)
- 圆角：medium 12dp→8dp，large 16dp→12dp，small 8dp→6dp，对标 shadcn --radius=0.5rem
- 卡片：border 0dp→1dp 描边风格，cornerRadius shapes.large→shapes.medium
- 按钮：cornerRadius shapes.medium*2→shapes.medium
- 标签/Chip：cornerRadius 20dp→shapes.full 胶囊形
- Card 新增 borderColor/borderWidth 参数支持

**令牌系统全面迁移（LocalThemeColors → LocalAppColors）：**
- 组件层：13 个组件全部迁移到新令牌系统（Fab/Input/Section/TopBar/BottomNav/Button/Chip/Dialog/IconButton/Scaffold/RecordCard/BabyIllustration/BorderContainer）
- 补充令牌定义：FabTokens/BottomBarTokens/AppBarTokens/ListItemTokens/ChipTokens 新增颜色字段；新增 IconButtonTokens/ScaffoldTokens/BorderContainerTokens
- Feature 层：13 个页面文件批量迁移，属性映射 bg→pageBackground, primaryLight→primaryContainer, card→surface, accent→warning 等
- Gradients 工具类参数类型从 ThemeColors 迁移到 AppColors
- 消除全部硬编码颜色值：CountdownChip/BadgeIcon/BabyIllustration/RecordCard 改用令牌

### [1.4.0] — 2026-06-25

**记录编辑与删除优化：**
- **编辑功能**：喂养/睡眠/尿布/生长/疫苗/健康 6 种记录全部支持点击编辑，表单预填原有数据，保存调用 `repo.update()`
- **滑动删除**：新增 `SwipeToDeleteContainer` 组件，左滑红色背景 + 删除图标松手即删；`SwipeToEditDeleteContainer` 双方向（左滑删/右滑编）
- **撤销删除**：所有删除操作后弹出 Snackbar「撤销」，点击自动重新插入原记录

### [1.3.0] — 2026-06-18

**依赖升级：**
- **SDK 36**：compileSdk/targetSdk 35→36，配合新版 AndroidX 库
- **AGP 9.2.1**：构建工具链更新
- **Kotlin 2.3.21 + KSP 2.3.9**：语言及注解处理升级
- **Compose BOM 2026.05.01**：Compose 1.11.x 系列
- **Koin 4.2.1**：ViewModel DSL 从 `koin-androidx-viewmodel` 迁移到 `koin-core-module-dsl`
- **Retrofit 3.0.0 / OkHttp 5.4.0**：网络栈升级
- **Room 2.8.4 / Lifecycle 2.10.0 / Core KTX 1.18.0**：AndroidX 全线升级

**基础设施：**
- **版本目录**：从直接声明依赖迁移到 `gradle/libs.versions.toml` 统一管理
- **Termux AAPT2 修复**：替换为 ReVanced 预编译 aapt2-arm64-v8a（build-tools 35.0.2），支持 SDK 35+ 编译
- **添加 AAPT2 修复文档**：`docs/aapt2-termux-fix.md`

### [1.2.0] — 2026-06-18

**架构改进：**
- **ViewModel 生命周期修复**：`HomeViewModel` / `StatsViewModel` 从 Koin `single` 改为 `viewModel` 注册，UI 用 `koinViewModel()` 获取，Activity 重建后正确重置状态
- **BabyController 自动 fallback**：监听 `BabyRepository.watchAll()`，删除当前宝宝后自动切换到第一个宝宝；当前 id 为 0 且列表非空时自动选中第一个
- **HomeViewModel Flow 修复**：改用 `_trigger + flatMapLatest`，避免每次切换宝宝启动新 Flow 不取消旧 Flow
- **HomeViewModel 移除未用依赖**：构造参数从 4 个减为 3 个（移除 growthRepo）
- **Domain Model 层占位**：新增 `domain/model/Models.kt` 定义 8 个纯 Kotlin 数据类 + 枚举（FeedingType/SleepType/GrowthType/VaccinationStatus/DiaperType/BreastSide）
- **Mapper 占位**：新增 `data/database/mapper/Mappers.kt` 提供 Entity ↔ Domain Model 双向映射（待后续 Repository 接口切换时启用）

**Bug 修复：**
- **SleepListScreen 今日睡眠过滤**：原来取"最近一条 night 记录"，会显示历史数据为今日睡眠。改为 `it.startTime.startsWith(today)` 过滤
- **SleepListScreen 进度条双向 clamp**：`coerceAtMost(1f)` 改为 `coerceIn(0f, 1f)`，避免负数时长导致进度反向
- **SleepListScreen 进度条渐变**：硬编码颜色改为 `Gradients.progress(c)` 跟随主题
- **HealthScreen › 引导符**：移除有引导符但 onClick 为空的反模式
- **BackupManager 不可达代码**：删除 `restoreFromUri` 末尾永远不会执行的 `Result.failure(Exception("未知错误"))`
- **健康/疫苗表单 DatePicker**：手敲 yyyy-MM-dd 改为 DatePickerDialog，加 leadingIcon + 校验

**发布配置：**
- **release minify 开启**：`isMinifyEnabled = true` + `isShrinkResources = true`
- **proguard-rules.pro**：新增完整 keep 规则（Room/Koin/Retrofit/OkHttp/Gson/Coroutines/Compose/Coil）
- **AndroidManifest 备份规则**：`allowBackup=false` + `dataExtractionRules` + `fullBackupContent` 排除数据库与 SP（避免与 App 自身备份系统冲突）
- **SplashScreen**：引入 `androidx.core:core-splashscreen`，新增 `Theme.BabyTracker.Splash` 主题，MainActivity `installSplashScreen()`

**性能与健壮性：**
- **HomeScreen FeatureGrid**：`items.indexOf()` O(n²) 查找改为 `forEachIndexed` 直接获取 index
- **DateUtils 新增 safeParse**：`safeParseDateTime(s)` / `safeParseDate(s)` 返回 null 而非抛异常，UI 层避免散落 try-catch

**功能补全：**
- **疫苗页"生成接种计划"**：空状态加 action 按钮，弹确认框后调用 `VaccineSchedule.createForBaby` 一键生成 21 条默认计划

**文档清理：**
- 删除严重过时的 `UPGRADE_ROADMAP.md`（声称缺表单 BottomSheet，实际已完成）

### [1.1.0] — 2026-06-18

**Bug 修复：**
- 修复 `FeedingListScreen` 中嵌套定义的 `NumberPicker` 死代码导致的编译风险
- 修复 `SettingsScreen` WebDAV 恢复确认弹窗被错误嵌套进本地恢复弹窗 if 块，导致完全不显示的问题
- 简化 `Theme.kt` 的 `isDark`/`darkTheme` 不一致 — 移除无用的 `dynamicColor` 分支与 `isSystemInDarkTheme` 调用
- 移除 `gradle.properties` 中的 Termux 专有 `aapt2FromMavenOverride` 路径
- 清理 6 个 UI 文件未使用的 `BabyRepository` import
- 删除 `BackupManager` 中未引用的 `ResultHolder` 数据类

**视觉与交互升级：**
- **Shapes 升级**：列表 Card 16dp / 容器 20dp / 弹窗 28dp
- **卡片质感**：移除全部 `BorderStroke(1.dp, outlineVariant)`，改用 `cardElevation = 1.dp` 微阴影
- **Gradients 工具**：新增 `core/theme/Gradients.kt`
- **首页头部**：背景从纯色升级为渐变；宝宝头像圆框用主色渐变
- **AppBar 主题色化**：所有页面统一 `primaryContainer` 背景
- **数字动画**：首页今日概览 `animateIntAsState` 平滑过渡
- **EmptyState 组件**：7 处列表空状态应用
- **触觉反馈**：6 处长按删除点统一加 `HapticFeedbackType.LongPress` 震动
- **Growth 图表精致化**：动态 Y 轴刻度 + 渐变填充 + 圆角端点 + 双层圆点
- **表单体验**：FeedingFormDialog 加 leadingIcon + isError 范围校验

### [1.0.0] — 2026-06-02

**初始实现：** 基于 MyApp 技术栈的宝宝记录 App。

- 首页 / 喂养 / 睡眠 / 生长 / 疫苗 / 健康 / 统计 / 设置 8 个页面
- 架构：MVVM + Koin + Room + Navigation Compose + Material 3
- 技术栈：Kotlin 1.9.24, Java 17, Gradle 9.5.1, AGP 8.9.3, SDK 34
