package com.subhamgupta.httpserver.security

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import javax.crypto.Cipher

object EncryptDecrypt {
    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    fun generateKeys(): Map<String, Any> {
        val map = HashMap<String, Any>()
        try {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(1024)
            val keyPair = generator.generateKeyPair()
            privateKey = keyPair.private
            publicKey = keyPair.public
            map["PRIVATE"] = privateKey
            map["PUBLIC"] = publicKey
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun encrypt(text: String): String {
        val msg = text.toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(msg)
        return encode(encryptedBytes)
    }

    fun decrypt(text: String): String {
        val encryptedBytes = decode(text)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedMsg = cipher.doFinal(encryptedBytes)
        return String(decryptedMsg, Charsets.UTF_8)

    }

    private fun encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    private fun decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }
}