package android.text

interface Spanned {
    companion object {
        const val SPAN_EXCLUSIVE_EXCLUSIVE = 33
    }
}

interface Spannable : Spanned {
    fun setSpan(what: Any, start: Int, end: Int, flags: Int)
}

class SpannableString(private val source: CharSequence) : Spannable, CharSequence {
    override val length: Int get() = source.length
    override fun get(index: Int): Char = source[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = source.subSequence(startIndex, endIndex)
    
    override fun setSpan(what: Any, start: Int, end: Int, flags: Int) {}
    override fun toString(): String = source.toString()
}

object Selection {
    fun removeSelection(text: Spannable) {}
}
