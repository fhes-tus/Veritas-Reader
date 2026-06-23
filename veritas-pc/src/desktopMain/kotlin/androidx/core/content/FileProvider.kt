package androidx.core.content

import android.content.Context
import android.net.Uri
import java.io.File

object FileProvider {
    fun getUriForFile(context: Context, authority: String, file: File): Uri {
        return Uri(file.absolutePath)
    }
}
