package com.veritas.reader

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.Locale

object VeritasFileBrowserScanner {

    fun persistedRoots(context: Context): List<VeritasBrowserRoot> {
        val userHome = System.getProperty("user.home")
        return listOf(
            VeritasBrowserRoot(Uri(userHome), "Home Directory")
        )
    }

    fun initialLocation(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean
    ): VeritasBrowserLocation? {
        val home = System.getProperty("user.home")
        return VeritasBrowserLocation(
            rootLabel = "Home Directory",
            filePath = home
        )
    }

    fun scan(
        context: Context,
        roots: List<VeritasBrowserRoot>,
        includeAllFilesAccess: Boolean = false,
        location: VeritasBrowserLocation? = null
    ): VeritasFileBrowserScanResult {
        val activeLocation = location ?: initialLocation(context, roots, includeAllFilesAccess)
        if (activeLocation == null) {
            return VeritasFileBrowserScanResult(emptyList())
        }
        val path = activeLocation.filePath ?: System.getProperty("user.home")
        val dir = File(path)
        val filesList = mutableListOf<VeritasBrowserFile>()
        val diagnostics = mutableListOf<String>()

        if (dir.exists() && dir.isDirectory) {
            val list = dir.listFiles()
            if (list != null) {
                for (file in list) {
                    val name = file.name
                    if (name.startsWith(".")) continue
                    val isDir = file.isDirectory
                    val relPath = if (activeLocation.relativePath.isEmpty()) name else "${activeLocation.relativePath}/$name"
                    filesList.add(
                        VeritasBrowserFile(
                            uri = Uri(file.absolutePath),
                            name = name,
                            mimeType = if (isDir) "directory" else mimeTypeForName(name),
                            sizeBytes = if (isDir) 0L else file.length(),
                            modifiedAt = file.lastModified(),
                            rootLabel = activeLocation.rootLabel,
                            relativePath = relPath,
                            isDirectory = isDir,
                            isSupported = isDir || isSupportedExtension(name),
                            targetLocation = if (isDir) VeritasBrowserLocation(
                                rootLabel = activeLocation.rootLabel,
                                relativePath = relPath,
                                filePath = file.absolutePath
                            ) else null
                        )
                    )
                }
            } else {
                diagnostics.add("Unable to read directory contents.")
            }
        } else {
            diagnostics.add("Directory does not exist.")
        }

        return VeritasFileBrowserScanResult(
            files = filesList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.getDefault()) })),
            location = activeLocation,
            diagnostics = diagnostics
        )
    }

    private fun mimeTypeForName(name: String): String {
        return when (name.substringAfterLast('.', "").lowercase(Locale.getDefault())) {
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "epub" -> "application/epub+zip"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }
    }

    private fun isSupportedExtension(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase(Locale.getDefault())
        return ext in listOf("pdf", "txt", "epub", "docx")
    }
}
