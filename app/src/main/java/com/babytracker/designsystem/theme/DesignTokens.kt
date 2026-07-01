package com.babytracker.designsystem.theme

/**
 * 设计 Token — 宝宝追踪 App 统一视觉规范
 *
 * 风格：母婴温馨简洁、扁平化、圆润、浅蓝主调
 * @deprecated 使用 AppTokens/AppComponentDefaults 下的对应令牌代替
 */
object DT {
    // —— 间距 ——
    val pageMargin = 20          // 页面边距
    val pageMarginSm = 16        // 紧凑页边距
    val cardGap = 16             // 卡片间距
    val cardGapSm = 12           // 紧凑卡片间距
    val cardInnerPadding = 16    // 卡片内边距

    // —— 圆角（圆润母婴风，比之前更大）——
    val cardRadius = 16          // 卡片圆角（原 8 → 16，更圆润）
    val cardRadiusLg = 24        // 大卡片圆角（首页概览卡等）
    val buttonRadius = 24        // 按钮圆角（胶囊感）
    val buttonRadiusSm = 16      // 小按钮圆角
    val inputRadius = 12         // 输入框圆角
    val chipRadius = 20          // 标签/Chip 圆角
    val iconBgRadius = 14        // 图标背景圆角

    // —— 尺寸 ——
    val iconSize = 22            // 标准图标
    val iconSizeSm = 18          // 小图标
    val iconSizeLg = 28          // 大图标
    val iconBgSize = 40          // 图标背景尺寸
    val iconBgSizeLg = 56        // 大图标背景（首页功能图标）
    val appBarHeight = 56        // 顶部栏高度
    val bottomBarHeight = 64     // 底部导航栏高度
    val cardElevation = 2        // 卡片阴影（轻微）
    val badgeSize = 18            // 角标尺寸
    val skeletonHeight = 16       // 骨架屏占位高度
    val countdownChipHeight = 28  // 倒计时标签高度
    val timelineLineWidth = 2     // 时间轴竖线宽度

    // —— 字号（sp）——
    val textSizeXs = 11
    val textSizeSm = 12
    val textSizeMd = 14
    val textSizeLg = 16
    val textSizeXl = 18
    val textSizeXxl = 22
    val textSizeTitle = 24
    val textSizeDisplay = 32
}
