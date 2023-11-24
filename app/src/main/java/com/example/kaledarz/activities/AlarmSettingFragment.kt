package com.example.kaledarz.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kaledarz.helpers.ApplicationContext
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.databinding.FragmentAlarmSettingBinding
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import com.example.kaledarz.helpers.PickerHelper

class AlarmSettingFragment : Fragment() {


    private var _binding: FragmentAlarmSettingBinding? = null
    private val binding get() = _binding!!
    private var alarmHelper: AlarmHelper? = null

    private var myPref: SharedPreferences? = null
    private lateinit var pickerHelper: PickerHelper
    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ApplicationContext.context?.let {
            myPref = it.getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
            alarmHelper = AlarmHelper(it)
        }


        pickerHelper = PickerHelper(requireContext())

        val alarmOnOffPref = myPref?.getString(Constants.ALARM_ON_OFF, "false") == "true"
        val alarmExactPref = myPref?.getString(Constants.ALARM_EXACT, "false") == "true"

        binding.turnOnOffSwitch.isChecked = alarmOnOffPref
        binding.exactSwitch.isChecked = alarmExactPref
        switchOnOffAlarmBehaviour(alarmOnOffPref)

        databaseHelper = MyDatabaseHelper(requireContext())
        originalList.addAll(databaseHelper.readAllData())

            binding.restartButton.setOnClickListener {
            cancelAndSetAllAlarms()
        }

        binding.turnOnOffSwitch.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            switchOnOffAlarmBehaviour(value)
            myPref?.let {
                it.edit()?.putString(Constants.ALARM_EXACT, "false")?.apply()
                it.edit()?.putString(Constants.ALARM_ON_OFF, value.toString())?.apply()
            }
        }

        binding.exactSwitch.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
            myPref?.let {
                it.edit()?.putString(Constants.ALARM_EXACT, value.toString())?.apply()
            }
            cancelAndSetAllAlarms()
        }
    }
    private fun switchOnOffAlarmBehaviour(value: Boolean) {
        binding.exactSwitch.isEnabled = value
        binding.alarmTurnOnText.text = getInfoForOnOff(value)

        binding.restartButton.isEnabled = value
        if (value) {
            alarmHelper?.setAlarmForNotes(originalList)
        } else {
            binding.exactSwitch.isChecked = false
            alarmHelper?.unsetAlarmForNotes(originalList)
        }
    }

    private fun cancelAndSetAllAlarms() {
        alarmHelper?.unsetAlarmForNotes(originalList)
        alarmHelper?.setAlarmForNotes(originalList)
        Toast.makeText(requireContext(), "Alarms restarted", Toast.LENGTH_SHORT).show()
    }

    private fun getInfoForOnOff(value: Boolean): String {
        if (value) {
            return "Alarms are turned on"
        }
        return "Alarms are turned off"
    }
}