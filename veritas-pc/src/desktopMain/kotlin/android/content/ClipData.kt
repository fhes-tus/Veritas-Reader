package android.content

import android.net.Uri

class ClipData(val text: String = "", val uri: Uri? = null) {
    companion object {
        fun newRawUri(label: CharSequence, uri: Uri): ClipData = ClipData(uri = uri)
        fun newPlainText(label: CharSequence, text: CharSequence): ClipData = ClipData(text = text.toString())
    }
}
