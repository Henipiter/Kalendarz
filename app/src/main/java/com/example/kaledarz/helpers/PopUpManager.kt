package com.example.kaledarz.helpers

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.kaledarz.R

class PopUpManager(private val context: Context) {

    private val minValue = 0
    private val maxValue = 31
    private lateinit var btPlus: Button
    private lateinit var btMinus: Button
    private lateinit var number: EditText

    private var valueNumber = 0

    fun getNumber(
        currentValue: Int,
        view: View,
        button: Button,
        startDate: String,
        textView: TextView
    ) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val title: TextView = view.findViewById(R.id.number_picker_title)
        number = view.findViewById(R.id.number_text)
        btPlus = view.findViewById(R.id.plus_button)
        btMinus = view.findViewById(R.id.minus_button)
        val btConfirm: Button = view.findViewById(R.id.confirm_number_button)
        title.text = "Select number"

        valueNumber = currentValue
        buttonController(valueNumber)
        number.setText(valueNumber.toString())

        number.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                when {
                    s.isEmpty() -> {
                        valueNumber = minValue
                        number.setText(minValue.toString())
                    }
                    s.toString().toInt() > maxValue -> {
                        valueNumber = maxValue
                        number.setText(maxValue.toString())
                    }
                    else -> {
                        valueNumber = s.toString().toInt()
                    }
                }
                buttonController(valueNumber)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
        btPlus.setOnClickListener {
            if (valueNumber < maxValue) {
                valueNumber++
                setValueAfterButtonClick()
            }
        }
        btMinus.setOnClickListener {
            if (valueNumber > minValue) {
                valueNumber--
                setValueAfterButtonClick()
            }
        }
        btMinus.setOnLongClickListener {
            if (valueNumber != minValue) {
                valueNumber = minValue
                setValueAfterButtonClick()
            }
            true
        }
        btPlus.setOnLongClickListener {
            if (valueNumber != maxValue) {
                valueNumber = maxValue
                setValueAfterButtonClick()
            }
            true
        }
        btConfirm.setOnClickListener {
            button.text = valueNumber.toString()
            if (valueNumber > minValue) {
                textView.text = "<" + startDate + " : " + DateFormatHelper.getNextDayFromString(
                    startDate, valueNumber
                ) + ">"
            } else {
                textView.text = ""
            }
            dialog?.dismiss()
        }
        builder.setView(view)

        dialog = builder.create()
        dialog.show()
    }

    private fun setValueAfterButtonClick() {
        number.setText(valueNumber.toString())
        buttonController(valueNumber)
    }

    private fun buttonController(valueNumber: Int) {
        when (valueNumber) {
            maxValue -> {
                btMinus.isEnabled = true
                btPlus.isEnabled = false
            }
            minValue -> {
                btMinus.isEnabled = false
                btPlus.isEnabled = true
            }
            else -> {
                btMinus.isEnabled = true
                btPlus.isEnabled = true
            }
        }
    }


}
