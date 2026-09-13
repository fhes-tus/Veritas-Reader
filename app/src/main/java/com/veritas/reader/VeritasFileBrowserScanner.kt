package com.veritas.reader

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import java.io.File
import java.util.Locale

enum class VeritasBrowserTab(val label: String, val emoji: String) {
    ALL("ALL", "📁"),
    BOOKS("EPUB", "📕"),
    PDF("PDF", "📄"),
    DOC("DOCX", "📘"),
    SLIDES("PPTX", "📙"),
    HTML("WEB", "🌐"),
    TXT("TXT", "📝"),
    OCR("OCR", "📷");

    val icon: androidx.compose.ui.graphics.vector.ImageVector
        get() = when (this) {
            ALL -> Icons.Outlined.Folder
            BOOKS -> Icons.Outlined.Book
            PDF -> Icons.Outlined.PictureAsPdf
            DOC -> Icons.Outlined.Description
            SLIDES -> Icons.Outlined.Slideshow
            HTML -> Icons.Outlined.Language
            TXT -> Icons.AutoMirrored.Outlined.Article
            OCR -> Icons.Outlined.PhotoCamera
        }
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

object VeritasFileBrowserScanner {
    private val childProjection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    fun persistedRoots(context: Context): List<VeritasBrowserRoot> {
        return context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { permission ->
                VeritasBrowserRoot(
                    uri = permission.uri,
                    label = displayNameForRoot(context, permission.uri)
                )
            }
            .distinctBy { it.uri }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    fun initialLocation(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean
    ): VeritasBrowserLocation? {
        if (includeAllFilesAccess) {
            val storageRoot = Environment.getExternalStorageDirectory()
            return VeritasBrowserLocation(
                rootLabel = "Phone storage",
                filePath = storageRoot.absolutePath
            )
        }
        val root = roots.firstOrNull() ?: return null
        return VeritasBrowserLocation(
            rootLabel = root.label,
            rootUri = root.uri,
            documentId = runCatching { DocumentsContract.getTreeDocumentId(root.uri) }.getOrNull()
        )
    }

    fun scan(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean = false,
        location: VeritasBrowserLocation? = null
    ): VeritasFileBrowserScanResult {
        val diagnostics = mutableListOf<String>()
        val activeLocation = location ?: initialLocation(context, roots, includeAllFilesAccess)
        if (activeLocation == null) {
            return VeritasFileBrowserScanResult(
                emptyList(),
                null,
                listOf("Grant All Files access or approve a folder to browse files.")
            )
        }
        val entries = when {
            activeLocation.filePath != null -> listFileDirectory(context, activeLocation, diagnostics)
            activeLocation.rootUri != null && activeLocation.documentId != null -> {
                val root = roots.firstOrNull { it.uri == activeLocation.rootUri }
                if (root == null) {
                    diagnostics.add("This approved folder is no longer available. Add it again from Folders to scan.")
                    emptyList()
                } else {
                    listSafDirectory(
                        context = context,
                        root = root,
                        location = activeLocation,
                        diagnostics = diagnostics
                    )
                }
            }

            else -> emptyList()
        }
        return VeritasFileBrowserScanResult(entries, activeLocation, diagnostics)
    }

    /**
     * Every readable storage volume, not just the built-in one. getExternalFilesDirs reports
     * one entry per mounted volume, including SD cards and USB OTG; four parents up from the
     * app-private directory is the volume root, which All Files access can read. The browser
     * previously clamped everything to primary storage, so a document on an SD card could not
     * be reached at all.
     */
    private fun storageVolumeRoots(context: Context): List<File> {
        val roots = LinkedHashSet<File>()
        runCatching { Environment.getExternalStorageDirectory() }.getOrNull()?.let { roots.add(it) }
        runCatching {
            context.getExternalFilesDirs(null).filterNotNull().forEach { dir ->
                var candidate: File? = dir
                repeat(4) { candidate = candidate?.parentFile }
                candidate?.takeIf { it.exists() && it.isDirectory && it.canRead() }?.let { roots.add(it) }
            }
        }
        return roots.toList()
    }

    /**
     * Every compatible file on the device in one pass, wherever it lives.
     *
     * Folder-by-folder walking only finds what the user thinks to look for — a document in
     * Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents is six levels down and
     * effectively invisible. MediaStore already indexes all of shared storage, across every
     * volume, so one query reaches everything a recursive walk would, in a fraction of the
     * time. Rows whose extension we cannot parse are dropped rather than listed as unopenable.
     */
    private fun queryDeviceWideFiles(
        context: Context,
        diagnostics: MutableList<String>
    ): List<VeritasBrowserFile> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.RELATIVE_PATH
        )
        val results = mutableListOf<VeritasBrowserFile>()
        val seen = HashSet<String>()
        // Every mounted volume, not just the built-in one.
        val volumes = runCatching { MediaStore.getExternalVolumeNames(context) }
            .getOrDefault(setOf(MediaStore.VOLUME_EXTERNAL))
            .ifEmpty { setOf(MediaStore.VOLUME_EXTERNAL) }
        volumes.forEach { volume ->
            val uri = runCatching { MediaStore.Files.getContentUri(volume) }.getOrNull() ?: return@forEach
            runCatching {
                context.contentResolver.query(
                    uri, projection, null, null,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameCol) ?: continue
                        val mime = cursor.getString(mimeCol).orEmpty()
                        val type = fileTypeFor(name, mime) ?: continue
                        val relative = cursor.getString(pathCol).orEmpty().trimEnd('/')
                        if (!seen.add("$relative/$name")) continue
                        results.add(
                            VeritasBrowserFile(
                                uri = ContentUris.withAppendedId(uri, cursor.getLong(idCol)),
                                name = name,
                                mimeType = mime.ifBlank { mimeTypeForFileName(name) },
                                sizeBytes = cursor.getLong(sizeCol),
                                // MediaStore stores seconds; the rest of the browser uses millis.
                                modifiedAt = cursor.getLong(dateCol) * 1000L,
                                rootLabel = "On this phone",
                                relativePath = relative,
                                type = type,
                                isDirectory = false,
                                isSupported = true
                            )
                        )
                    }
                }
            }.onFailure { e ->
                diagnostics.add("Could not index $volume: ${e.message ?: "unavailable"}.")
            }
        }
        return results
    }

    private fun listFileDirectory(
        context: Context,
        location: VeritasBrowserLocation,
        diagnostics: MutableList<String>
    ): List<VeritasBrowserFile> {
        val storageRoot = Environment.getExternalStorageDirectory()
        val volumeRoots = storageVolumeRoots(context)
        val current = location.filePath?.let(::File) ?: storageRoot
        val safeCurrent =
            if (volumeRoots.any { current.absolutePath.startsWith(it.absolutePath) }) current else storageRoot
        if (!safeCurrent.exists()) {
            diagnostics.add("${location.label} no longer exists.")
            return emptyList()
        }
        val children =
            runCatching { safeCurrent.listFiles()?.toList().orEmpty() }.getOrElse { error ->
                diagnostics.add("Android blocked access to ${location.label}: ${error.message ?: "folder is protected"}.")
                emptyList()
            }
        if (children.isEmpty() && safeCurrent.isDirectory && !safeCurrent.canRead()) {
            diagnostics.add("Android protects this folder. Shared storage can be browsed, but private system/app folders may remain unavailable.")
        }
        val atVolumeRoot = volumeRoots.any { it.absolutePath == safeCurrent.absolutePath }
        val currentEntries = children.sortedWith(compareBy<File> { !it.isDirectory }.thenBy {
            it.name.lowercase(Locale.getDefault())
        })
            .mapNotNull { child ->
                val nameLower = child.name.lowercase(Locale.US)
                // Dot-prefixed entries were skipped wholesale, which hid documents parked in
                // folders like .Documents or app caches. Only the navigation aliases are dropped.
                if (child.name == "." || child.name == "..") return@mapNotNull null
                if (!child.isDirectory) {
                    val isBinaryOrSystem = nameLower.endsWith(".bin") ||
                            nameLower.endsWith(".apk") ||
                            nameLower.endsWith(".exe") ||
                            nameLower.endsWith(".so") ||
                            nameLower.endsWith(".class") ||
                            nameLower.endsWith(".dex") ||
                            nameLower.endsWith(".tmp") ||
                            nameLower.endsWith(".temp") ||
                            nameLower.endsWith(".db") ||
                            nameLower.endsWith(".sqlite") ||
                            nameLower.endsWith(".sys") ||
                            nameLower.endsWith(".dll") ||
                            nameLower.endsWith(".log") ||
                            nameLower.endsWith(".dat")
                    if (isBinaryOrSystem) return@mapNotNull null
                }
                val relativePath =
                    child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
                if (child.isDirectory) {
                    VeritasBrowserFile(
                        uri = Uri.fromFile(child),
                        name = child.name.ifBlank { "Folder" },
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        sizeBytes = 0L,
                        modifiedAt = child.lastModified(),
                        rootLabel = location.rootLabel,
                        relativePath = relativePath,
                        isDirectory = true,
                        isSupported = child.canRead(),
                        targetLocation = if (child.canRead()) {
                            VeritasBrowserLocation(
                                rootLabel = location.rootLabel,
                                relativePath = relativePath,
                                filePath = child.absolutePath
                            )
                        } else {
                            null
                        }
                    )
                } else {
                    val type = fileTypeFor(child.name, "")
                    VeritasBrowserFile(
                        uri = Uri.fromFile(child),
                        name = child.name.ifBlank { "Untitled file" },
                        mimeType = mimeTypeForFileName(child.name),
                        sizeBytes = child.length(),
                        modifiedAt = child.lastModified(),
                        rootLabel = location.rootLabel,
                        relativePath = relativePath,
                        type = type ?: VeritasBrowserTab.ALL,
                        isSupported = type != null
                    )
                }
            }
        if (atVolumeRoot) {
            // MediaStore first: it already indexes every volume with no depth limit, so it
            // reaches things the walk below cannot, and does it in one query. The walk still
            // runs afterwards to pick up anything the index misses (.nomedia folders, files
            // written without notifying the media scanner).
            val indexed = queryDeviceWideFiles(context, diagnostics)
            val documentEntries = indexed + collectSupportedDocumentFiles(
                storageRoot = safeCurrent,
                current = safeCurrent,
                diagnostics = diagnostics
            )
            return (documentEntries + currentEntries)
                .distinctBy { it.uri.toString() }
                .sortedWith(compareBy<VeritasBrowserFile> { it.isDirectory }.thenByDescending { it.modifiedAt })
        }
        return currentEntries
    }

    private fun collectSupportedDocumentFiles(
        storageRoot: File,
        current: File,
        diagnostics: MutableList<String>,
        depth: Int = 0,
        results: MutableList<VeritasBrowserFile> = mutableListOf()
    ): List<VeritasBrowserFile> {
        if (depth > MAX_SCAN_DEPTH || results.size >= MAX_SCAN_RESULTS || shouldSkipRecursiveDirectory(
                current,
                storageRoot
            )
        ) return results
        val children = runCatching { current.listFiles()?.toList().orEmpty() }.getOrElse { error ->
            if (depth <= 1) diagnostics.add("Some protected folders could not be indexed: ${error.message ?: "access denied"}.")
            emptyList()
        }
        children.forEach { child ->
            if (results.size >= MAX_SCAN_RESULTS) return@forEach
            if (child.isDirectory) {
                collectSupportedDocumentFiles(storageRoot, child, diagnostics, depth + 1, results)
            } else {
                val type = fileTypeFor(child.name, "")
                if (type != null) {
                    val relativePath =
                        child.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
                    results.add(
                        VeritasBrowserFile(
                            uri = Uri.fromFile(child),
                            name = child.name.ifBlank { "Untitled file" },
                            mimeType = mimeTypeForFileName(child.name),
                            sizeBytes = child.length(),
                            modifiedAt = child.lastModified(),
                            rootLabel = "Phone storage",
                            relativePath = relativePath,
                            type = type,
                            isSupported = true
                        )
                    )
                }
            }
        }
        return results
    }

    private fun shouldSkipRecursiveDirectory(folder: File, storageRoot: File): Boolean {
        val relative = folder.relativeToOrSelf(storageRoot).path.replace(File.separatorChar, '/')
            .lowercase(Locale.getDefault())
        if (relative.isBlank() || relative == ".") return false
        // android/data and android/obb are unreadable on Android 11+ whatever permission we
        // hold, so descending them only wastes time. Everything else — including hidden
        // folders and Android/media, where messaging apps keep received documents — is fair game.
        return relative.startsWith("android/data") ||
                relative.startsWith("android/obb") ||
                relative.contains("/cache")
    }

    private fun listSafDirectory(
        context: Context,
        root: VeritasBrowserRoot,
        location: VeritasBrowserLocation,
        diagnostics: MutableList<String>
    ): List<VeritasBrowserFile> {
        val documentId = location.documentId ?: return emptyList()
        val entries = mutableListOf<VeritasBrowserFile>()
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(root.uri, documentId)
        runCatching {
            context.contentResolver.query(childrenUri, childProjection, null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val childId =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                                ?: continue
                        val name =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                                .orEmpty()
                        if (name.startsWith(".")) continue
                        val mimeType =
                            cursor.stringValue(DocumentsContract.Document.COLUMN_MIME_TYPE)
                                .orEmpty()
                        val isDir = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                        if (!isDir) {
                            val nameLower = name.lowercase(Locale.US)
                            val isBinaryOrSystem = nameLower.endsWith(".bin") ||
                                    nameLower.endsWith(".apk") ||
                                    nameLower.endsWith(".exe") ||
                                    nameLower.endsWith(".so") ||
                                    nameLower.endsWith(".class") ||
                                    nameLower.endsWith(".dex") ||
                                    nameLower.endsWith(".tmp") ||
                                    nameLower.endsWith(".temp") ||
                                    nameLower.endsWith(".db") ||
                                    nameLower.endsWith(".sqlite") ||
                                    nameLower.endsWith(".sys") ||
                                    nameLower.endsWith(".dll") ||
                                    nameLower.endsWith(".log") ||
                                    nameLower.endsWith(".dat")
                            if (isBinaryOrSystem) continue
                        }
                        val size = cursor.longValue(DocumentsContract.Document.COLUMN_SIZE)
                        val modified =
                            cursor.longValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        val childPath =
                            if (location.relativePath.isBlank()) name else "${location.relativePath}/$name"
                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            entries.add(
                                VeritasBrowserFile(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(
                                        root.uri,
                                        childId
                                    ),
                                    name = name.ifBlank { "Folder" },
                                    mimeType = mimeType,
                                    sizeBytes = 0L,
                                    modifiedAt = modified,
                                    rootLabel = root.label,
                                    relativePath = childPath,
                                    isDirectory = true,
                                    targetLocation = VeritasBrowserLocation(
                                        rootLabel = root.label,
                                        relativePath = childPath,
                                        rootUri = root.uri,
                                        documentId = childId
                                    )
                                )
                            )
                        } else {
                            val type = fileTypeFor(name, mimeType)
                            entries.add(
                                VeritasBrowserFile(
                                    uri = DocumentsContract.buildDocumentUriUsingTree(
                                        root.uri,
                                        childId
                                    ),
                                    name = name.ifBlank { "Untitled file" },
                                    mimeType = mimeType,
                                    sizeBytes = size,
                                    modifiedAt = modified,
                                    rootLabel = root.label,
                                    relativePath = childPath,
                                    type = type ?: VeritasBrowserTab.ALL,
                                    isSupported = type != null
                                )
                            )
                        }
                    }
                }
        }.onFailure { error ->
            diagnostics.add("Android blocked access to ${location.label}: ${error.message ?: "folder is protected"}.")
        }
        return entries.sortedWith(compareBy<VeritasBrowserFile> { !it.isDirectory }.thenBy {
            it.name.lowercase(
                Locale.getDefault()
            )
        })
    }

    private fun fileTypeFor(name: String, mimeType: String): VeritasBrowserTab? {
        val lowerName = name.lowercase(Locale.getDefault())
        val lowerMime = mimeType.lowercase(Locale.getDefault())
        fun named(vararg suffixes: String) = suffixes.any { lowerName.endsWith(it) }
        return when {
            lowerMime.contains("pdf") || named(".pdf") -> VeritasBrowserTab.PDF
            // .docm is the macro-enabled variant of the same OOXML package the parser reads.
            lowerMime.contains("wordprocessingml") || named(".docx", ".docm") -> VeritasBrowserTab.DOC
            // .ppt shipped a parser in 2.1.0 (PptLegacyExtractor) but was never listed here,
            // so the browser hid every legacy deck on the device.
            lowerMime.contains("presentationml") ||
                lowerMime.contains("ms-powerpoint") ||
                named(".pptx", ".pptm", ".ppt") -> VeritasBrowserTab.SLIDES
            lowerMime.contains("epub") || named(".epub") -> VeritasBrowserTab.BOOKS
            lowerMime.contains("html") || named(".html", ".htm", ".xhtml") -> VeritasBrowserTab.HTML
            // Anything the plain-text fallback can read. Phones are full of .log and .json
            // that were previously greyed out for no reason.
            lowerMime.startsWith("text/") ||
                named(".txt", ".text", ".md", ".markdown", ".csv", ".tsv", ".log",
                      ".json", ".xml", ".yml", ".yaml", ".rst", ".srt", ".vtt") -> VeritasBrowserTab.TXT

            // HEIC/HEIF is the default camera format on modern Samsung and iPhone handsets;
            // omitting it hid most of the photos on the device from OCR import.
            lowerMime.startsWith("image/") ||
                named(".png", ".jpg", ".jpeg", ".webp", ".bmp", ".tif", ".tiff",
                      ".heic", ".heif", ".avif", ".gif") -> VeritasBrowserTab.OCR

            else -> null
        }
    }

    private fun mimeTypeForFileName(name: String): String {
        val lowerName = name.lowercase(Locale.getDefault())
        return when {
            lowerName.endsWith(".pdf") -> "application/pdf"
            lowerName.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            lowerName.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            lowerName.endsWith(".pptm") -> "application/vnd.ms-powerpoint.presentation.macroEnabled.12"
            lowerName.endsWith(".ppt") -> "application/vnd.ms-powerpoint"
            lowerName.endsWith(".docm") -> "application/vnd.ms-word.document.macroEnabled.12"
            lowerName.endsWith(".epub") -> "application/epub+zip"
            lowerName.endsWith(".xhtml") -> "application/xhtml+xml"
            lowerName.endsWith(".html") || lowerName.endsWith(".htm") -> "text/html"
            lowerName.endsWith(".txt") || lowerName.endsWith(".text") || lowerName.endsWith(".md") ||
                lowerName.endsWith(".markdown") || lowerName.endsWith(".csv") || lowerName.endsWith(".tsv") ||
                lowerName.endsWith(".log") || lowerName.endsWith(".json") || lowerName.endsWith(".xml") ||
                lowerName.endsWith(".yml") || lowerName.endsWith(".yaml") || lowerName.endsWith(".rst") ||
                lowerName.endsWith(".srt") || lowerName.endsWith(".vtt") -> "text/plain"
            lowerName.endsWith(".png") -> "image/png"
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
            lowerName.endsWith(".webp") -> "image/webp"
            // Reported as octet-stream before, which the OCR import path skipped as non-image.
            lowerName.endsWith(".heic") -> "image/heic"
            lowerName.endsWith(".heif") -> "image/heif"
            lowerName.endsWith(".avif") -> "image/avif"
            lowerName.endsWith(".gif") -> "image/gif"
            lowerName.endsWith(".bmp") -> "image/bmp"
            lowerName.endsWith(".tif") || lowerName.endsWith(".tiff") -> "image/tiff"
            else -> "application/octet-stream"
        }
    }

    private fun displayNameForRoot(context: Context, uri: Uri): String {
        return runCatching {
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            context.contentResolver.query(
                documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.stringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME) else null
            }
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Approved folder"
    }

    private fun Cursor.stringValue(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.longValue(columnName: String): Long {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else 0L
    }
}

/** Walk stops at ten levels. Android/media/<app>/<app>/Media/<folder>/Sent is seven, so
 *  messaging-app documents are comfortably inside it. */
private const val MAX_SCAN_DEPTH = 10
private const val MAX_SCAN_RESULTS = 2000

internal fun readableImportMimeTypes(): Array<String> = arrayOf(
    "text/plain",
    "text/*",
    "text/html",
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    // Legacy decks: PptLegacyExtractor reads these, but without the type here the system
    // picker greyed every .ppt out.
    "application/vnd.ms-powerpoint",
    "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
    "application/vnd.ms-word.document.macroEnabled.12",
    "application/epub+zip",
    "application/xhtml+xml",
    "application/octet-stream",
    "image/*"
)
