package com.veritas.reader

import android.graphics.RectF
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.PDFTextStripperByArea
import com.tom_roush.pdfbox.text.TextPosition
import java.util.Locale

internal data class PositionedPdfLine(
    val text: String,
    val minX: Float,
    val maxX: Float,
    val y: Float,
    val height: Float
) {
    val centerX: Float
        get() = (minX + maxX) / 2f

    val width: Float
        get() = maxX - minX
}

internal data class PositionedPdfSegment(
    val minX: Float,
    val maxX: Float,
    val y: Float,
    val height: Float,
    val glyphCount: Int
) {
    val centerX: Float
        get() = (minX + maxX) / 2f

    val width: Float
        get() = maxX - minX
}

internal data class PdfPageProbe(
    val plainText: String,
    val lines: List<PositionedPdfLine>,
    val segments: List<PositionedPdfSegment>
)

internal data class PdfColumnLayout(
    val splitX: Float,
    val columnTopY: Float,
    val columnBottomY: Float,
    val pageWidth: Float,
    val pageHeight: Float,
    val rowBreaks: List<Pair<Float, Float>> = emptyList()
)

internal class PdfLayoutProbeStripper : PDFTextStripper() {
    val positionedLines = mutableListOf<PositionedPdfLine>()
    val positionedSegments = mutableListOf<PositionedPdfSegment>()

    init {
        sortByPosition = true
        setShouldSeparateByBeads(false)
    }

    override fun writeString(text: String, textPositions: MutableList<TextPosition>) {
        if (textPositions.isEmpty()) {
            super.writeString(text, textPositions)
            return
        }

        val cleanText = text.replace(Regex("\\s+"), " ").trim()
        if (cleanText.isNotBlank()) {
            var minX = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var sumY = 0f
            var sumHeight = 0f
            val count = textPositions.size

            for (i in 0 until count) {
                val tp = textPositions[i]
                val x = tp.xDirAdj
                val right = x + tp.widthDirAdj
                if (x < minX) minX = x
                if (right > maxX) maxX = right
                sumY += tp.yDirAdj
                sumHeight += tp.heightDir
            }

            val avgY = sumY / count
            val avgHeight = (sumHeight / count).takeIf { !it.isNaN() && it > 0f } ?: 8f
            positionedLines.add(PositionedPdfLine(cleanText, minX, maxX, avgY, avgHeight))

            // Segment extraction within this line:
            // With sortByPosition = true, textPositions are in left-to-right reading order.
            // When multiple columns are present on the same line, there is a distinct gutter gap.
            val avgGlyphWidth = if (count > 0 && maxX > minX) (maxX - minX) / count else 4f
            val segmentGapThreshold = maxOf(14f, avgGlyphWidth * 4.0f)

            var segStart = 0
            for (i in 0 until count - 1) {
                val curr = textPositions[i]
                val next = textPositions[i + 1]
                val gap = next.xDirAdj - (curr.xDirAdj + curr.widthDirAdj)
                if (gap > segmentGapThreshold) {
                    val segMinX = textPositions[segStart].xDirAdj
                    val segMaxX = curr.xDirAdj + curr.widthDirAdj
                    val segGlyphs = i - segStart + 1
                    if (segGlyphs >= 2 && (segMaxX - segMinX) >= 8f) {
                        positionedSegments.add(
                            PositionedPdfSegment(
                                minX = segMinX,
                                maxX = segMaxX,
                                y = avgY,
                                height = avgHeight,
                                glyphCount = segGlyphs
                            )
                        )
                    }
                    segStart = i + 1
                }
            }

            val lastTp = textPositions[count - 1]
            val lastSegMinX = textPositions[segStart].xDirAdj
            val lastSegMaxX = lastTp.xDirAdj + lastTp.widthDirAdj
            val lastSegGlyphs = count - segStart
            if (lastSegGlyphs >= 2 && (lastSegMaxX - lastSegMinX) >= 8f) {
                positionedSegments.add(
                    PositionedPdfSegment(
                        minX = lastSegMinX,
                        maxX = lastSegMaxX,
                        y = avgY,
                        height = avgHeight,
                        glyphCount = lastSegGlyphs
                    )
                )
            }
        }
        super.writeString(text, textPositions)
    }

    companion object {
        fun extract(document: PDDocument, pageNumber: Int): PdfPageProbe {
            val stripper = PdfLayoutProbeStripper().apply {
                startPage = pageNumber
                endPage = pageNumber
            }
            val plainText = stripper.getText(document)
            return PdfPageProbe(
                plainText = plainText,
                lines = stripper.positionedLines.toList(),
                segments = stripper.positionedSegments.toList()
            )
        }
    }
}

internal object PdfPageTextExtractor {
    fun extractPage(document: PDDocument, pageNumber: Int): String {
        val page = document.getPage((pageNumber - 1).coerceAtLeast(0))
        val probe = PdfLayoutProbeStripper.extract(document, pageNumber)
        val layout = detectColumns(probe, page)
        return if (layout == null) {
            probe.plainText
        } else {
            extractColumnPage(page, layout).ifBlank { probe.plainText }
        }
    }

    private fun detectColumns(probe: PdfPageProbe, page: PDPage): PdfColumnLayout? {
        val box = page.cropBox ?: page.mediaBox ?: return null
        val pageWidth = box.width.coerceAtLeast(1f)
        val pageHeight = box.height.coerceAtLeast(1f)
        val fromSegments = detectColumnsFromSegments(probe.segments, pageWidth, pageHeight)

        val usefulLines = probe.lines
            .filter { it.text.length >= 2 && it.width > 8f && it.y in (pageHeight * 0.08f)..(pageHeight * 0.92f) }
            .sortedWith(compareBy<PositionedPdfLine> { it.y }.thenBy { it.minX })
        if (usefulLines.size < 12 && fromSegments == null) return null

        val contentMinX = usefulLines.minOfOrNull { it.minX } ?: (pageWidth * 0.08f)
        val contentMaxX = usefulLines.maxOfOrNull { it.maxX } ?: (pageWidth * 0.92f)
        val contentWidth = (contentMaxX - contentMinX).coerceAtLeast(1f)
        val midX = contentMinX + contentWidth / 2f
        val fullWidthThreshold = contentWidth * 0.64f
        val columnLines = usefulLines.filterNot { line ->
            line.width >= fullWidthThreshold || (line.minX < midX && line.maxX > midX)
        }

        val baseLayout = if (columnLines.size >= 10) {
            val left = columnLines.filter { it.centerX < midX }
            val right = columnLines.filter { it.centerX >= midX }
            if (left.size >= 5 && right.size >= 5) {
                val leftMaxX = left.maxOf { it.maxX }
                val rightMinX = right.minOf { it.minX }
                val gutter = rightMinX - leftMaxX
                if (gutter >= maxOf(12f, contentWidth * 0.02f)) {
                    val leftTop = left.minOf { it.y }
                    val rightTop = right.minOf { it.y }
                    val leftBottom = left.maxOf { it.y }
                    val rightBottom = right.maxOf { it.y }
                    val overlapTop = maxOf(leftTop, rightTop)
                    val overlapBottom = minOf(leftBottom, rightBottom)
                    val overlapHeight = overlapBottom - overlapTop
                    val columnHeight = (maxOf(leftBottom, rightBottom) - minOf(leftTop, rightTop)).coerceAtLeast(1f)
                    if (overlapHeight >= columnHeight * 0.42f) {
                        val averageLineHeight = columnLines.map { it.height.toDouble() }.average()
                            .takeIf { !it.isNaN() }
                            ?.toFloat()
                            ?: 10f
                        val columnTopY = (minOf(leftTop, rightTop) - averageLineHeight).coerceIn(0f, pageHeight)
                        val columnBottomY = (maxOf(leftBottom, rightBottom) + averageLineHeight * 2f).coerceIn(columnTopY, pageHeight)
                        PdfColumnLayout(
                            splitX = ((leftMaxX + rightMinX) / 2f).coerceIn(1f, pageWidth - 1f),
                            columnTopY = columnTopY,
                            columnBottomY = columnBottomY,
                            pageWidth = pageWidth,
                            pageHeight = pageHeight
                        )
                    } else null
                } else null
            } else null
        } else null

        val finalBase = baseLayout ?: fromSegments ?: return null

        // Detect horizontal breaks / chapter dividers spanning across the page within the column area
        val breakLines = usefulLines.filter { line ->
            val isHeader = Regex("""^(CHAPTER|Chapter|PROLOGUE|Prologue|EPILOGUE|Epilogue|INTRODUCTION|Introduction|PART|Part|BOOK|Book|SECTION|Section)\b.*""", RegexOption.IGNORE_CASE).containsMatchIn(line.text)
            val crossesGutter = (line.minX < finalBase.splitX - 10f && line.maxX > finalBase.splitX + 10f)
            val isCenteredBreak = kotlin.math.abs(line.centerX - finalBase.splitX) < contentWidth * 0.15f && line.width >= contentWidth * 0.35f
            val isPageSpanningHeader = (crossesGutter || isCenteredBreak) && (isHeader || line.width >= contentWidth * 0.45f)
            isPageSpanningHeader && line.y in (finalBase.columnTopY + 35f)..(finalBase.columnBottomY - 35f)
        }

        val rowBreaks = if (breakLines.isNotEmpty()) {
            val sortedBreaks = breakLines.sortedBy { it.y }
            val clusters = mutableListOf<MutableList<PositionedPdfLine>>()
            sortedBreaks.forEach { line ->
                val lastCluster = clusters.lastOrNull()
                if (lastCluster != null && line.y - lastCluster.last().y < 28f) {
                    lastCluster.add(line)
                } else {
                    clusters.add(mutableListOf(line))
                }
            }
            clusters.map { cluster ->
                val topY = (cluster.minOf { it.y } - 8f).coerceAtLeast(finalBase.columnTopY)
                val bottomY = (cluster.maxOf { it.y + it.height } + 8f).coerceAtMost(finalBase.columnBottomY)
                Pair(topY, bottomY)
            }
        } else emptyList()

        return finalBase.copy(rowBreaks = rowBreaks)
    }

    private fun detectColumnsFromSegments(
        segments: List<PositionedPdfSegment>,
        pageWidth: Float,
        pageHeight: Float
    ): PdfColumnLayout? {
        val usefulSegments = segments
            .filter { it.glyphCount >= 2 && it.width > 8f && it.y in (pageHeight * 0.08f)..(pageHeight * 0.92f) }
            .sortedWith(compareBy<PositionedPdfSegment> { it.y }.thenBy { it.minX })
        if (usefulSegments.size < 12) return null

        val contentMinX = usefulSegments.minOf { it.minX }
        val contentMaxX = usefulSegments.maxOf { it.maxX }
        val contentWidth = (contentMaxX - contentMinX).coerceAtLeast(1f)
        val midX = contentMinX + contentWidth / 2f
        val fullWidthThreshold = contentWidth * 0.64f
        val columnCandidates = usefulSegments.filterNot { segment ->
            segment.width >= fullWidthThreshold || (segment.minX < midX && segment.maxX > midX)
        }
        val left = columnCandidates.filter { it.centerX < midX }
        val right = columnCandidates.filter { it.centerX >= midX }
        if (left.size < 5 || right.size < 5) return null

        val leftMaxX = left.maxOf { it.maxX }
        val rightMinX = right.minOf { it.minX }
        val gutter = rightMinX - leftMaxX
        if (gutter < maxOf(10f, contentWidth * 0.015f)) return null

        val leftTop = left.minOf { it.y }
        val rightTop = right.minOf { it.y }
        val leftBottom = left.maxOf { it.y }
        val rightBottom = right.maxOf { it.y }
        val overlapTop = maxOf(leftTop, rightTop)
        val overlapBottom = minOf(leftBottom, rightBottom)
        val overlapHeight = overlapBottom - overlapTop
        val columnHeight = (maxOf(leftBottom, rightBottom) - minOf(leftTop, rightTop)).coerceAtLeast(1f)
        if (overlapHeight < columnHeight * 0.36f) return null

        val averageLineHeight = columnCandidates.map { it.height.toDouble() }.average()
            .takeIf { !it.isNaN() }
            ?.toFloat()
            ?: 10f
        return PdfColumnLayout(
            splitX = ((leftMaxX + rightMinX) / 2f).coerceIn(1f, pageWidth - 1f),
            columnTopY = (minOf(leftTop, rightTop) - averageLineHeight).coerceIn(0f, pageHeight),
            columnBottomY = (maxOf(leftBottom, rightBottom) + averageLineHeight * 2f).coerceIn(0f, pageHeight),
            pageWidth = pageWidth,
            pageHeight = pageHeight
        )
    }

    private fun extractColumnPage(page: PDPage, layout: PdfColumnLayout): String {
        val stripper = PDFTextStripperByArea().apply {
            sortByPosition = true
        }

        val top = RectF(0f, 0f, layout.pageWidth, layout.columnTopY)
        if (top.height() > 4f) stripper.addRegion("top", top)

        val bottom = RectF(0f, layout.columnBottomY, layout.pageWidth, layout.pageHeight)
        if (bottom.height() > 4f) stripper.addRegion("bottom", bottom)

        val resultParts = mutableListOf<String>()

        if (layout.rowBreaks.isEmpty()) {
            val left = RectF(0f, layout.columnTopY, layout.splitX, layout.columnBottomY)
            val right = RectF(layout.splitX, layout.columnTopY, layout.pageWidth, layout.columnBottomY)
            stripper.addRegion("left", left)
            stripper.addRegion("right", right)
            stripper.extractRegions(page)

            fun regionText(name: String): String =
                if (name in stripper.regions) cleanRegionText(stripper.getTextForRegion(name)) else ""

            val topText = regionText("top")
            val leftText = regionText("left")
            val rightText = regionText("right")
            val bottomText = regionText("bottom")
            val middleText = if (looksLikeDuplicateColumnText(leftText, rightText)) leftText else {
                listOf(leftText, rightText).filter { it.isNotBlank() }.joinToString("\n\n")
            }
            return listOf(topText, middleText, bottomText).filter { it.isNotBlank() }.joinToString("\n\n").trim()
        } else {
            var currentY = layout.columnTopY
            layout.rowBreaks.forEachIndexed { index, (breakTop, breakBottom) ->
                if (breakTop > currentY + 10f) {
                    val leftBand = RectF(0f, currentY, layout.splitX, breakTop)
                    val rightBand = RectF(layout.splitX, currentY, layout.pageWidth, breakTop)
                    stripper.addRegion("left_$index", leftBand)
                    stripper.addRegion("right_$index", rightBand)
                }
                val headerBand = RectF(0f, breakTop, layout.pageWidth, breakBottom)
                stripper.addRegion("header_$index", headerBand)
                currentY = breakBottom
            }
            if (layout.columnBottomY > currentY + 10f) {
                val lastIdx = layout.rowBreaks.size
                val leftBand = RectF(0f, currentY, layout.splitX, layout.columnBottomY)
                val rightBand = RectF(layout.splitX, currentY, layout.pageWidth, layout.columnBottomY)
                stripper.addRegion("left_$lastIdx", leftBand)
                stripper.addRegion("right_$lastIdx", rightBand)
            }

            stripper.extractRegions(page)

            fun regionText(name: String): String =
                if (name in stripper.regions) cleanRegionText(stripper.getTextForRegion(name)) else ""

            val topText = regionText("top")
            if (topText.isNotBlank()) resultParts.add(topText)

            var bandY = layout.columnTopY
            layout.rowBreaks.forEachIndexed { index, (breakTop, breakBottom) ->
                if (breakTop > bandY + 10f) {
                    val l = regionText("left_$index")
                    val r = regionText("right_$index")
                    if (l.isNotBlank()) resultParts.add(l)
                    if (r.isNotBlank() && !looksLikeDuplicateColumnText(l, r)) resultParts.add(r)
                }
                val h = regionText("header_$index")
                if (h.isNotBlank()) resultParts.add(h)
                bandY = breakBottom
            }
            if (layout.columnBottomY > bandY + 10f) {
                val lastIdx = layout.rowBreaks.size
                val l = regionText("left_$lastIdx")
                val r = regionText("right_$lastIdx")
                if (l.isNotBlank()) resultParts.add(l)
                if (r.isNotBlank() && !looksLikeDuplicateColumnText(l, r)) resultParts.add(r)
            }

            val bottomText = regionText("bottom")
            if (bottomText.isNotBlank()) resultParts.add(bottomText)

            return resultParts.filter { it.isNotBlank() }.joinToString("\n\n").trim()
        }
    }

    private fun cleanRegionText(text: String): String {
        return text.replace('\r', '\n')
            .lineSequence()
            .map { it.trimEnd() }
            .dropWhile { it.isBlank() }
            .joinToString("\n")
            .trim()
    }

    private fun looksLikeDuplicateColumnText(left: String, right: String): Boolean {
        val normalizedLeft = normalizeForColumnDuplicate(left)
        val normalizedRight = normalizeForColumnDuplicate(right)
        if (normalizedLeft.length < 120 || normalizedRight.length < 120) return false
        val minLength = minOf(normalizedLeft.length, normalizedRight.length)
        val maxLength = maxOf(normalizedLeft.length, normalizedRight.length)
        if (minLength.toFloat() / maxLength.toFloat() < 0.86f) return false
        val prefixLength = minOf(900, minLength)
        var same = 0
        for (index in 0 until prefixLength) {
            if (normalizedLeft[index] == normalizedRight[index]) same++
        }
        return same.toFloat() / prefixLength.toFloat() >= 0.90f
    }

    private fun normalizeForColumnDuplicate(text: String): String {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9]+"), "")
    }
}