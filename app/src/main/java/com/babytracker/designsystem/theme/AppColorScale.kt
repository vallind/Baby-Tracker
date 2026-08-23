package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
//  分档色板 — 对标 HeroUI semantic scale（@heroui/theme 2.4.26）
//
//  为什么需要分档：此前组件做"浅色底 + 深色前景"只能靠
//  primary.copy(alpha = 0.12f) 现场调，各处 alpha 不一致是
//  视觉不统一的根源之一。分档后统一取用约定：
//    浅底/徽章底   → shade100
//    选中底/hover  → shade200（或 shade100）
//    前景强调文字  → shade600（浅色模式）/ default（暗色模式）
//    主按钮容器    → default
//  禁止再用 .copy(alpha) 伪造浅档。
// ═══════════════════════════════════════════════════════════

/** 十档色阶 + 默认档。default 为该语义色的主值（对应 HeroUI [500] 档） */
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
         * 旗舰主题（pure/night）请直接使用 HeroUI 官方表保证像素级对齐。
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
 * HeroUI 官方色阶常量 — 逐 hex 抄录自 @heroui/theme 2.4.26 源码
 * （packages/core/theme/src/colors/ 目录各色表文件），仅本文件允许出现这些硬编码值
 * （designsystem/theme 为 detekt HardcodedColor 白名单层）。
 */
public object HeroUiPalettes {

    val blue = AppColorScale(
        default = Color(0xFF006FEE),
        shade50 = Color(0xFFE6F1FE),
        shade100 = Color(0xFFCCE3FD),
        shade200 = Color(0xFF99C7FB),
        shade300 = Color(0xFF66AAF9),
        shade400 = Color(0xFF338EF7),
        shade500 = Color(0xFF006FEE),
        shade600 = Color(0xFF005BC4),
        shade700 = Color(0xFF004493),
        shade800 = Color(0xFF002E62),
        shade900 = Color(0xFF001731),
    )

    val purple = AppColorScale(
        default = Color(0xFF7828C8),
        shade50 = Color(0xFFF2EAFA),
        shade100 = Color(0xFFE4D4F4),
        shade200 = Color(0xFFC9A9E9),
        shade300 = Color(0xFFAE7EDE),
        shade400 = Color(0xFF9353D3),
        shade500 = Color(0xFF7828C8),
        shade600 = Color(0xFF6020A0),
        shade700 = Color(0xFF481878),
        shade800 = Color(0xFF301050),
        shade900 = Color(0xFF180828),
    )

    val green = AppColorScale(
        default = Color(0xFF17C964),
        shade50 = Color(0xFFE8FAF0),
        shade100 = Color(0xFFD1F4E0),
        shade200 = Color(0xFFA2E9C1),
        shade300 = Color(0xFF74DFA2),
        shade400 = Color(0xFF45D483),
        shade500 = Color(0xFF17C964),
        shade600 = Color(0xFF12A150),
        shade700 = Color(0xFF0E793C),
        shade800 = Color(0xFF095028),
        shade900 = Color(0xFF052814),
    )

    val yellow = AppColorScale(
        default = Color(0xFFF5A524),
        shade50 = Color(0xFFFEFCE8),
        shade100 = Color(0xFFFDEDD3),
        shade200 = Color(0xFFFBDBA7),
        shade300 = Color(0xFFF9C97C),
        shade400 = Color(0xFFF7B750),
        shade500 = Color(0xFFF5A524),
        shade600 = Color(0xFFC4841D),
        shade700 = Color(0xFF936316),
        shade800 = Color(0xFF62420E),
        shade900 = Color(0xFF312107),
    )

    val red = AppColorScale(
        default = Color(0xFFF31260),
        shade50 = Color(0xFFFEE7EF),
        shade100 = Color(0xFFFDD0DF),
        shade200 = Color(0xFFFAA0BF),
        shade300 = Color(0xFFF871A0),
        shade400 = Color(0xFFF54180),
        shade500 = Color(0xFFF31260),
        shade600 = Color(0xFFC20E4D),
        shade700 = Color(0xFF920B3A),
        shade800 = Color(0xFF610726),
        shade900 = Color(0xFF310413),
    )

    /** 中性阶梯（zinc），亮暗主题共用；default 取 zinc-500 */
    val zinc = AppColorScale(
        default = Color(0xFF71717A),
        shade50 = Color(0xFFFAFAFA),
        shade100 = Color(0xFFF4F4F5),
        shade200 = Color(0xFFE4E4E7),
        shade300 = Color(0xFFD4D4D8),
        shade400 = Color(0xFFA1A1AA),
        shade500 = Color(0xFF71717A),
        shade600 = Color(0xFF52525B),
        shade700 = Color(0xFF3F3F46),
        shade800 = Color(0xFF27272A),
        shade900 = Color(0xFF18181B),
    )

    /** HeroUI 旗舰主题的官方语义五色组合（primary=blue / secondary=purple / …） */
    val officialSemantics = AppSemanticScales(
        primary = blue,
        secondary = purple,
        success = green,
        warning = yellow,
        danger = red,
    )
}
