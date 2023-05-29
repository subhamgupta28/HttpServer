package com.subhamgupta.httpserver.routes

import android.util.Log
import com.subhamgupta.httpserver.data.repository.MainRepository
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.hashing.HashingService
import com.subhamgupta.httpserver.hashing.SaltedHash
import com.subhamgupta.httpserver.security.TokenClaim
import com.subhamgupta.httpserver.security.TokenConfig
import com.subhamgupta.httpserver.security.TokenService
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.utils.AuthRequest
import com.subhamgupta.httpserver.utils.AuthResponse
import com.subhamgupta.httpserver.utils.AuthStatus
import com.subhamgupta.httpserver.utils.Request
import com.subhamgupta.httpserver.utils.sendEvent
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authentication(
    tokenConfig: TokenConfig,
    dataSource: UserDataSource,
    hashingService: HashingService,
) {
    post("/signin") {
        val request = call.receive<AuthRequest>()
        val user = dataSource.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid credentials")
            return@post
        }
        val token = generateToken(user, request.password)

        Log.e("server", "user $token")
        if (token.isNotEmpty()) {
            dataSource.updateUserStatus(user._id, "Logged In")
            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponse(
                    status = AuthStatus.AUTHENTICATED,
                    token = token,
                    username = user.username,
                    email = user.email,
                    expiresIn = tokenConfig.expiresIn,
                    hasAccessTo = "A,B,C",
                    userType = user.type
                )
            )
        } else {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = "User in invalid"
            )
        }
    }
    post("/logout") {
        val request = call.receive<AuthRequest>()
        val user = dataSource.getUserByUsername(request.username)
        if (user == null && request.token.isEmpty()) {
            call.respond(HttpStatusCode.Conflict, "Invalid token or user doesn't exist")
            return@post
        }
        val tokenInvalidate = InvalidateToken()
        tokenInvalidate.token = request.token
        tokenInvalidate.username = request.username

        user?.let {
            dataSource.invalidateToken(tokenInvalidate)
            dataSource.updateUserStatus(user._id, "Logged Out")
            call.respond(HttpStatusCode.Accepted, "User logged out")
        }
        sendEvent(Request("${call.request.headers["Authorization"]}", "logout", ""))
    }
    post("/signup") {

        val request = call.receive<AuthRequest>()

        Log.e("server", "$request")

        if (request.username.isEmpty() || request.password.isEmpty() || request.email.isEmpty()) {
            call.respond(HttpStatusCode.Conflict, "Missing credentials")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User()
        user.username = request.username
        user.salt = saltedHash.salt
        user.password = saltedHash.hash
        user.email = request.email
        user.status = "Registered"

        try {
            val res = dataSource.insertUser(user)
            if (res)
                call.respond(HttpStatusCode.Created)
            else
                call.respond(HttpStatusCode.Conflict, "username already present")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
            Log.e("server error", "$e")
        }

        sendEvent(Request("${call.request.headers["Authorization"]}", "signup", ""))
    }


}