package com.example.kaledarz

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import java.util.*

class DateFormatHelper {
    companion object {
        private var locale = Locale.US

        fun getCalendarFromStrings(date: String, clock: String): Calendar {

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, date.split("-")[0].toInt())
            calendar.set(Calendar.MONTH, date.split("-")[1].toInt() - 1)
            calendar.set(Calendar.YEAR, date.split("-")[2].toInt())
            calendar.set(Calendar.HOUR_OF_DAY, clock.split(":")[0].toInt())
            calendar.set(Calendar.MINUTE, clock.split(":")[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            return calendar
        }

        fun isEndDateGreaterThanStartDate(startDate: String, endDate: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            return sdf.parse(startDate) < sdf.parse(endDate)
        }

        private fun isEndDateEqualToStartDate(startDate: String, endDate: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            return sdf.parse(startDate) == sdf.parse(endDate)
        }

        fun isEndTimeGreaterThanStartTime(startTime: String, endTime: String): Boolean {
            val sdf = SimpleDateFormat("HH:mm", locale)
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
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
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

        fun getCurrentDateTimeForDatabase(): String? {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale).format(Date())
        }

        fun getCurrentDateTime(): String {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss", locale).format(Date())
        }

        fun isFirstDateGreaterThanSecond(date1: String, date2: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", locale)
            return sdf.parse(date1) > sdf.parse(date2)
        }

        fun getTodayDate(calendarInMillis: Long): String {
            val calender = Calendar.getInstance()
            calender.timeInMillis = calendarInMillis
            val year = calender[java.util.Calendar.YEAR].toString()
            var month = (1 + calender[java.util.Calendar.MONTH]).toString()
            var curDate = calender[java.util.Calendar.DAY_OF_MONTH].toString()

            if (curDate.length == 1) {
                curDate = "0$curDate"
            }
            if (month.length == 1) {
                month = "0$month"
            }
            return "$curDate-$month-$year"
        }

        fun getChosenDate(year: Int, month: Int, dayOfMonth: Int): String {
            val curDate = if (dayOfMonth < 10) {
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            val monthStr: String = if (month + 1 < 10) {
                "0" + (month + 1).toString()
            } else {
                (month + 1).toString()
            }
            return "$curDate-$monthStr-$year"
        }
    }
}