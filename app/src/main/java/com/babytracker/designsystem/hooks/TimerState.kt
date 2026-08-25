package com.babytracker.designsystem.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * 计时状态钩子 — 表单族通用组件（G4 收敛）。
 *
 * 只管 UI 计时（running/startMs/elapsedSec + 秒级跳动），对持久化零依赖：
 * SharedPreferences 等业务约定由 feature 调用方在 start/stop 前后自行读写，
 * koinInject 不进入 designsystem（边界红线）。
 *
 * 与原 Feeding/Sleep FormDialog 内联手写计时行为等价：
 * - [start] 归零已计秒数并置 running；
 * - [stop] 停止跳动并返回已计秒数（供回填时长字段）；
 * - 编辑态可经构造参数预置 elapsedSec（如已有记录的时长展示）。
 *
 * 用法：
 *   val timer = rememberTimerState()
 *   TimerTickEffect(timer)
 *   AppTimerRow(display = timer.formatDisplay(), running = timer.running, ...)
 */
class TimerState(
    initialRunning: Boolean = false,
    initialStartMs: Long = 0L,
    initialElapsedSec: Int = 0,
) {
    var running: Boolean by mutableStateOf(initialRunning)
    var startMs: Long by mutableLongStateOf(initialStartMs)
    var elapsedSec: Int by mutableIntStateOf(initialElapsedSec)

    /** 开始计时（调用方负责同步持久化写入） */
    fun start(nowMs: Long) {
        startMs = nowMs
        elapsedSec = 0
        running = true
    }

    /** 结束计时并返回已计秒数（调用方负责同步持久化清理） */
    fun stop(): Int {
        running = false
        return elapsedSec
    }

    /** mm:ss 展示串（与原表单 String.format(Locale.US, "%02d:%02d", ...) 一致） */
    fun formatDisplay(): String = String.format(Locale.US, "%02d:%02d", elapsedSec / 60, elapsedSec % 60)
}

/** 创建并记忆 [TimerState]。 */
@Composable
fun rememberTimerState(
    initialRunning: Boolean = false,
    initialStartMs: Long = 0L,
    initialElapsedSec: Int = 0,
): TimerState = remember { TimerState(initialRunning, initialStartMs, initialElapsedSec) }

/** 驱动 [TimerState] 的秒级跳动；未运行时挂起，行为与原表单内联 LaunchedEffect 循环一致。 */
@Composable
fun TimerTickEffect(timer: TimerState) {
    LaunchedEffect(timer.running) {
        if (timer.running) {
            while (true) {
                timer.elapsedSec = ((System.currentTimeMillis() - timer.startMs) / 1000).toInt()
                delay(1000L)
            }
        }
    }
}
