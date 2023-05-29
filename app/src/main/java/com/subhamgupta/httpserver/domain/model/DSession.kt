package com.subhamgupta.httpserver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DSession(
    var clientId: String = "",
    var clientIP: String = "",
    var osName: String = "",
    var osVersion: String = "",
    var browserName: String = "",
    var browserVersion: String = "",
    var token: String = "",
    var updatedAt: Long = 0
)
