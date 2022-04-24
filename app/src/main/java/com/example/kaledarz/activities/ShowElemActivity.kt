package com.example.kaledarz.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.kaledarz.*
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import com.example.kaledarz.helpers.NotificationHelper

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


    private var calendar = Calendar.getInstance()
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmHelper: AlarmHelper

    var picker: TimePickerDialog? = null

    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()

    override fun onCreate(savedInstanceState: Bundle?) {
        notificationHelper = NotificationHelper(this)
        alarmHelper = AlarmHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        notificationHelper.createNotificationChannel()
        myDB = MyDatabaseHelper(this)

        findViews()
        getAndSetIntentData()
        buttonStartTime.setOnClickListener {
            val hour = buttonStartTime.text.subSequence(0, 2).toString().toInt()
            val minutes = buttonStartTime.text.subSequence(3, 5).toString().toInt()
            picker = TimePickerDialog(
                this@ShowElemActivity, { tp, sHour, sMinute ->
                    buttonStartTime.text =
                        DateFormatHelper.setHour(sHour) + ":" + DateFormatHelper.setMinutes(sMinute)
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
                        DateFormatHelper.setHour(sHour) + ":" + DateFormatHelper.setMinutes(sMinute)
                }, hour, minutes, true
            )
            picker!!.show()
        }

        buttonStartDate.setOnClickListener {
            DatePickerDialog(
                this@ShowElemActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonStartDate.text = DateFormatHelper.updateDateInView(calendar)
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
                    buttonEndDate.text = DateFormatHelper.updateDateInView(calendar)
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
                editNoteAndExit()
            }
        }

        buttonDone.setOnClickListener {
            note.done = !note.done
            refreshDoneButton()
            myDB.updateDone(note.id.toString(), note.done)
            if (note.done) {
                alarmHelper.unsetAlarm(note.id!!, notificationHelper)
            } else {
                alarmHelper.setAlarm(note)
            }
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        buttonAdd.setOnClickListener {

            if (DateFormatHelper.isCorrectDate(
                    buttonStartDate.text.toString(),
                    buttonEndDate.text.toString(),
                    buttonStartTime.text.toString(),
                    buttonEndTime.text.toString()
                )
            ) {
                addNoteToDatabase()
                finishAndReturnToMainActivity()
            } else {
                showErrorDateDialog(this@ShowElemActivity)
            }
        }
    }

    private fun addNoteToDatabase() {
        val myDB = MyDatabaseHelper(this)
        var content = contentText.text.toString().trim()
        if(content ==""){
            content = "Reminder"
        }
        val note = Note(
            null,
            buttonStartDate.text.toString().trim(),
            buttonEndDate.text.toString().trim(),
            buttonStartTime.text.toString().trim(),
            buttonEndTime.text.toString().trim(),
            content,
            false,
            "",
            Status.UNDONE
        )
        myDB.addGame(note)
        note.id = myDB.readLastRow().id
    }

    private fun finishAndReturnToMainActivity() {
        finish()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }


    private fun showErrorDateDialog(c: Context) {
        var messageError = ""
        if (!DateFormatHelper.isEndDateGreaterThanStartDate(
                buttonStartDate.text.toString(),
                buttonEndDate.text.toString()
            )
        ) {
            messageError = "Start date is later than end date"
        }
        if (!DateFormatHelper.isEndTimeGreaterThanStartTime(
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

    private fun showDeleteConfirmDialog(c: Context) {
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Are you sure?")
            .setMessage("Are you sure to delete that event?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteNoteAndAlarm()
                finishAndReturnToMainActivity()
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
            contentText.setText(note.content)
        }
    }

    private fun editNoteAndExit() {
        deleteNoteAndAlarm()
        addNoteToDatabase()
        enableButtonIfSave()
        finishAndReturnToMainActivity()
    }

    private fun setHoursOnButtons() {
        val cldr = Calendar.getInstance()
        val hour = cldr[Calendar.HOUR_OF_DAY] + 1
        val initHourValue = DateFormatHelper.setHour(hour)
        buttonStartTime.text = DateFormatHelper.setHour(initHourValue.toInt()) + ":00"
        buttonEndTime.text = DateFormatHelper.setHour(initHourValue.toInt() + 1) + ":00"
    }

    private fun refreshDoneButton() {
        if (note.done) {
            buttonDone.text = "Mark as undone"
        } else {
            buttonDone.text = "Mark as done"
        }
    }

    private fun deleteNoteAndAlarm() {
        val myDB = MyDatabaseHelper(this)
        alarmHelper.unsetAlarm(note.id!!,notificationHelper)
        myDB.deleteEvent(note.id!!)
    }

    private fun enableButtonIfCancel() {

        enableEditText(contentText, false)
        buttonStartDate.text = note.start_date
        buttonEndDate.text = note.end_date
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
        buttonStartTime = findViewById(R.id.start_time_button_2)
        buttonEndTime = findViewById(R.id.end_time_button_)
        buttonEdit = findViewById(R.id.edit_button_2)
        buttonAdd = findViewById(R.id.add_button_2)
    }
}
