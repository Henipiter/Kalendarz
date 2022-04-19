package com.example.kaledarz

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import java.util.*

class DateFormatHelper {

    fun getCalendarFromStrings(date: String, clock: String): Calendar {

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, date.split("-")[0].toInt())
        c.set(Calendar.MONTH, date.split("-")[1].toInt())
        c.set(Calendar.DAY_OF_MONTH, date.split("-")[2].toInt())
        c[Calendar.HOUR_OF_DAY] = clock.split(":")[0].toInt()
        c[Calendar.MINUTE] = clock.split(":")[1].toInt()
        c[Calendar.SECOND] = 0
        return c
    }

    fun isEndDateGreaterThanStartDate(startDate: String, endDate: String): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        return sdf.parse(startDate) < sdf.parse(endDate)
    }

    private fun isEndDateEqualToStartDate(startDate: String, endDate: String): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        return sdf.parse(startDate) == sdf.parse(endDate)
    }

    fun isEndTimeGreaterThanStartTime(startTime: String, endTime: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.parse(startTime) < sdf.parse(endTime)
    }

    fun isCorrectDate(
        startDate: String,
        endDate: String,
        startTime: String,
        endTime: String
    ): Boolean {
        return isEndDateGreaterThanStartDate(startDate, endDate) ||
                (isEndDateEqualToStartDate(startDate, endDate) &&
                        isEndTimeGreaterThanStartTime(startTime, endTime))
    }

    fun updateDateInView(calendar: Calendar): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return sdf.format(calendar.time)
    }

    fun setHour(time: Int): String {
        return if (time.toString().length == 1) {
            "0$time"
        } else {
            time.toString()
        }
    }

    fun setMinutes(sMinute: Int): String {
        return if (sMinute.toString().length == 1) {
            "0$sMinute"
        } else {
            sMinute.toString()
        }
    }

}