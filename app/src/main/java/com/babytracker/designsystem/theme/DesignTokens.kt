package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Color

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

data class ThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val bg: Color,
    val card: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val pink: Color,
    val blue: Color,
    val green: Color,
    val yellow: Color,
    val purple: Color,
    val cyan: Color,
    val tagBg: Color,
    val tagText: Color,
    // —— 宝宝追踪专属辅助色 ——
    val accent: Color,           // 橙色辅助色（图标/提示，#FFA500）
    val accentLight: Color,      // 橙色浅背景
    val pageBg: Color,           // 页面背景（浅蓝 #E6F0FF）
    val cardShadow: Color,       // 卡片阴影色
    // —— 交互态色阶 ——
    val borderHover: Color,      // 边框 hover 态
    val borderFocus: Color,      // 边框 focus 态
    val textDisabled: Color,     // 禁用文本
    val bgHover: Color,          // 背景 hover 态
    val bgPressed: Color,        // 背景 pressed 态
)

data class AppTheme(
    val name: String,
    val colors: ThemeColors,
) {
    companion object {
        // —— pure 主题升级为"宝宝蓝"目标风格 ——
        val pure = AppTheme("pure", ThemeColors(
            primary = Color(0xFF4285F4),
            primaryLight = Color(0xFFE6F0FF),
            bg = Color(0xFFE6F0FF),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE0EAF5),
            textPrimary = Color(0xFF333333),
            textSecondary = Color(0xFF666666),
            textHint = Color(0xFFB0B0B0),
            divider = Color(0xFFE0EAF5),
            success = Color(0xFF4CAF50),
            warning = Color(0xFFFFA500),
            danger = Color(0xFFEF4444),
            pink = Color(0xFFFF8A9E),
            blue = Color(0xFF4285F4),
            green = Color(0xFF4CAF50),
            yellow = Color(0xFFFFD54F),
            purple = Color(0xFFA78BFA),
            cyan = Color(0xFF4DD0E1),
            tagBg = Color(0xFFFFF3E0),
            tagText = Color(0xFFFFA500),
            accent = Color(0xFFFFA500),
            accentLight = Color(0xFFFFF3E0),
            pageBg = Color(0xFFE6F0FF),
            cardShadow = Color(0xFFB0C4DE),
            borderHover = Color(0xFFD0D8E6),
            borderFocus = Color(0xFF4285F4),
            textDisabled = Color(0xFFC7C7CC),
            bgHover = Color(0xFFEAF1FB),
            bgPressed = Color(0xFFD6E4F5),
        ))

        val aurora = AppTheme("aurora", ThemeColors(
            primary = Color(0xFF7C6CF0),
            primaryLight = Color(0xFFE8E0FF),
            bg = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE4E4E7),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE4E4E7),
            success = Color(0xFF6EE7B7),
            warning = Color(0xFFFCD34D),
            danger = Color(0xFFF87171),
            pink = Color(0xFFC4B5FD),
            blue = Color(0xFF7C6CF0),
            green = Color(0xFF6EE7B7),
            yellow = Color(0xFFFCD34D),
            purple = Color(0xFFA78BFA),
            cyan = Color(0xFF67E8F9),
            tagBg = Color(0xFFF3E8FF),
            tagText = Color(0xFF8B5CF6),
            accent = Color(0xFFFCD34D),
            accentLight = Color(0xFFFEF9C3),
            pageBg = Color(0xFFF8F5FF),
            cardShadow = Color(0xFFD4CCEF),
            borderHover = Color(0xFFD4D0E0),
            borderFocus = Color(0xFF7C6CF0),
            textDisabled = Color(0xFFC7C7CC),
            bgHover = Color(0xFFF0EDF7),
            bgPressed = Color(0xFFE4DFF0),
        ))

        val warm = AppTheme("warm", ThemeColors(
            primary = Color(0xFFFF8A80),
            primaryLight = Color(0xFFFFE8E0),
            bg = Color(0xFFFFFBF7),
            card = Color(0xFFFFFBF7),
            cardBorder = Color(0xFFE8E0E0),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE8E0E0),
            success = Color(0xFF81C784),
            warning = Color(0xFFFFB74D),
            danger = Color(0xFFE57373),
            pink = Color(0xFFFF8A9E),
            blue = Color(0xFF90CAF9),
            green = Color(0xFF81C784),
            yellow = Color(0xFFFFE082),
            purple = Color(0xFFCE93D8),
            cyan = Color(0xFF80DEEA),
            tagBg = Color(0xFFFFE8E0),
            tagText = Color(0xFFE07060),
            accent = Color(0xFFFFB74D),
            accentLight = Color(0xFFFFF3E0),
            pageBg = Color(0xFFFFFBF7),
            cardShadow = Color(0xFFE8C5B8),
            borderHover = Color(0xFFE0D0D0),
            borderFocus = Color(0xFFFF8A80),
            textDisabled = Color(0xFFC7C7CC),
            bgHover = Color(0xFFFFF0ED),
            bgPressed = Color(0xFFFFE0D8),
        ))

        val sunny = AppTheme("sunny", ThemeColors(
            primary = Color(0xFFF5A623),
            primaryLight = Color(0xFFFFF3E0),
            bg = Color(0xFFFFFAF0),
            card = Color(0xFFFFFDF5),
            cardBorder = Color(0xFFF0E8D8),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFF0E8D8),
            success = Color(0xFF7CB342),
            warning = Color(0xFFFFB300),
            danger = Color(0xFFE57373),
            pink = Color(0xFFFFAB91),
            blue = Color(0xFF90CAF9),
            green = Color(0xFFAED581),
            yellow = Color(0xFFFFD54F),
            purple = Color(0xFFCE93D8),
            cyan = Color(0xFF80DEEA),
            tagBg = Color(0xFFFFF0E0),
            tagText = Color(0xFFE67A2E),
            accent = Color(0xFFE67A2E),
            accentLight = Color(0xFFFFF0E0),
            pageBg = Color(0xFFFFFAF0),
            cardShadow = Color(0xFFE8D5A8),
            borderHover = Color(0xFFE8DCC8),
            borderFocus = Color(0xFFF5A623),
            textDisabled = Color(0xFFC7C7CC),
            bgHover = Color(0xFFFFF5E6),
            bgPressed = Color(0xFFFFECD0),
        ))

        val night = AppTheme("night", ThemeColors(
            primary = Color(0xFF5C6BC0),
            primaryLight = Color(0xFF1A2744),
            bg = Color(0xFF12121F),
            card = Color(0xFF1E1E32),
            cardBorder = Color(0xFF2A2A3E),
            textPrimary = Color.White,
            textSecondary = Color(0xFF8E8E93),
            textHint = Color(0xFF666680),
            divider = Color(0xFF2A2A3E),
            success = Color(0xFF4DB6AC),
            warning = Color(0xFFFFB74D),
            danger = Color(0xFFE57373),
            pink = Color(0xFFE57373),
            blue = Color(0xFF5C6BC0),
            green = Color(0xFF4DB6AC),
            yellow = Color(0xFFFFB74D),
            purple = Color(0xFF9575CD),
            cyan = Color(0xFF4DD0E1),
            tagBg = Color(0xFF3A2020),
            tagText = Color(0xFFE57373),
            accent = Color(0xFFFFB74D),
            accentLight = Color(0xFF3A2E1E),
            pageBg = Color(0xFF12121F),
            cardShadow = Color(0xFF000000),
            borderHover = Color(0xFF3A3A50),
            borderFocus = Color(0xFF5C6BC0),
            textDisabled = Color(0xFF555570),
            bgHover = Color(0xFF252540),
            bgPressed = Color(0xFF2E2E50),
        ))

        val morandi = AppTheme("morandi", ThemeColors(
            primary = Color(0xFFB0BEC5),
            primaryLight = Color(0xFFECEFF1),
            bg = Color(0xFFFAFAFA),
            card = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFE8E8E8),
            textPrimary = Color(0xFF09090B),
            textSecondary = Color(0xFF71717A),
            textHint = Color(0xFFC7C7CC),
            divider = Color(0xFFE8E8E8),
            success = Color(0xFFA5D6A7),
            warning = Color(0xFFFFCC80),
            danger = Color(0xFFEF9A9A),
            pink = Color(0xFFE0B0B0),
            blue = Color(0xFFA0B0C0),
            green = Color(0xFFA0C0A0),
            yellow = Color(0xFFD0C0A0),
            purple = Color(0xFFC0B0D0),
            cyan = Color(0xFFA0D0D0),
            tagBg = Color(0xFFF5E8E8),
            tagText = Color(0xFFC09090),
            accent = Color(0xFFD0A878),
            accentLight = Color(0xFFF5EDE0),
            pageBg = Color(0xFFFAFAFA),
            cardShadow = Color(0xFFD8D0C8),
            borderHover = Color(0xFFD8D8D8),
            borderFocus = Color(0xFFB0BEC5),
            textDisabled = Color(0xFFC7C7CC),
            bgHover = Color(0xFFF2F2F2),
            bgPressed = Color(0xFFE8E8E8),
        ))

        val all = listOf(pure, aurora, warm, sunny, night, morandi)
    }
}
