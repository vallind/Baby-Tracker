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
// AppCard            统一卡片
// AppTopBar          统一导航栏
// PrimaryButton      主按钮（填充）
// SecondaryButton    次按钮（描边）
// AppTextButton      文本按钮
// AppInput           统一输入框
// AppChip            标签
// AppFAB             浮动操作按钮
// SectionHeader      分区标题
// AppListItem        标准列表项
// AppIconButton      主题化图标按钮
// AppConfirmDialog   确认删除对话框
// AppBottomSheet     表单底部弹层
// AppSnackbar        撤销 Snackbar

// —— 专用组件 ——
// BabyIllustration   宝宝头像
// BadgeIcon          角标
// BottomNavBar       底部导航
// CountdownChip      倒计时标签
// EmptyState         空状态
// SkeletonLoader     骨架屏

// —— 交互组件 ——
// SwipeToDeleteContainer    左滑删除
// SwipeToEditContainer      右滑编辑
// SwipeToEditDeleteContainer 双方向滑动

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
