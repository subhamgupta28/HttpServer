package com.subhamgupta.httpserver.server


import android.content.Context
import android.util.Log
import com.subhamgupta.httpserver.PORT
import com.subhamgupta.httpserver.SSL_KEY_ALIAS
import com.subhamgupta.httpserver.SSL_PORT
import com.subhamgupta.httpserver.utils.JksHelper
import io.ktor.server.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore

object HttpServerManager {
    var password = "12345678"
    private val clientId = password


    private fun getSSLKeyStore(context: Context): KeyStore {
        val file = File(context.filesDir, "release.jks")
        if (!file.exists()) {
            Log.e("server", "file")
            val keyStore = JksHelper.genJksFile(SSL_KEY_ALIAS, clientId, SSL_KEY_ALIAS)
            val out = FileOutputStream(file)
            keyStore.store(out, null)
            out.close()
        }

        return KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            file.inputStream().use {
                load(it, null)
            }
        }
    }

    fun createHttpServer(context: Context): NettyApplicationEngine {
        val environment = applicationEngineEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
            connector {
                port = PORT
            }
            sslConnector(
                keyStore = getSSLKeyStore(context),
                keyAlias = SSL_KEY_ALIAS,
                keyStorePassword = { clientId.toCharArray() },
                privateKeyPassword = { clientId.toCharArray() }
            ) {
                port = SSL_PORT
            }
            module(Application::module)
        }

        return embeddedServer(Netty, environment)
    }
}