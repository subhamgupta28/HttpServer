package com.subhamgupta.httpserver.routes

import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.CurrentSession
import io.ktor.server.sessions.get
import io.ktor.util.hex
import kotlin.reflect.KClass

fun redirect(path: String, permanent: Boolean = false): Nothing =
    throw RedirectException(path, permanent)

data class HttpSession(val user: String, val token: String)

val secretHashKey = hex("6819b57a326945c1968f45236581")

class RedirectException(val path: String, val permanent: Boolean) : Exception()
class SessionNotFoundException(val clazz: KClass<*>) : Exception()


inline fun <reified T> CurrentSession.getOrThrow(): T =
    this.get() ?: throw SessionNotFoundException(T::class)

inline fun <reified T> StatusPagesConfig.registerSessionNotFoundRedirect(path: String) {
    exception<SessionNotFoundException> { call, cause ->
        if (cause.clazz == T::class) call.respondRedirect(path)
    }
}