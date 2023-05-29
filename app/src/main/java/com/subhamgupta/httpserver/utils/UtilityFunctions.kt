package com.subhamgupta.httpserver.utils

import android.content.ContentResolver
import android.content.Context.BATTERY_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.BatteryManager
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.database.getStringOrNull
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.subhamgupta.httpserver.IV
import com.subhamgupta.httpserver.JWT_SECRET
import com.subhamgupta.httpserver.KEY
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.db.UserDataSource
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.User
import io.ktor.server.application.ApplicationCall
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

suspend fun checkAuth(call: ApplicationCall, dataSource: UserDataSource): User? {
    val tokenData = decodeJwt(getTokenFromCall(call))
    val id = tokenData["userId"] ?: tokenData["username"]

    Log.e("token id", "$id")
    return dataSource.getUserByUsername(id.toString().replace("\"", ""))
}

suspend fun checkAuth(token: String, dataSource: UserDataSource): User? {
    val tokenData = decodeJwt(token)
    val id = tokenData["userId"] ?: tokenData["username"]

    Log.e("token id", "$id")
    return dataSource.getUserByUsername(id.toString().replace("\"", ""))
}

fun decodeJwt(token: String): Map<String, Any> {
    Log.e("token", "$token")
    val algorithm = Algorithm.HMAC256(JWT_SECRET)
    val jwtVerifier = JWT.require(algorithm).build()
    val decodedJWT = jwtVerifier.verify(token)
    Log.e("token", "${decodedJWT.claims}")
    return decodedJWT.claims
}

fun getTokenFromCall(call: ApplicationCall): String {
    val token = call.request.headers["Authorization"] ?: ""
    return token.replace("Bearer ", "")
}

fun compressImage(
    inputFile: File,
    maxWidth: Int,
    maxHeight: Int,
    quality: Int
): ByteArrayOutputStream {


    val outputStream = ByteArrayOutputStream()
    // Load the input image into a Bitmap
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(inputFile.absolutePath, options)
    val imageHeight = options.outHeight
    val imageWidth = options.outWidth
    var scaleFactor = 1
    if (imageHeight > maxHeight || imageWidth > maxWidth) {
        scaleFactor = Math.min(imageWidth / maxWidth, imageHeight / maxHeight)
    }
    options.inJustDecodeBounds = false
    options.inSampleSize = scaleFactor
    val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath, options)

    val compressedBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, maxHeight, true)
    compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream
}

val video = listOf("mp3", "mp4")
val image = listOf("jpeg", "png")
fun getFolderMap(rootPath: String): Map<String, Any> {
    val rootDir = File(rootPath)
    if (!rootDir.exists() || !rootDir.isDirectory) {
        throw IllegalArgumentException("Invalid root directory path")
    }
    val folderMap = mutableMapOf<String, Any>()
    for (file in rootDir.listFiles()!!) {
        if (file.isDirectory && !file.name.startsWith(".")) {
            folderMap[file.name] = getFolderMap(file.path)
        } else {
            val fileList = folderMap.getOrDefault(
                rootDir.name,
                mutableListOf<FileObj>()
            ) as MutableList<FileObj>
            val fileObj = FileObj(
                fileName = file.name,
                uri = file.absolutePath,
                size = file.length(),
                contentType = if (video.contains(file.extension)) "Video" else if (image.contains(
                        file.extension
                    )
                ) "Image" else file.extension,
                lastModified = file.lastModified()
            )
            fileList.add(fileObj)
            fileList.sortByDescending { it.lastModified }
            folderMap[rootDir.name] = fileList

        }
    }
    return folderMap
}

fun decryptData(data: String): String {
    val encryptedBytes = Base64.getDecoder().decode(data)
    Log.e("server", "$data")

    val cipher = Cipher.getInstance("AES/CBC/NoPadding")
    val keySpec = SecretKeySpec(KEY.toByteArray(), "AES")
    val ivSpec = IvParameterSpec(IV.toByteArray())

    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

    val original = cipher.doFinal(encryptedBytes)
    val originalString = String(original)
    println(originalString.trim())
    Log.e("server", "${originalString.replace("\u003F", "")} $original")
    return originalString.replace(Regex("\\?+"), "")
}

fun uriForMediaWithFilename(
    resolver: ContentResolver,
    filename: String
): String {
    val columns = arrayOf<String>(BaseColumns._ID, MediaStore.MediaColumns.DATA)
    val selection: String = MediaStore.MediaColumns.DATA + " LIKE ?"
    val selectionArgs = arrayOf("%$filename")
    val collection =
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val cursor = resolver.query(
        collection, columns,
        selection, selectionArgs, null
    )
    var path = ""
    val pathCol = cursor?.getColumnIndex(MediaStore.Files.FileColumns.DATA)
    val nameCol = cursor?.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
    while (cursor!!.moveToNext()) {
        path = pathCol?.let { cursor.getStringOrNull(it) }.toString()
        val name = nameCol?.let { cursor.getStringOrNull(it) }
        Log.e("file", "$path")
    }
    cursor.close()
    return path
}

fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
    var fileName: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    if (cursor != null && cursor.moveToFirst()) {
        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (displayNameIndex != -1) {
            fileName = cursor.getString(displayNameIndex)
        }
        cursor.close()
    }
    return fileName
}

fun getDeviceInfo(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val bm = MyApp.instance.getSystemService(BATTERY_SERVICE) as BatteryManager

    val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    map["battery"] = "$batLevel%"
    return map
}


