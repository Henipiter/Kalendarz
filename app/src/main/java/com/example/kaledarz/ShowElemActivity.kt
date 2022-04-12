package com.example.kaledarz

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
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

    private lateinit var contentText: EditText
    lateinit var buttonEdit: Button
    lateinit var buttonDelete: Button
    lateinit var buttonBack: Button
    lateinit var buttonDone: Button
    lateinit var buttonDate: Button
    lateinit var buttonStartTime: Button
    lateinit var buttonEndTime: Button
    lateinit var buttonInterval: Button

    private lateinit var myDB: MyDatabaseHelper
    private var note = Note()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)

        findViews()
        getAndSetIntentData()
        storeDataInArrays()
        enableButtonIfInit()

        buttonInterval.setOnClickListener { showAddItemDialog(this@ShowElemActivity) }

        buttonDelete.setOnClickListener {
            buttonDone.isEnabled = true
            if (buttonDelete.text == CANCEL_INFO) { //CANCEL
                enableButtonIfCancel()
            } else {  //DELETE
                deleteNoteAndExit()
            }
        }
        buttonBack.setOnClickListener {
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        buttonEdit.setOnClickListener {
            if (buttonEdit.text != SAVE_INFO) { //EDIT
                enableButtonIfEdit()
            } else { //SAVE
                saveNoteAndExit()
            }
        }

        buttonDone.setOnClickListener {
            buttonDoneManager()
            myDB.updateDone(note.id.toString(), !note.done)
        }
    }

    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        taskEditText.inputType = InputType.TYPE_CLASS_NUMBER
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Change interval time")
            .setView(taskEditText)
            .setPositiveButton("Save") { dialog, which ->
                note.interval = taskEditText.text.toString().toInt()
                editIntervalText()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun editIntervalText() {
        buttonInterval.text = note.interval.toString() + " minutes"
    }


    private fun getAndSetIntentData() {
        if (intent.hasExtra("id")) {
            note.id = intent.getStringExtra("id").toString()
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
            buttonDate.text = note.date
            buttonStartTime.text = note.start_time
            buttonEndTime.text = note.start_time
            buttonInterval.text = note.interval.toString() + " minutes"
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
        disableEditText(contentText)
        buttonEdit.text = EDIT_INFO
        buttonDelete.text = DELETE_INFO
        buttonDone.isEnabled = true
    }

    private fun enableButtonIfEdit() {
        enableEditText(contentText)
        buttonEdit.text = SAVE_INFO
        buttonDelete.text = CANCEL_INFO
        buttonDone.isEnabled = false
        buttonBack.isEnabled = false
        buttonDate.isEnabled = true
        buttonInterval.isEnabled = true
        buttonStartTime.isEnabled = true
        buttonEndTime.isEnabled = true
    }

    private fun buttonDoneManager() {
        if (note.done) {
            buttonDone.text = "Mark as undone"
            note.done = false
        } else {
            buttonDone.text = "Mark as done"
            note.done = true
        }
    }

    private fun saveNoteAndExit() {
        val myDB = MyDatabaseHelper(this)
        myDB.deleteEvent(note.id.toString())

        val note = Note(
            null,
            buttonDate.text.toString().trim(),
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

    private fun deleteNoteAndExit() {
        myDB.deleteEvent(note.id.toString())
        finish()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun enableButtonIfCancel() {
        buttonDate.text = note.date
        buttonInterval.text = note.interval.toString()
        contentText.setText(note.content)
        buttonStartTime.text = note.start_time

        buttonDone.isEnabled = true
        buttonBack.isEnabled = true
        buttonDate.isEnabled = false
        buttonInterval.isEnabled = false
        buttonStartTime.isEnabled = false
        buttonEndTime.isEnabled = false
        buttonEdit.text = Companion.EDIT_INFO
        buttonDelete.text = DELETE_INFO
    }

    private fun enableButtonIfInit() {
        disableEditText(contentText)
        buttonDone.isEnabled = true
        buttonBack.isEnabled = true
        buttonDate.isEnabled = false
        buttonInterval.isEnabled = false
        buttonStartTime.isEnabled = false
        buttonEndTime.isEnabled = false

        buttonDoneManager()
    }

    private fun findViews() {
        contentText = findViewById(R.id.contentText_2)
        buttonEdit = findViewById(R.id.edit_button_2)
        buttonDelete = findViewById(R.id.delete_button_2)
        buttonBack = findViewById(R.id.back_button_2)
        buttonDone = findViewById(R.id.done_button_2)
        buttonDate = findViewById(R.id.date_button_2)
        buttonInterval = findViewById(R.id.interval_button_2)
        buttonStartTime = findViewById(R.id.start_time_button_2)
        buttonEndTime = findViewById(R.id.end_time_button_)
        buttonEdit = findViewById(R.id.edit_button_2)
    }
}
