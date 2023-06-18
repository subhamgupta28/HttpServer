package com.subhamgupta.httpserver.security

import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.hashing.SHA256HashingService
import com.subhamgupta.httpserver.hashing.SaltedHash

private val hashingService = SHA256HashingService()
private val tokenService = JwtTokenService()

fun generateToken(user: User, password: String, tokenConfig :TokenConfig): String {
    val valid = hashingService.verify(
        value = password,
        saltedHash = SaltedHash(
            hash = user.password,
            salt = user.salt
        )
    )
    return if (valid) {
        tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "uuid",
                value = user.uuid
            )
        )
    } else ""
}