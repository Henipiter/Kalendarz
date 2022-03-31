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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class AddEventActivity : AppCompatActivity() {

    var H: String = "09"
    var M: String = "00"
    var interval = "5"
    lateinit var button_remind: Button
    lateinit var add_button: Button
    lateinit var button_date: Button
    lateinit var data_text: TextView
    lateinit var contentText1: EditText
    lateinit var button_time: Button
    lateinit var time_text: TextView
    lateinit var interval_text: TextView
    var cal = Calendar.getInstance()
    var picker: TimePickerDialog? = null

    private fun findViews() {

        data_text = findViewById(R.id.data_text)
        time_text = findViewById(R.id.time_text)
        button_date = findViewById(R.id.date_button)
        add_button = findViewById(R.id.edit_button_1)
        button_time = findViewById(R.id.time_button)
        button_remind = findViewById(R.id.remind_button)
        interval_text = findViewById(R.id.interval_text)
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
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        button_remind.setOnClickListener { showAddItemDialog(this@AddEventActivity) }
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date.setOnClickListener {
            DatePickerDialog(
                this@AddEventActivity,
                dateSetListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        button_time.setOnClickListener {
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]
            val minutes = cldr[Calendar.MINUTE]
            // time picker dialog
            picker = TimePickerDialog(
                this@AddEventActivity, { tp, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                    time_text.text = "$H:$M"
                }, hour, minutes, true
            )
            picker!!.show()
        }
        add_button.setOnClickListener {
            val myDB = MyDatabaseHelper(this)
            myDB.addGame(
                data_text.text.toString().trim(),
                time_text.text.toString().trim(),
                Integer.valueOf(interval.trim()),
                contentText1.text.toString().trim()
            )
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }
    }

    private fun setHourAndMinutes(sHour: Int, sMinute: Int) {
        H = if (sHour.toString().length == 1) {
            "0$sHour"
        } else {
            sHour.toString()
        }
        M = if (sMinute.toString().length == 1) {
            "0$sMinute"
        } else {
            sMinute.toString()
        }
    }

    private fun editIntervalText() {
        interval_text.text = "Remind in every $interval minutes"
    }

    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        data_text.text = sdf.format(cal.time)
    }

    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        taskEditText.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Change interval time")
            .setView(taskEditText)
            .setPositiveButton("Save") { dialog, which ->
                interval = taskEditText.text.toString()
                editIntervalText()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun getAndSetIntentData() {

        if (intent.hasExtra("date")) {
            data_text.text = intent.getStringExtra("date").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }
}
