package com.example.kaledarz.helpers

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Button

class PickerHelper(private val context: Context) {


    fun runDatePicker(button: Button) {
        val yearValue = button.text.subSequence(6, 10).toString().toInt()
        val monthValue = button.text.subSequence(3, 5).toString().toInt() - 1
        val dayValue = button.text.subSequence(0, 2).toString().toInt()
        DatePickerDialog(
            context,
            { _, sYear, sMonthOfYear, sDayOfMonth ->
                button.text = DateFormatHelper.makeFullDate(sYear, sMonthOfYear + 1, sDayOfMonth)
            }, yearValue, monthValue, dayValue
        ).show()
    }

    fun runTimePicker(button: Button) {
        val hourValue = button.text.subSequence(0, 2).toString().toInt()
        val minuteValue = button.text.subSequence(3, 5).toString().toInt()

        TimePickerDialog(
            context, { _, sHour, sMinute ->
                button.text = DateFormatHelper.makeFullHour(sHour, sMinute)
            }, hourValue, minuteValue, true
        ).show()
    }


}