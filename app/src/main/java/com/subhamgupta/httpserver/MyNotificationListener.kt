package com.subhamgupta.httpserver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MyNotificationListener : NotificationListenerService() {


    override fun onCreate() {
        super.onCreate()
        // Do any initialization here
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            val title = it.notification.extras.getString("android.title")
            val text = it.notification.extras.getCharSequence("android.text")
            val map = mapOf("package" to packageName, "title" to title, "text" to text)
            listener?.onMessageReceived(map)
            Log.d(TAG, "New notification posted: $packageName, Title: $title, Text: $text")

            // Process the notification as needed
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val packageName = it.packageName
            val title = it.notification.extras.getString("android.title")
            val text = it.notification.extras.getCharSequence("android.text")

            Log.d(TAG, "Notification removed: $packageName, Title: $title, Text: $text")

            // Process the notification removal as needed
        }
    }


    companion object {
        private const val TAG = "MyNotificationListener"
        var listener: MyListener? = null
        fun isNotificationListenerEnabled(context: Context): Boolean {
            val componentName = ComponentName(context, MyNotificationListener::class.java)
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat != null && flat.contains(componentName.flattenToString())
        }

        fun requestNotificationListenerPermission(context: Context) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            context.startActivity(intent)
        }
    }
}