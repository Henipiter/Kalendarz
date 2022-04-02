package com.example.kaledarz

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ShowElemActivity : AppCompatActivity() {

    lateinit var dateText: EditText
    lateinit var timeText: EditText
    lateinit var intervalText: TextView
    lateinit var contentText: EditText
    lateinit var editButton: Button
    lateinit var deleteButton: Button
    lateinit var backButton: Button
    lateinit var doneButton: Button
    lateinit var changeDateButton: Button
    lateinit var changeTimeButton: Button
    lateinit var changeIntevalButton: Button

    lateinit var myDB: MyDatabaseHelper

    private val CANCEL_INFO = "CANCEL"
    private val DELETE_INFO = "DELETE"
    private val EDIT_INFO = "EDIT"
    private val SAVE_INFO = "SAVE"

    var id = "1"
    var date = "1"
    var time = "1"
    var interval = "1"
    var content = "1"
    var done = "1"

    private fun findViews() {
        dateText = findViewById(R.id.date_text_2)
        timeText = findViewById(R.id.time_text_2)
        intervalText = findViewById(R.id.interval_text_2)
        contentText = findViewById(R.id.contentText_2)
        editButton = findViewById(R.id.edit_button_2)
        deleteButton = findViewById(R.id.delete_button_2)
        backButton = findViewById(R.id.back_button_2)
        doneButton = findViewById(R.id.done_button_2)
        changeDateButton = findViewById(R.id.date_button_2)
        changeIntevalButton = findViewById(R.id.interval_button_2)
        changeTimeButton = findViewById(R.id.time_button_2)
        editButton = findViewById(R.id.edit_button_2)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)

        findViews()
        enableButtonIfInit()

        changeIntevalButton.setOnClickListener { showAddItemDialog(this@ShowElemActivity) }

        deleteButton.setOnClickListener {
            doneButton.isEnabled = true
            if (deleteButton.text == CANCEL_INFO) { //CANCEL
                enableButtonIfCancel()
            } else {  //DELETE
                deleteNoteAndExit()
            }
        }
        backButton.setOnClickListener {
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        editButton.setOnClickListener {
            if (editButton.text != SAVE_INFO) { //EDIT
                enableButtonIfEdit()
            } else { //SAVE
                saveNoteAndExit()
            }
        }

        getAndSetIntentData()
        storeDataInArrays()
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

    private fun editIntervalText() {
        intervalText.text = "Remind in every $interval minutes"
    }


    private fun getAndSetIntentData() {
        if (intent.hasExtra("id")) {
            this.id = intent.getStringExtra("id").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeDataInArrays() {
        val note = myDB.readOneData(this.id)
        if (note.id == "") {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        } else {
            dateText.setText(note.date)
            timeText.setText(note.time)
            intervalText.text = "Remind in every "+note.interval+" minutes"
            contentText.setText(note.content)
        }

    }

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
    }

    private fun enableEditText(editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.isEnabled = true
        editText.isCursorVisible = true
    }

    private fun enableButtonIfSave() {
        disableEditText(dateText)
        disableEditText(timeText)
        disableEditText(contentText)

        editButton.text = EDIT_INFO
        deleteButton.text = DELETE_INFO
        doneButton.isEnabled = true
    }

    private fun enableButtonIfEdit() {
        enableEditText(contentText)

        editButton.text = SAVE_INFO
        deleteButton.text = CANCEL_INFO
        doneButton.isEnabled = false
        backButton.isEnabled = false
        changeDateButton.isEnabled = true
        changeIntevalButton.isEnabled = true
        changeTimeButton.isEnabled = true
    }

    private fun saveNoteAndExit() {
        val myDB = MyDatabaseHelper(this)
        myDB.deleteEvent(this.id)

        val note = Note(
            null,
            dateText.text.toString().trim(),
            timeText.text.toString().trim(),
            Integer.valueOf(intervalText.text.split(" ")[3].trim()),
            contentText.text.toString().trim(),
            false
        )

        myDB.addGame(note)
        finish()
        enableButtonIfSave()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun deleteNoteAndExit() {
        myDB.deleteEvent(this.id)
        finish()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun enableButtonIfCancel() {
        dateText.setText(date)
        intervalText.text = interval
        contentText.setText(content)
        timeText.setText(time)

        doneButton.isEnabled = true
        backButton.isEnabled = true
        changeDateButton.isEnabled = false
        changeIntevalButton.isEnabled = false
        changeTimeButton.isEnabled = false
        editButton.text = EDIT_INFO
        deleteButton.text = DELETE_INFO
    }

    private fun enableButtonIfInit() {
        disableEditText(dateText)
        disableEditText(timeText)
        disableEditText(contentText)
        doneButton.isEnabled = true
        backButton.isEnabled = true
        changeDateButton.isEnabled = false
        changeIntevalButton.isEnabled = false
        changeTimeButton.isEnabled = false
    }

}
