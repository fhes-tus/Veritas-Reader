package androidx.core.content

import android.content.Context
import android.content.Intent

object ContextCompat {
    fun startForegroundService(context: Context, intent: Intent) {
        context.startService(intent)
    }

    fun checkSelfPermission(context: Context, permission: String): Int {
        return 0 // PackageManager.PERMISSION_GRANTED
    }
}
