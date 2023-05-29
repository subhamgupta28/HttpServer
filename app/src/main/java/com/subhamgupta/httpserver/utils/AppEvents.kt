package com.subhamgupta.httpserver.utils

import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.security.TokenConfig
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class ConfirmToAcceptLoginEvent(
    val session: DefaultWebSocketServerSession,
    val user: User,
    val tokenConfig: TokenConfig,
    val token: String
)
class Transfer(
    val status: String,
    val fileList: List<FileObj>,
)
class GetFilesList(
    val filesList: MutableStateFlow<List<File>>
)
class Streaming(
    val fileName: String,
    val message: String
)
class Request(
    val from: String,
    val isFor: String,
    val clientIp: String
)
class UserSession(
    val session: DefaultWebSocketServerSession,
    val user: User
)