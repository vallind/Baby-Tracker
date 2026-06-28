package com.babytracker.designsystem.components.tag

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object TagDefaults {
    @Composable fun primary(): Pair<Color, Color> =
        LocalAppComponentTokens.current.tag.primary.let { it.backgroundColor to it.textColor }
    @Composable fun success(): Pair<Color, Color> =
        LocalAppComponentTokens.current.tag.success.let { it.backgroundColor to it.textColor }
    @Composable fun warning(): Pair<Color, Color> =
        LocalAppComponentTokens.current.tag.warning.let { it.backgroundColor to it.textColor }
    @Composable fun danger(): Pair<Color, Color> =
        LocalAppComponentTokens.current.tag.danger.let { it.backgroundColor to it.textColor }
    @Composable fun default(): Pair<Color, Color> =
        LocalAppComponentTokens.current.tag.default.let { it.backgroundColor to it.textColor }

    @Composable fun cornerRadius(): Dp = LocalAppComponentTokens.current.tag.cornerRadius
    @Composable fun fontSize(): TextUnit = LocalAppComponentTokens.current.tag.fontSize
    @Composable fun fontWeight(): FontWeight = LocalAppComponentTokens.current.tag.fontWeight
    @Composable fun horizontalPadding(): Dp = LocalAppComponentTokens.current.tag.horizontalPadding
    @Composable fun verticalPadding(): Dp = LocalAppComponentTokens.current.tag.verticalPadding
}
