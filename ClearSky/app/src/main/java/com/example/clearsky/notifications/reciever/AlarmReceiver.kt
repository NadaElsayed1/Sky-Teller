package com.example.clearsky.notifications.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.clearsky.notifications.view.AlertActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered")
        val alarmIntent = Intent(context, AlertActivity::class.java)
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        alarmIntent.putExtra("message", "Alert! It's time")
        context.startActivity(alarmIntent)
    }
}