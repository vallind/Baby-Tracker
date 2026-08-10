package com.babytracker.designsystem.components.rate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.babytracker.i18n.AppStrings

/**
 * 星级评分组件 — 对标 Palette Rate，消费 AppComponentTokens.rate。
 *
 * 用法：
 *   AppRate(rating = 3, maxStars = 5, onRate = { newRating -> saveRating(newRating) })
 *   AppRate(rating = 4, maxStars = 5, readOnly = true)  // 只读展示
 */
@Composable
fun AppRate(
    rating: Int,
    maxStars: Int = 5,
    onRate: ((Int) -> Unit)? = null,
    readOnly: Boolean = onRate == null,
    starSize: Dp = RateDefaults.starSize(),
    starSpacing: Dp = RateDefaults.starSpacing(),
    selectedColor: Color = RateDefaults.selectedColor(),
    unselectedColor: Color = RateDefaults.unselectedColor(),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = String.format(AppStrings.rateDescription, rating, maxStars)
        },
        horizontalArrangement = Arrangement.spacedBy(starSpacing),
    ) {
        for (index in 1..maxStars) {
            val isSelected = index <= rating
            val tint = if (isSelected) selectedColor else unselectedColor

            if (readOnly) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(starSize).clearAndSetSemantics {}, // 只读星仅随容器朗读一次
                )
            } else {
                IconButton(
                    onClick = { onRate?.invoke(index) },
                    modifier = Modifier.size(starSize),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "评分 $index",
                        tint = tint,
                        modifier = Modifier.size(starSize),
                    )
                }
            }
        }
    }
}
