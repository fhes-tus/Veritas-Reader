package com.veritas.reader

import android.content.Context
import android.net.Uri
import java.io.File
import javax.imageio.ImageIO

object CoverExtractor {
    fun coversDir(context: Context): File = File(context.filesDir, "document_covers").apply { mkdirs() }
    
    fun extractPdfCover(context: Context, documentId: String, uri: Uri): String? {
        return runCatching {
            val file = File(uri.path ?: uri.toString())
            if (!file.exists()) return null
            org.apache.pdfbox.pdmodel.PDDocument.load(file).use { doc ->
                if (doc.numberOfPages > 0) {
                    val renderer = org.apache.pdfbox.rendering.PDFRenderer(doc)
                    val image = renderer.renderImageWithDPI(0, 150f)
                    val outFile = File(coversDir(context), "$documentId.png")
                    ImageIO.write(image, "PNG", outFile)
                    outFile.absolutePath
                } else null
            }
        }.getOrNull()
    }

    fun extractCoverFromFile(context: Context, documentId: String, originalFile: File): String? {
        return runCatching {
            if (!originalFile.exists()) return null
            val nameLower = originalFile.name.lowercase()
            if (nameLower.endsWith(".pdf")) {
                extractPdfCover(context, documentId, Uri.parse(originalFile.absolutePath))
            } else if (nameLower.endsWith(".png") || nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) {
                extractImageCover(context, documentId, Uri.parse(originalFile.absolutePath))
            } else null
        }.getOrNull()
    }

    fun coverFile(context: Context, documentId: String): File? {
        val file = File(coversDir(context), "$documentId.png")
        return if (file.exists()) file else null
    }

    fun deleteCover(context: Context, documentId: String) {
        runCatching {
            File(coversDir(context), "$documentId.png").delete()
        }
    }

    fun extractEpubCover(context: Context, documentId: String, uri: Uri): String? = null

    fun extractImageCover(context: Context, documentId: String, uri: Uri): String? {
        return runCatching {
            val srcFile = File(uri.path ?: uri.toString())
            if (!srcFile.exists()) return null
            val outFile = File(coversDir(context), "$documentId.png")
            srcFile.copyTo(outFile, overwrite = true)
            outFile.absolutePath
        }.getOrNull()
    }
}
