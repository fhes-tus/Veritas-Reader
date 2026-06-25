package android.net

class Uri(private val uriString: String) {
    val path: String?
        get() {
            val rawPath = if (uriString.contains("://")) {
                uriString.substringAfter("://")
            } else {
                uriString
            }
            return if (rawPath.startsWith("/") && rawPath.length > 2 && rawPath[1].isLetter() && rawPath[2] == ':') {
                rawPath.substring(1)
            } else {
                rawPath
            }
        }

    val scheme: String?
        get() = if (uriString.contains("://")) uriString.substringBefore("://") else "file"

    val lastPathSegment: String?
        get() = path?.replace('\\', '/')?.substringAfterLast('/')

    override fun toString(): String = uriString

    companion object {
        fun parse(uriString: String): Uri = Uri(uriString)
    }
}
