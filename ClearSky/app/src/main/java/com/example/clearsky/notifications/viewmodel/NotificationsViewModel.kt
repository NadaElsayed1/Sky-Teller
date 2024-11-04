package com.example.clearsky.notifications.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.clearsky.model.NotificationCard

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableLiveData<MutableList<NotificationCard>>(mutableListOf())
    val notifications: LiveData<MutableList<NotificationCard>> get() = _notifications

    fun addNotification(notification: NotificationCard) {
        _notifications.value?.add(notification)
        _notifications.value = _notifications.value
    }
}
class NotificationsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}