package android.widget

import android.content.Context

class Toast(val context: Context) {
    fun show() {
        // Log toasts to standard console on desktop
    }

    companion object {
        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1

        fun makeText(context: Context, text: CharSequence, duration: Int): Toast {
            println("[Toast Notification]: $text")
            return Toast(context)
        }
    }
}
