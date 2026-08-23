package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 分档色板审计 — 守护「柔和奶油」色板锚点不被无意篡改、
 * 自定义主题种子生成的色阶单调可用、derive() 注入路径完整。
 */
class AppColorScaleTest {

    @Test
    fun `柔和色板语义锚点值不得被篡改`() {
        val p = SoftPalettes
        // 「柔和奶油 + 多彩分区」设计语言的锚点主值
        assertEquals(Color(0xFF3B6FE0), p.blue.default)
        assertEquals(Color(0xFF8B7BF0), p.violet.default)
        assertEquals(Color(0xFF34A96F), p.green.default)
        assertEquals(Color(0xFFE8930C), p.amber.default)
        assertEquals(Color(0xFFE85D5D), p.coral.default)
        assertEquals(Color(0xFF78716C), p.stone.default)
    }

    @Test
    fun `官方色阶档位抽样校验`() {
        val blue = SoftPalettes.blue
        assertEquals(Color(0xFFDCE6FD), blue.shade100)   // 徽章浅底档
        assertEquals(Color(0xFF2C57BE), blue.shade600)   // 前景强调档
        assertEquals(Color(0xFFEDF2FE), blue.shade50)
        assertEquals(Color(0xFF0E1D46), blue.shade900)
    }

    @Test
    fun `pure 主题派生后主色档位与官方表完全一致`() {
        val c = AppTheme.pure.colors
        assertEquals(SoftPalettes.blue, c.primaryScale)
        assertEquals(SoftPalettes.coral, c.dangerScale)
        // danger 单点色与档位 default 必须同源，禁止出现两套红
        assertEquals(c.dangerScale.default, c.error)
    }

    @Test
    fun `fromSeed 生成的色阶亮度必须单调且全不透明`() {
        val seed = Color(0xFF7C6CF0)   // aurora 品牌紫，走生成路径的典型场景
        val s = AppColorScale.fromSeed(seed)

        val ramp = listOf(s.shade50, s.shade100, s.shade200, s.shade300, s.shade400, s.shade500, s.shade600, s.shade700, s.shade800, s.shade900)
        // 全不透明：分档底色不允许带透明度（旧 alpha 叠加方案的替代品）
        assertTrue("色阶不得含透明度: $ramp", ramp.all { it.alpha == 1f })
        // 亮度单调递减：shade50 最亮 → shade900 最暗
        val luminances = ramp.map { 0.299f * it.red + 0.587f * it.green + 0.114f * it.blue }
        assertTrue("亮度必须单调递减: $luminances", luminances.zipWithNext().all { (a, b) -> a >= b })
        assertEquals(seed, s.default)
        assertEquals(seed, s.shade500)
    }

    @Test
    fun `derive 未注入 scales 时从种子自动生成且与单点色同源`() {
        val primary = Color(0xFFFF8A80)   // warm 主题粉，走生成路径
        val warning = Color(0xFFE67A2E)
        val c = AppColors.derive(primary = primary, warning = warning)

        assertEquals(primary, c.primaryScale.default)
        assertEquals(warning, c.warningScale.default)
        // 中性阶梯恒为 stone 暖灰，不随主题漂移
        assertEquals(SoftPalettes.stone, c.neutralScale)
        // 缺省容器色取主色 shade100（不透明），不再是 alpha 叠加
        assertEquals(c.primaryScale.shade100, c.primaryContainer)
    }

    @Test
    fun `surfaceMuted 亮暗主题不同且非透明`() {
        val light = AppColors.light()
        val dark = AppColors.dark()
        assertNotEquals(light.surfaceMuted, dark.surfaceMuted)
        assertTrue(light.surfaceMuted.alpha == 1f && dark.surfaceMuted.alpha == 1f)
        assertEquals(SoftPalettes.stone.shade100, light.surfaceMuted)
        assertEquals(SoftPalettes.stone.shade800, dark.surfaceMuted)
    }
}
