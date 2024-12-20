package com.example.clearsky.notifications.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.R
import com.example.clearsky.model.NotificationCard

class NotificationAdapter(private val notifications: MutableList<NotificationCard>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text_notification_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.textView.text = "From: ${notification.startDate} ${notification.startTime}\n" +
                "To: ${notification.endDate} ${notification.endTime}\n" }

    override fun getItemCount(): Int {
        return notifications.size
    }

    fun addNotificationCard(notificationCard: NotificationCard) {
        notifications.add(notificationCard)
        notifyItemInserted(notifications.size - 1)
    }

}
