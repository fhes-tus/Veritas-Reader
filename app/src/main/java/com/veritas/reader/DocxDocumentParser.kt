package com.veritas.reader

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.util.Locale
import java.util.zip.ZipInputStream

sealed class DocxBlock {
    data class Heading(val level: Int, val text: String) : DocxBlock()
    data class Paragraph(val text: String) : DocxBlock()
    data class Bullet(val level: Int, val text: String) : DocxBlock()
    data class Table(val rows: List<List<String>>) : DocxBlock()
}

data class DocxPage(
    val pageNumber: Int,
    val blocks: List<DocxBlock>
)

data class DocxDocument(
    val title: String,
    val pages: List<DocxPage>,
    val totalPages: Int = pages.size
)

object DocxDocumentParser {

    fun parse(bytes: ByteArray, defaultTitle: String): DocxDocument {
        var documentXml: String? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && (entry.name == "word/document.xml" || entry.name == "document.xml")) {
                    documentXml = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
            }
        }

        if (documentXml == null) {
            return DocxDocument(
                title = defaultTitle,
                pages = listOf(
                    DocxPage(
                        pageNumber = 1,
                        blocks = listOf(DocxBlock.Paragraph("Could not read Word document content."))
                    )
                )
            )
        }

        val allBlocks = parseXmlBlocks(documentXml)
        val pages = paginateBlocks(allBlocks)

        return DocxDocument(
            title = defaultTitle,
            pages = if (pages.isNotEmpty()) pages else listOf(DocxPage(1, listOf(DocxBlock.Paragraph("Empty document."))))
        )
    }

    private fun parseXmlBlocks(xml: String): List<DocxBlock> {
        val blocks = mutableListOf<DocxBlock>()
        val dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        dbFactory.isNamespaceAware = false
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        doc.documentElement.normalize()

        val bodyList = doc.getElementsByTagName("w:body")
        val bodyNode = if (bodyList.length > 0) bodyList.item(0) else doc.documentElement
        val bodyNodes = bodyNode?.childNodes ?: return blocks

        for (i in 0 until bodyNodes.length) {
            val node = bodyNodes.item(i)
            if (node.nodeType != org.w3c.dom.Node.ELEMENT_NODE) continue
            val element = node as org.w3c.dom.Element
            val tagName = element.tagName.substringAfter(':').lowercase(Locale.getDefault())

            when (tagName) {
                "p" -> {
                    val (blockType, text) = parseParagraphElement(element)
                    if (text.isNotBlank()) {
                        when (blockType) {
                            is BlockType.Heading -> blocks.add(DocxBlock.Heading(blockType.level, text))
                            is BlockType.Bullet -> blocks.add(DocxBlock.Bullet(blockType.level, text))
                            is BlockType.Normal -> blocks.add(DocxBlock.Paragraph(text))
                        }
                    }
                }
                "tbl" -> {
                    val tableBlock = parseTableElement(element)
                    if (tableBlock.rows.isNotEmpty()) {
                        blocks.add(tableBlock)
                    }
                }
            }
        }
        return blocks
    }

    private sealed class BlockType {
        data class Heading(val level: Int) : BlockType()
        data class Bullet(val level: Int) : BlockType()
        object Normal : BlockType()
    }

    private fun parseParagraphElement(p: org.w3c.dom.Element): Pair<BlockType, String> {
        var blockType: BlockType = BlockType.Normal
        val pPrList = p.getElementsByTagName("w:pPr")
        if (pPrList.length > 0) {
            val pPr = pPrList.item(0) as org.w3c.dom.Element
            val pStyleList = pPr.getElementsByTagName("w:pStyle")
            if (pStyleList.length > 0) {
                val pStyle = pStyleList.item(0) as org.w3c.dom.Element
                val styleVal = pStyle.getAttribute("w:val").lowercase(Locale.getDefault())
                if (styleVal.startsWith("heading1") || styleVal == "1" || styleVal == "title") {
                    blockType = BlockType.Heading(1)
                } else if (styleVal.startsWith("heading2") || styleVal == "2" || styleVal == "subtitle") {
                    blockType = BlockType.Heading(2)
                } else if (styleVal.startsWith("heading3") || styleVal == "3") {
                    blockType = BlockType.Heading(3)
                } else if (styleVal.contains("list") || styleVal.contains("bullet")) {
                    blockType = BlockType.Bullet(0)
                }
            }

            val numPrList = pPr.getElementsByTagName("w:numPr")
            if (numPrList.length > 0) {
                val numPr = numPrList.item(0) as org.w3c.dom.Element
                val ilvlNode = numPr.getElementsByTagName("w:ilvl").item(0)
                val ilvl = if (ilvlNode != null) (ilvlNode as org.w3c.dom.Element).getAttribute("w:val").toIntOrNull() ?: 0 else 0
                blockType = BlockType.Bullet(ilvl)
            }
        }

        val textSb = StringBuilder()
        val tList = p.getElementsByTagName("w:t")
        for (i in 0 until tList.length) {
            val t = tList.item(i)
            textSb.append(t.textContent.orEmpty())
        }

        return blockType to textSb.toString().trim()
    }

    private fun parseTableElement(tbl: org.w3c.dom.Element): DocxBlock.Table {
        val rows = mutableListOf<List<String>>()
        val trList = tbl.getElementsByTagName("w:tr")
        for (r in 0 until trList.length) {
            val tr = trList.item(r) as org.w3c.dom.Element
            val cells = mutableListOf<String>()
            val tcList = tr.getElementsByTagName("w:tc")
            for (c in 0 until tcList.length) {
                val tc = tcList.item(c) as org.w3c.dom.Element
                val tList = tc.getElementsByTagName("w:t")
                val cellSb = StringBuilder()
                for (t in 0 until tList.length) {
                    cellSb.append(tList.item(t).textContent.orEmpty())
                }
                cells.add(cellSb.toString().trim())
            }
            if (cells.isNotEmpty()) {
                rows.add(cells)
            }
        }
        return DocxBlock.Table(rows)
    }

    private fun paginateBlocks(blocks: List<DocxBlock>): List<DocxPage> {
        val pages = mutableListOf<DocxPage>()
        val currentPageBlocks = mutableListOf<DocxBlock>()
        var currentWeight = 0
        var pageNum = 1

        for (block in blocks) {
            val weight = when (block) {
                is DocxBlock.Heading -> 3
                is DocxBlock.Bullet -> 1
                is DocxBlock.Paragraph -> maxOf(1, block.text.length / 150)
                is DocxBlock.Table -> maxOf(3, block.rows.size * 2)
            }

            if (currentWeight + weight > 12 && currentPageBlocks.isNotEmpty()) {
                pages.add(DocxPage(pageNum, currentPageBlocks.toList()))
                pageNum++
                currentPageBlocks.clear()
                currentWeight = 0
            }

            currentPageBlocks.add(block)
            currentWeight += weight
        }

        if (currentPageBlocks.isNotEmpty()) {
            pages.add(DocxPage(pageNum, currentPageBlocks.toList()))
        }

        return pages
    }
}
