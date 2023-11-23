package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.DateFormatHelper
import com.example.kaledarz.helpers.MyDatabaseHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonExport: Button
    private lateinit var buttonImport: Button
    private lateinit var buttonClear: Button
    private lateinit var buttonAlarm: Button
    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()

    private var invalidRowsInfo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        buttonExport = findViewById(R.id.export_button)
        buttonImport = findViewById(R.id.import_button)
        buttonClear = findViewById(R.id.clear_button)
        buttonAlarm = findViewById(R.id.sleep_alarms)

        databaseHelper = MyDatabaseHelper(this)
        originalList.addAll(databaseHelper.readAllData())

        buttonExport.setOnClickListener {
            exportDatabase()
        }
        buttonImport.setOnClickListener {
            importDatabase()
        }
        buttonClear.setOnClickListener {
            deleteAllRows()
        }
        buttonAlarm.setOnClickListener {
            val intent = Intent(this, AlarmSettingActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun importDatabase() {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(this)
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
            } else {
                val dialogFailure: AlertDialog = AlertDialog.Builder(this)
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
        val rows = importText.split("``\n``")

        val sb = StringBuilder()
        for (i in rows.indices) {
            if (rows[i] != "" && !validateRow(rows[i])) {
                sb.append(i)
                sb.append(",")
            }
        }
        invalidRowsInfo = sb.substring(0, sb.length - 1).toString()
        return invalidRowsInfo.isEmpty()

    }

    private fun validateRow(row: String): Boolean {
        val fields = row.split("`")
        if (fields.size != 7) {
            return false
        }
        if (!DateFormatHelper.validate(fields[0], "dd-MM-yyyy")) {
            return false
        }
        if (!DateFormatHelper.validate(fields[1], "dd-MM-yyyy")) {
            return false
        }
        if (!DateFormatHelper.validate(fields[2], "HH:mm")) {
            return false
        }
        if (!DateFormatHelper.validate(fields[3], "HH:mm")) {
            return false
        }
        if (!DateFormatHelper.validate(fields[3], "HH:mm")) {
            return false
        }
        if (fields[4] == "false" || fields[4] == "true") {
            return false
        }
        if (fields[5] == Status.UNDONE.name || fields[5] == Status.DONE.name ||
            fields[5] == Status.PAST.name || fields[5] == Status.FUTURE.name
        ) {
            return false
        }
        return true
    }


    private fun exportDatabase() {
//        val mapper = ObjectMapper()
//        val serialized = mapper.writeValueAsString(originalList)

        val serialized = "dwa"
//        val sb = StringBuilder()
//
//        for (note in originalList) {
//            sb.append(note.export())
//        }
//        val finalString = sb.toString().replace(" ", "_")

        if (serialized.isEmpty()) {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Exporting failure")
                .setMessage("Nothing to export")
                .setNegativeButton("OK", null)
                .create()
            dialog.show()
        } else {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Exporting success")
                .setMessage(serialized)
                .setNegativeButton("OK", null)
                .setNeutralButton("COPY") { dialog, which ->
                    copyToClipboard(serialized.toString())
                    Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .create()
            dialog.show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun deleteAllRows() {
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Delete all notes")
            .setMessage("Are you sure to delete all notes?")
            .setNegativeButton("CANCEL", null)
            .setNeutralButton("CLEAR") { dialog, which ->
                val alarmHelper = AlarmHelper(applicationContext)
                alarmHelper.unsetAlarmForNotes(originalList)
                databaseHelper.deleteAllRows()
                Toast.makeText(this, "All notes has been deleted", Toast.LENGTH_SHORT).show()

                finish()
                val homepage = Intent(this, MainActivity::class.java)
                startActivity(homepage)
            }
            .create()
        dialog.show()
    }
}