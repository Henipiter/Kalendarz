package com.example.kaledarz

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class ShowElemActivity : AppCompatActivity() {

    lateinit var date_text: EditText
    lateinit var time_text: EditText
    lateinit var interval_text: EditText
    lateinit var content_text: EditText
    lateinit var edit_button: Button
    lateinit var delete_button: Button
    lateinit var back_button: Button
    lateinit var done_button: Button
    lateinit var change_date_button: Button
    lateinit var change_time_button: Button
    lateinit var change_inteval_button: Button

    lateinit var myDB: MyDatabaseHelper
    var id="1"
    var date="1"
    var time="1"
    var interval="1"
    var content="1"
    var done="1"

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
    }
    private fun enableEditText(editText: EditText) {
        editText.setFocusableInTouchMode(true)
        editText.isEnabled = true
        editText.isCursorVisible = true
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_elem)
        myDB = MyDatabaseHelper(this)
        date_text = findViewById(R.id.date_text_2)
        time_text = findViewById(R.id.time_text_2)
        interval_text = findViewById(R.id.interval_text_2)
        content_text= findViewById(R.id.contentText_2)

        disableEditText(date_text)
        disableEditText(time_text)
        disableEditText(content_text)
        disableEditText(interval_text)
        edit_button=findViewById(R.id.edit_button_2)
        delete_button=findViewById(R.id.delete_button_2)
        back_button=findViewById(R.id.back_button_2)
        done_button=findViewById(R.id.done_button_2)
        change_date_button=findViewById(R.id.date_button_2)
        change_inteval_button=findViewById(R.id.interval_button_2)
        change_time_button=findViewById(R.id.time_button_2)
        done_button.setEnabled(true);
        back_button.setEnabled(true);
        change_date_button.setEnabled(false);
        change_inteval_button.setEnabled(false);
        change_time_button.setEnabled(false);
        delete_button.setOnClickListener(View.OnClickListener {

            done_button.setEnabled(true);

            if(delete_button.text == "CANCEL") { //CANCEL

                date_text.setText(date)
                interval_text.setText(interval)
                content_text.setText(content)
                time_text.setText(time)




                done_button.setEnabled(true);
                back_button.setEnabled(true);
                change_date_button.setEnabled(false);
                change_inteval_button.setEnabled(false);
                change_time_button.setEnabled(false);
                edit_button.text = "EDIT"
                delete_button.text = "DELETE"
            }
            else{  //DELETE



                myDB.deleteEvent(this.id)
                finish()
                val homepage = Intent(this, MainActivity::class.java)
                startActivity(homepage)
            }


        })
        back_button.setOnClickListener(View.OnClickListener {

            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        })




        edit_button=findViewById(R.id.edit_button_2)


        edit_button.setOnClickListener(View.OnClickListener {
            if(edit_button.text != "SAVE") { //EDIT

                enableEditText(content_text)

                edit_button.text = "SAVE"
                delete_button.text = "CANCEL"
                done_button.setEnabled(false);
                back_button.setEnabled(false);
                change_date_button.setEnabled(true);
                change_inteval_button.setEnabled(true);
                change_time_button.setEnabled(true);

            }
            else{ //SAVE
                val myDB = MyDatabaseHelper(this)


                myDB.deleteEvent(this.id)
                myDB.addGame(
                    date_text.getText().toString().trim(),
                    time_text.getText().toString().trim(),
                    Integer.valueOf(interval_text.toString().trim()),
                    content_text.getText().toString().trim()
                )

                disableEditText(date_text)
                disableEditText(time_text)
                disableEditText(content_text)
                disableEditText(interval_text)

                edit_button.text = "EDIT"
                delete_button.text = "DELETE"
                done_button.setEnabled(true);
            }

        })

        getAndSetIntentData()
        storeDataInArrays()

    }
    private fun editIntervalText(){
        val text1 = "Remind in every "
        val text2 = " minutes"
        interval_text.setText( text1 + interval + text2 )
    }
    fun getAndSetIntentData(){
        if( getIntent().hasExtra("id")){
            this.id = intent.getStringExtra("id").toString()
        }else
        {            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show() }
    }
    fun storeDataInArrays(){
        val cursor = myDB.readOneData(this.id)
        if(cursor?.count == 0){            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()        }
        else{
            while(cursor?.moveToNext() == true){
                if(cursor.getString(0) == this.id){

                    this.id = cursor.getString(0)
                    this.date = cursor.getString(1)
                    this.time =cursor.getString(2)
                    this.content=cursor.getString(4)
                    this.interval=cursor.getString(3)


                    date_text.setText(this.date)
                    time_text.setText(this.time)
                    editIntervalText()
                    content_text.setText(this.content)

                    break;

                }
            }
        }
    }
}