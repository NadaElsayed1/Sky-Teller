package com.example.clearsky.notifications.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.clearsky.R
import com.example.clearsky.notifications.reciever.AlarmReceiver

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        Log.d("AlertActivity", "AlertDialog is opened")

        window.setBackgroundDrawableResource(android.R.color.transparent)

        val message = intent.getStringExtra("message") ?: "No message provided"
        val messageTextView = findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message

        val dismissButton = findViewById<Button>(R.id.dismissButton)
        dismissButton.setOnClickListener {
            AlarmReceiver.stopAlarm()
            finish()
        }

        val layoutParams = window.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams
    }
}