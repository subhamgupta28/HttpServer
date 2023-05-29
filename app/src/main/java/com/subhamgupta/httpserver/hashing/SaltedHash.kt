package com.subhamgupta.httpserver.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)