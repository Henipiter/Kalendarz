package com.example.kaledarz

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class AddEventActivity : AppCompatActivity() {

    var hourValue: String = "09"
    var minuteValue: String = "00"
    var intervalValue = "5"
    lateinit var buttonInterval: Button
    lateinit var addButton: Button
    lateinit var buttonDate: Button
    lateinit var contentText1: EditText
    lateinit var buttonStartTime: Button
    lateinit var buttonEndTime: Button
    var calendar = Calendar.getInstance()
    var picker: TimePickerDialog? = null

    private fun findViews() {
        buttonDate = findViewById(R.id.date_button)
        addButton = findViewById(R.id.edit_button_1)
        buttonStartTime = findViewById(R.id.start_time_button)
        buttonEndTime = findViewById(R.id.end_time_button)
        buttonInterval = findViewById(R.id.remind_button)
        contentText1 = findViewById(R.id.contentText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        findViews()
        getAndSetIntentData()
        editIntervalText()

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        buttonInterval.setOnClickListener { showAddItemDialog(this@AddEventActivity) }
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        buttonDate.setOnClickListener {
            DatePickerDialog(
                this@AddEventActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        buttonStartTime.setOnClickListener {
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]
            val minutes = cldr[Calendar.MINUTE]
            // time picker dialog
            picker = TimePickerDialog(
                this@AddEventActivity, { tp, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                    buttonStartTime.text = "$hourValue:$minuteValue"
                }, hour, minutes, true
            )
            picker!!.show()
        }
        buttonEndTime.setOnClickListener {
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]+1
            val minutes = 0
            // time picker dialog
            picker = TimePickerDialog(
                this@AddEventActivity, { tp, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                    buttonStartTime.text = "$hourValue:$minuteValue"
                }, hour, minutes, true
            )
            picker!!.show()
        }
        addButton.setOnClickListener {
            val myDB = MyDatabaseHelper(this)

            val note = Note(
                null,
                buttonDate.text.toString().trim(),
                buttonStartTime.text.toString().trim(),
                buttonEndTime.text.toString().trim(),
                Integer.valueOf(intervalValue.trim()),
                contentText1.text.toString().trim(),
                false
            )
            myDB.addGame(note)
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }
    }

    private fun setHourAndMinutes(sHour: Int, sMinute: Int) {
        hourValue = if (sHour.toString().length == 1) {
            "0$sHour"
        } else {
            sHour.toString()
        }
        minuteValue = if (sMinute.toString().length == 1) {
            "0$sMinute"
        } else {
            sMinute.toString()
        }
    }

    private fun editIntervalText() {
        buttonInterval.text = "$intervalValue minutes"
    }

    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        buttonDate.text = sdf.format(calendar.time)
    }

    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        taskEditText.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Change interval time")
            .setView(taskEditText)
            .setPositiveButton("Save") { dialog, which ->
                intervalValue = taskEditText.text.toString()
                editIntervalText()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun getAndSetIntentData() {

        if (intent.hasExtra("date")) {
            buttonDate.text = intent.getStringExtra("date").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }
}
