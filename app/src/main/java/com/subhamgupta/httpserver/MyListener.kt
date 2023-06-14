package com.subhamgupta.httpserver

interface MyListener {
    fun onMessageReceived(message: Map<String, CharSequence?>)
}