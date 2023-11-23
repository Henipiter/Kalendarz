package com.example.kaledarz.activities


import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var buttonList: Button
    private lateinit var buttonSettings: Button
    private lateinit var recyclerViewEvent: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var databaseHelper: MyDatabaseHelper
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var noRowsInfo: TextView
    private lateinit var imageMute: ImageView
    private var chooseDate = "2020-01-01"
    private var noteList: ArrayList<Note> = ArrayList()

    private lateinit var myPref: SharedPreferences

    override fun onResume() {
        super.onResume()
        storeDataInArrays()
        if (myPref.getString(Constants.ALARM_ON_OFF, "true") == "true") {
            alarmHelper.setAlarmForNotes(noteList)
        }
        customAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calendar = findViewById(R.id.calendarView)
        addNew = findViewById(R.id.add_button)
        buttonList = findViewById(R.id.list_button)
        buttonSettings = findViewById(R.id.settings_button)
        recyclerViewEvent = findViewById(R.id.recyclerViewEvent)
        noRowsInfo = findViewById(R.id.no_rows_info2)
        imageMute = findViewById(R.id.imageMute)

        myPref = applicationContext.getSharedPreferences("run_alarms", MODE_PRIVATE)
        alarmHelper = AlarmHelper(applicationContext)

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
        buttonList.setOnClickListener {
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

        if (!allPermissionsGranted()) {
            requestAppPermissions()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            recreate()
        }
    }

    private fun storeDataInArrays() {
        filterNoteList(databaseHelper.readAllData(), chooseDate)
        if (noteList.size == 0) {
            noRowsInfo.isVisible = true
            imageMute.isVisible = myPref.getString(Constants.ALARM_ON_OFF, "false") != "true"
        } else {
            imageMute.isVisible = false
            noRowsInfo.isVisible = false
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

    private fun requestAppPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    this,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    companion object {
        private const val TAG = "Notifications"


        private val REQUIRED_PERMISSIONS =

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mutableListOf(android.Manifest.permission.POST_NOTIFICATIONS).toTypedArray()
            } else {
                arrayOf()
            }
    }
}
