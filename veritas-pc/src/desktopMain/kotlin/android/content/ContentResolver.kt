package android.content

import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.FileOutputStream

class UriPermission {
    val uri: Uri = Uri("")
    val isReadPermission: Boolean = false
}

class ParcelFileDescriptor(val statSize: Long) : java.io.Closeable {
    override fun close() {}
}

class ContentResolver {
    val persistedUriPermissions: List<UriPermission> = emptyList()

    fun releasePersistableUriPermission(uri: Uri, flags: Int) {}
    
    fun takePersistableUriPermission(uri: Uri, flags: Int) {}

    fun openInputStream(uri: Uri): InputStream? {
        return try {
            FileInputStream(File(uri.path ?: uri.toString()))
        } catch (e: Exception) {
            null
        }
    }

    fun openOutputStream(uri: Uri): OutputStream? {
        return try {
            FileOutputStream(File(uri.path ?: uri.toString()))
        } catch (e: Exception) {
            null
        }
    }

    fun getType(uri: Uri): String? {
        val path = (uri.path ?: uri.toString()).lowercase()
        return when {
            path.endsWith(".pdf") -> "application/pdf"
            path.endsWith(".epub") -> "application/epub+zip"
            path.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            path.endsWith(".html") || path.endsWith(".htm") -> "text/html"
            else -> "text/plain"
        }
    }

    fun openFileDescriptor(uri: Uri, mode: String): ParcelFileDescriptor? {
        return try {
            val file = File(uri.path ?: uri.toString())
            if (file.exists()) ParcelFileDescriptor(file.length()) else null
        } catch (e: Exception) {
            null
        }
    }
}
