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
import com.example.kaledarz.helpers.DrawableHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    val readingNoteProcessFinished = AtomicBoolean(false)
    private val calculatingDays = AtomicBoolean(false)
    var updateCalendarProcessing = MutableLiveData(false)
    var currentPrepareProcessing = MutableLiveData(false)
    var prevAndNextPrepareProcessing = MutableLiveData(false)
    var calendarDaySleep = MutableLiveData(false)
    var calendarDayList = MutableLiveData<ArrayList<CalendarDay>>()
    private val monthsList: MutableList<String> = Collections.synchronizedList(mutableListOf())
    var currentPageUpdated = MutableLiveData(false)
    var shouldUpdateGrid = MutableLiveData(false)

    init {
        databaseHelper = MyDatabaseHelper(getApplication<Application>().applicationContext)
    }

    fun resetProcessing() {
        calculatingDays.set(false)
        currentPrepareProcessing.postValue(false)
        prevAndNextPrepareProcessing.postValue(false)
        updateCalendarProcessing.postValue(false)
        currentPageUpdated.postValue(false)
        shouldUpdateGrid.postValue(false)
        calendarDayList.value!!.clear()
        monthsList.clear()
    }

    fun isPrepareProcessing(): Boolean {
        Log.d(
            "BLOCKING", "currentPrepareProcessing" +
                    " ${currentPrepareProcessing.value}" +
                    " ${prevAndNextPrepareProcessing.value}" +
                    " ${updateCalendarProcessing.value}"
        )
        return currentPrepareProcessing.value!! || prevAndNextPrepareProcessing.value!! || updateCalendarProcessing.value!!
    }

    fun getFilteredList(): ArrayList<Note> {
        return filteredList.value ?: arrayListOf()
    }

    fun readAllNotes() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                readingNoteProcessFinished.set(false)
                databaseHelper?.let { databaseHelper ->
                    noteList.postValue(databaseHelper.readAllData())
                    Log.d("DATEE", "calendarViewModel.noteList BBBBB")
                    readingNoteProcessFinished.set(true)
                }
            }
        }
    }

    fun runCalendarDaySleep() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                delay(150)
                calendarDaySleep.postValue(true)
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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (calculatingDays.get()) {
                    Log.d("DATEE", "CAUTION processing prepareCurrent")
                    return@withContext
                }
                currentPrepareProcessing.postValue(true)
                currentPageUpdated.postValue(false)
                monthsList.clear()
                calculatingDays.set(true)
                val date = LocalDate.of(currentYear, currentMonth + 1, 1)
                if (addMonthsList("${date.monthValue}-${date.year}")) {
                    Log.d("DATEE", "prepareCurrent ${date.monthValue}-${date.year}")
                    val list = iterateCalendar(date, date.lengthOfMonth())
                    calendarDayList.postValue(ArrayList(list))
                    currentPageUpdated.postValue(true)
                    updateCalendarProcessing.postValue(true)
                    shouldUpdateGrid.postValue(true)
                } else {
                    updateCalendarProcessing.postValue(false)
                }
                Log.d("DATEE", "monthsList $monthsList")
                Log.d("DATEE", "calendarDayList size ${calendarDayList.value?.size ?: 0}")
                calculatingDays.set(false)
                currentPrepareProcessing.postValue(false)
            }
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

    fun preparePreviousAndNextMonths(currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (calculatingDays.get()) {
                    Log.d("DATEE", "CAUTION processing calculatingDays")
                    return@withContext
                }
                calculatingDays.set(true)
                prevAndNextPrepareProcessing.postValue(true)
                val dateNow = LocalDate.of(currentYear, currentMonth + 1, 1)
                val datePrevious = dateNow.minusMonths(1)
                val dateNext = dateNow.plusMonths(1)
                val isPrevMonthExist =
                    !addMonthsList("${datePrevious.monthValue}-${datePrevious.year}")
                val isNextMonthExist =
                    !addMonthsList("${dateNext.monthValue}-${dateNext.year}")

                val prevList = arrayListOf<CalendarDay>()
                val nextList = arrayListOf<CalendarDay>()

                if (!isPrevMonthExist) {
                    prevList.addAll(iterateCalendar(datePrevious, datePrevious.lengthOfMonth()))
                } else {
                    Log.d(
                        "DATEE",
                        "preparePrevious - stop - exits ${datePrevious.monthValue}-${datePrevious.year}"
                    )
                }

                if (!isNextMonthExist) {
                    nextList.addAll(iterateCalendar(dateNext, dateNext.lengthOfMonth()))
                } else {
                    Log.d(
                        "DATEE",
                        "prepareNext - stop - exits ${dateNext.monthValue}-${dateNext.year}"
                    )

                }
                var prevUpdated = false
                var nextUpdated = false
                if (currentPageUpdated.value == true) {
                    if (!isPrevMonthExist) {
                        calendarDayList.value?.addAll(prevList)
                        prevUpdated = true
                    }
                    if (!isNextMonthExist) {
                        calendarDayList.value?.addAll(nextList)
                        nextUpdated = true
                    }
                    if (prevUpdated || nextUpdated) {
                        shouldUpdateGrid.postValue(true)
                        updateCalendarProcessing.postValue(true)

                    }
                }
                Log.d("DATEE", "monthsList $monthsList")
                calculatingDays.set(false)
                updateCalendarProcessing.postValue(false)
                prevAndNextPrepareProcessing.postValue(false)
            }
        }
    }


    private suspend fun iterateCalendar(
        startDate: LocalDate,
        daysCount: Int
    ): List<CalendarDay> {
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
                        DrawableHelper.getDrawableByStatus(
                            cyclicNoteStatuses,
                            isRegularNotePresent
                        )
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