package android.app

import android.content.Context

class DatePickerDialog(
    context: Context,
    listener: (Any?, Int, Int, Int) -> Unit,
    year: Int,
    month: Int,
    dayOfMonth: Int
) {
    val datePicker = DatePicker()
    fun show() {}

    class DatePicker {
        var minDate: Long = 0L
    }
}
