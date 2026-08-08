package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.babytracker.designsystem.theme.LocalAppColors

enum class BabyPose {
    SITTING, SLEEPING, PLAYING, CRAWLING
}

@Composable
fun BabyIllustration(
    pose: BabyPose = BabyPose.SITTING,
    size: Int = 140,
    name: String = "",
    color: Color? = null,
) {
    val c = LocalAppColors.current
    val bgColor = color ?: c.primaryContainer

    Box(
        // 纯装饰插图，整块对读屏静默，避免 emoji 逐个朗读
        modifier = Modifier.size(size.dp).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(bgColor, bgColor.copy(alpha = 0.6f))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (pose) {
                BabyPose.SITTING -> SittingBaby(name)
                BabyPose.SLEEPING -> SleepingBaby()
                BabyPose.PLAYING -> PlayingBaby()
                BabyPose.CRAWLING -> CrawlingBaby()
            }
        }
        // Decorative ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Transparent)
                .then(
                    Modifier
                        .size((size - 12).dp)
                        .clip(CircleShape)
                        .background(bgColor.copy(alpha = 0.15f))
                )
        )
    }
}

@Composable
private fun SittingBaby(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp),
    ) {
        Text("🧒", fontSize = 48.sp)
        if (name.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                name.take(1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SleepingBaby() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😴", fontSize = 52.sp)
            Spacer(Modifier.height(4.dp))
            Text("💤", fontSize = 20.sp)
        }
    }
}

@Composable
private fun PlayingBaby() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🧸", fontSize = 28.sp)
            Spacer(Modifier.width(4.dp))
            Text("👶", fontSize = 44.sp)
            Spacer(Modifier.width(4.dp))
            Text("🎨", fontSize = 24.sp)
        }
    }
}

@Composable
private fun CrawlingBaby() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👶", fontSize = 48.sp)
            Text("🚀", fontSize = 16.sp)
        }
    }
}
