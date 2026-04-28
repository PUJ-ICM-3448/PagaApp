package com.example.pagaapp.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    var isRead: Boolean = false
)

object NotificationHelper {
    private const val CHANNEL_ID = "payments_channel"
    
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun showNotification(context: Context, title: String, message: String) {
        // 1. Show System Notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = System.currentTimeMillis().toInt()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)

        // 2. Save in App History
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timestamp = sdf.format(Date())
        
        val newAppNotification = AppNotification(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp
        )
        
        _notifications.value = listOf(newAppNotification) + _notifications.value
    }

    fun markAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}
