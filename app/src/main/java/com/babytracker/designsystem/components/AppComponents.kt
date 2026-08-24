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
// AppLinearProgress  线性进度条             (progress/)
// AppCircularProgress 圆形进度指示器         (progress/)
// AppSlider          主题化滑块             (slider/)
// AppLabeledSlider   带标签滑块             (slider/)
// AppRate            星级评分               (rate/)

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

// —— 工具 ——
// AppDefaults        非 Composable Token 快照
// AppStrings         国际化文案
