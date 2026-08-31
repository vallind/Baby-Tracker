package com.babytracker.ui.patterns.chat

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

internal sealed interface MarkdownBlock {
    data class ParagraphBlock(val text: AnnotatedString) : MarkdownBlock
    data class HeadingBlock(val level: Int, val text: AnnotatedString) : MarkdownBlock
    data class ListBlock(
        val marker: String,
        val level: Int,
        val text: AnnotatedString,
    ) : MarkdownBlock
    data class QuoteBlock(val text: AnnotatedString) : MarkdownBlock
    data class CodeBlock(val code: String) : MarkdownBlock
    data object DividerBlock : MarkdownBlock
}

private val markdownParser = Parser.builder()
    .extensions(listOf(StrikethroughExtension.create()))
    .build()

internal fun parseMarkdown(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) return emptyList()
    val blocks = mutableListOf<MarkdownBlock>()
    appendBlocks(markdownParser.parse(markdown), blocks, listLevel = 0)
    return blocks.ifEmpty {
        listOf(MarkdownBlock.ParagraphBlock(AnnotatedString(markdown)))
    }
}

private fun appendBlocks(node: Node, blocks: MutableList<MarkdownBlock>, listLevel: Int) {
    var child = node.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> blocks += MarkdownBlock.ParagraphBlock(renderInline(child))
            is Heading -> blocks += MarkdownBlock.HeadingBlock(child.level, renderInline(child))
            is FencedCodeBlock -> blocks += MarkdownBlock.CodeBlock(child.literal)
            is IndentedCodeBlock -> blocks += MarkdownBlock.CodeBlock(child.literal)
            is BlockQuote -> blocks += MarkdownBlock.QuoteBlock(renderDescendantText(child))
            is BulletList -> appendList(child, blocks, listLevel)
            is OrderedList -> appendList(child, blocks, listLevel)
            is ThematicBreak -> blocks += MarkdownBlock.DividerBlock
            is HtmlBlock -> blocks += MarkdownBlock.CodeBlock(child.literal)
            else -> appendBlocks(child, blocks, listLevel)
        }
        child = child.next
    }
}

private fun appendList(node: Node, blocks: MutableList<MarkdownBlock>, level: Int) {
    var item = node.firstChild
    var number = (node as? OrderedList)?.markerStartNumber ?: 1
    while (item != null) {
        if (item is ListItem) {
            var itemChild = item.firstChild
            var emitted = false
            while (itemChild != null) {
                when (itemChild) {
                    is Paragraph -> {
                        blocks += MarkdownBlock.ListBlock(
                            marker = if (node is OrderedList) "${number}." else "•",
                            level = level,
                            text = renderInline(itemChild),
                        )
                        emitted = true
                    }
                    is BulletList, is OrderedList -> appendList(itemChild, blocks, level + 1)
                    else -> appendBlocks(itemChild, blocks, level + 1)
                }
                itemChild = itemChild.next
            }
            if (!emitted && item.firstChild != null) {
                blocks += MarkdownBlock.ListBlock(
                    marker = if (node is OrderedList) "${number}." else "•",
                    level = level,
                    text = renderDescendantText(item),
                )
            }
            number++
        }
        item = item.next
    }
}

private fun renderDescendantText(node: Node): AnnotatedString = buildAnnotatedString {
    appendInlineChildren(node)
}

private fun renderInline(node: Node): AnnotatedString = buildAnnotatedString {
    var child = node.firstChild
    while (child != null) {
        appendInline(child)
        child = child.next
    }
}

private fun AnnotatedString.Builder.appendInlineChildren(node: Node) {
    var child = node.firstChild
    while (child != null) {
        appendInline(child)
        child = child.next
    }
}

private fun AnnotatedString.Builder.appendInline(node: Node) {
    when (node) {
        is Text -> append(node.literal)
        is Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(node.literal) }
        is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { appendInlineChildren(node) }
        is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            appendInlineChildren(node)
        }
        is Strikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            appendInlineChildren(node)
        }
        is Link -> withLink(LinkAnnotation.Url(node.destination)) { appendInlineChildren(node) }
        is Image -> appendInlineChildren(node)
        is SoftLineBreak -> append(" ")
        is HardLineBreak -> append("\n")
        is HtmlInline -> append(node.literal)
        else -> appendInlineChildren(node)
    }
}