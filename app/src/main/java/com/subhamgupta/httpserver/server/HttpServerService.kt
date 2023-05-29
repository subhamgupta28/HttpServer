package com.subhamgupta.httpserver.server

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.NOTIFICATION_ID
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.mainapp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HttpServerService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        Log.e("server", "starting")
        instance = this
        NotificationHelper.ensureDefaultChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (MyApp.instance.httpServer == null) {
                    MyApp.instance.httpServer = HttpServerManager.createHttpServer(MyApp.instance)
                    MyApp.instance.httpServer?.start(wait = true)
                    Log.e("server", "started")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("error HttpServerService", "$ex")
            }
        }
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = "com.subhamgupta.httpserver.stop_service"
            }, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_ID).apply {
            setSmallIcon(R.drawable.baseline_local_laundry_service)
            setContentTitle("Server running")
            setContentText("")
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setOnlyAlertOnce(true)
            setSilent(true)
            setWhen(System.currentTimeMillis())
            setAutoCancel(false)
            setContentIntent(NotificationHelper.createContentIntent(this@HttpServerService))
            addAction(-1, "Stop Service", stopPendingIntent)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }.build()
    }

    fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MyApp.instance.httpServer?.stop(1000, 5000)
                MyApp.instance.httpServer = null
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    companion object {
        var instance: HttpServerService? = null
    }
}