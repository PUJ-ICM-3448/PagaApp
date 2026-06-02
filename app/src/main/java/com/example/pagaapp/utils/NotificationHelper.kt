package com.example.pagaapp.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    var isRead: Boolean = false
)

object NotificationHelper {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun addNotification(title: String, message: String) {
        val newNotification = AppNotification(title = title, message = message)
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun markAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
