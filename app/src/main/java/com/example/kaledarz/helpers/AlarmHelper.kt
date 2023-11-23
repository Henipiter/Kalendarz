package com.example.kaledarz.helpers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status

class AlarmHelper(private val context: Context) {

    private val notificationHelper = NotificationHelper(context)
    private var myPref = context.getSharedPreferences("run_alarms", MODE_PRIVATE)


    fun setAlarmForNotes(noteArray: ArrayList<Note>) {
        Note.computeStatusForNoteList(noteArray)
        for (note in noteArray) {
            if (note.status == Status.UNDONE || note.status == Status.FUTURE) {
                setAlarm(note)
            }
        }
    }

    fun setAlarm(note: Note) {
        val now = DateFormatHelper.getCurrentDateTime()
        val startAt = note.start_date + " " + note.start_time + ":00"
        val endAt = note.end_date + " " + note.end_time + ":00"

        val shouldPush = DateFormatHelper.isFirstDateGreaterThanSecond(startAt, now)
        val shouldDelete = DateFormatHelper.isFirstDateGreaterThanSecond(endAt, now)
        startAlarmToAddNotification(shouldPush, shouldDelete, note)
        startAlarmToDeleteNotification(shouldDelete, note)
    }

    fun unsetAlarmForNotes(noteArray: ArrayList<Note>) {
        for (note in noteArray) {
            if (note.status == Status.UNDONE) {
                unsetAlarm(note.id!!)
            }
        }
    }

    fun unsetAlarm(id: String) {
        cancelAlarm(id)
        notificationHelper.deleteNotification(id.toInt())
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun startAlarm(c: Calendar, note: Note, mode: String) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("id", note.id)
        intent.putExtra("content", note.content)
        intent.putExtra("title", getTitle(note))
        var id = note.id!!.toInt()
        if (mode == "UNSET") {
            id *= -1
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        if (myPref.getString(Constants.ALARM_EXACT, "false") == "false") {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(c.timeInMillis, pendingIntent),
                pendingIntent
            )
        }
    }

    private fun getTitle(note: Note): String {
        return "To " + note.end_time + " " + note.end_date
    }

    private fun cancelAlarm(id: String) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("mode", "UNSET")
        intent.putExtra("id", id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun startAlarmToAddNotification(
        shouldPush: Boolean,
        shouldDelete: Boolean,
        note: Note
    ) {
        if (shouldPush) {
            Log.e("Alarm", "Alarm to to push notification")
            startAlarm(
                DateFormatHelper.getCalendarFromStrings(
                    note.start_date, note.start_time
                ), note, "SET"
            )
        } else {
            if (shouldDelete) {
                notificationHelper.createNotification(note)
                Log.e("Alarm", "Notification pushed without alarm")
            } else {
                Log.e("Alarm", "Notification not pushed")
            }
        }
    }

    private fun startAlarmToDeleteNotification(shouldDelete: Boolean, note: Note) {
        if (shouldDelete) {
            startAlarm(
                DateFormatHelper.getCalendarFromStrings(note.end_date!!, note.end_time!!),
                note, "UNSET"
            )
            Log.e("Alarm", "Alarm to hide notification")
        } else {
            Log.e("Alarm", "No reaction")
        }
    }
}