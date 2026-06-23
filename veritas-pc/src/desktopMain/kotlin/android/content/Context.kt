package android.content

import java.io.File
import com.veritas.reader.DesktopPreferences

open class Context {
    val applicationContext: Context get() = this

    val filesDir: File by lazy {
        val userHome = System.getProperty("user.home")
        File(userHome, ".veritas_reader").apply { mkdirs() }
    }

    val packageName: String get() = "com.veritas.reader"
    val contentResolver = ContentResolver()
    val cacheDir: File get() = filesDir

    fun getSharedPreferences(name: String, mode: Int): DesktopPreferences {
        return DesktopPreferences()
    }

    fun startService(intent: Intent) {
        com.veritas.reader.PlaybackService.start(intent)
    }
    fun startActivity(intent: Intent) {}

    val packageManager: android.content.pm.PackageManager
        get() = android.content.pm.PackageManager()

    fun getSystemService(name: String): Any {
        return when (name) {
            CLIPBOARD_SERVICE -> ClipboardManager()
            else -> Any()
        }
    }

    companion object {
        const val MODE_PRIVATE = 0
        const val CLIPBOARD_SERVICE = "clipboard"
    }
}
