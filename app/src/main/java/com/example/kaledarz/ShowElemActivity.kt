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
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ShowElemActivity : AppCompatActivity() {

    companion object {
        private const val EDIT_INFO = "EDIT"
        private const val CANCEL_INFO = "CANCEL"
        private const val DELETE_INFO = "DELETE"
        private const val SAVE_INFO = "SAVE"
    }

    private var activityType = "ADD"
    private lateinit var contentText: EditText
    lateinit var buttonAdd: Button
    lateinit var buttonEdit: Button
    lateinit var buttonDelete: Button
    lateinit var buttonDone: Button
    lateinit var buttonStartDate: Button
    lateinit var buttonEndDate: Button
    lateinit var buttonStartTime: Button
    lateinit var buttonEndTime: Button
    lateinit var buttonInterval: Button
    lateinit var warningTextView: TextView


    private var calendar = Calendar.getInstance()
    var picker: TimePickerDialog? = null

    var initHourValue = ""
    var initMinuteValue = ""
    var hourValue = "09"
    var minuteValue = "00"
    var intervalValue = "5"

    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)

        findViews()
        getAndSetIntentData()
        warningTextView.visibility = View.GONE
        buttonStartTime.setOnClickListener {
            val hour = buttonStartTime.text.subSequence(0, 2).toString().toInt()
            val minutes = buttonStartTime.text.subSequence(3, 5).toString().toInt()
            picker = TimePickerDialog(
                this@ShowElemActivity, { tp, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                    buttonStartTime.text = "$hourValue:$minuteValue"
                }, hour, minutes, true
            )
            picker!!.show()
        }

        buttonEndTime.setOnClickListener {
            val hour = buttonEndTime.text.subSequence(0, 2).toString().toInt()
            val minutes = buttonEndTime.text.subSequence(3, 5).toString().toInt()
            picker = TimePickerDialog(
                this@ShowElemActivity, { tp, sHour, sMinute ->
                    setHourAndMinutes(sHour, sMinute)
                    buttonEndTime.text = "$hourValue:$minuteValue"
                }, hour, minutes, true
            )
            picker!!.show()
        }

        buttonInterval.setOnClickListener { showAddItemDialog(this@ShowElemActivity) }

        buttonStartDate.setOnClickListener {
            DatePickerDialog(
                this@ShowElemActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonStartDate.text = updateDateInView()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonEndDate.setOnClickListener {
            DatePickerDialog(
                this@ShowElemActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonEndDate.text = updateDateInView()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonDelete.setOnClickListener {
            buttonDone.isEnabled = true
            if (buttonDelete.text == CANCEL_INFO) { //CANCEL
                enableButtonIfCancel()
            } else {  //DELETE
                deleteNoteAndExit()
            }
        }

        buttonEdit.setOnClickListener {
            if (buttonEdit.text != SAVE_INFO) { //EDIT
                enableButtonIfEdit()
            } else { //SAVE
                saveNoteAndExit()
            }
        }

        buttonDone.setOnClickListener {
            note.done = !note.done
            refreshDoneButton()
            myDB.updateDone(note.id.toString(), note.done)
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        buttonAdd.setOnClickListener {

            if (isCorrectDate()) {

                val myDB = MyDatabaseHelper(this)
                val note = Note(
                    null,
                    buttonStartDate.text.toString().trim(),
                    buttonEndDate.text.toString().trim(),
                    buttonStartTime.text.toString().trim(),
                    buttonEndTime.text.toString().trim(),
                    Integer.valueOf(intervalValue.trim()),
                    contentText.text.toString().trim(),
                    false
                )
                myDB.addGame(note)
                finish()
                val homepage = Intent(this, MainActivity::class.java)
                startActivity(homepage)
            } else {
                showErrorDateDialog(this@ShowElemActivity)
            }
        }
    }

    private fun isEndDateGreaterThanStartDate(): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val startDate = sdf.parse(buttonStartDate.text.toString())
        val endDate = sdf.parse(buttonEndDate.text.toString())

        return startDate < endDate
    }

    private fun isEndDateEqualToStartDate(): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val startDate = sdf.parse(buttonStartDate.text.toString())
        val endDate = sdf.parse(buttonEndDate.text.toString())

        return startDate == endDate
    }

    private fun isEndTimeGreaterThanStartTime(): Boolean {
        val sdf = SimpleDateFormat("HH:mm")
        val startTime = sdf.parse(buttonStartTime.text.toString())
        val endTime = sdf.parse(buttonEndTime.text.toString())

        return startTime < endTime
    }

    private fun isCorrectDate(): Boolean {
        return isEndDateGreaterThanStartDate() || (isEndDateEqualToStartDate() && isEndTimeGreaterThanStartTime())
    }

    private fun showErrorDateDialog(c: Context) {
        var messageError = ""
        if (!isEndDateGreaterThanStartDate()) {
            messageError = "Start date is later than end date"
        }
        if (!isEndTimeGreaterThanStartTime()) {
            messageError = "End time is not later than start date"
        }
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle(messageError)
            .setNegativeButton("OK", null)
            .create()
        dialog.show()
    }

    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        taskEditText.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Change interval time")
            .setView(taskEditText)
            .setPositiveButton("Save") { dialog, which ->
                note.interval = taskEditText.text.toString().toInt()
                buttonInterval.text = note.interval.toString() + " minutes"
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun getAndSetIntentData() {
        if (intent.hasExtra("type")) {
            activityType = intent.getStringExtra("type").toString()
            if (activityType == "EDIT") {
                getIntentForEditView()
                showEditViewButton()
                enableButtonIfEdit(false)
                storeDataInArrays()
                refreshDoneButton()
            } else if (activityType == "ADD") {
                getIntentForAddView()
                hideEditViewButton()
                enableButtonIfEdit()
                setHoursOnButtons()
            }
        }
    }

    private fun getIntentForEditView() {
        if (intent.hasExtra("id")) {
            note.id = intent.getStringExtra("id").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIntentForAddView() {
        buttonDone.text = "Add"
        if (intent.hasExtra("date")) {
            buttonStartDate.text = intent.getStringExtra("date").toString()
            buttonEndDate.text = intent.getStringExtra("date").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeDataInArrays() {
        note = myDB.readOneData(note.id.toString())
        Log.e("done:", note.done.toString())
        if (note.id == "") {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        } else {
            buttonStartDate.text = note.start_date
            buttonEndDate.text = note.end_date
            buttonStartTime.text = note.start_time
            buttonEndTime.text = note.end_time
            buttonInterval.text = note.interval.toString() + " minutes"
            contentText.setText(note.content)
        }
    }

    private fun saveNoteAndExit() {
        val myDB = MyDatabaseHelper(this)
        myDB.deleteEvent(note.id.toString())

        val note = Note(
            null,
            buttonStartDate.text.toString().trim(),
            buttonEndDate.text.toString().trim(),
            buttonStartTime.text.toString().trim(),
            buttonEndTime.text.toString().trim(),
            Integer.valueOf(buttonInterval.text.split(" ")[0].trim()),
            contentText.text.toString().trim(),
            false
        )
        myDB.addGame(note)
        finish()
        enableButtonIfSave()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun updateDateInView(): String {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        return sdf.format(calendar.time)
    }

    private fun setHour(time: Int): String {
        return if (time.toString().length == 1) {
            "0$time"
        } else {
            time.toString()
        }
    }

    private fun setHourAndMinutes(sHour: Int, sMinute: Int) {
        hourValue = setHour(sHour)
        minuteValue = setHour(sMinute)
    }

    private fun setHoursOnButtons() {
        val cldr = Calendar.getInstance()
        val hour = cldr[Calendar.HOUR_OF_DAY] + 1
        setHourAndMinutes(hour, 0).toString()
        initHourValue = hourValue
        initMinuteValue = minuteValue
        buttonStartTime.text = setHour(hourValue.toInt() + 1) + ":00"
        buttonEndTime.text = setHour(hourValue.toInt() + 2) + ":00"
    }

    private fun refreshDoneButton() {
        if (note.done) {
            buttonDone.text = "Mark as undone"
        } else {
            buttonDone.text = "Mark as done"
        }
    }

    private fun deleteNoteAndExit() {
        myDB.deleteEvent(note.id.toString())
        finish()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun enableButtonIfCancel() {

        enableEditText(contentText, false)
        buttonStartDate.text = note.start_date
        buttonEndDate.text = note.end_date
        buttonInterval.text = note.interval.toString()
        contentText.setText(note.content)
        buttonStartTime.text = note.start_time

        enableButtonIfEdit(false)
        buttonEdit.text = EDIT_INFO
        buttonDelete.text = DELETE_INFO
    }

    private fun hideEditViewButton() {
        buttonEdit.visibility = View.GONE
        buttonDelete.visibility = View.GONE
        buttonDone.visibility = View.GONE
        buttonAdd.visibility = View.VISIBLE
    }

    private fun showEditViewButton() {
        buttonEdit.visibility = View.VISIBLE
        buttonDelete.visibility = View.VISIBLE
        buttonDone.visibility = View.VISIBLE
        buttonAdd.visibility = View.GONE
    }

    private fun enableEditText(editText: EditText, bool: Boolean) {
        editText.isFocusableInTouchMode = bool
        editText.isEnabled = bool
        editText.isCursorVisible = bool
    }

    private fun enableButtonIfSave() {
        enableEditText(contentText, false)
        buttonEdit.text = EDIT_INFO
        buttonDelete.text = DELETE_INFO
        buttonDone.isEnabled = true
    }

    private fun enableButtonIfEdit() {
        enableEditText(contentText, true)
        buttonEdit.text = SAVE_INFO
        buttonDelete.text = CANCEL_INFO
        enableButtonIfEdit(true)
    }

    private fun enableButtonIfEdit(bool: Boolean) {
        buttonDone.isEnabled = !bool
        buttonStartDate.isEnabled = bool
        buttonEndDate.isEnabled = bool
        buttonInterval.isEnabled = bool
        buttonStartTime.isEnabled = bool
        buttonEndTime.isEnabled = bool
        contentText.isEnabled = bool
    }

    private fun findViews() {
        contentText = findViewById(R.id.contentText_2)
        buttonEdit = findViewById(R.id.edit_button_2)
        buttonDelete = findViewById(R.id.delete_button_2)
        buttonDone = findViewById(R.id.done_button_2)
        buttonStartDate = findViewById(R.id.date_button_2)
        buttonEndDate = findViewById(R.id.date_button_3)
        buttonInterval = findViewById(R.id.interval_button_2)
        buttonStartTime = findViewById(R.id.start_time_button_2)
        buttonEndTime = findViewById(R.id.end_time_button_)
        buttonEdit = findViewById(R.id.edit_button_2)
        buttonAdd = findViewById(R.id.add_button_2)
        warningTextView = findViewById(R.id.warningTextView)
    }
}
