package com.babytracker.designsystem.components.tilegrid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.babytracker.designsystem.components.tilegrid.TileGridDefaults as AppTileGridDefaults
import com.babytracker.designsystem.theme.AppColorScale
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.isDarkTheme

/**
 * 宫格单元描述 — key 供点击回传后由调用方反查业务对象（如枚举名），
 * seedColor 驱动分档渐变色块；emoji/label 为展示内容。
 */
data class AppTileSpec(
    val key: String,
    val emoji: String,
    val label: String,
    val seedColor: Color,
)

/**
 * 功能宫格 — 「emoji 渐变色块 + 标题」的等宽宫格（首页功能分区形态收编）。
 *
 * 按 [columns] 切行，行内 Row+weight(1f) 等分，末行不满也左对齐；
 * 色块用 `AppColorScale.fromSeed(seedColor)` 分档垂直渐变：
 * 亮色 shade100→shade200 / 暗色 shade800→shade700。
 * 页边距与首行上方留白留在调用方 modifier/布局，组件只管格子本体。
 *
 * 用法：
 *   AppTileGrid(tiles = listOf(AppTileSpec("Feeding", "🍼", "喂养", c.danger)), onTileClick = { ... })
 */
@Composable
fun AppTileGrid(
    tiles: List<AppTileSpec>,
    onTileClick: (AppTileSpec) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(AppTileGridDefaults.crossAxisSpacing()),
    ) {
        tiles.chunked(columns).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppTileGridDefaults.crossAxisSpacing())) {
                rowTiles.forEach { tile ->
                    TileCell(tile, onTileClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TileCell(
    tile: AppTileSpec,
    onTileClick: (AppTileSpec) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val scale = AppColorScale.fromSeed(tile.seedColor)
    // 亮色：粉彩渐变（shade100→shade200）；暗色：深彩渐变（shade800→shade700）
    val tileBrush = Brush.verticalGradient(
        if (c.isDarkTheme) listOf(scale.shade800, scale.shade700)
        else listOf(scale.shade100, scale.shade200),
    )
    Column(
        modifier
            .clip(RoundedCornerShape(AppTileGridDefaults.cellCornerRadius()))
            .clickable { onTileClick(tile) }
            .padding(vertical = AppTileGridDefaults.cellVerticalPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(AppTileGridDefaults.tileSize())
                .clip(RoundedCornerShape(AppTileGridDefaults.tileCornerRadius()))
                .background(tileBrush),
            contentAlignment = Alignment.Center,
        ) {
            Text(tile.emoji, fontSize = AppTileGridDefaults.emojiFontSize())
        }
        Spacer(Modifier.height(AppTileGridDefaults.labelTopGap()))
        Text(
            tile.label,
            style = AppTileGridDefaults.labelTextStyle(),
            color = AppTileGridDefaults.labelColor(),
        )
    }
}
