package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.databinding.FragmentSettingsBinding
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper
import com.example.kaledarz.helpers.PickerHelper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class SettingsFragment : Fragment() {


    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()
    private var alarmHelper: AlarmHelper? = null
    private var myPref: SharedPreferences? = null
    private lateinit var pickerHelper: PickerHelper

    private var invalidRowsInfo = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myPref = requireContext().getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
        alarmHelper = AlarmHelper(requireContext())
        pickerHelper = PickerHelper(requireContext())
        databaseHelper = MyDatabaseHelper(requireContext())

        val alarmOnOffPref = myPref?.getString(Constants.ALARM_ON_OFF, "true") == "true"
        val alarmExactPref = myPref?.getString(Constants.ALARM_EXACT, "false") == "true"
        binding.turnOnOffSwitch.isChecked = alarmOnOffPref
        binding.exactSwitch.isChecked = alarmExactPref
        switchOnOffAlarmBehaviour(alarmOnOffPref)

        originalList.addAll(databaseHelper.readAllData())

        binding.exportButton.setOnClickListener {
            exportDatabase()
        }
        binding.importButton.setOnClickListener {
            importDatabase()
        }
        binding.clearButton.setOnClickListener {
            deleteAllRows()
        }
        binding.restartButton.setOnClickListener {
            cancelAndSetAllAlarms()
            Toast.makeText(requireContext(), "Alarms restarted", Toast.LENGTH_SHORT).show()
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
    }

    private fun getInfoForOnOff(value: Boolean): String {
        if (value) {
            return "Alarm active"
        }
        return "Alarm inactive"
    }

    private fun importDatabase() {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.test_alert, null)
        val title: TextView = view.findViewById(R.id.title)
        val content: EditText = view.findViewById(R.id.edit_text)
        val btDone: Button = view.findViewById(R.id.bt_done)
        title.text = "Import"
        content.setText("")

        btDone.setOnClickListener {
            val isValid = validateImportText(content.text.toString())
            if (isValid) {
                dialog?.dismiss()

                originalList.forEach {
                    databaseHelper.addGame(it)
                }


            } else {
                val dialogFailure: AlertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Importing failure")
                    .setMessage("Given rows are invalid: $invalidRowsInfo")
                    .setNegativeButton("OK", null)
                    .create()
                dialogFailure.show()
            }
        }

        builder.setView(view)

        dialog = builder.create()
        dialog.show()
    }

    private fun validateImportText(importText: String): Boolean {
        if (importText.isEmpty()) {
            return false
        }
        val mapper = ObjectMapper()
        val sb = StringBuilder()
        originalList = mapper.readValue(importText)
        for ((index, note) in originalList.withIndex()) {
            if (!validateNote(note)) {
                sb.append(index)
                sb.append(",")
            }
        }
        invalidRowsInfo = sb.toString()
        return invalidRowsInfo.isEmpty()
    }

    private fun validateNote(note: Note): Boolean {
        if (!DateFormatHelper.validate(note.start_date, "dd-MM-yyyy")) {
            return false
        }
        if (!DateFormatHelper.validate(note.end_date, "dd-MM-yyyy")) {
            return false
        }
        if (!DateFormatHelper.validate(note.start_time, "HH:mm")) {
            return false
        }
        if (!DateFormatHelper.validate(note.start_time, "HH:mm")) {
            return false
        }
        if (!Status.values().map { it.name }.contains(note.status.name)) {
            return false
        }
        return true
    }

    private fun exportDatabase() {
        val mapper = ObjectMapper()
        val serialized = mapper.writeValueAsString(originalList)
        if (serialized.isEmpty()) {
            val dialog: AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Exporting failure")
                .setMessage("Nothing to export")
                .setNegativeButton("OK", null)
                .create()
            dialog.show()
        } else {
            val dialog: AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Exporting success")
                .setMessage(serialized)
                .setNegativeButton("OK", null)
                .setNeutralButton("COPY") { _, _ ->
                    copyToClipboard(serialized.toString())
                    Toast.makeText(
                        requireContext(),
                        "Text copied to clipboard",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                .create()
            dialog.show()
        }
    }

    private fun copyToClipboard(text: String) {
        Toast.makeText(requireContext(), "Feature is not handled", Toast.LENGTH_SHORT).show()
        val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun deleteAllRows() {
        val dialog: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete all notes")
            .setMessage("Are you sure to delete all notes?")
            .setNegativeButton("CANCEL", null)
            .setNeutralButton("CLEAR") { dialog, which ->

                alarmHelper?.unsetAlarmForNotes(originalList)
                databaseHelper.deleteAllRows()
                Toast.makeText(
                    requireContext(),
                    "All notes has been deleted",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .create()
        dialog.show()
    }
}