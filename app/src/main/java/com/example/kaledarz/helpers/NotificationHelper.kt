package com.example.kaledarz.helpers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.R
import com.example.kaledarz.activities.ShowElemActivity

class NotificationHelper(base: Context) : ContextWrapper(base) {
    private val context = base
    private val CHANNEL_ID = "channelID"

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(note: Note) {
        createNotification(
            note.id!!.toInt(),
            "To " + note.end_time + " " + note.end_date,
            note.content!!
        )
    }

    fun createNotification(id: Int, title: String, content: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)

            .setSmallIcon(R.drawable.icon3)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(getPendingIntentToNote(id))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(this)) {
                notify(id, builder.build())
            }
        }
    }


    private fun getPendingIntentToNote(id: Int): PendingIntent {
        val intentToNote = Intent(this, ShowElemActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intentToNote.putExtra("type", "EDIT")
        intentToNote.putExtra("id", id.toString())
        return PendingIntent.getActivity(
            this,
            id,
            intentToNote,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun deleteNotification(id: Int) {
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.cancel(id)
    }

}