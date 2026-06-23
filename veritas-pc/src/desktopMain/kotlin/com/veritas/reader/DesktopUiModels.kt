package com.veritas.reader

import android.net.Uri

data class PdfImportOptions(
    val startPage: Int? = null,
    val endPage: Int? = null,
    val cleanupRepeatedLines: Boolean = true,
    val removePageNumbers: Boolean = true,
    val repairHyphenation: Boolean = true,
    val includePageMarkers: Boolean = false,
    val forceOcr: Boolean = false,
    val preferOcrWhenLowText: Boolean = true,
    val extractionMode: String = "HTML with images",
    val removeTopPageNoise: Boolean = true,
    val removeBottomPageNoise: Boolean = true,
    val manualCropBeforeExtract: Boolean = false,
    val minWordGap: String = "0.1",
    val separateWordsOnFontChange: Boolean = true,
    val markPdfLinesForCanvas: Boolean = true,
    val forceFreshExtraction: Boolean = false,
    val cropRect: android.graphics.RectF? = null
) {
    fun normalized(pageCount: Int): PdfImportOptions {
        val safePageCount = pageCount.coerceAtLeast(1)
        val safeStart = (startPage ?: 1).coerceIn(1, safePageCount)
        val safeEnd = (endPage ?: safePageCount).coerceIn(safeStart, safePageCount)
        return copy(startPage = safeStart, endPage = safeEnd)
    }
}

data class TextImportOptions(
    val encodingId: String = "AUTO_DETECT"
)

data class TtsEngineOption(
    val packageName: String,
    val label: String
)

data class TtsVoiceOption(
    val name: String,
    val label: String,
    val localeTag: String,
    val requiresNetwork: Boolean,
    val quality: Int,
    val latency: Int
)

enum class VeritasBrowserTab(val label: String, val emoji: String) {
    ALL("ALL", "📁"),
    BOOKS("EPUB", "📕"),
    PDF("PDF", "📄"),
    DOC("DOCX", "📘"),
    HTML("WEB", "🌐"),
    TXT("TXT", "📝"),
    OCR("OCR", "📷")
}

enum class VeritasBrowserSort(val label: String) {
    NAME("File name"),
    DATE("Date/time"),
    SIZE("Size"),
    PATH("Path")
}

data class VeritasBrowserRoot(
    val uri: Uri,
    val label: String
)

data class VeritasBrowserLocation(
    val rootLabel: String,
    val relativePath: String = "",
    val filePath: String? = null,
    val rootUri: Uri? = null,
    val documentId: String? = null
) {
    val label: String
        get() = if (relativePath.isBlank()) rootLabel else "$rootLabel/$relativePath"
}

data class VeritasBrowserFile(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedAt: Long,
    val rootLabel: String,
    val relativePath: String,
    val type: VeritasBrowserTab = VeritasBrowserTab.ALL,
    val isDirectory: Boolean = false,
    val isSupported: Boolean = true,
    val targetLocation: VeritasBrowserLocation? = null
)

data class VeritasFileBrowserScanResult(
    val files: List<VeritasBrowserFile>,
    val location: VeritasBrowserLocation? = null,
    val diagnostics: List<String> = emptyList()
)
