package com.subhamgupta.httpserver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationObj(
    val msg: String = "",
    val notify: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val files: List<String> = mutableListOf(),
    val notifyObj:  Map<String, CharSequence?> = mapOf()
)
