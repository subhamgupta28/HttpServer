package com.subhamgupta.httpserver.utils

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val username: String,
    val password: String,
    val email: String,
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
    val username: String,
    val email: String,
    val expiresIn: Long,
    val userType: String,
    val hasAccessTo: String
)

enum class AuthStatus {
    PENDING,
    AUTHENTICATED,
    Failed
}