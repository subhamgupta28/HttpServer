package com.subhamgupta.httpserver.server

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.NOTIFICATION_ID


object NotificationHelper {
    fun createContentIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context, 0, context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun ensureDefaultChannel() {
        val notificationManager = MyApp.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(NOTIFICATION_ID) == null) {
            notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_ID, "HTTP_SERVER", NotificationManager.IMPORTANCE_DEFAULT).apply {
                setShowBadge(false)
            })
        }
    }
}