package com.example.kaledarz.activities


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.R
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper


class MainActivity : AppCompatActivity() {
    private lateinit var calendar: CalendarView
    private lateinit var addNew: Button
    private lateinit var buttonNotify: Button
    private lateinit var buttonSettings: Button
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var noRowsInfo: TextView
    private var chooseDate = "2020-01-01"
    private var noteList: ArrayList<Note> = ArrayList()

    private lateinit var myPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        buttonNotify = findViewById(R.id.list_button)
        buttonSettings = findViewById(R.id.settings_button)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)
        noRowsInfo = findViewById(R.id.no_rows_info2)

        myPref = applicationContext.getSharedPreferences("run_alarms", MODE_PRIVATE)
        alarmHelper = AlarmHelper(applicationContext)

        val calendarView = CalendarView(this)
        calendarView.setDate(System.currentTimeMillis(), false, true)

        chooseDate = DateFormatHelper.getTodayDate(calendar.date)
        databaseHelper = MyDatabaseHelper(this)

        customAdapter = CustomAdapter(this, this, noteList)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()

        if (myPref.getString(Constants.ALARM_ON_OFF, "true") == "true") {
            alarmHelper.setAlarmForNotes(noteList)
        }
        customAdapter.notifyDataSetChanged()
        buttonNotify.setOnClickListener {
            val intent = Intent(this, SegregatedListActivity::class.java)
            this.startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            this.startActivity(intent)
        }

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            chooseDate = DateFormatHelper.getChosenDate(year, month, dayOfMonth)
            Log.e("aa", chooseDate)
            storeDataInArrays()
            customAdapter.notifyDataSetChanged()
        }

        addNew.setOnClickListener {
            val intent = Intent(this, ShowElemActivity::class.java)
            intent.putExtra("type", "ADD")
            intent.putExtra("date", chooseDate)
            this.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            recreate()
        }
    }

    private fun storeDataInArrays() {

        val list = ArrayList<Note>()
        list.addAll(databaseHelper.readAllData())
        if (list.size == 0) {
            noRowsInfo.isVisible = true
        } else {
            noRowsInfo.isVisible = false
            filterNoteList(list, chooseDate)
            Note.computeStatusForNoteList(noteList)
        }
    }

    private fun filterNoteList(list: ArrayList<Note>, chosenDate: String) {
        noteList.clear()
        for (note in list) {
            val isAboveStart =
                DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                    chosenDate, note.start_date, "dd-MM-yyyy"
                )
            val isUnderEnd =
                DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                    note.end_date, chosenDate, "dd-MM-yyyy"
                )
            if (isAboveStart && isUnderEnd)
                noteList.add(note)
        }
    }
}
