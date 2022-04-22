package com.example.kaledarz


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var calendar: CalendarView
    private lateinit var addNew: Button
    private lateinit var buttonNotify: Button
    private lateinit var buttonStopNotify: Button
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private var chooseDate = "2020-01-01"
    private var noteList: ArrayList<Note> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        buttonNotify = findViewById(R.id.notify_button)
        buttonStopNotify = findViewById(R.id.notify_button_stop)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)

        val calendarView = CalendarView(this)
        calendarView.setDate(System.currentTimeMillis(), false, true)

        chooseDate = getTodayDate()
        databaseHelper = MyDatabaseHelper(this)

        customAdapter = CustomAdapter(this, this, noteList)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()
        customAdapter.notifyDataSetChanged()
        buttonNotify.setOnClickListener {
            val intent = Intent(this, SegregatedList::class.java)
            this.startActivity(intent)
        }

        buttonStopNotify.setOnClickListener {

        }

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            chooseDate = getChosenDate(year, month, dayOfMonth)
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
        noteList.clear()
        noteList.addAll(databaseHelper.readAllDataByDate(chooseDate))
        if (noteList.size == 0) {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTodayDate(): String {
        val calender = Calendar.getInstance()
        calender.timeInMillis = calendar.date
        val year = calender[Calendar.YEAR].toString()
        var month = (1 + calender[Calendar.MONTH]).toString()
        var curDate = calender[Calendar.DAY_OF_MONTH].toString()

        if (curDate.length == 1) {
            curDate = "0$curDate"
        }
        if (month.length == 1) {
            month = "0$month"
        }
        return "$curDate-$month-$year"
    }

    private fun getChosenDate(year: Int, month: Int, dayOfMonth: Int): String {
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
