package android.net

class Uri(private val uriString: String) {
    val path: String?
        get() = if (uriString.contains("://")) uriString.substringAfter("://") else uriString

    val scheme: String?
        get() = if (uriString.contains("://")) uriString.substringBefore("://") else "file"

    val lastPathSegment: String?
        get() = path?.replace('\\', '/')?.substringAfterLast('/')

    override fun toString(): String = uriString

    companion object {
        fun parse(uriString: String): Uri = Uri(uriString)
    }
}
