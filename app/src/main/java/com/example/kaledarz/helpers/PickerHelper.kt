package com.example.kaledarz.helpers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context

class PickerHelper(private val context: Context) {


    fun runDatePicker(buttonText: CharSequence, confirm: (String) -> Unit) {
        val yearValue = buttonText.subSequence(6, 10).toString().toInt()
        val monthValue = buttonText.subSequence(3, 5).toString().toInt() - 1
        val dayValue = buttonText.subSequence(0, 2).toString().toInt()
        DatePickerDialog(
            context,
            { _, sYear, sMonthOfYear, sDayOfMonth ->
                val date = DateFormatHelper.changeDateFormatToUser(
                    DateFormatHelper.makeFullDate(sYear, sMonthOfYear + 1, sDayOfMonth)
                )
                confirm(date)
            }, yearValue, monthValue, dayValue
        ).show()
    }

    fun runTimePicker(buttonText: CharSequence, confirm: (String) -> Unit) {
        val hourValue = buttonText.subSequence(0, 2).toString().toInt()
        val minuteValue = buttonText.subSequence(3, 5).toString().toInt()

        TimePickerDialog(
            context, { _, sHour, sMinute ->
                val hour = DateFormatHelper.makeFullHour(sHour, sMinute)
                confirm(hour)
            }, hourValue, minuteValue, true
        ).show()
    }


}