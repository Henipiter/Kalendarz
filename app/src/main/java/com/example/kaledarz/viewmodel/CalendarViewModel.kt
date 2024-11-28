package com.example.kaledarz.viewmodel

import android.app.Application
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.applandeo.materialcalendarview.CalendarDay
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private var databaseHelper: MyDatabaseHelper? = null

    var noteList = MutableLiveData<ArrayList<Note>>()
    var filteredList = MutableLiveData<ArrayList<Note>>()

    var calendarDayList = MutableLiveData<ArrayList<CalendarDay>>()

    init {
        databaseHelper = MyDatabaseHelper(getApplication<Application>().applicationContext)
    }

    fun getFilteredList(): ArrayList<Note> {
        return filteredList.value ?: arrayListOf()
    }

    fun readAllNotes() {
        viewModelScope.launch {
            databaseHelper?.let { databaseHelper ->
                noteList.postValue(databaseHelper.readAllData())
            }
        }
    }

    fun filterNoteList(chosenDate: String) {
        val list = filterNoteListForSingleDay(chosenDate)
        filteredList.postValue(list)
    }

    private fun filterNoteListForSingleDay(chosenDate: String): ArrayList<Note> {
        val list = arrayListOf<Note>()
        noteList.value?.let {
            for (note in it) {
                val isAboveStart =
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        chosenDate, note.start_date, "dd-MM-yyyy"
                    )
                val isUnderEnd =
                    DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                        note.end_date, chosenDate, "dd-MM-yyyy"
                    )
                if (isAboveStart && isUnderEnd)
                    list.add(note)
            }
        }
        Note.computeStatusForNoteList(list)
        return list
    }

    fun prepareCalendarEvents(currentMonth: Int, currentYear: Int) {
        val dayList = arrayListOf<CalendarDay>()
        val lastDay = DateFormatHelper.getLastDayOfMonth(currentYear, currentMonth + 1)
        for (i in 1..lastDay) {
            val currentDate = String.format("%02d", i) + "-" + String.format(
                "%02d",
                currentMonth + 1
            ) + "-" + currentYear
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
                        getColorByNoteCyclicType(isRegularNotePresent),
                        getDrawableByStatus((cyclicNoteStatuses))
                    )
                )
            }
        }
        calendarDayList.postValue(dayList)
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

    @ColorRes
    private fun getColorByNoteCyclicType(isRegular: Boolean): Int {
        return if (isRegular) {
            R.color.done
        } else {
            R.color.white
        }
    }

    @DrawableRes
    private fun getDrawableByStatus(statuses: Set<Status>): Int? {
        if (statuses.size == 4) {
            return R.drawable.event_four_done_undone_late_future
        }
        if (statuses.size == 3) {
            if (!statuses.contains(Status.DONE)) {
                return R.drawable.event_three_undone_past_future
            }
            if (!statuses.contains(Status.UNDONE)) {
                return R.drawable.event_three_done_past_future
            }
            if (!statuses.contains(Status.PAST)) {
                return R.drawable.event_three_done_undone_future
            }
            if (!statuses.contains(Status.FUTURE)) {
                return R.drawable.event_three_done_undone_past
            }
        }
        if (statuses.size == 2) {
            if (statuses.contains(Status.DONE) && statuses.contains(Status.UNDONE)) {
                return R.drawable.event_two_done_undone
            }
            if (statuses.contains(Status.DONE) && statuses.contains(Status.PAST)) {
                return R.drawable.event_two_done_past
            }
            if (statuses.contains(Status.DONE) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_done_future
            }
            if (statuses.contains(Status.UNDONE) && statuses.contains(Status.PAST)) {
                return R.drawable.event_two_undone_past
            }
            if (statuses.contains(Status.UNDONE) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_undone_future
            }
            if (statuses.contains(Status.PAST) && statuses.contains(Status.FUTURE)) {
                return R.drawable.event_two_past_future
            }
        }
        if (statuses.size == 1) {
            if (statuses.contains(Status.DONE)) {
                return R.drawable.event_one_done
            }
            if (statuses.contains(Status.UNDONE)) {
                return R.drawable.event_one_undone
            }
            if (statuses.contains(Status.PAST)) {
                return R.drawable.event_one_past
            }
            if (statuses.contains(Status.FUTURE)) {
                return R.drawable.event_one_future
            }
        }
        return null
    }
}