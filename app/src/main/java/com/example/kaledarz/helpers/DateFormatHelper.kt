package com.example.kaledarz.helpers

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import java.text.ParseException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale

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

        fun getNextDayFromString(date: String): String {
            return getNextDayFromString(date, 1)
        }

        fun getNextDayFromString(date: String, numOfDays: Int): String {
            val calendar = getCalendarFromStrings(date, "00:00")
            calendar.add(Calendar.DAY_OF_MONTH, numOfDays)
            return makeFullDate(calendar)
        }

        fun isEndDateGreaterAndEqualThanStartDate(startDate: String, endDate: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            return sdf.parse(startDate) <= sdf.parse(endDate)
        }

        fun isEndDateGreaterThanStartDate(startDate: String, endDate: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            return sdf.parse(startDate) < sdf.parse(endDate)
        }

        fun isEndDateEqualToStartDate(startDate: String, endDate: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            return sdf.parse(startDate) == sdf.parse(endDate)
        }

        fun validate(dateStr: String, dateFormat: String): Boolean {
            val sdf = SimpleDateFormat(dateFormat, locale)
            sdf.isLenient = false
            try {
                sdf.parse(dateStr)
            } catch (e: ParseException) {
                return false
            }
            return true
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

        private fun makeTwoCipherNumber(time: Int): String {
            return if (time.toString().length == 1) {
                "0$time"
            } else {
                time.toString()
            }
        }

        fun makeFullHour(hour: Int, minute: Int): String {
            return makeTwoCipherNumber(hour) + ":" + makeTwoCipherNumber(minute)
        }

        fun makeFullDate(year: Int, month: Int, day: Int): String {
            return makeTwoCipherNumber(day) + "-" + makeTwoCipherNumber(month) + "-" + year
        }

        private fun makeFullDate(calendar: Calendar): String {
            return makeFullDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun getCurrentDateTime(): String {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss", locale).format(Date())
        }

        fun isFirstDateGreaterThanSecond(date1: String, date2: String): Boolean {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", locale)
            return sdf.parse(date1) > sdf.parse(date2)
        }

        fun isFirstDateGreaterAndEqualToSecond(
            date1: String,
            date2: String,
            pattern: String
        ): Boolean {
            val sdf = SimpleDateFormat(pattern, locale)
            return sdf.parse(date1) >= sdf.parse(date2)
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

        fun getLastDayOfMonth(year: Int, month: Int): Int {
            val firstDayOfMonth = LocalDate.of(year, month, 1)
            return firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
        }

        fun getSecondLastMondayOfMonth(month: Int, year: Int): Int {
            val lastDayOfMonth = YearMonth.of(year, month + 1).atEndOfMonth()
            var lastMonday = lastDayOfMonth
            while (lastMonday.dayOfWeek != DayOfWeek.MONDAY) {
                lastMonday = lastMonday.minusDays(1)
            }
            return lastMonday.dayOfMonth - 7
        }

        fun getSecondSundayOfMonth(month: Int, year: Int): Int {
            val firstDayOfMonth = YearMonth.of(year, month + 1).atDay(1)
            var firstSunday = firstDayOfMonth
            while (firstSunday.dayOfWeek != DayOfWeek.SUNDAY) {
                firstSunday = firstSunday.plusDays(1)
            }

            return firstSunday.dayOfMonth + 7
        }

        fun getPreviousMonthAndYear(month: Int, year: Int): Pair<Int, Int> {
            return if (month == 0) {
                Pair(11, year - 1)
            } else {
                Pair(month - 1, year)
            }
        }

        fun getNextMonthAndYear(month: Int, year: Int): Pair<Int, Int> {
            return if (month == 11) {
                Pair(0, year + 1)
            } else {
                Pair(month + 1, year)
            }
        }
    }
}