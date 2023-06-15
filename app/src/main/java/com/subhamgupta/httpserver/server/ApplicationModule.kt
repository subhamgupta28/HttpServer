package com.subhamgupta.httpserver.server

import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.subhamgupta.httpserver.AUDIENCE
import com.subhamgupta.httpserver.ISSUER
import com.subhamgupta.httpserver.JWT_SECRET
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.db.MongoUserDataSource
import com.subhamgupta.httpserver.hashing.SHA256HashingService
import com.subhamgupta.httpserver.routes.HttpSession
import com.subhamgupta.httpserver.routes.RedirectException
import com.subhamgupta.httpserver.routes.authentication
import com.subhamgupta.httpserver.routes.mainApp
import com.subhamgupta.httpserver.routes.public
import com.subhamgupta.httpserver.routes.registerSessionNotFoundRedirect
import com.subhamgupta.httpserver.routes.secretHashKey
import com.subhamgupta.httpserver.routes.websockets
import com.subhamgupta.httpserver.security.TokenConfig
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.http.content.react
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


fun Application.module() {
    val settingStorage = MyApp.instance.settingStorage

    val dataSource = MongoUserDataSource
    environment.monitor.subscribe(ApplicationStarted) { application ->
        application.environment.log.info("Server is started")
    }
    install(CachingHeaders) {
        options { _, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS, ContentType.Application.JavaScript -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = 3600 * 24 * 30)
                )

                else -> null
            }
        }
    }
//    install(ShutDownUrl.ApplicationCallPlugin) {
//        shutDownUrl = "/shutdown"
//        exitCodeSupplier = { 0 }
//    }




    install(CORS) {
//        anyHost()
        allowHost("localhost:3000")
        allowCredentials = true
        allowHeader("Authorization")
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
    }
    install(StatusPages){
        exception<RedirectException> { call, cause ->
            call.respondRedirect(cause.path, cause.permanent)
        }
        registerSessionNotFoundRedirect<HttpSession>("/login")
        exception<Throwable> { call, cause ->
            Log.e("SERVER ERROR", "$cause")
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    install(Sessions) {
        cookie<HttpSession>("session_id") {
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey))
        }
    }


    install(ConditionalHeaders)
    install(WebSockets)
    install(Compression)
    install(ForwardedHeaders)
    install(PartialContent)
    install(AutoHeadResponse)
    install(CallLogging) {
        level = Level.INFO
    }
    val requestLoggingPlugin = createApplicationPlugin(name = "RequestLoggingPlugin") {
        onCall { call ->
            call.request.origin.apply {
                val request = "Request URL: $scheme://$remoteHost:$localPort$uri"
//                sendEvent(Request(this.remoteHost, "", request))
                Log.e("requests", request)
            }
        }
    }
    install(requestLoggingPlugin)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }


    val tokenConfig = TokenConfig(
        issuer = ISSUER,
        audience = AUDIENCE,
        expiresIn = 3L * 1000L * 60L * 60L * 24L,
        secret = JWT_SECRET
    )
    val hashingService = SHA256HashingService()

    install(Authentication) {
        jwt {
            realm = "HTTP-SERVER"
            verifier {
                JWT.require(Algorithm.HMAC256(tokenConfig.secret))
                    .withAudience(tokenConfig.audience)
                    .withIssuer(tokenConfig.issuer)
                    .acceptExpiresAt(tokenConfig.expiresIn)
                    .acceptNotBefore(tokenConfig.expiresIn)
                    .build()
            }
            validate { cred ->
                if (cred.payload.audience.contains(tokenConfig.audience)) {
                    JWTPrincipal(cred.payload)
                } else null
            }
        }
    }

    routing {
        singlePageApplication {
            useResources = true
            react("web1")
        }
        websockets(tokenConfig, dataSource)
        public(tokenConfig, dataSource)
        authentication(settingStorage, tokenConfig, dataSource, hashingService)
        mainApp(dataSource)
    }
}
