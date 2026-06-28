## 更新日志
遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 规范。
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

### [Unreleased]

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

### [Unreleased] — 2026-06-28

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
- 左滑删除统一弹出 `AppConfirmDialog` 确认后执行
- 7 个屏幕长按删除全部移除，仅保留左滑删除

**记录页直接编辑：**
- TimelineScreen 点击卡片直接弹出编辑表单（不再跳转功能页）
- TimelineViewModel 新增 `findXxx(id)` / `updateXxx(entity)` 方法

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

**滑动删除体验改进：**
- **滑动确认弹窗**：`SwipeToDeleteContainer` / `SwipeToEditDeleteContainer` 左滑不再直接删除，改为弹出 `AppConfirmDialog` 确认后执行
- **红色背景裁剪**：滑动容器外层加 `Box.clip(RoundedCornerShape(DT.cardRadius.dp))`，解决红色超出卡片边界的问题
- **卡片间距外移**：卡片内部 `padding(vertical=4dp)` 移到 `SwipeToDeleteContainer` 外层 modifier，避免红色填充卡片间隙

**记录页直接编辑：**
- **TimelineScreen 点击编辑**：点击时间线卡片直接弹出对应类型的编辑表单（喂养/睡眠/尿布/生长/健康），不再跳转功能页
- **TimelineViewModel 新增**：`findXxx(id)` / `updateXxx(entity)` 方法供直接编辑使用

**长按删除统一移除：**
- 移除 Feeding/Sleep/Diaper/Growth/Vaccination/Health/Timeline 共 7 个屏幕的长按删除逻辑，仅保留左滑删除（已含确认弹窗）

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
