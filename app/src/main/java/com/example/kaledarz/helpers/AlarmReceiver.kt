package com.example.kaledarz.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    private var mode = "SET"
    private var id = "1"
    private var title = "title"
    private var subtitle = "subtitle"
    private var content = "content"

    private lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        prepareNotificationHelper(context)
        receiveExtraIntent(intent)
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            val alarmHelper = AlarmHelper(context)
            alarmHelper.setAlarmForAllNotes()
            return
        }

        if (mode == "SET") {
            notificationHelper.createNotification(id.toInt(), title, subtitle, content)
        } else {
            notificationHelper.deleteNotification(id.toInt())
        }
    }

    private fun receiveExtraIntent(intent: Intent) {
        if (intent.hasExtra("mode")) {
            mode = intent.getStringExtra("mode").toString()
        }
        if (intent.hasExtra("id")) {
            id = intent.getStringExtra("id").toString()
        }
        if (intent.hasExtra("title")) {
            title = intent.getStringExtra("title").toString()
        }
        if (intent.hasExtra("subtitle")) {
            title = intent.getStringExtra("subtitle").toString()
        }
        if (intent.hasExtra("content")) {
            content = intent.getStringExtra("content").toString()
        }
    }

    private fun prepareNotificationHelper(context: Context) {
        notificationHelper = NotificationHelper(context)
        notificationHelper.createNotificationChannel()
    }

}