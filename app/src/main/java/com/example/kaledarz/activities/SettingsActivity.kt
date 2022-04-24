package com.example.kaledarz.activities

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.R
import com.example.kaledarz.helpers.AlarmHelper
import com.example.kaledarz.helpers.MyDatabaseHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var buttonRestart: Button
    private lateinit var buttonExport: Button
    private lateinit var buttonClear: Button
    private lateinit var databaseHelper: MyDatabaseHelper
    private var originalList = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        buttonRestart = findViewById(R.id.restart)
        buttonExport = findViewById(R.id.export)
        buttonClear = findViewById(R.id.clear)


        databaseHelper = MyDatabaseHelper(this)
        originalList.addAll(databaseHelper.readAllData())

        buttonRestart.setOnClickListener {
            cancelAndSetAllAlarms()
        }
        buttonExport.setOnClickListener {
            exportDatabase()
        }
        buttonClear.setOnClickListener {
            deleteAllRows()
        }
    }


    private fun cancelAndSetAllAlarms() {
        val alarmHelper = AlarmHelper(applicationContext)
        alarmHelper.unsetAlarmForNotes(originalList)
        alarmHelper.setAlarmForNotes(originalList)
        Toast.makeText(this, "Alarms restarted", Toast.LENGTH_SHORT).show()
    }

    private fun exportDatabase() {
        val sb = StringBuilder()

        for (note in originalList) {
            sb.append(note.export())
        }

        if(sb.isEmpty()){
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Nothing to export")
                .setMessage(sb)
                .setNegativeButton("OK", null)
                .create()
            dialog.show()
        }
        else {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Exported")
                .setMessage(sb)
                .setNegativeButton("OK", null)
                .setNeutralButton("COPY") { dialog, which ->
                    copyToClipboard(sb.toString())
                    Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .create()
            dialog.show()
        }
    }

    private fun copyToClipboard(text:String){
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun deleteAllRows(){
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