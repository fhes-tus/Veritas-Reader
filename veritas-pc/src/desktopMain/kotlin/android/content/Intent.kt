package android.content

class Intent {
    var action: String? = null
    var type: String? = null
    var flags: Int = 0
    var clipData: ClipData? = null
    val extras = mutableMapOf<String, Any>()

    var `package`: String? = null

    constructor(action: String? = null) {
        this.action = action
    }

    constructor(context: Context, clazz: Class<*>) {
        this.action = clazz.name
    }

    fun setAction(action: String?): Intent {
        this.action = action
        return this
    }

    fun putExtra(name: String, value: String?): Intent {
        if (value != null) extras[name] = value
        return this
    }

    fun putExtra(name: String, value: Int): Intent {
        extras[name] = value
        return this
    }

    fun putExtra(name: String, value: Long): Intent {
        extras[name] = value
        return this
    }

    fun putExtra(name: String, value: Boolean): Intent {
        extras[name] = value
        return this
    }

    fun putExtra(name: String, value: Any?): Intent {
        if (value != null) extras[name] = value
        return this
    }

    fun setPackage(pkg: String?): Intent {
        this.`package` = pkg
        return this
    }

    fun addFlags(flags: Int): Intent {
        this.flags = this.flags or flags
        return this
    }

    fun getStringExtra(name: String): String? {
        return extras[name] as? String
    }

    fun getIntExtra(name: String, defaultValue: Int): Int {
        return (extras[name] as? Number)?.toInt() ?: defaultValue
    }

    fun getFloatExtra(name: String, defaultValue: Float): Float {
        return (extras[name] as? Number)?.toFloat() ?: defaultValue
    }

    fun getLongExtra(name: String, defaultValue: Long): Long {
        return (extras[name] as? Number)?.toLong() ?: defaultValue
    }

    fun getBooleanExtra(name: String, defaultValue: Boolean): Boolean {
        return (extras[name] as? Boolean) ?: defaultValue
    }

    companion object {
        const val ACTION_SEND = "android.intent.action.SEND"
        const val ACTION_VIEW = "android.intent.action.VIEW"
        const val EXTRA_SUBJECT = "android.intent.extra.SUBJECT"
        const val EXTRA_TEXT = "android.intent.extra.TEXT"
        const val EXTRA_STREAM = "android.intent.extra.STREAM"
        const val FLAG_ACTIVITY_NEW_TASK = 268435456
        const val FLAG_ACTIVITY_CLEAR_TOP = 67108864
        const val FLAG_GRANT_READ_URI_PERMISSION = 1

        fun createChooser(target: Intent, title: CharSequence?): Intent {
            return target
        }
    }
}
