package com.example.kaledarz.helpers

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.kaledarz.R

class PopUpManager(private val context: Context) {

    fun getNumber(currentValue: Int, view: View, button: Button) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(context)
        val title: TextView = view.findViewById(R.id.number_picker_title)
        val number: EditText = view.findViewById(R.id.number_text)
        val btPlus: Button = view.findViewById(R.id.plus_button)
        val btMinus: Button = view.findViewById(R.id.minus_button)
        val btConfirm: Button = view.findViewById(R.id.confirm_number_button)
        title.text = "Select number"

        var valueNumber = currentValue
        number.setText(valueNumber.toString())

        btPlus.setOnClickListener {
            valueNumber++
            number.setText(valueNumber.toString())
        }
        btMinus.setOnClickListener {
            if (valueNumber > 0) {
                valueNumber--
            }
            number.setText(valueNumber.toString())
        }
        btConfirm.setOnClickListener {
            button.text = valueNumber.toString()
            dialog?.dismiss()
        }
        builder.setView(view)

        dialog = builder.create()
        dialog.show()
    }


}