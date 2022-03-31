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

    lateinit var date_text: EditText
    lateinit var time_text: EditText
    lateinit var interval_text: TextView
    lateinit var content_text: EditText
    lateinit var edit_button: Button
    lateinit var delete_button: Button
    lateinit var back_button: Button
    lateinit var done_button: Button
    lateinit var change_date_button: Button
    lateinit var change_time_button: Button
    lateinit var change_inteval_button: Button

    lateinit var myDB: MyDatabaseHelper

    var CANCEL_INFO = "CANCEL"
    var DELETE_INFO = "DELETE"
    var EDIT_INFO = "EDIT"
    var SAVE_INFO = "SAVE"

    var id = "1"
    var date = "1"
    var time = "1"
    var interval = "1"
    var content = "1"
    var done = "1"

    private fun findViews() {
        date_text = findViewById(R.id.date_text_2)
        time_text = findViewById(R.id.time_text_2)
        interval_text = findViewById(R.id.interval_text_2)
        content_text = findViewById(R.id.contentText_2)
        edit_button = findViewById(R.id.edit_button_2)
        delete_button = findViewById(R.id.delete_button_2)
        back_button = findViewById(R.id.back_button_2)
        done_button = findViewById(R.id.done_button_2)
        change_date_button = findViewById(R.id.date_button_2)
        change_inteval_button = findViewById(R.id.interval_button_2)
        change_time_button = findViewById(R.id.time_button_2)
        edit_button = findViewById(R.id.edit_button_2)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)

        findViews()
        enableButtonIfInit()

        change_inteval_button.setOnClickListener { showAddItemDialog(this@ShowElemActivity) }


        delete_button.setOnClickListener {
            done_button.isEnabled = true
            if (delete_button.text == CANCEL_INFO) { //CANCEL
                enableButtonIfCancel()
            } else {  //DELETE
                enableButtonIfDelete()
            }
        }
        back_button.setOnClickListener {
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        }

        edit_button.setOnClickListener {
            if (edit_button.text != SAVE_INFO) { //EDIT
                enableButtonIfEdit()
            } else { //SAVE
                val myDB = MyDatabaseHelper(this)
                myDB.deleteEvent(this.id)
                myDB.addGame(
                    date_text.text.toString().trim(),
                    time_text.text.toString().trim(),
                    Integer.valueOf(interval_text.text.split(" ")[3].trim()),
                    content_text.text.toString().trim(),
                )
                finish()
                enableButtonIfSave()
                val homepage = Intent(this, MainActivity::class.java)
                startActivity(homepage)
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
        interval_text.text = "Remind in every $interval minutes"
    }


    private fun getAndSetIntentData() {
        if (intent.hasExtra("id")) {
            this.id = intent.getStringExtra("id").toString()
        } else {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeDataInArrays() {
        val cursor = myDB.readOneData(this.id)
        if (cursor?.count == 0) {
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        } else {
            while (cursor?.moveToNext() == true) {
                if (cursor.getString(0) == this.id) {

                    this.id = cursor.getString(0)
                    this.date = cursor.getString(1)
                    this.time = cursor.getString(2)
                    this.content = cursor.getString(4)
                    this.interval = cursor.getString(3)

                    date_text.setText(this.date)
                    time_text.setText(this.time)
                    interval_text.text = "Remind in every $interval minutes"
                    content_text.setText(this.content)
                    break
                }
            }
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
        disableEditText(date_text)
        disableEditText(time_text)
        disableEditText(content_text)

        edit_button.text = EDIT_INFO
        delete_button.text = DELETE_INFO
        done_button.isEnabled = true
    }

    private fun enableButtonIfEdit() {
        enableEditText(content_text)

        edit_button.text = SAVE_INFO
        delete_button.text = CANCEL_INFO
        done_button.isEnabled = false
        back_button.isEnabled = false
        change_date_button.isEnabled = true
        change_inteval_button.isEnabled = true
        change_time_button.isEnabled = true
    }

    private fun enableButtonIfDelete() {
        myDB.deleteEvent(this.id)
        finish()
        val homepage = Intent(this, MainActivity::class.java)
        startActivity(homepage)
    }

    private fun enableButtonIfCancel() {
        date_text.setText(date)
        interval_text.setText(interval)
        content_text.setText(content)
        time_text.setText(time)

        done_button.isEnabled = true
        back_button.isEnabled = true
        change_date_button.isEnabled = false
        change_inteval_button.isEnabled = false
        change_time_button.isEnabled = false
        edit_button.text = EDIT_INFO
        delete_button.text = DELETE_INFO
    }

    private fun enableButtonIfInit() {
        disableEditText(date_text)
        disableEditText(time_text)
        disableEditText(content_text)
        done_button.isEnabled = true
        back_button.isEnabled = true
        change_date_button.isEnabled = false
        change_inteval_button.isEnabled = false
        change_time_button.isEnabled = false
    }

}
