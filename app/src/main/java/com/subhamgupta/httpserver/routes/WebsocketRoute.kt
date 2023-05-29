package com.subhamgupta.httpserver.routes

import android.util.Log
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.hashing.HashingService
import com.subhamgupta.httpserver.security.TokenConfig
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.utils.AuthRequest
import com.subhamgupta.httpserver.utils.ConfirmToAcceptLoginEvent
import com.subhamgupta.httpserver.utils.Request
import com.subhamgupta.httpserver.utils.UserSession
import com.subhamgupta.httpserver.utils.checkAuth
import com.subhamgupta.httpserver.utils.sendEvent
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


fun Route.websockets(
    tokenConfig: TokenConfig,
    dataSource: UserDataSource,
    hashingService: HashingService
) {

    webSocket("/session") {
        val query = call.request.queryParameters
        val token = query["token"] ?: ""
        if (token.isEmpty())
            close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    "token invalid"
                )
            )

        val user = checkAuth(token, dataSource) ?: return@webSocket
//        sendEvent(UserSession(this, user))
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    sendEvent(UserSession(this, user))
                }

                else -> {}
            }
        }
    }
    webSocket("/auth") {
        val clientIp = call.request.origin.remoteAddress
        sendEvent(Request("${call.request.headers["User-Agent"]}", "", clientIp))
        val query = call.request.queryParameters

        val session = this

        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val rawData = frame.readText()
                        if ((query["auth"] ?: "") == "1") {
                            Log.e("server", "$rawData")
//                            val decrypt = decryptData(rawData)
                            val request = Json.decodeFromString<AuthRequest>(rawData)
                            val user = dataSource.getUserByUsername(request.username)
                            if (user == null) {
                                call.respond(HttpStatusCode.Conflict, "Invalid credentials")
                                return@webSocket
                            }
                            val token = generateToken(user, request.password)
                            Log.e("server", "user $token")
                            if (token.isNotEmpty()) {
                                sendEvent(
                                    ConfirmToAcceptLoginEvent(
                                        session,
                                        user,
                                        tokenConfig,
                                        token
                                    )
                                )
                            } else {
                                close(
                                    CloseReason(
                                        CloseReason.Codes.TRY_AGAIN_LATER,
                                        "invalid_password"
                                    )
                                )
                            }

                        }
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            Log.e("server error", "$e")
        }
    }

}