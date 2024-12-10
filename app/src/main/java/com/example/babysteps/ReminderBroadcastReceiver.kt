package com.example.babysteps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationTitle = intent.getStringExtra("title") ?: "Reminder"
        val notificationMessage = intent.getStringExtra("message") ?: "You have a reminder today!"

        // Log the intent data for debugging
        Log.d("ReminderReceiver", "Notification Title: $notificationTitle, Message: $notificationMessage")

        // Open the Homepage activity and programmatically select the Calendar tab
        val calendarIntent = Intent(context, Homepage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_calendar_tab", true) // Pass extra to identify Calendar tab
        }

        context.startActivity(calendarIntent)
    }
}
