package com.subhamgupta.httpserver.routes

import android.os.Environment
import android.util.Log
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.security.EncryptDecrypt
import com.subhamgupta.httpserver.security.TokenConfig
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.utils.AuthResponse
import com.subhamgupta.httpserver.utils.AuthStatus
import com.subhamgupta.httpserver.utils.Streaming
import com.subhamgupta.httpserver.utils.checkAuth
import com.subhamgupta.httpserver.utils.compressImage
import com.subhamgupta.httpserver.utils.sendEvent
import com.subhamgupta.httpserver.utils.toThumbBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder
import java.security.PublicKey


fun Route.public(
    tokenConfig: TokenConfig,
    dataSource: UserDataSource
) {
    post("/enc") {
        try {
            val encryptedData = call.receive<Map<String, String>>()

            call.respondText("Data received and decrypted")
        } catch (e: Exception) {
            Log.e("server erro", "$e")
        }

    }

    get("/gk") {
        val keys = EncryptDecrypt.generateKeys()
        val pubKey = keys["PUBLIC"] as PublicKey
        val strKey = pubKey.toString()
        Log.e("key", "strKey")
        call.respond(HttpStatusCode.Created, strKey)
    }
    get("/refresh") {
        try {
            val user = checkAuth(call, dataSource)
            if (user != null) {
                val token = generateToken(user, user.password)
                if (token.isNotEmpty()) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = AuthResponse(
                            status = AuthStatus.AUTHENTICATED,
                            token = token,
                            username = user.username,
                            email = user.email,
                            expiresIn = tokenConfig.expiresIn,
                            hasAccessTo = "",
                            userType = user.type
                        )
                    )
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
            }
        } catch (e: Exception) {
            Log.e("server error", "$e")
            call.respond(HttpStatusCode.BadRequest, "Something went wrong")
        }

    }
    get("/thumb") {
        try {
            val q = call.request.queryParameters
            val uri = q["uri"] ?: ""
            val fs = q["fs"] ?: ""
            val file = File(uri)
            if (uri.contains(".mp4")) {
                val w = 200
                val h = 200
//                call.response.header(HttpHeaders.CacheControl, "max-age=3600")
                call.response.header(HttpHeaders.ContentType, ContentType.Video.Any.toString())
                call.respondBytes(file.toThumbBytes(MyApp.instance, w, h, true))
            } else {
                if (fs == "f") {
                    call.respondFile(file)
                    return@get
                }
                val outputStream = compressImage(file, 200, 200, 50)
                // Write the compressed image to the response
                call.respondBytes(outputStream.toByteArray())
                withContext(Dispatchers.IO) {
                    outputStream.close()
                }

            }
        } catch (e: Exception) {
            Log.e("server error", "$e")
        }

    }
    get("/stream") {
        val clientIp = call.request.origin.remoteHost
        val q = call.request.queryParameters


        val id = q["id"] ?: ""
        val uri = q["uri"] ?: ""

        try {

            var path = File(
                Environment.getExternalStorageDirectory(),
                Environment.DIRECTORY_DOWNLOADS + "/$id"
            ).toString()
            if (uri.isNotEmpty()) {
                path = uri
            }

            val file = File(path)
            val fileName = URLEncoder.encode(file.name, "UTF-8")
            sendEvent(Streaming(fileName = file.name, "Streaming started"))
            call.response.header(
                "Content-Disposition",
                "inline;filename=\"${fileName}\";filename*=utf-8''\"${fileName}\""
            )
            call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respondFile(file)

        } catch (ex: Exception) {
            ex.printStackTrace()
            call.respondText(
                "File is expired or does not exist. $ex",
                status = HttpStatusCode.Forbidden
            )
        }
    }

}