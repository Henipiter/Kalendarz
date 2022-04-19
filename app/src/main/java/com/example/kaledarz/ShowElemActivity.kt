package com.example.kaledarz

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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


    private var calendar = Calendar.getInstance()
    private var dateFormatHelper = DateFormatHelper()
    var picker: TimePickerDialog? = null

    var intervalValue = "5"

    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()

    private fun startAlarm(c: Calendar, note: Note, mode: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("id", note.id)
        intent.putExtra("title", note.start_date + " " + note.start_time)
        intent.putExtra("content", note.content)
        var id = note.id!!.toInt()
        if (mode == "UNSET") {
            id *= -1
        }
        val pendingIntent =
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(note: Note) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.putExtra("mode", "UNSET")
        intent.putExtra("id", note.id)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            note.id!!.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)

        findViews()
        getAndSetIntentData()
        buttonStartTime.setOnClickListener {
            val hour = buttonStartTime.text.subSequence(0, 2).toString().toInt()
            val minutes = buttonStartTime.text.subSequence(3, 5).toString().toInt()
            picker = TimePickerDialog(
                this@ShowElemActivity, { tp, sHour, sMinute ->
                    buttonStartTime.text =
                        dateFormatHelper.setHour(sHour) + ":" + dateFormatHelper.setMinutes(sMinute)
                }, hour, minutes, true
            )
            picker!!.show()
        }

        buttonEndTime.setOnClickListener {
            val hour = buttonEndTime.text.subSequence(0, 2).toString().toInt()
            val minutes = buttonEndTime.text.subSequence(3, 5).toString().toInt()
            picker = TimePickerDialog(
                this@ShowElemActivity, { tp, sHour, sMinute ->
                    buttonEndTime.text =
                        dateFormatHelper.setHour(sHour) + ":" + dateFormatHelper.setMinutes(sMinute)
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
                    buttonStartDate.text = dateFormatHelper.updateDateInView(calendar)
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
                    buttonEndDate.text = dateFormatHelper.updateDateInView(calendar)
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
                showDeleteConfirmDialog(this@ShowElemActivity)
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
            if (note.done) {
                cancelAlarm(note)
            }
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        buttonAdd.setOnClickListener {

            if (dateFormatHelper.isCorrectDate(
                    buttonStartDate.text.toString(),
                    buttonEndDate.text.toString(),
                    buttonStartTime.text.toString(),
                    buttonEndTime.text.toString()
                )
            ) {

                val myDB = MyDatabaseHelper(this)
                val note = Note(
                    null,
                    buttonStartDate.text.toString().trim(),
                    buttonEndDate.text.toString().trim(),
                    buttonStartTime.text.toString().trim(),
                    buttonEndTime.text.toString().trim(),
                    Integer.valueOf(intervalValue.trim()),
                    contentText.text.toString().trim(),
                    false,
                    ""
                )
                myDB.addGame(note)

                note.id = myDB.readLastRow().id
                startAlarm(
                    getCalendarFromStrings(
                        buttonStartDate.text.toString(),
                        buttonStartTime.text.toString()
                    ), note, "SET"
                )
                startAlarm(
                    getCalendarFromStrings(
                        buttonEndDate.text.toString(),
                        buttonEndTime.text.toString()
                    ), note, "UNSET"
                )
                finish()
                val homepage = Intent(this, MainActivity::class.java)
                startActivity(homepage)
            } else {
                showErrorDateDialog(this@ShowElemActivity)
            }
        }
    }

    private fun getCalendarFromStrings(date: String, clock: String): Calendar {

        val c = Calendar.getInstance()
        calendar.set(Calendar.YEAR, date.split("-")[0].toInt())
        calendar.set(Calendar.MONTH, date.split("-")[1].toInt())
        calendar.set(Calendar.DAY_OF_MONTH, date.split("-")[2].toInt())
        c[Calendar.HOUR_OF_DAY] = clock.split(":")[0].toInt()
        c[Calendar.MINUTE] = clock.split(":")[1].toInt()
        c[Calendar.SECOND] = 0
        return c
    }

    private fun showErrorDateDialog(c: Context) {
        var messageError = ""
        if (!dateFormatHelper.isEndDateGreaterThanStartDate(
                buttonStartDate.text.toString(),
                buttonEndDate.text.toString()
            )
        ) {
            messageError = "Start date is later than end date"
        }
        if (!dateFormatHelper.isEndTimeGreaterThanStartTime(
                buttonStartTime.text.toString(),
                buttonEndTime.text.toString()
            )
        ) {
            messageError = "End time is not later than start date"
        }
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Date error")
            .setMessage(messageError)
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

    private fun showDeleteConfirmDialog(c: Context) {
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Are you sure?")
            .setMessage("Are you sure to delete that event?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteNoteAndExit()
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
            false,
            ""
        )
        myDB.addGame(note)
        finish()
        enableButtonIfSave()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun setHoursOnButtons() {
        val cldr = Calendar.getInstance()
        val hour = cldr[Calendar.HOUR_OF_DAY] + 1
        val initHourValue = dateFormatHelper.setHour(hour)
        buttonStartTime.text = dateFormatHelper.setHour(initHourValue.toInt()) + ":00"
        buttonEndTime.text = dateFormatHelper.setHour(initHourValue.toInt() + 1) + ":00"
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
    }
}
