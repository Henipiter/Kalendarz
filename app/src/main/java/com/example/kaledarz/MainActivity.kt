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
    lateinit var calendar: CalendarView
    lateinit var addNew: Button
    lateinit var recyclerViewEvent: RecyclerView
    lateinit var myDB: MyDatabaseHelper
    var choose_date = "2020-01-01"


    var _id: ArrayList<String> = ArrayList()
    var _date: ArrayList<String> = ArrayList()
    var _time: ArrayList<String> = ArrayList()
    var _interval: ArrayList<String> = ArrayList()
    var _content: ArrayList<String> = ArrayList()
    lateinit var customAdapter: CustomAdapter

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
        val curDate: String
        val monthStr: String
        if (dayOfMonth < 10) {
            curDate = "0$dayOfMonth"
        } else {
            curDate = dayOfMonth.toString()
        }
        if (month + 1 < 10) {
            monthStr = "0" + (month + 1).toString()
        } else {
            monthStr = (month + 1).toString()
        }
        return "$curDate-$monthStr-$year"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)
        val cal = CalendarView(this)
        cal.setDate(System.currentTimeMillis(), false, true)

        choose_date = getTodayDate()
        myDB = MyDatabaseHelper(this)

        customAdapter = CustomAdapter(this, this, _id, _date, _time, _interval, _content)
        recyclerViewEvent.adapter = customAdapter
        recyclerViewEvent.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        storeDataInArrays()
        customAdapter.notifyDataSetChanged()



        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->


            choose_date = getChosenDate(year, month, dayOfMonth)
            Log.e("aa", choose_date)
            storeDataInArrays()
            customAdapter.notifyDataSetChanged()

        }
        addNew.setOnClickListener {

            val i = Intent(this, AddEventActivity::class.java)
            i.putExtra("date", choose_date)
            this.startActivity(i)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            recreate()
        }
    }

    private fun storeDataInArrays() {
        _id.clear()
        _date.clear()
        _time.clear()
        _interval.clear()
        _content.clear()
        val cursor = myDB.readAllData(choose_date)
        if (cursor?.count == 0) {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        } else {
            while (cursor?.moveToNext() == true) {
                _id.add(cursor.getString(0))
                _date.add(cursor.getString(1))
                _time.add(cursor.getString(2))
                _interval.add(cursor.getString(3))
                _content.add(cursor.getString(4))

            }
        }
    }
}