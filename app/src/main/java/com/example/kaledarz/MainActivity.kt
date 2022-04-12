package com.example.kaledarz


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var calendar: CalendarView
    private lateinit var addNew: Button
    private lateinit var buttonNotify: Button
    private lateinit var buttonStopNotify: Button
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var databaseHelper: MyDatabaseHelper
    private var chooseDate = "2020-01-01"
    private var noteList: ArrayList<Note> = ArrayList()

    private val CHANNEL_ID = "dupa"

    private lateinit var customAdapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        buttonNotify = findViewById(R.id.notify_button)
        buttonStopNotify = findViewById(R.id.notify_button_stop)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)

        createNotificationChannel()


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
            createNotification()
        }

        buttonStopNotify.setOnClickListener{
            deleteNotification()
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

    private fun deleteNotification(){
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.cancel(1)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.bbb)
            .setContentTitle("Titel")
            .setContentText("Content dupa")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
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
        noteList.addAll(databaseHelper.readAllData(chooseDate))
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
}
