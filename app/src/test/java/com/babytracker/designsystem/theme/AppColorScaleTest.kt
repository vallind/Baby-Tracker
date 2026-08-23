package com.babytracker.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 分档色板审计 — 守护 HeroUI 对标色板不被无意篡改、
 * 自定义主题种子生成的色阶单调可用、derive() 注入路径完整。
 */
class AppColorScaleTest {

    @Test
    fun `HeroUI 官方语义锚点值不得被篡改`() {
        val p = HeroUiPalettes
        // 抄录自 @heroui/theme 2.4.26 semantic 默认值
        assertEquals(Color(0xFF006FEE), p.blue.default)
        assertEquals(Color(0xFF7828C8), p.purple.default)
        assertEquals(Color(0xFF17C964), p.green.default)
        assertEquals(Color(0xFFF5A524), p.yellow.default)
        assertEquals(Color(0xFFF31260), p.red.default)
        assertEquals(Color(0xFF71717A), p.zinc.default)
    }

    @Test
    fun `官方色阶档位抽样校验`() {
        val blue = HeroUiPalettes.blue
        assertEquals(Color(0xFFCCE3FD), blue.shade100)   // 徽章浅底档
        assertEquals(Color(0xFF005BC4), blue.shade600)   // 前景强调档
        assertEquals(Color(0xFFE6F1FE), blue.shade50)
        assertEquals(Color(0xFF001731), blue.shade900)
    }

    @Test
    fun `pure 主题派生后主色档位与官方表完全一致`() {
        val c = AppTheme.pure.colors
        assertEquals(HeroUiPalettes.blue, c.primaryScale)
        assertEquals(HeroUiPalettes.red, c.dangerScale)
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
        // 中性阶梯恒为 zinc，不随主题漂移
        assertEquals(HeroUiPalettes.zinc, c.neutralScale)
        // 缺省容器色取主色 shade100（不透明），不再是 alpha 叠加
        assertEquals(c.primaryScale.shade100, c.primaryContainer)
    }

    @Test
    fun `surfaceMuted 亮暗主题不同且非透明`() {
        val light = AppColors.light()
        val dark = AppColors.dark()
        assertNotEquals(light.surfaceMuted, dark.surfaceMuted)
        assertTrue(light.surfaceMuted.alpha == 1f && dark.surfaceMuted.alpha == 1f)
        assertEquals(HeroUiPalettes.zinc.shade100, light.surfaceMuted)
        assertEquals(HeroUiPalettes.zinc.shade800, dark.surfaceMuted)
    }
}
