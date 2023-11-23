package com.example.kaledarz.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.R
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import com.example.kaledarz.helpers.PickerHelper

class AlarmSettingActivity : AppCompatActivity() {

    private lateinit var buttonRestart: Button
    private lateinit var switchOnOff: SwitchCompat
    private lateinit var switchExact: SwitchCompat
    private lateinit var onOffText: TextView
    private lateinit var exactText: TextView

    private lateinit var pickerHelper: PickerHelper

    private lateinit var myPref: SharedPreferences

    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()

    private lateinit var alarmHelper: AlarmHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        buttonRestart = findViewById(R.id.restart_button)
        pickerHelper = PickerHelper(this@AlarmSettingActivity)

        switchOnOff = findViewById(R.id.turn_on_off_switch)
        switchExact = findViewById(R.id.turn_on_off_exact)

        onOffText = findViewById(R.id.alarm_turn_on_text)
        exactText = findViewById(R.id.exact_turn_on_text)

        myPref = applicationContext.getSharedPreferences("run_alarms", MODE_PRIVATE)
        alarmHelper = AlarmHelper(applicationContext)

        val alarmOnOffPref = myPref.getString(Constants.ALARM_ON_OFF, "false") == "true"
        val alarmExactPref = myPref.getString(Constants.ALARM_EXACT, "false") == "true"

        switchOnOff.isChecked = alarmOnOffPref
        switchExact.isChecked = alarmExactPref
        switchOnOffAlarmBehaviour(alarmOnOffPref)

        databaseHelper = MyDatabaseHelper(this)
        originalList.addAll(databaseHelper.readAllData())

        buttonRestart.setOnClickListener {
            cancelAndSetAllAlarms()
        }

        switchOnOff.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            switchOnOffAlarmBehaviour(value)
            myPref.edit().putString(Constants.ALARM_EXACT, "false").apply()
            myPref.edit().putString(Constants.ALARM_ON_OFF, value.toString()).apply()
        }

        switchExact.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            myPref.edit().putString(Constants.ALARM_EXACT, value.toString()).apply()
            cancelAndSetAllAlarms()
        }

    }

    private fun switchOnOffAlarmBehaviour(value: Boolean) {
        switchExact.isEnabled = value
        onOffText.text = getInfoForOnOff(value)

        buttonRestart.isEnabled = value
        if (value) {
            alarmHelper.setAlarmForNotes(originalList)
        } else {
            switchExact.isChecked = false
            alarmHelper.unsetAlarmForNotes(originalList)
        }
    }

    private fun cancelAndSetAllAlarms() {
        alarmHelper.unsetAlarmForNotes(originalList)
        alarmHelper.setAlarmForNotes(originalList)
        Toast.makeText(this, "Alarms restarted", Toast.LENGTH_SHORT).show()
    }

    private fun getInfoForOnOff(value: Boolean): String {
        if (value) {
            return "Alarms are turned on"
        }
        return "Alarms are turned off"
    }
}