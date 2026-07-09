package com.babytracker.designsystem

import org.junit.Test
import java.io.File

class FeatureStaticAuditTest {

    private val featureFiles: List<File> by lazy {
        val root = File("app/src/main/java/com/babytracker/feature")
        root.walkTopDown().filter { it.extension == "kt" }.toList()
    }

    private val bannedPatterns = listOf(
        "androidx.compose.material3.AlertDialog" to "禁止直接使用 Material3 AlertDialog，应使用 AppConfirmDialog",
        "androidx.compose.material3.Button(" to "禁止直接使用 Material3 Button，应使用 AppButton/PrimaryButton",
        "androidx.compose.material3.Card(" to "禁止直接使用 Material3 Card，应使用 AppCard",
        "androidx.compose.material3.FilterChip" to "禁止直接使用 Material3 FilterChip，应使用 designsystem 组件",
        "androidx.compose.material3.DatePicker" to "禁止直接使用 Material3 DatePicker，应使用 designsystem 组件",
        "androidx.compose.material3.OutlinedTextField" to "禁止直接使用 Material3 OutlinedTextField，应使用 designsystem 组件",
        "MaterialTheme.colorScheme." to "禁止直接使用 MaterialTheme.colorScheme，应使用 AppColors",
        "MaterialTheme.shapes." to "禁止直接使用 MaterialTheme.shapes，应使用 AppShapes",
        "MaterialTheme.typography." to "禁止直接使用 MaterialTheme.typography，应使用 AppTypography",
        "Color(0xFF" to "禁止硬编码颜色，应使用 AppColors 令牌",
        "isSystemInDarkTheme" to "禁止直接使用 isSystemInDarkTheme，应使用 theme.name 判断",
    )

    @Test
    fun `feature code must not use banned Material3 or design system patterns`() {
        val violations = mutableListOf<String>()

        for (file in featureFiles) {
            val lines = file.readLines()
            for ((index, line) in lines.withIndex()) {
                val lineNumber = index + 1
                for ((pattern, message) in bannedPatterns) {
                    if (line.contains(pattern, ignoreCase = false)) {
                        violations.add("${file.relativeTo(File("app/src/main/java"))}:$lineNumber · $pattern  —— $message")
                    }
                }
            }
        }

        assert(violations.isEmpty()) {
            buildString {
                appendLine("feature/ 目录中发现 ${violations.size} 处违反设计系统规范的使用：")
                for (v in violations) {
                    appendLine("  $v")
                }
            }
        }
    }
}
