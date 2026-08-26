package com.babytracker.designsystem.components.stepper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.stepper.StepperDefaults as AppStepperDefaults
import com.babytracker.designsystem.i18n.AppStrings
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppMotion
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/** 步骤条方向轴（收敛蓝图：StepIndicator 与 Stepper 收敛为一个组件的方向参数） */
enum class AppStepperDirection { Horizontal, Vertical }

/** 步骤数据模型 */
data class AppStep(
    val title: String,
    val supportingText: String? = null,
)

/** 步骤三态（纯状态机，可 JVM 单测） */
enum class AppStepState { Completed, Current, Upcoming }

/** 状态推导：currentStep 越界自动钳制到 [0, count-1] */
internal fun stepStates(count: Int, currentStep: Int): List<AppStepState> {
    require(count >= 1) { "steps 必须 ≥ 1" }
    val current = currentStep.coerceIn(0, count - 1)
    return List(count) { i ->
        when {
            i < current -> AppStepState.Completed
            i == current -> AppStepState.Current
            else -> AppStepState.Upcoming
        }
    }
}

/**
 * 步骤条 —— 消费 AppComponentTokens.steps（P0 五件套之一，参照组件范式）。
 *
 * State 轴：Completed / Current / Upcoming 三态；圆点、连接线、文字色全部由 StepsTokens 驱动；
 * Motion 轴：当前步圆点放大反馈走 LocalAppMotion（时长/缓动令牌，MotionHardcodedDuration 守门）；
 * 语义：已完成/进行中/未开始三态描述走 AppStrings；可回跳的已完成步声明 Button 角色。
 *
 * 交互约定：仅"已完成"步骤可点击回跳（Upcoming 未解锁），onStepClick 为空则整条只读。
 *
 * 用法：
 *   AppStepper(
 *       steps = listOf(AppStep("填写"), AppStep("确认"), AppStep("完成")),
 *       currentStep = step,
 *   )
 *   AppStepper(steps = steps, currentStep = step, direction = AppStepperDirection.Vertical)
 */
@Composable
fun AppStepper(
    steps: List<AppStep>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    direction: AppStepperDirection = AppStepperDirection.Horizontal,
    onStepClick: ((Int) -> Unit)? = null,
) {
    val states = stepStates(steps.size, currentStep)
    when (direction) {
        AppStepperDirection.Horizontal -> Row(modifier, verticalAlignment = Alignment.Top) {
            steps.forEachIndexed { i, step ->
                StepCell(
                    step = step,
                    state = states[i],
                    clickable = onStepClick != null && states[i] == AppStepState.Completed,
                    onClick = onStepClick?.let { cb -> { cb(i) } },
                )
                if (i < steps.lastIndex) {
                    HConnector(
                        isCompleted = states[i] == AppStepState.Completed,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        AppStepperDirection.Vertical -> Column(modifier) {
            steps.forEachIndexed { i, step ->
                VStepCell(
                    step = step,
                    state = states[i],
                    clickable = onStepClick != null && states[i] == AppStepState.Completed,
                    onClick = onStepClick?.let { cb -> { cb(i) } },
                )
                if (i < steps.lastIndex) {
                    VConnector(isCompleted = states[i] == AppStepState.Completed)
                }
            }
        }
    }
}

/** 水平连接线：宽度弹性填充，颜色随左侧步骤是否已完成 */
@Composable
private fun HConnector(isCompleted: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = LocalAppSpacing.current.sm)
            .height(AppStepperDefaults.connectorThickness())
            .clip(CircleShape)
            .background(connectorColor(isCompleted)),
    )
}

/** 垂直连接线：高度走间距令牌 */
@Composable
private fun VConnector(isCompleted: Boolean) {
    Box(
        Modifier
            .padding(start = LocalAppSpacing.current.lg)
            .height(LocalAppSpacing.current.lg)
            .width(AppStepperDefaults.connectorThickness())
            .clip(CircleShape)
            .background(connectorColor(isCompleted)),
    )
}

@Composable
private fun connectorColor(isCompleted: Boolean): Color =
    if (isCompleted) AppStepperDefaults.completedColor() else AppStepperDefaults.inactiveColor()

/** 水平步单元：圆点在上，文案在下居中 */
@Composable
private fun StepCell(
    step: AppStep,
    state: AppStepState,
    clickable: Boolean,
    onClick: (() -> Unit)?,
) {
    Column(
        stepCellModifier(state, clickable, onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepDot(state)
        StepTexts(step, state, horizontal = true)
    }
}

/** 垂直步单元：圆点在左，文案在右 */
@Composable
private fun VStepCell(
    step: AppStep,
    state: AppStepState,
    clickable: Boolean,
    onClick: (() -> Unit)?,
) {
    Row(
        stepCellModifier(state, clickable, onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepDot(state)
        Column(Modifier.padding(start = LocalAppSpacing.current.md)) {
            StepTexts(step, state)
        }
    }
}

private fun stepCellModifier(state: AppStepState, clickable: Boolean, onClick: (() -> Unit)?): Modifier =
    Modifier
        .then(
            if (clickable && onClick != null) {
                Modifier.clickable(role = Role.Button, onClick = onClick)
            } else {
                Modifier
            }
        )
        .semantics { stateDescription = stepStateText(state) }

@Composable
private fun StepDot(state: AppStepState) {
    val colors = LocalAppColors.current
    val motion = LocalAppMotion.current

    // Motion 参照：当前步圆点放大反馈；时长/缓动一律走令牌
    val dotScale by animateFloatAsState(
        targetValue = if (state == AppStepState.Current) 1.35f else 1f,
        animationSpec = tween(durationMillis = motion.duration.fast, easing = motion.easing.standard),
        label = "appStepperDotScale",
    )

    val fillColor = when (state) {
        AppStepState.Completed -> AppStepperDefaults.completedColor()
        AppStepState.Current -> AppStepperDefaults.activeColor()
        AppStepState.Upcoming -> colors.surfaceMuted
    }
    val ringColor = when (state) {
        AppStepState.Upcoming -> AppStepperDefaults.inactiveColor()
        else -> fillColor
    }

    Box(
        Modifier
            .graphicsLayer {
                scaleX = dotScale
                scaleY = dotScale
            }
            .size(AppStepperDefaults.dotSize())
            .clip(CircleShape)
            .background(fillColor)
            .border(width = AppStepperDefaults.connectorThickness(), color = ringColor, shape = CircleShape),
    )
}

@Composable
private fun StepTexts(step: AppStep, state: AppStepState, horizontal: Boolean = false) {
    val colors = LocalAppColors.current
    val titleColor =
        if (state == AppStepState.Upcoming) colors.textSecondary else colors.textPrimary

    Column(
        if (horizontal) Modifier.padding(top = LocalAppSpacing.current.xs) else Modifier,
        horizontalAlignment = if (horizontal) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            step.title,
            style = AppStepperDefaults.labelStyle().merge(fontWeight = FontWeight.Medium),
            color = titleColor,
            textAlign = if (horizontal) TextAlign.Center else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (step.supportingText != null) {
            Text(
                step.supportingText,
                style = LocalAppTypography.current.bodySmall,
                color = colors.textSecondary,
                maxLines = if (horizontal) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun stepStateText(state: AppStepState): String = when (state) {
    AppStepState.Completed -> AppStrings.stepCompleted
    AppStepState.Current -> AppStrings.stepCurrent
    AppStepState.Upcoming -> AppStrings.stepUpcoming
}
