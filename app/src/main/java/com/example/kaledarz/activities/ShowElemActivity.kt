package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.helpers.*

class ShowElemActivity : AppCompatActivity() {

    companion object {
        private const val EDIT_INFO = "EDIT"
        private const val CANCEL_INFO = "CANCEL"
        private const val DELETE_INFO = "DELETE"
        private const val SAVE_INFO = "SAVE"
    }

    private var activityType = "ADD"
    private lateinit var textDuplicate: TextView
    private lateinit var contentText: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonDone: Button
    private lateinit var buttonStartDate: Button
    private lateinit var buttonEndDate: Button
    private lateinit var buttonStartTime: Button
    private lateinit var buttonEndTime: Button
    private lateinit var buttonDuplicateNumber: Button
    private lateinit var buttonDuplicate: Button

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmHelper: AlarmHelper

    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()
    private lateinit var pickerHelper: PickerHelper
    private var isRedButtonSet=false

    override fun onCreate(savedInstanceState: Bundle?) {
        notificationHelper = NotificationHelper(this)
        alarmHelper = AlarmHelper(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        notificationHelper.createNotificationChannel()
        myDB = MyDatabaseHelper(this)
        pickerHelper = PickerHelper(this@ShowElemActivity)
        findViews()
        getAndSetIntentData()
        buttonStartTime.setOnClickListener {
            pickerHelper.runTimePicker(buttonStartTime)
        }

        buttonEndTime.setOnClickListener {
            pickerHelper.runTimePicker(buttonEndTime)
        }

        buttonStartDate.setOnClickListener {
            pickerHelper.runDatePicker(buttonStartDate)
        }

        buttonEndDate.setOnClickListener {
            pickerHelper.runDatePicker(buttonEndDate)
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
                alarmHelper.unsetAlarm(note.id!!)
            } else {
                alarmHelper.setAlarm(note)
            }
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        buttonAdd.setOnClickListener {
            validDatesAndAddNote()
        }

        buttonDuplicateNumber.setOnClickListener {
            val popUpManager = PopUpManager(this)
            popUpManager.getNumber(
                buttonDuplicateNumber.text.toString().toInt(),
                layoutInflater.inflate(R.layout.number_picker, null),
                buttonDuplicateNumber
            )
        }

        buttonDuplicate.setOnClickListener {
            val intent = Intent(this, ShowElemActivity::class.java)
            intent.putExtra("type", "ADD")
            intent.putExtra("start_date", buttonStartDate.text.toString())
            intent.putExtra("end_date", buttonEndDate.text.toString())
            intent.putExtra("start_time", buttonStartTime.text.toString())
            intent.putExtra("end_time", buttonEndTime.text.toString())
            intent.putExtra("content", contentText.text.toString())
            this.startActivity(intent)
            finish()
        }
    }

    private fun addDuplicatedNotes() {
        for (i in 1..buttonDuplicateNumber.text.toString().toInt()) {
            note.start_date = DateFormatHelper.getNextDayFromString(note.start_date)
            note.end_date = DateFormatHelper.getNextDayFromString(note.end_date)
            addNoteToDatabase()
        }
    }

    private fun createNote(): Note {

        var content = contentText.text.toString().trim()
        if (content == "") {
            content = "Reminder"
        }
        return Note(
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
    }

    private fun addNoteToDatabase() {
        val myDB = MyDatabaseHelper(this)
        myDB.addGame(note)
        note.id = myDB.readLastRow().id
    }

    private fun setRedButtonIfDatesWrong() {
        isRedButtonSet = if (checkRightDate()) {
            buttonEndDate.setBackgroundColor(Color.parseColor("#0FB104"))
            if (checkRightHour()) {
                buttonEndTime.setBackgroundColor(Color.parseColor("#0FB104"))
                false
            } else {
                buttonEndTime.setBackgroundColor(Color.parseColor("#910000"))
                true
            }
        } else {
            buttonEndDate.setBackgroundColor(Color.parseColor("#910000"))
            buttonEndTime.setBackgroundColor(Color.parseColor("#0FB104"))
            true
        }
    }

    private fun checkRightDate(): Boolean {
        return DateFormatHelper.isEndDateGreaterAndEqualThanStartDate(
            buttonStartDate.text.toString(),
            buttonEndDate.text.toString()
        )
    }

    private fun checkRightHour(): Boolean {
        return DateFormatHelper.isEndDateEqualToStartDate(
            buttonStartDate.text.toString(),
            buttonStartDate.text.toString()
        ) && DateFormatHelper.isEndTimeGreaterThanStartTime(
            buttonStartTime.text.toString(),
            buttonEndTime.text.toString()
        ) || DateFormatHelper.isEndDateGreaterThanStartDate(
            buttonStartDate.text.toString(),
            buttonEndDate.text.toString()
        )
    }

    private fun showErrorDateDialog(c: Context) {
        var messageError = ""
        if (!checkRightDate()) {
            messageError = "Start date is later than end date"
        }
        if (!checkRightHour()
        ) {
            messageError = "End time is not later than start time"
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
                finish()
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
        }
    }

    private fun getIntentForAddView() {
        buttonDone.text = "Add"
        if (intent.hasExtra("date")) {
            buttonStartDate.text = intent.getStringExtra("date").toString()
            buttonEndDate.text = intent.getStringExtra("date").toString()
        }
        if (intent.hasExtra("start_date")) {
            buttonStartDate.text = intent.getStringExtra("start_date").toString()
        }
        if (intent.hasExtra("end_date")) {
            buttonEndDate.text = intent.getStringExtra("end_date").toString()
        }
        if (intent.hasExtra("start_time")) {
            buttonStartTime.text = intent.getStringExtra("start_time").toString()
        }
        if (intent.hasExtra("end_time")) {
            buttonEndTime.text = intent.getStringExtra("end_time").toString()
        }
        if (intent.hasExtra("content")) {
            contentText.setText(intent.getStringExtra("content").toString())
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
        validDatesAndAddNote()
    }

    private fun validDatesAndAddNote(){
        setRedButtonIfDatesWrong()
        if (!isRedButtonSet) {
            note = createNote()
            addNoteToDatabase()
            addDuplicatedNotes()
            finish()
        } else {
            showErrorDateDialog(this@ShowElemActivity)
        }
    }

    private fun setHoursOnButtons() {
        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY] + 1
        buttonStartTime.text = DateFormatHelper.makeFullHour(hour, 0)
        buttonEndTime.text = DateFormatHelper.makeFullHour(23, 59)
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
        alarmHelper.unsetAlarm(note.id!!)
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
        buttonDuplicate.visibility = View.GONE
        buttonAdd.visibility = View.VISIBLE
        buttonDuplicateNumber.visibility = View.VISIBLE
        textDuplicate.visibility = View.VISIBLE
    }

    private fun showEditViewButton() {
        buttonEdit.visibility = View.VISIBLE
        buttonDelete.visibility = View.VISIBLE
        buttonDone.visibility = View.VISIBLE
        buttonDuplicate.visibility = View.VISIBLE
        buttonAdd.visibility = View.GONE
        buttonDuplicateNumber.visibility = View.GONE
        textDuplicate.visibility = View.GONE

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
        buttonDuplicate.isEnabled = !bool
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
        buttonDuplicateNumber = findViewById(R.id.duplication_number_button)
        textDuplicate = findViewById(R.id.textView15)
        buttonDuplicate = findViewById(R.id.duplication_button)

    }
}
