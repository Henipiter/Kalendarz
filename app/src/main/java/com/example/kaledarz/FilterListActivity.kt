package com.example.kaledarz

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat


class FilterListActivity : AppCompatActivity() {

    private lateinit var buttonExit: Button
    private lateinit var buttonUpperStart: Button
    private lateinit var buttonUpperEnd: Button
    private lateinit var buttonLowerStart: Button
    private lateinit var buttonLowerEnd: Button

    private lateinit var switchLowerStart: SwitchCompat
    private lateinit var switchLowerEnd: SwitchCompat
    private lateinit var switchUpperStart: SwitchCompat
    private lateinit var switchUpperEnd: SwitchCompat


    private var enableStart = false
    private var enableEnd = false
    private var calendar = Calendar.getInstance()

    companion object{
        const val LOWER_START = "LOWER_START"
        const val LOWER_END = "LOWER_END"
        const val UPPER_START = "UPPER_START"
        const val UPPER_END = "UPPER_END"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_list)

        buttonExit = findViewById(R.id.button_exit)
        buttonUpperStart = findViewById(R.id.upper_start_date_button)
        buttonUpperEnd = findViewById(R.id.upper_end_date_button)
        buttonLowerStart = findViewById(R.id.lower_start_date_button)
        buttonLowerEnd = findViewById(R.id.lower_end_date_button)

        switchLowerStart = findViewById(R.id.lower_start_switch)
        switchLowerEnd = findViewById(R.id.lower_end_switch)
        switchUpperStart = findViewById(R.id.upper_start_switch)
        switchUpperEnd = findViewById(R.id.upper_end_switch)

        receiveIntents()
        buttonExit.setOnClickListener {
            val returnIntent = addIntentAtConfirm()
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        buttonLowerStart.setOnClickListener {
            DatePickerDialog(
                this@FilterListActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonLowerStart.text = DateFormatHelper.updateDateInView(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonLowerEnd.setOnClickListener {
            DatePickerDialog(
                this@FilterListActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonLowerEnd.text = DateFormatHelper.updateDateInView(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonUpperStart.setOnClickListener {
            DatePickerDialog(
                this@FilterListActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonUpperStart.text = DateFormatHelper.updateDateInView(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        buttonUpperEnd.setOnClickListener {
            DatePickerDialog(
                this@FilterListActivity,
                { view, year, monthOfYear, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, monthOfYear)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    buttonUpperEnd.text = DateFormatHelper.updateDateInView(calendar)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        switchLowerEnd.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            buttonLowerEnd.isEnabled = value
        }
        switchLowerStart.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            buttonLowerStart.isEnabled = value
        }
        switchUpperEnd.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            buttonUpperEnd.isEnabled = value
        }
        switchUpperStart.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            buttonUpperStart.isEnabled = value
        }
    }

    private fun receiveIntents() {
        analyzeIntent(LOWER_START)
        analyzeIntent(LOWER_END)
        analyzeIntent(UPPER_START)
        analyzeIntent(UPPER_END)
    }

    private fun analyzeIntent(name:String){
        if (intent.hasExtra(name)) {
            val intentValue = intent.getStringExtra(name)
            val switch = getSwitchByName(name)
            val button = getButtonByName(name)
            if (intentValue == "none") {
                switch.isChecked = false
                button.isEnabled = false
                button.text = DateFormatHelper.getCurrentDateTime().substring(0, 10)
            } else {
                switch.isChecked = true
                button.isEnabled = true
                button.text = intentValue
            }
        }
    }

    private fun addIntentAtConfirm(): Intent {
        val returnIntent = Intent()
        if (switchLowerEnd.isChecked) {
            returnIntent.putExtra(LOWER_END, buttonLowerEnd.text)
        }
        if (switchLowerStart.isChecked) {
            returnIntent.putExtra(LOWER_START, buttonLowerStart.text)
        }
        if (switchUpperEnd.isChecked) {
            returnIntent.putExtra(UPPER_END, buttonUpperEnd.text)
        }
        if (switchUpperStart.isChecked) {
            returnIntent.putExtra(UPPER_START, buttonUpperStart.text)
        }
        return returnIntent
    }

    private fun getSwitchByName(name:String):SwitchCompat{
        return when(name){
            LOWER_END -> {
                switchLowerEnd
            }
            UPPER_END -> {
                switchUpperEnd
            }
            LOWER_START -> {
                switchLowerStart
            }
            else -> {
                switchUpperStart
            }
        }
    }

    private fun getButtonByName(name:String):Button{
        return when(name){
            LOWER_END -> {
                buttonLowerEnd
            }
            UPPER_END -> {
                buttonUpperEnd
            }
            LOWER_START -> {
                buttonLowerStart
            }
            else -> {
                buttonUpperStart
            }
        }
    }
}