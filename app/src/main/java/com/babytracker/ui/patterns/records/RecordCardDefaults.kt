package com.babytracker.ui.patterns.records

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object RecordCardDefaults {
    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.card.cornerRadius
    @Composable fun innerPadding(): Dp = LocalAppComponentTokens.current.card.innerPadding
    @Composable fun elevation(): Dp = LocalAppComponentTokens.current.card.elevation
}