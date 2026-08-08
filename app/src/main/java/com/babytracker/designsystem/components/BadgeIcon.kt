package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.babytracker.designsystem.theme.LocalAppColors
// DT 已弃用，inline 18.dp 替代 DT.badgeSize.dp

@Composable
fun BadgeIcon(
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        content()
        if (count > 0) {
            val text = if (count > 99) "99+" else count.toString()
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(LocalAppColors.current.danger),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text,
                    color = Color.White,
                    fontSize = if (count > 99) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    // 角标数字对读屏无意义，静默处理
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}
