package com.example.kaledarz


import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var calendar: CalendarView
    lateinit var addNew: Button
    lateinit var recyclerViewEvent: RecyclerView
    lateinit var myDB: MyDatabaseHelper
    var choose_date = "2020-01-01"


    var _id:ArrayList<String> = ArrayList<String>()
    var _date:ArrayList<String> = ArrayList<String>()
    var _time:ArrayList<String> = ArrayList<String>()
    var _interval:ArrayList<String> = ArrayList<String>()
    var _content:ArrayList<String> = ArrayList<String>()
    lateinit var customAdapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)
        val cal = CalendarView(this)
        cal.setDate(System.currentTimeMillis(), false, true)

        val date: Long = calendar.getDate()
        val calender = Calendar.getInstance()
        calender.timeInMillis = date
        var Year = calender[Calendar.YEAR].toString()
        var Month = (1+calender[Calendar.MONTH]).toString()
        var curDate = calender[Calendar.DAY_OF_MONTH].toString()

        choose_date=curDate+"-"+Month+"-"+Year
        choose_date = "11-07-2021"
        myDB = MyDatabaseHelper(this)

        customAdapter = CustomAdapter(this,this,_id,_date, _time, _interval, _content)
        recyclerViewEvent.setAdapter(customAdapter)
        recyclerViewEvent.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL ,false)

        storeDataInArrays()
        customAdapter.notifyDataSetChanged()



        calendar.setOnDateChangeListener(OnDateChangeListener { view, year, month, dayOfMonth ->
            if(dayOfMonth<10)
                curDate = "0"+ dayOfMonth.toString()
            else
                curDate = dayOfMonth.toString()
            Year = year.toString()
            if(month+1<10)
                Month = "0"+ (month+1).toString()
            else
                Month =  (month+1).toString()

            choose_date=curDate+"-"+Month+"-"+Year
            Log.e("aa", choose_date)
            storeDataInArrays()
            customAdapter.notifyDataSetChanged()

        })
        addNew.setOnClickListener(View.OnClickListener {

            val i = Intent(this, com.example.kaledarz.AddEventActivity::class.java)
            i.putExtra("year", Year)
            i.putExtra("month", Month)
            i.putExtra("dayOfMonth", curDate)
            this.startActivity(i)
        })


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1){
            recreate()
        }
    }
    fun storeDataInArrays(){
        _id.clear()
        _date.clear()
        _time.clear()
        _interval.clear()
        _content.clear()
        val cursor = myDB.readAllData(choose_date)
        if(cursor?.count == 0){
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
        else{
            while(cursor?.moveToNext() == true){
                _id.add(cursor.getString(0))
                _date.add(cursor.getString(1))
                _time.add(cursor.getString(2))
                _interval.add(cursor.getString(3))
                _content.add(cursor.getString(4))

            }
        }
    }
}