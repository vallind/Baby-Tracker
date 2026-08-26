package com.babytracker.designsystem.components.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.theme.LocalAppColors

/**
 * 表面容器 —— 超级参照组件：Theme / Material / Shape / Elevation 四要素基准。
 *
 * 后续一切"容器类"组件（卡片、弹层、面板）的材质表达都以本组件的写法为模板：
 *   1. Theme     颜色/圆角/高度默认值全部来自 AppComponentTokens.surface，组件不持有视觉常量；
 *   2. Material  contentColor 经 M3 Surface 自动注入 LocalContentColor，子文本无需逐个上色；
 *   3. Shape     形状是参数而非内部常量，调用方可整体替换（胶囊/圆角/切角）；
 *   4. Elevation 双通道语义——tonalElevation 叠色层次（同底色加深），shadowElevation 投影层次。
 *
 * 暖阴影约定：投影颜色统一取 LocalAppColors.shadow（奶油底暖棕调），与 AppCard 同一写法；
 * 因此投影在本组件内自绘（Modifier.shadow + 暖色 ambient/spot），M3 通道归零，
 * 避免 M3 默认冷黑阴影在暖色主题里突兀。
 *
 * 用法：
 *   AppSurface { Text("令牌默认平面") }
 *   AppSurface(tonalElevation = AppSurfaceDefaults.tonalElevation() /* 层次叠色 */) { ... }
 *   AppSurface(
 *       shadowElevation = 8.dp,
 *       shape = RoundedCornerShape(28.dp),
 *   ) { ... }                                                // 悬浮暖投影面板
 *   AppSurface(border = BorderStroke(AppSurfaceDefaults.borderWidth(), AppSurfaceDefaults.borderColor())) { ... }
 *   AppSurface(onClick = { ... }) { ... }                    // 可点击表面（涟漪裁切进形状）
 */
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    color: Color = SurfaceDefaults.color(),
    contentColor: Color = SurfaceDefaults.contentColor(),
    shape: Shape = SurfaceDefaults.shape(),
    tonalElevation: Dp = SurfaceDefaults.tonalElevation(),
    shadowElevation: Dp = SurfaceDefaults.shadowElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    // 暖色调投影：自绘 shadow 并将 M3 通道归零（见 KDoc 约定）
    val warmShadow = LocalAppColors.current.shadow
    val shadowedModifier = if (shadowElevation > 0.dp) {
        modifier.shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = warmShadow,
            spotColor = warmShadow,
        )
    } else {
        modifier
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = shadowedModifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            shadowElevation = 0.dp,
            border = border,
            content = content,
        )
    } else {
        Surface(
            modifier = shadowedModifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            shadowElevation = 0.dp,
            border = border,
            content = content,
        )
    }
}
