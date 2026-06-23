package android.widget

import android.content.Context
import android.view.ActionMode

class TextView(val context: Context) {
    var text: CharSequence? = null
    var customSelectionActionModeCallback: ActionMode.Callback? = null
    var includeFontPadding: Boolean = false
    var textSize: Float = 0f
    val layout: android.text.Layout? = null
    
    fun setTextColor(color: Int) {}
    fun setLineSpacing(add: Float, mult: Float) {}
    fun setTextIsSelectable(selectable: Boolean) {}

    val selectionStart: Int get() = 0
    val selectionEnd: Int get() = 0

    fun getOffsetForPosition(x: Float, y: Float): Int = 0
    fun setOnTouchListener(listener: (TextView, android.view.MotionEvent) -> Boolean) {}
    fun clearFocus() {}
}
