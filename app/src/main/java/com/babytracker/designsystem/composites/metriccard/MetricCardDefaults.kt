package com.babytracker.designsystem.composites.metriccard

import androidx.compose.runtime.Composable
import com.babytracker.designsystem.i18n.AppStrings

/** 指标卡默认值 —— 几何与颜色复用既有令牌体系，此处仅聚合文案回落 */
object MetricCardDefaults {
    @Composable fun chartEmptyFallback(): String = AppStrings.noData
}
