package com.subhamgupta.httpserver.utils

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val password: String,
    val browserName: String = "",
    val browserVersion: String = "",
    val osName: String = "",
    val osVersion: String = "",
    val token: String = "",
    val isMobile: Boolean = false
)

@Serializable
data class AuthResponse(
    val status: AuthStatus,
    val token: String = "",
    val uuid: String,
    val expiresIn: Long,
    val userType: String,
)

enum class AuthStatus {
    PENDING,
    AUTHENTICATED,
    Failed
}