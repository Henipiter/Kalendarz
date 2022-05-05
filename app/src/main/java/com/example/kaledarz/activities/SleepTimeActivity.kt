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

class SleepTimeActivity : AppCompatActivity() {

    private lateinit var buttonRestart: Button
    private lateinit var buttonStartDate: Button
    private lateinit var buttonStartTime: Button
    private lateinit var switchSleep: SwitchCompat
    private lateinit var switchOnOff: SwitchCompat
    private lateinit var sleepText: TextView
    private lateinit var onOffText: TextView

    private lateinit var pickerHelper: PickerHelper

    private lateinit var myPref: SharedPreferences

    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()

    private lateinit var alarmHelper: AlarmHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_time)

        buttonRestart = findViewById(R.id.restart_button)
        pickerHelper = PickerHelper(this@SleepTimeActivity)
        buttonStartDate = findViewById(R.id.sleep_end_time_button)
        buttonStartTime = findViewById(R.id.sleep_start_time_button)

        switchSleep = findViewById(R.id.sleep_switch)
        switchOnOff = findViewById(R.id.turn_on_off_switch)

        sleepText = findViewById(R.id.alarm_sleep_mode_text)
        onOffText = findViewById(R.id.alarm_turn_on_text)

        myPref = applicationContext.getSharedPreferences("run_alarms", MODE_PRIVATE)
        alarmHelper = AlarmHelper(applicationContext)

        val sleepOnOffPref = myPref.getString(Constants.SLEEP_ON_OFF, "false") == "true"
        val alarmOnOffPref = myPref.getString(Constants.ALARM_ON_OFF, "false") == "true"

        switchSleep.isChecked = sleepOnOffPref
        switchOnOff.isChecked = alarmOnOffPref
        if (sleepOnOffPref) {
            switchOnOffAlarmBehaviour(alarmOnOffPref)
            switchOnOffSleepBehaviour(sleepOnOffPref)
        } else {
            switchOnOffSleepBehaviour(sleepOnOffPref)
            switchOnOffAlarmBehaviour(alarmOnOffPref)
        }

        databaseHelper = MyDatabaseHelper(this)
        originalList.addAll(databaseHelper.readAllData())

        buttonRestart.setOnClickListener {
            cancelAndSetAllAlarms()
        }

        buttonStartTime.setOnClickListener {
            pickerHelper.runTimePicker(buttonStartTime)
        }
        buttonStartDate.setOnClickListener {
            pickerHelper.runTimePicker(buttonStartDate)
        }

        switchOnOff.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            switchOnOffAlarmBehaviour(value)
            myPref.edit().putString(Constants.ALARM_ON_OFF, value.toString()).apply()
        }

        switchSleep.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            switchOnOffSleepBehaviour(value)
            myPref.edit().putString(Constants.SLEEP_ON_OFF, value.toString()).apply()

        }
    }

    private fun switchOnOffAlarmBehaviour(value: Boolean) {
        switchSleep.isEnabled = value
        onOffText.text = getInfoForOnOff(value)

        buttonRestart.isEnabled = value
        setButtons(value)
        if (value) {

            alarmHelper.setAlarmForNotes(originalList)
        } else {
            switchSleep.isChecked = false
            alarmHelper.unsetAlarmForNotes(originalList)
        }
    }

    private fun switchOnOffSleepBehaviour(value: Boolean) {
        sleepText.text = getInfoForSleep(value)
        setButtons(!value)
    }

    private fun cancelAndSetAllAlarms() {
        alarmHelper.unsetAlarmForNotes(originalList)
        alarmHelper.setAlarmForNotes(originalList)
        Toast.makeText(this, "Alarms restarted", Toast.LENGTH_SHORT).show()
    }

    private fun setButtons(boolean: Boolean) {
        buttonStartDate.isEnabled = boolean
        buttonStartTime.isEnabled = boolean
    }

    private fun getInfoForOnOff(value: Boolean): String {
        if (value) {
            return "Alarms are turn on"
        }
        return "Alarms are turn off"
    }

    private fun getInfoForSleep(value: Boolean): String {
        if (value) {
            return "Sleep mode is turn on"
        }
        return "Sleep mode is turn off"
    }
}