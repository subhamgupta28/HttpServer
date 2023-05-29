package com.subhamgupta.httpserver.domain.model



import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse(
    var success: Boolean = true,
    var data: String = "No data",
    var length: Int = 0
)
