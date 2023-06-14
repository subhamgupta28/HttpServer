package com.subhamgupta.httpserver.routes

import android.util.Log
import com.google.gson.Gson
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.hashing.HashingService
import com.subhamgupta.httpserver.security.TokenConfig
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.utils.AuthRequest
import com.subhamgupta.httpserver.utils.Request
import com.subhamgupta.httpserver.utils.SettingStorage
import com.subhamgupta.httpserver.utils.decryptPassword
import com.subhamgupta.httpserver.utils.sendEvent
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set

fun Route.authentication(
    settingStorage: SettingStorage,
    tokenConfig: TokenConfig,
    dataSource: UserDataSource,
    hashingService: HashingService,
) {
    get("/login") {
        val params = call.request.queryParameters
        val session = call.sessions.get<HttpSession>()
        Log.e("r session", "$session")
        val user = dataSource.getCurrentUser()
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Invalid Credentials")
            return@get
        }

        if (session == null) {
            val token = generateToken(user, decryptPassword(params["password"] ?: "", 10))
            Log.e("redirect", "$token $user")
            call.sessions.set(HttpSession(user.uuid, token))
        } else {
            call.respond(status = HttpStatusCode.OK, Gson().toJson(mapOf("auth" to true)))
        }
        call.respond(HttpStatusCode.OK)
        redirect("/")
    }
    post("/logout") {
        call.respond(message = "Hello")
    }
    post("/signup") {

        val request = call.receive<AuthRequest>()

        Log.e("server", "$request")


        val saltedHash = hashingService.generateSaltedHash(request.password)


        try {

        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
            Log.e("server error", "$e")
        }

        sendEvent(Request("${call.request.headers["Authorization"]}", "signup", ""))
    }


}