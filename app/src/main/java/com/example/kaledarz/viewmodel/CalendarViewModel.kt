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
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private var databaseHelper: MyDatabaseHelper? = null

    var noteList = MutableLiveData<ArrayList<Note>>()
    var filteredList = MutableLiveData<ArrayList<Note>>()

    val atomicBoolean = AtomicBoolean(false)
    var calendarDayList = MutableLiveData<ArrayList<CalendarDay>>()
    private val monthsList: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var currentPageUpdated = MutableLiveData(false)
    var shouldUpdateGrid = MutableLiveData(false)

    init {
        databaseHelper = MyDatabaseHelper(getApplication<Application>().applicationContext)
    }

    fun getFilteredList(): ArrayList<Note> {
        return filteredList.value ?: arrayListOf()
    }

    fun readAllNotes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                atomicBoolean.set(false)
                databaseHelper?.let { databaseHelper ->
                    noteList.postValue(databaseHelper.readAllData())
                    atomicBoolean.set(true)
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

    fun prepareCurrent(currentMonth: Int, currentYear: Int) {
        val date = LocalDate.of(currentYear, currentMonth + 1, 1)
        currentPageUpdated.postValue(false)
        monthsList.clear()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("DATEE", "prepareCurrent ${date.monthValue}-${date.year}")
                val list = iterateCalendar(date, date.lengthOfMonth())
                if (addMonthsList("${date.monthValue}-${date.year}")) {
                    calendarDayList.postValue(ArrayList(list))
                    currentPageUpdated.postValue(true)
                    shouldUpdateGrid.postValue(true)
                }
                Log.d("DATEE", "monthsList $monthsList")
                Log.d("DATEE", "calendarDayList size ${calendarDayList.value?.size ?: 0}")
            }
        }
    }

    private fun isMonthsListContains(text: String): Boolean {
        synchronized(monthsList) {
            return monthsList.contains(text)
        }
    }

    private fun addMonthsList(text: String): Boolean {
        synchronized(monthsList) {
            if (!monthsList.contains(text)) {
                monthsList.add(text)
                return true
            }
            return false
        }
    }

    fun preparePrevious(currentMonth: Int, currentYear: Int) {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.minusMonths(1)
        if (isMonthsListContains("${date.monthValue}-${date.year}")) {
            Log.d("DATEE", "preparePrevious - stop - exits ${date.monthValue}-${date.year}")
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("DATEE", "preparePrevious ${date.monthValue}-${date.year}")
                val list = iterateCalendar(date, date.lengthOfMonth())
                if (currentPageUpdated.value == true) {
                    if (addMonthsList("${date.monthValue}-${date.year}")) {
                        calendarDayList.value?.addAll(list)
                        shouldUpdateGrid.postValue(true)
                    }
                    Log.d("DATEE", "monthsList $monthsList")
                    Log.d("DATEE", "calendarDayList size ${calendarDayList.value?.size ?: 0}")
                }
            }
        }
    }

    fun prepareNext(currentMonth: Int, currentYear: Int) {
        var date = LocalDate.of(currentYear, currentMonth + 1, 1)
        date = date.plusMonths(1)

        if (isMonthsListContains("${date.monthValue}-${date.year}")) {
            Log.d("DATEE", "prepareNext - stop - exits ${date.monthValue}-${date.year}")
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("DATEE", "prepareNext ${date.monthValue}-${date.year}")
                val list = iterateCalendar(date, date.lengthOfMonth())
                if (currentPageUpdated.value == true) {
                    if (addMonthsList("${date.monthValue}-${date.year}")) {
                        calendarDayList.value?.addAll(list)
                        shouldUpdateGrid.postValue(true)
                    }
                    Log.d("DATEE", "monthsList $monthsList")
                    Log.d("DATEE", "calendarDayList size ${calendarDayList.value?.size ?: 0}")
                }
            }
        }
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

    private suspend fun iterateCalendar(startDate: LocalDate, daysCount: Int): List<CalendarDay> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayList = arrayListOf<CalendarDay>()
        var currentDate = startDate
        Log.d("DATEE", "iterateCalendar from $currentDate for $daysCount days")
        for (i in 0..<daysCount) {
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
            currentDate = currentDate.plusDays(1)
        }
        Log.d(
            "DATEE",
            "iterateCalendar found ${dayList.size} elements: ${
                dayList.map {
                    it.calendar.get(Calendar.DAY_OF_MONTH)
                }
            }"
        )
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