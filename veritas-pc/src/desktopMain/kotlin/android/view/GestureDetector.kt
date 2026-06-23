package android.view

import android.content.Context

class GestureDetector(context: Context, listener: OnGestureListener) {
    fun onTouchEvent(event: MotionEvent): Boolean = false

    interface OnGestureListener

    open class SimpleOnGestureListener : OnGestureListener {
        open fun onDoubleTap(event: MotionEvent): Boolean = false
    }
}
