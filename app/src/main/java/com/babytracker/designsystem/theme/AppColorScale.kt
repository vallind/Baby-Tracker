package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
//  分档色板 —「柔和奶油 + 多彩分区」设计语言
//
//  为什么需要分档：此前组件做"浅色底 + 深色前景"只能靠
//  primary.copy(alpha = 0.12f) 现场调，各处 alpha 不一致是
//  视觉不统一的根源之一。分档后统一取用约定：
//    浅底/徽章底   → shade100
//    选中底/hover  → shade200（或 shade100）
//    前景强调文字  → shade600（浅色模式）/ shade400（暗色模式）
//    主按钮容器    → default
//  禁止再用 .copy(alpha) 伪造浅档。
//
//  色板基调：
//    - 中性阶梯为暖灰 stone（米白→暖黑），替代冷灰 zinc，
//      让所有卡片/表面带奶油温度；
//    - 语义五色降饱和提柔和：蓝(indigo)/紫(violet)/绿(green)/
//      琥珀(amber)/珊瑚红(coral)，容器色自带粉彩底，
//      喂养=珊瑚、睡眠=紫、尿布=青、生长=绿、提醒=琥珀、AI=紫。
// ═══════════════════════════════════════════════════════════

/** 十档色阶 + 默认档。default 为该语义色的主值（对应档位 [500]） */
@Immutable
data class AppColorScale(
    val default: Color,
    val shade50: Color,
    val shade100: Color,
    val shade200: Color,
    val shade300: Color,
    val shade400: Color,
    val shade500: Color,
    val shade600: Color,
    val shade700: Color,
    val shade800: Color,
    val shade900: Color,
) {
    companion object {
        /**
         * 从任意品牌种子色生成整条色阶（供自定义主题使用）：
         * 亮档向白混合、暗档向黑混合，权重参照常规 ramp 设计。
         * 旗舰主题（pure/night）请直接使用 SoftPalettes 官方表保证一致。
         */
        fun fromSeed(seed: Color): AppColorScale = AppColorScale(
            default = seed,
            shade50 = seed.mixToward(Color.White, 0.92f),
            shade100 = seed.mixToward(Color.White, 0.84f),
            shade200 = seed.mixToward(Color.White, 0.68f),
            shade300 = seed.mixToward(Color.White, 0.52f),
            shade400 = seed.mixToward(Color.White, 0.28f),
            shade500 = seed,
            shade600 = seed.mixToward(Color.Black, 0.16f),
            shade700 = seed.mixToward(Color.Black, 0.34f),
            shade800 = seed.mixToward(Color.Black, 0.55f),
            shade900 = seed.mixToward(Color.Black, 0.75f),
        )

        private fun Color.mixToward(other: Color, weight: Float): Color = Color(
            red = red * (1 - weight) + other.red * weight,
            green = green * (1 - weight) + other.green * weight,
            blue = blue * (1 - weight) + other.blue * weight,
            alpha = 1f,
        )
    }
}

/** 五个语义色的分档集合，作为 derive() 的可选注入项 */
@Immutable
data class AppSemanticScales(
    val primary: AppColorScale,
    val secondary: AppColorScale,
    val success: AppColorScale,
    val warning: AppColorScale,
    val danger: AppColorScale,
)

/**
 * 官方柔和色板常量 —「柔和奶油 + 多彩分区」设计语言的锚点色阶。
 * 色值按现代母婴 App 常见取色人工校准（亮档粉彩、中间档主色、暗档加深），
 * 仅本文件允许出现这些硬编码值（designsystem/theme 为 detekt HardcodedColor 白名单层）。
 */
public object SoftPalettes {

    /** 品牌蓝（indigo）：主按钮/链接/选中态 */
    val blue = AppColorScale(
        default = Color(0xFF3B6FE0),
        shade50 = Color(0xFFEDF2FE),
        shade100 = Color(0xFFDCE6FD),
        shade200 = Color(0xFFB9CCFB),
        shade300 = Color(0xFF96B3F9),
        shade400 = Color(0xFF6F96F6),
        shade500 = Color(0xFF3B6FE0),
        shade600 = Color(0xFF2C57BE),
        shade700 = Color(0xFF204396),
        shade800 = Color(0xFF172F6E),
        shade900 = Color(0xFF0E1D46),
    )

    /** 睡眠紫（violet）：夜间睡眠/小睡/发育/AI 辅助 */
    val violet = AppColorScale(
        default = Color(0xFF8B7BF0),
        shade50 = Color(0xFFF2F0FE),
        shade100 = Color(0xFFE6E2FD),
        shade200 = Color(0xFFCDC5FB),
        shade300 = Color(0xFFB3A8F8),
        shade400 = Color(0xFF9C8DF5),
        shade500 = Color(0xFF8B7BF0),
        shade600 = Color(0xFF6F5BDD),
        shade700 = Color(0xFF5743B8),
        shade800 = Color(0xFF3F2F8C),
        shade900 = Color(0xFF271C59),
    )

    /** 生长绿（green）：生长记录/成功回执 */
    val green = AppColorScale(
        default = Color(0xFF34A96F),
        shade50 = Color(0xFFECF9F2),
        shade100 = Color(0xFFD9F2E4),
        shade200 = Color(0xFFB2E5C9),
        shade300 = Color(0xFF8BD8AF),
        shade400 = Color(0xFF5DC893),
        shade500 = Color(0xFF34A96F),
        shade600 = Color(0xFF278A59),
        shade700 = Color(0xFF1D6B44),
        shade800 = Color(0xFF144C30),
        shade900 = Color(0xFF0C2E1C),
    )

    /** 提醒琥珀（amber）：提醒/成长月龄/警示类强调 */
    val amber = AppColorScale(
        default = Color(0xFFE8930C),
        shade50 = Color(0xFFFEF7EA),
        shade100 = Color(0xFFFCEDD3),
        shade200 = Color(0xFFF9DBA7),
        shade300 = Color(0xFFF5C475),
        shade400 = Color(0xFFF0AC43),
        shade500 = Color(0xFFE8930C),
        shade600 = Color(0xFFC0760A),
        shade700 = Color(0xFF925908),
        shade800 = Color(0xFF644005),
        shade900 = Color(0xFF332003),
    )

    /** 喂养珊瑚红（coral）：母乳/辅食标识，兼作错误/删除语义色 */
    val coral = AppColorScale(
        default = Color(0xFFE85D5D),
        shade50 = Color(0xFFFEF0EF),
        shade100 = Color(0xFFFCE0DF),
        shade200 = Color(0xFFF8C1C0),
        shade300 = Color(0xFFF3A0A0),
        shade400 = Color(0xFFED7D7E),
        shade500 = Color(0xFFE85D5D),
        shade600 = Color(0xFFC94343),
        shade700 = Color(0xFFA03131),
        shade800 = Color(0xFF702222),
        shade900 = Color(0xFF3D1010),
    )

    /** 尿布青（teal）：尿布记录的专属色（供 tertiary 单点色参考） */
    val teal = AppColorScale(
        default = Color(0xFF2FB8C6),
        shade50 = Color(0xFFECFAFB),
        shade100 = Color(0xFFD8F4F6),
        shade200 = Color(0xFFB0E9ED),
        shade300 = Color(0xFF87DDE3),
        shade400 = Color(0xFF5CCDD6),
        shade500 = Color(0xFF2FB8C6),
        shade600 = Color(0xFF2196A3),
        shade700 = Color(0xFF197380),
        shade800 = Color(0xFF124F58),
        shade900 = Color(0xFF0A2D33),
    )

    /** 中性阶梯（stone 暖灰），亮暗主题共用；default 取 stone-500 */
    val stone = AppColorScale(
        default = Color(0xFF78716C),
        shade50 = Color(0xFFFAFAF9),
        shade100 = Color(0xFFF5F5F4),
        shade200 = Color(0xFFE7E5E4),
        shade300 = Color(0xFFD6D3D1),
        shade400 = Color(0xFFA8A29E),
        shade500 = Color(0xFF78716C),
        shade600 = Color(0xFF57534E),
        shade700 = Color(0xFF44403C),
        shade800 = Color(0xFF292524),
        shade900 = Color(0xFF1C1917),
    )

    /** 旗舰主题的语义五色组合（primary=blue / secondary=violet / success=green / warning=amber / danger=coral） */
    val softSemantics = AppSemanticScales(
        primary = blue,
        secondary = violet,
        success = green,
        warning = amber,
        danger = coral,
    )
}

// ═══════════════════════════════════════════════════════════
//  意图取档 — 组件按"用途"取色，不按固定档位硬编码，
//  暗色主题自动倒序（对标 HeroUI swapColorValues 策略）
// ═══════════════════════════════════════════════════════════

/** 当前主题是否暗色（判定口径与 AppColors.derive 一致：surface 亮度） */
val AppColors.isDarkTheme: Boolean get() = surface.luminance() < 0.5f

/** 浅底容器档（图标徽章底/选中底）：亮色 shade100，暗色 shade800 */
fun AppColorScale.tintContainer(colors: AppColors): Color =
    if (colors.isDarkTheme) shade800 else shade100

/** 强调前景档（选中文字/强调数值）：亮色 shade600，暗色 shade400 提亮 */
fun AppColorScale.accentContent(colors: AppColors): Color =
    if (colors.isDarkTheme) shade400 else shade600