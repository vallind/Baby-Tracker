package com.babytracker.designsystem.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babytracker.designsystem.components.card.AppCard
import com.babytracker.designsystem.theme.LocalAppColors
import com.babytracker.designsystem.theme.LocalAppSpacing
import com.babytracker.designsystem.theme.LocalAppTypography

/**
 * 安全文本 Markdown 渲染组件。
 *
 * 支持标题、强调、删除线、链接、列表、引用和代码；图片只显示替代文字，
 * HTML 仅按纯文本展示，避免内容触发远程资源加载或脚本执行。
 */
@Composable
fun AppMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalAppTypography.current.bodyLarge,
    color: Color = LocalAppColors.current.textPrimary,
) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    val spacing = LocalAppSpacing.current
    val colors = LocalAppColors.current
    val typography = LocalAppTypography.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.ParagraphBlock -> Text(
                    text = block.text,
                    style = style,
                    color = color,
                )
                is MarkdownBlock.HeadingBlock -> Text(
                    text = block.text,
                    style = when (block.level) {
                        1 -> typography.titleLarge
                        2 -> typography.titleMedium
                        else -> typography.bodyLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                is MarkdownBlock.ListBlock -> Row(
                    modifier = Modifier.padding(start = (block.level * 16).dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = block.marker,
                        style = style,
                        color = color,
                        modifier = Modifier.widthIn(min = 24.dp),
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        text = block.text,
                        style = style,
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                }
                is MarkdownBlock.QuoteBlock -> Row {
                    Box(
                        Modifier
                            .width(3.dp)
                            .heightIn(min = 24.dp)
                            .background(colors.primary),
                    )
                    Text(
                        text = block.text,
                        style = style,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = spacing.sm),
                    )
                }
                is MarkdownBlock.CodeBlock -> AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colors.pageBackground,
                ) {
                    Text(
                        text = block.code,
                        style = style.copy(fontFamily = FontFamily.Monospace),
                        color = color,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(spacing.sm),
                    )
                }
                MarkdownBlock.DividerBlock -> HorizontalDivider(color = colors.divider)
            }
        }
    }
}
