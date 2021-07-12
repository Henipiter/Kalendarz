package com.example.kaledarz

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class AddEventActivity : AppCompatActivity() {

    var year= "0";
    var month ="0";
    var dayOfMonth="0";
    var H:String = "09"
    var M:String = "00"
    var interval = "5"
    lateinit var button_remind: Button
    lateinit var add_button: Button
    lateinit var button_date: Button
    lateinit var data_text: TextView
    lateinit var contentText1: EditText
    lateinit var button_time: Button
    lateinit var time_text:TextView;
    lateinit var interval_text:TextView;
    var cal = Calendar.getInstance()
    var picker: TimePickerDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        data_text = findViewById(R.id.data_text)
        time_text = findViewById(R.id.time_text)
        button_date = findViewById(R.id.date_button)
        add_button = findViewById(R.id.edit_button_1)
        button_time = findViewById(R.id.time_button)
        button_remind = findViewById(R.id.remind_button)
        interval_text = findViewById(R.id.interval_text)
        contentText1 = findViewById(R.id.contentText)
        getAndSetIntentData()
        editIntervalText()



        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }

        button_remind.setOnClickListener(View.OnClickListener { showAddItemDialog(this@AddEventActivity) })


        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        button_date!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@AddEventActivity,
                        dateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        })
        button_time.setOnClickListener(View.OnClickListener {
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]
            val minutes = cldr[Calendar.MINUTE]
            // time picker dialog
            picker = TimePickerDialog(this@AddEventActivity,
                    { tp, sHour, sMinute ->

                        if (sHour.toString().length == 1) H = "0" + sHour.toString()
                        else H = sHour.toString()
                        if (sMinute.toString().length == 1) M = "0" + sMinute.toString()
                        else M = sMinute.toString()
                        time_text.setText(H + ":" + M)

                    }, hour, minutes, true)
            picker!!.show()
        })


        add_button.setOnClickListener(View.OnClickListener {

            val myDB = MyDatabaseHelper(this)

            myDB.addGame(
                    data_text.getText().toString().trim(),
                    time_text.getText().toString().trim(),
                    Integer.valueOf(interval.toString().trim()),
                    contentText1.getText().toString().trim()


            )
            finish()
            val homepage = Intent(this, MainActivity::class.java)
            startActivity(homepage)
        })


    }
    private fun editIntervalText(){
        val text1 = "Remind in every "
        val text2 = " minutes"
        interval_text.setText( text1 + interval + text2 )
    }

    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        data_text!!.text = sdf.format(cal.getTime())
    }
    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        taskEditText.setInputType(InputType.TYPE_CLASS_NUMBER )
        val dialog: AlertDialog = AlertDialog.Builder(c)
                .setTitle("Change interval time")
                .setView(taskEditText)
                .setPositiveButton("Save", DialogInterface.OnClickListener {
                    dialog, which ->
                    interval = taskEditText.text.toString()
                    editIntervalText()
                })
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }
    fun getAndSetIntentData(){

        if( getIntent().hasExtra("year") &&
                getIntent().hasExtra("month") &&
                getIntent().hasExtra("dayOfMonth")  ){
            this.year= intent.getStringExtra("year").toString()
            this.month= intent.getStringExtra("month").toString()
            this.dayOfMonth= intent.getStringExtra("dayOfMonth").toString()
            if(this.month.length==1)
                this.month = "0"+this.month
            data_text.text = this.dayOfMonth + "-" + this.month + "-" + this.year;
        }else{
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show()
        }
    }
}