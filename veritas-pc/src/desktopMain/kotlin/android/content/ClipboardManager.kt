package android.content

class ActivityNotFoundException(message: String? = null) : Exception(message)



class ClipboardManager {
    fun setPrimaryClip(clip: ClipData) {
        if (clip.text.isNotEmpty()) {
            runCatching {
                val selection = java.awt.datatransfer.StringSelection(clip.text)
                java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            }
        }
    }

    companion object {
        fun newPlainText(label: CharSequence, text: CharSequence): ClipData = ClipData.newPlainText(label, text)
    }
}
