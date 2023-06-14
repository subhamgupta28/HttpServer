package com.subhamgupta.httpserver.routes

import android.os.Environment
import android.util.Log
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.security.EncryptDecrypt
import com.subhamgupta.httpserver.security.TokenConfig
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.utils.Streaming
import com.subhamgupta.httpserver.utils.compressImage
import com.subhamgupta.httpserver.utils.sendEvent
import com.subhamgupta.httpserver.utils.toThumbBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
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
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
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
    get("/file") {
        try {
            val params = call.request.queryParameters
            val uri = params["uri"] ?: ""
            val file = File(uri)
            if (file.exists()) {
                call.response.header(
                    "Content-Disposition",
                    "inline;filename=\"${file.name}\";filename*=utf-8''\"${file.name}\""
                )
                call.respondFile(file)
            } else {
                call.respondText(
                    "File not found",
                    status = HttpStatusCode.NotFound
                )
            }
        } catch (e: Exception) {
            Log.e("server error", "$e")
            call.respondText(
                "Something went wrong $e",
                status = HttpStatusCode.Forbidden
            )
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