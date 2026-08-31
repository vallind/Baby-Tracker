package com.babytracker.designsystem.components

import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

/**
 * 可交互组件无障碍语义静态审计。
 *
 * 规则：自定义可交互组件必须显式声明语义（角色 / 选中态 / 内容描述），
 * 且 M3 包装组件内置语义的位置不得重复添加 contentDescription 造成双重朗读。
 *
 * 注意：JVM 单测工作目录为 app 模块根（非仓库根），文件路径以 src/ 开头。
 */
class A11ySemanticsAuditTest {

    // 四层架构（taxonomy §〇.4）：可交互业务形态组件外移至 app/ui/patterns，
    // 无障碍契约随行守门（此处与 Gradle 任务同语义，双路独立）
    private val roots = listOf(
        File("src/main/java/com/babytracker/designsystem/components"),
        File("src/main/java/com/babytracker/ui/patterns"),
    )

    private fun read(name: String): String {
        val file = roots.asSequence().flatMap { root -> root.walkTopDown() }
            .firstOrNull { it.name == name }
            ?: throw FileNotFoundException("审计目标文件不存在（路径漂移？）：$name")
        return file.readText()
    }

    // 防呆（lessons #14）：walkTopDown 对不存在的路径静默返回空流，必须断言扫描命中文件数 > 0，防止路径漂移后审计假绿
    private fun assertScanNonEmpty() {
        assert(roots.any { it.walkTopDown().count() > 0 }) { "组件目录扫描为空，路径可能已漂移，审计已失效：$roots" }
    }

    @Test
    fun `自定义可交互组件必须带显式语义`() {
        assertScanNonEmpty()
        // 分段控件：选项声明 Role.Tab 角色与 selected 选中态
        assert(read("SegmentedControl.kt").contains("Role.Tab")) { "SegmentedControl 缺少 Role.Tab 角色声明" }
        assert(read("SegmentedControl.kt").contains("selected =")) { "SegmentedControl 缺少 selected 选中语义" }
        // 记录卡片：下拉操作以 customActions 暴露给读屏
        assert(read("RecordCard.kt").contains("customActions")) { "RecordCard 缺少 customActions 自定义操作" }
        // 评分与滑块：图标/值变化必须带 contentDescription
        assert(read("AppRate.kt").contains("contentDescription")) { "AppRate 缺少 contentDescription" }
        assert(read("AppSlider.kt").contains("contentDescription")) { "AppSlider 缺少 contentDescription" }
        // 时间滚轮：当前项声明 selected 选中语义
        assert(read("TimePickerLogic.kt").contains("selected =")) { "TimePickerLogic 滚轮当前项缺少 selected 选中语义" }
        // 日历日期格：声明 Button 角色与 selected 选中语义
        assert(read("DateTimeCascade.kt").contains("selected =")) { "DateTimeCascade 日历日期格缺少 selected 选中语义" }
        // FAB label 变体与底部导航：图标不重复朗读，置空 contentDescription
        assert(read("Fab.kt").contains("contentDescription = null")) { "Fab label 变体图标未置空 contentDescription（会与文字重复朗读）" }
        assert(read("AppNavigationBar.kt").contains("contentDescription = null")) { "AppNavigationBar 图标未置空 contentDescription（M3 内置 label 已朗读）" }
    }
}
