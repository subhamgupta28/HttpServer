package com.subhamgupta.httpserver.utils

class ChannelEvent<T>(val event: T) {
    override fun toString(): String {
        return "event = $event"
    }
}