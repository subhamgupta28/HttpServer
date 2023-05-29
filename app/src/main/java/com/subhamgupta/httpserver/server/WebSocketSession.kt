package com.subhamgupta.httpserver.server

import io.ktor.server.websocket.*

data class WebSocketSession(val id: Long, val clientId: String, val session: DefaultWebSocketServerSession)