package com.babytracker.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.babytracker.designsystem.theme.CareType
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppShapes
import com.babytracker.designsystem.theme.LocalCareTypePalette
import com.babytracker.navigation.Screen

private data class QuickRecordItem(
    val type: CareType,
    val screen: Screen,
)

private val quickRecords = listOf(
    QuickRecordItem(CareType.FEEDING, Screen.Feeding),
    QuickRecordItem(CareType.SLEEP, Screen.Sleep),
    QuickRecordItem(CareType.DIAPER, Screen.Diaper),
    QuickRecordItem(CareType.GROWTH, Screen.Growth),
)

@Composable
fun QuickRecordBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val palette = LocalCareTypePalette.current
    val c = LocalAppColors.current
    val shapes = LocalAppShapes.current

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        quickRecords.forEach { item ->
            val visuals = palette.of(item.type)
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(shapes.medium))
                    .background(visuals.container.copy(alpha = 0.08f))
                    .clickable {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(visuals.emoji, fontSize = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    visuals.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = visuals.content,
                )
            }
        }
    }
}
