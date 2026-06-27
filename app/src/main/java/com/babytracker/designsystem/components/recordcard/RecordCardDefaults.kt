package com.babytracker.designsystem.components.recordcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.components.card.CardDefaults

object RecordCardDefaults {
    @Composable fun cornerRadius(): Dp = CardDefaults.cornerRadius()
    @Composable fun innerPadding(): Dp = CardDefaults.innerPadding()
    @Composable fun elevation(): Dp = CardDefaults.elevation()
}
