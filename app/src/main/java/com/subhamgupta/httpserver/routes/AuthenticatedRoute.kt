package com.subhamgupta.httpserver.routes

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.UserObj
import com.subhamgupta.httpserver.domain.objects.getFolders
import com.subhamgupta.httpserver.domain.objects.getImages
import com.subhamgupta.httpserver.domain.objects.getVideos
import com.subhamgupta.httpserver.utils.checkAuth
import com.subhamgupta.httpserver.utils.getDeviceInfo
import com.subhamgupta.httpserver.utils.getFolderMap
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.io.File


fun Route.mainApp(
    dataSource: UserDataSource
) {
    authenticate {
        get("/info") {
            val info = getDeviceInfo()
            call.respond(HttpStatusCode.OK, Gson().toJson(info))
        }

        get("/user") {
            val user = checkAuth(call, dataSource) ?: return@get call.respondText(
                status = HttpStatusCode.Unauthorized,
                text = "No logged in user found"
            )

            val userObj = UserObj(


            )
            call.respond(HttpStatusCode.OK, userObj)
        }

        get("/all/{path}") {
            val path = call.parameters["path"]?.replace(".", "/") ?: ""
            val user = checkAuth(call, dataSource) ?: return@get call.respondText(
                status = HttpStatusCode.Unauthorized,
                text = "No logged in user found"
            )
            if (path.isNotEmpty()) {

                val root = File(Environment.getExternalStorageDirectory(), path).toString()
                val files = getFolderMap(root)
                val sp = path.split("/")
                var rootFolder = path
                if (sp.size != 1) {
                    rootFolder = sp[sp.size - 1]
                }
                val result = mapOf(path to files, "root" to rootFolder)
                call.respond(HttpStatusCode.OK, Gson().toJson(result))
            }
        }
        get("/folders") {
            try {
                val user = checkAuth(call, dataSource) ?: return@get call.respondText(
                    status = HttpStatusCode.Unauthorized,
                    text = "No logged in user found"
                )
                val folders = getFolders()

                call.respond(HttpStatusCode.OK, folders)

//                call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
            } catch (e: Exception) {
                Log.e("server error", "$e")
                call.respondText(
                    "Something wen wrong $e",
                    status = HttpStatusCode.BadRequest
                )
            }
        }
        get("/allFiles/{id}") {
            try {
                val id = call.parameters["id"]?.replace(".", "/") ?: ""
                Log.e("server", "$id")
                val user = checkAuth(call, dataSource) ?: return@get call.respondText(
                    status = HttpStatusCode.Unauthorized,
                    text = "No logged in user found"
                )

                val videos = getVideos().filter {
                    it.uri.contains("/$id/")
                }
                val images = getImages().filter {
                    it.uri.contains("/$id/")
                }
                val list = videos + images
                call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                Log.e("server error", "$e")
                call.respondText(
                    "Something went wrong $e",
                    status = HttpStatusCode.Forbidden
                )
            }
        }

        post("/upload") {
            val user = checkAuth(call, dataSource) ?: return@post call.respondText(
                status = HttpStatusCode.Unauthorized,
                text = "You are not Authorized"
            )
            try {
                val multipart = call.receiveMultipart()
                val listOfFiles = mutableListOf<FileObj>()
//                sendEvent(Transfer("START", listOfFiles))
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val name = part.originalFileName!!
                        val file = File(
                            Environment.getExternalStorageDirectory(),
                            Environment.DIRECTORY_DOWNLOADS + "/$name"
                        )
                        listOfFiles.add(
                            FileObj(
                                file.name,
                                file.length(),
                                file.extension,
                                file.absolutePath
                            )
                        )
                        val receivedFile = ReceivedFile()
                        receivedFile.fileName = file.name
                        receivedFile.uri = file.absolutePath
                        dataSource.saveReceivedFile(receivedFile)
                        part.streamProvider().use { its ->
                            file.outputStream().buffered().use {
                                its.copyTo(it)
                                Log.e("server file", "$it")
                            }
                        }
                    }
                    part.dispose()
                }
                val map = mapOf(
                    "success" to true,
                    "msg" to "file uploaded successfully",
                    "location" to "Download",
                    "files" to listOfFiles
                )
                call.respond(HttpStatusCode.Created, Gson().toJson(map))
//                sendEvent(Transfer("START", listOfFiles))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        get("/download") {
            val user = checkAuth(call, dataSource) ?: return@get call.respondText(
                status = HttpStatusCode.Unauthorized,
                text = "No logged in user found"
            )
            val query = call.request.queryParameters
            val uri = query["uri"] ?: ""
            Log.e("server file", uri)
            if (uri.isEmpty())
                return@get
            if (uri.startsWith("content:/")) {
                val bytes =
                    MyApp.instance.contentResolver.openInputStream(Uri.parse(uri))?.buffered()
                        ?.use { it.readBytes() }
                call.respondBytes(bytes!!)
            } else {
                val file = File(uri)
                Log.e("server", "$file")
                if (file.exists()) {
                    call.response.header(
                        "Content-Disposition",
                        "attachment; filename=\"${file.name}\""
                    )
                    call.respondFile(file)
                }
            }
        }

    }
}

