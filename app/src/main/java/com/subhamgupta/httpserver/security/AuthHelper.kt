package com.subhamgupta.httpserver.security

import com.subhamgupta.httpserver.AUDIENCE
import com.subhamgupta.httpserver.ISSUER
import com.subhamgupta.httpserver.JWT_SECRET
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.hashing.SHA256HashingService
import com.subhamgupta.httpserver.hashing.SaltedHash

private val hashingService = SHA256HashingService()
private val tokenService = JwtTokenService()
private val tokenConfig = TokenConfig(
    issuer = ISSUER,
    audience = AUDIENCE,
    expiresIn = 365L * 1000L * 60L * 60L * 24L,
    secret = JWT_SECRET
)

fun generateToken(user: User, password: String): String {
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
                name = "username",
                value = user.username
            )
        )
    } else ""
}