package com.example.kaledarz.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.kaledarz.DTO.Constants.Companion.LOWER_START
import com.example.kaledarz.DTO.Constants.Companion.LOWER_END
import com.example.kaledarz.DTO.Constants.Companion.UPPER_START
import com.example.kaledarz.DTO.Constants.Companion.UPPER_END
import com.example.kaledarz.DTO.Constants.Companion.CONTENT
import com.example.kaledarz.DTO.Constants.Companion.NONE
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.R


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
    private lateinit var switchContent: SwitchCompat

    private lateinit var textContent: EditText

    private var calendar = Calendar.getInstance()

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
        switchContent = findViewById(R.id.content_switch)

        textContent = findViewById(R.id.content_text2)

        receiveIntents()
        buttonExit.setOnClickListener {


            if (checkIfFiltersAreValid()) {
                val returnIntent = addIntentAtConfirm()
                setResult(RESULT_OK, returnIntent)
                finish()
            }
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
        switchContent.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            textContent.isEnabled = value
        }
    }

    private fun receiveIntents() {
        analyzeIntentForButtons(LOWER_START)
        analyzeIntentForButtons(LOWER_END)
        analyzeIntentForButtons(UPPER_START)
        analyzeIntentForButtons(UPPER_END)
        analyzeIntentForText(CONTENT)

    }

    private fun analyzeIntentForButtons(name: String) {
        if (intent.hasExtra(name)) {
            val intentValue = intent.getStringExtra(name)
            val switch = getSwitchByName(name)
            val button = getButtonByName(name)
            if (intentValue == NONE) {
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

    private fun analyzeIntentForText(name: String) {
        if (intent.hasExtra(name)) {
            val intentValue = intent.getStringExtra(name)
            val switch = getSwitchByName(name)
            val enabled = intentValue != NONE
            switch.isChecked = enabled
            textContent.isEnabled = enabled
            if (enabled) {
                textContent.setText(intentValue)
            } else {
                textContent.setText("")
            }
        }
    }

    private fun addIntentAtConfirm(): Intent {
        val returnIntent = Intent()
        if (switchContent.isChecked) {
            returnIntent.putExtra(CONTENT, textContent.text.toString())
        }
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

    private fun getSwitchByName(name: String): SwitchCompat {
        return when (name) {
            LOWER_END -> {
                switchLowerEnd
            }
            UPPER_END -> {
                switchUpperEnd
            }
            LOWER_START -> {
                switchLowerStart
            }
            UPPER_START -> {
                switchUpperStart
            }
            else -> {
                switchContent
            }
        }
    }

    private fun getButtonByName(name: String): Button {
        return when (name) {
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

    private fun checkIfFiltersAreValid(): Boolean {

        return checkStartDateFilter() && checkEndDateFilter() && checkContentFilter()
    }

    private fun checkStartDateFilter(): Boolean {
        return checkDateFilter(
            "Start",
            switchLowerStart,
            switchLowerStart,
            buttonLowerStart,
            buttonUpperStart
        )
    }

    private fun checkEndDateFilter(): Boolean {
        return checkDateFilter(
            "Start",
            switchLowerEnd,
            switchLowerEnd,
            buttonLowerEnd,
            buttonUpperEnd
        )
    }

    private fun checkDateFilter(
        whichDate: String,
        lowerSwitch: SwitchCompat,
        upperSwitch: SwitchCompat,
        lowerDate: Button,
        upperDate: Button
    ): Boolean {
        if (lowerSwitch.isChecked && upperSwitch.isChecked &&
            !DateFormatHelper.isFirstDateGreaterAndEqualToSecond(
                upperDate.text.toString(),
                lowerDate.text.toString(),
                "yyyy-MM-dd"
            )
        ) {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("$whichDate date value error")
                .setMessage("Lower limit is greater than upper limit")
                .setNegativeButton("OK", null)
                .create()
            dialog.show()
            return false
        }
        return true
    }

    private fun checkContentFilter(): Boolean {
        if (switchContent.isChecked && textContent.text.toString() == "") {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Content value error")
                .setMessage("Fill this field")
                .setNegativeButton("OK", null)
                .create()
            dialog.show()
            return false
        }
        return true
    }
}