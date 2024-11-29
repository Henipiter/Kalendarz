package com.example.kaledarz.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.CalendarDay
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.DrawableHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private var databaseHelper: MyDatabaseHelper? = null

    var noteList = MutableLiveData<ArrayList<Note>>()
    var filteredList = MutableLiveData<ArrayList<Note>>()

    var calendarDayList = MutableLiveData<ArrayList<CalendarDay>>()
    private var boundaryPreviousCalendarDayList = ArrayList<CalendarDay>()
    private var previousCalendarDayList = ArrayList<CalendarDay>()
    private var currentCalendarDayList = ArrayList<CalendarDay>()
    private var nextCalendarDayList = ArrayList<CalendarDay>()
    private var boundaryNextCalendarDayList = ArrayList<CalendarDay>()

    init {
        databaseHelper = MyDatabaseHelper(getApplication<Application>().applicationContext)
    }

    fun getFilteredList(): ArrayList<Note> {
        return filteredList.value ?: arrayListOf()
    }

    fun readAllNotes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                databaseHelper?.let { databaseHelper ->
                    noteList.postValue(databaseHelper.readAllData())
                }
            }
        }
    }

    fun filterNoteList(chosenDate: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val list = filterNoteListForSingleDay(chosenDate)
                filteredList.postValue(list)
            }
        }
    }

    private suspend fun filterNoteListForSingleDay(chosenDate: String): ArrayList<Note> {
        val list = arrayListOf<Note>()
        noteList.value?.let {
            for (note in it) {
                if (chosenDate >= note.startDate && chosenDate <= note.endDate)
                    list.add(note)
            }
        }
        Note.computeStatusForNoteList(list)
        return list
    }

    fun prepareCalendarEvents(currentMonth: Int, currentYear: Int) {
        currentCalendarDayList.clear()
        previousCalendarDayList.clear()
        nextCalendarDayList.clear()
        boundaryPreviousCalendarDayList.clear()
        boundaryNextCalendarDayList.clear()
        if (noteList.value?.size == 0) {
            return
        }
        viewModelScope.launch {

            withContext(Dispatchers.IO) {


                Log.d("DATEE", "prepareCalendarEvents")
                currentCalendarDayList.addAll(
                    prepareCurrentCalendarEvents(
                        currentMonth,
                        currentYear
                    )
                )
                previousCalendarDayList.addAll(
                    preparePreviousCalendarEvents(
                        currentMonth,
                        currentYear
                    )
                )
                nextCalendarDayList.addAll(prepareNextCalendarEvents(currentMonth, currentYear))
                boundaryNextCalendarDayList.addAll(
                    prepareBoundaryNextCalendarEvents(
                        currentMonth,
                        currentYear
                    )
                )
                boundaryPreviousCalendarDayList.addAll(
                    prepareBoundaryPreviousCalendarEvents(currentMonth, currentYear)
                )
                buildAllList()
                Log.d("DATEE", "prepareCalendarEvents END")
            }
        }
    }

    fun preparePreviousCalendarEventsAA(currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                boundaryNextCalendarDayList.clear()
                boundaryNextCalendarDayList.addAll(nextCalendarDayList)
                nextCalendarDayList.clear()
                nextCalendarDayList.addAll(currentCalendarDayList)
                currentCalendarDayList.clear()
                currentCalendarDayList.addAll(previousCalendarDayList)
                previousCalendarDayList.clear()
                previousCalendarDayList.addAll(
                    preparePreviousCalendarEvents(
                        currentMonth,
                        currentYear
                    )
                )
                boundaryPreviousCalendarDayList.clear()
                boundaryPreviousCalendarDayList.addAll(
                    prepareBoundaryPreviousCalendarEvents(currentMonth, currentYear)
                )
                buildAllList()
            }
        }
    }

    fun prepareNextCalendarEventsAA(currentMonth: Int, currentYear: Int) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                boundaryPreviousCalendarDayList.clear()
                boundaryPreviousCalendarDayList.addAll(previousCalendarDayList)
                previousCalendarDayList.clear()
                previousCalendarDayList.addAll(currentCalendarDayList)
                currentCalendarDayList.clear()
                currentCalendarDayList.addAll(nextCalendarDayList)
                nextCalendarDayList.clear()
                nextCalendarDayList.addAll(prepareNextCalendarEvents(currentMonth, currentYear))
                boundaryNextCalendarDayList.clear()
                boundaryNextCalendarDayList.addAll(
                    prepareBoundaryNextCalendarEvents(currentMonth, currentYear)
                )
                buildAllList()
            }
        }
    }

    private fun buildAllList() {

        Log.d("EEE", "========================")
        Log.d("EEE", "boundaryPreviousCalendarDayList: ${boundaryPreviousCalendarDayList.size}")
        Log.d("EEE", "previousCalendarDayList: ${previousCalendarDayList.size}")
        Log.d("EEE", "currentCalendarDayList: ${currentCalendarDayList.size}")
        Log.d("EEE", "nextCalendarDayList: ${nextCalendarDayList.size}")
        Log.d("EEE", "boundaryNextCalendarDayList: ${boundaryNextCalendarDayList.size}")
        val allList = arrayListOf<CalendarDay>()
        allList.addAll(boundaryPreviousCalendarDayList)
        allList.addAll(previousCalendarDayList)
        allList.addAll(currentCalendarDayList)
        allList.addAll(nextCalendarDayList)
        allList.addAll(boundaryNextCalendarDayList)
        calendarDayList.postValue(allList)
    }

    private suspend fun prepareCurrentCalendarEvents(
        currentMonth: Int,
        currentYear: Int
    ): List<CalendarDay> {
        val date = LocalDate.of(currentYear, currentMonth + 1, 1)
        Log.d("DATEE", "prepareNextCalendarEvents $date")
        return iterateCalendar(date, date.lengthOfMonth())

    }

    private suspend fun prepareNextCalendarEvents(
        currentMonth: Int,
        currentYear: Int
    ): List<CalendarDay> {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.plusMonths(1)
        Log.d("DATEE", "prepareNextCalendarEvents $date")
        return iterateCalendar(date, date.lengthOfMonth())

    }

    private suspend fun prepareBoundaryNextCalendarEvents(
        currentMonth: Int, currentYear: Int
    ): List<CalendarDay> {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.plusMonths(2)
        val lastDayOfNextNextMonth = DateFormatHelper.getSecondSundayOfMonth(
            date.monthValue - 1, date.year
        )
        Log.d("DATEE", "prepareBoundaryNextCalendarEvents $date")
        return iterateCalendar(date, lastDayOfNextNextMonth)

    }

    private suspend fun prepareBoundaryPreviousCalendarEvents(
        currentMonth: Int,
        currentYear: Int
    ): List<CalendarDay> {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.minusMonths(2)
        val firstDayOfPreviousPreviousMonth = DateFormatHelper.getSecondLastMondayOfMonth(
            date.monthValue - 1, date.year
        )
        date.plusDays(firstDayOfPreviousPreviousMonth.toLong())
        Log.d("DATEE", "prepareBoundaryPreviousCalendarEvents $date")
        return iterateCalendar(date, date.lengthOfMonth() - firstDayOfPreviousPreviousMonth)

    }

    private suspend fun preparePreviousCalendarEvents(
        currentMonth: Int,
        currentYear: Int
    ): List<CalendarDay> {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.minusMonths(1)
        Log.d("DATEE", "preparePreviousCalendarEvents $date")
        return iterateCalendar(date, date.lengthOfMonth())
    }

    private suspend fun iterateCalendar(startDate: LocalDate, daysCount: Int): List<CalendarDay> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayList = arrayListOf<CalendarDay>()
        var currentDate = startDate
        for (i in 0..daysCount) {
            currentDate = currentDate.plusDays(1)
            val notes = filterNoteListForSingleDay(currentDate.format(formatter))
            if (notes.isNotEmpty()) {
                Note.computeStatusForNoteList(notes)
                var isRegularNotePresent = false
                val cyclicNoteStatuses = HashSet<Status>()
                notes.forEach { note ->
                    if (note.cyclic) {
                        cyclicNoteStatuses.add(note.status)
                    } else {
                        isRegularNotePresent = true
                    }
                }

                dayList.add(
                    getCalendarDay(
                        currentDate.year,
                        currentDate.monthValue - 1,
                        currentDate.dayOfMonth,
                        DrawableHelper.getColorByNoteCyclicType(isRegularNotePresent),
                        DrawableHelper.getDrawableByStatus(cyclicNoteStatuses, isRegularNotePresent)
                    )
                )
            }
        }
        return dayList
    }

    private suspend fun prepareCurrentCalendarEvents9(
        currentMonth: Int,
        currentYear: Int,
        firstDay: Int = 1,
        defaultLastDay: Int = -1
    ): List<CalendarDay> {
        Log.d(
            "DATEE",
            "currentMonth $currentMonth currentYear $currentYear " +
                    "firstDay $firstDay defaultLastDay $defaultLastDay"
        )
        val dayList = arrayListOf<CalendarDay>()
        val montAndYearString = "$currentYear-" + String.format("%02d", currentMonth + 1) + "-"
        val lastDay = if (defaultLastDay != -1) {
            defaultLastDay
        } else {
            DateFormatHelper.getLastDayOfMonth(currentYear, currentMonth + 1)
        }
        for (i in firstDay..lastDay) {
            val currentDate = montAndYearString + String.format("%02d", i)
            val notes = filterNoteListForSingleDay(currentDate)
            if (notes.isNotEmpty()) {
                Note.computeStatusForNoteList(notes)
                var isRegularNotePresent = false
                val cyclicNoteStatuses = HashSet<Status>()
                notes.forEach { note ->
                    if (note.cyclic) {
                        cyclicNoteStatuses.add(note.status)
                    } else {
                        isRegularNotePresent = true
                    }
                }

                dayList.add(
                    getCalendarDay(
                        currentYear,
                        currentMonth,
                        i,
                        DrawableHelper.getColorByNoteCyclicType(isRegularNotePresent),
                        DrawableHelper.getDrawableByStatus(cyclicNoteStatuses, isRegularNotePresent)
                    )
                )
            }
        }
        return dayList
    }

    private fun getCalendarDay(
        year: Int,
        month: Int,
        day: Int,
        @ColorRes labelColor: Int?,
        @DrawableRes imageResource: Int?
    ): CalendarDay {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val calendarDay = CalendarDay(calendar)
        calendarDay.imageResource = imageResource
        calendarDay.labelColor = labelColor
        return calendarDay
    }

}