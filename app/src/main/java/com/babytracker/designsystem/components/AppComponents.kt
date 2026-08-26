@file:Suppress("unused")

package com.babytracker.designsystem.components

// ═══════════════════════════════════════════════════════════
//  AppComponents 统一导出 — 对标 Palette Pai.kt
//
//  所有公共 Composable 组件、Hooks、Foundation 的索引入口。
//  使用者只需 import com.babytracker.designsystem.components.*
// ═══════════════════════════════════════════════════════════

// —— 基础层 ——
// BorderContainer     (foundation/border)
// CenterVerticallyRow (foundation/layout)

// —— 组件层 ——
// AppCard            统一卡片               (card/)
// AppTopBar          统一导航栏             (topbar/)
// AppButton          统一按钮（变体枚举）    (button/)
// AppInput           统一输入框             (input/)
// AppDivider         分割线                 (divider/)
// AppSurface         表面容器               (surface/)
// AppChip            标签                   (chip/)
// AppTag             语义标签（5色变体）      (tag/)
// AppFAB             浮动操作按钮            (fab/)
// SectionHeader      分区标题               (section/)
// AppListItem        标准列表项
// AppIconButton      主题化图标按钮          (iconbutton/)
// AppConfirmDialog   确认删除对话框          (dialog/)
// AppDialog          通用对话框             (dialog/)
// AppActionSheet     底部操作面板           (dialog/)
// AppBottomSheet     表单底部弹层           (sheet/)
// AppSnackbar        撤销 Snackbar          (snackbar/)
// AppSnackbarHost    全局提示宿主            (snackbar/)
// AppSwitch          主题化 Switch          (switchcontrol/)
// AppCheckbox        主题化复选框           (switchcontrol/)
// AppRadioButton     主题化单选按钮         (switchcontrol/)
// AppScaffold        Scaffold 包装（自动 bg） (scaffold/)
// SubPageScaffold    设置子页等二级页通用骨架 (scaffold/)
// AppLinearProgress  线性进度条             (progress/)
// AppCircularProgress 圆形进度指示器         (progress/)
// AppSlider          主题化滑块             (slider/)
// AppLabeledSlider   带标签滑块             (slider/)
// AppRate            星级评分               (rate/)

// —— 设置行家族 ——
// AppSettingItem          设置行（emoji 徽章+标题+尾部）   (settingitem/)
// AppSettingSwitchItem    开关设置行                       (settingitem/)
// AppSettingChoiceItem    胶囊单选设置行                   (settingitem/)
// AppSettingGroupTitle    设置分组标题                     (settingitem/)
// SettingItemDefaults     设置行令牌读取
// AppColorDots       颜色圆点组（选中描边环）(colordots/)

// —— G4 收编批次 ——
// MiniBarChart          迷你柱状图（指标卡内嵌）      (chart/)
// MiniLineChart         迷你折线图                    (chart/)
// AppKeyValueRow        键值行（label/value/caption）  (keyvaluerow/)
// AppOptionChipRow      key→label 单选胶囊行          (chip/)
// AppDateTimeField      只读时间字段+级联弹窗          (datetimecascade/)
// AppOptionPickerSheet  单选底部弹层                  (dialog/)
// AppTimerRow           计时器行                       (timer/)

// —— 专用组件 ——
// BabyIllustration   宝宝头像
// BadgeIcon          角标
// AppNavigationBar   底部导航（纯 UI）      (navigation/)
// AppNavigationItem  底部导航 Tab 数据
// CountdownChip      倒计时标签
// EmptyState         空状态
// EmptyStateDefaults 空状态令牌读取（TT-036）
// SkeletonLoader     骨架屏               (skeleton/)

// —— 交互组件 ——
// RecordCard         记录卡片（滑动删除+点击编辑）        (recordcard/)

// —— 动画 ——
// AnimatedListItem   列表项入场动画
// animateNumber      数字滚动动画

// —— 触觉反馈 ——
// rememberHaptic     获取 HapticFeedback 实例
// Modifier.longPressDeletable  长按删除 Modifier

// —— Hooks（core/hooks）——
// useDebounce        防抖
// useState           受控状态
// useLatestState     最新值引用

// —— 菜单（P0 五件套）——
// AppMenu             通用菜单（下拉/长按上下文共用）   (menu/)
// AppMenuItem         菜单项数据模型（key 泛型）

// —— 分页（P0 五件套）——
// AppPagination       数字分页器（首末常驻+省略号折叠） (pagination/)

// —— 步骤条（P0 五件套）——
// AppStepper          步骤条（水平/垂直方向轴，三态圆点）(stepper/)
// AppStep             步骤数据模型

// —— 下拉选择（P0 五件套）——
// AppSelect           下拉选择器（锚定形态；底部弹层形态走 AppOptionPickerSheet）(select/)

// —— 数据表格（P0 五件套）——
// AppDataTable        数据表格（列声明式 + 表头排序直连 TableLogic）(table/)
// AppTableColumn      列定义（weight/width/sortKey/cell 槽位）

// —— P1 增量 ——
// AppInput(style = Search)   搜索样式轴（SearchField 收敛归宿）      (input/)
// AppInput(suggestions)      自动补全槽位（锚定 AppMenu）           (input/)
// AppPinInput         验证码输入（吸收 Pin/OTP/Code）              (pininput/)
// AppTimeline         时间线（记录流形态，高亮节点放大档）          (timeline/)

// —— 工具 ——
// AppDefaults        非 Composable Token 快照
// AppStrings         国际化文案
