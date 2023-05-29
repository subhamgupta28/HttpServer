package com.subhamgupta.httpserver.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.subhamgupta.httpserver.server.HttpServerService


class MyBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.action == "com.subhamgupta.httpserver.stop_service") {
                HttpServerService.instance?.stop()
                HttpServerService.instance = null
            }
        }
    }
}