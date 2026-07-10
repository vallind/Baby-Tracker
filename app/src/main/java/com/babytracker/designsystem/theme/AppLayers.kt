package com.babytracker.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
//  Z 轴层级系统 — 统一视觉深度规范
//
//  模型：
//    第 0 层 — 背景/底层（脚手架、页面背景）
//    第 1 层 — 表面层（卡片、应用栏、底部导航、列表项）
//    第 2 层 — 悬浮层（底部表单、模态对话框、弹出菜单、抽屉）
//    第 3 层 — 模态层（全屏对话框、键盘上方覆盖、锁屏覆盖）
//    状态层 — 状态栏、系统导航、刘海/挖孔适配
//
//  用法：
//    val layer = LocalAppLayers.current
//    Box(modifier = Modifier.shadow(layer.surface.elevation)) { ... }
//    // 或直接用语义值：
//    elevation = AppLayers.card      // 第 1 层
//    elevation = AppLayers.modal     // 第 3 层
// ═══════════════════════════════════════════════════════════

/** 每层的遮罩透明度（覆盖在下一层之上） */
@Immutable
data class LayerScrim(
    /** 第 1 → 2 层：底部表单/菜单弹出时背景遮罩 */
    val float: Float = 0.28f,
    /** 第 2 → 3 层：模态对话框/全屏弹窗遮罩 */
    val modal: Float = 0.45f,
    /** 第 0 层之上：导航抽屉遮罩 */
    val drawer: Float = 0.32f,
)

/** 每层的表面色调叠加 — 亮色下变亮，暗色下变暗 */
@Immutable
data class LayerTonal(
    /** 第 0 层：基底，不叠加 */
    val base: Float = 0.00f,
    /** 第 1 层：卡片级，轻微叠加 */
    val surface: Float = 0.00f,
    /** 第 2 层：悬浮级，明显叠加 */
    val elevated: Float = 0.05f,
    /** 第 3 层：模态级，最亮/最暗 */
    val modal: Float = 0.08f,
)

/** 每层的阴影配置 */
@Immutable
data class LayerShadow(
    val elevation: Dp,
    val ambientAlpha: Float = 0.12f,
    val spotAlpha: Float = 0.14f,
)

/**
 * 完整层级定义
 *
 * 通过 `LocalAppLayers` 注入，可在任意 Composable 中读取。
 * 也提供顶层常量（AppLayers.card / AppLayers.modal 等）供非 Composable 场景使用。
 */
@Immutable
data class AppLayers(
    /** 第 0 层：背景/底层 — 0dp */
    val background: LayerShadow = LayerShadow(elevation = 0.dp),
    /** 第 1 层：表面层 — 1dp（卡片、列表项、应用栏） */
    val surface: LayerShadow = LayerShadow(elevation = 1.dp),
    /** 第 1 层·抬升：表面按压态 — 3dp */
    val surfacePressed: LayerShadow = LayerShadow(elevation = 3.dp),
    /** 第 2 层：悬浮层 — 6dp（底部表单、弹出菜单、FAB） */
    val float: LayerShadow = LayerShadow(elevation = 6.dp),
    /** 第 2 层·较高悬浮 — 8dp（对话框、日期选择器） */
    val floatHigh: LayerShadow = LayerShadow(elevation = 8.dp),
    /** 第 3 层：模态层 — 12dp（全屏对话框、锁屏覆盖） */
    val modal: LayerShadow = LayerShadow(elevation = 12.dp),
    /** 每层遮罩透明度 */
    val scrim: LayerScrim = LayerScrim(),
    /** 每层表面色调叠加 */
    val tonal: LayerTonal = LayerTonal(),
) {
    companion object {
        /** 默认层级配置 */
        val default = AppLayers()

        /** 快速访问常用层 shadow elevation */
        val card: Dp get() = default.surface.elevation
        val cardPressed: Dp get() = default.surfacePressed.elevation
        val fab: Dp get() = default.float.elevation
        val bottomSheet: Dp get() = default.float.elevation
        val dialog: Dp get() = default.floatHigh.elevation
        val menu: Dp get() = default.float.elevation
        val modalFullscreen: Dp get() = default.modal.elevation
    }
}

val LocalAppLayers = staticCompositionLocalOf { AppLayers.default }
