package com.example.kaledarz

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity

class AlarmHelper(private val context: Context) {

    fun startAlarm(c: Calendar, note: Note, mode: String) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("id", note.id)
        intent.putExtra("content", note.content)
        var id = note.id!!.toInt()
        if (mode == "UNSET") {
            intent.putExtra("title", note.end_date + " " + note.end_time)
            id *= -1
        }
        else{
            intent.putExtra("title", note.start_date + " " + note.start_time)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }

    fun cancelAlarm(id:String) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("mode", "UNSET")
        intent.putExtra("id", id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}