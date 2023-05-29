package com.subhamgupta.httpserver.data.repository

import android.app.Application
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.subhamgupta.httpserver.PORT
import com.subhamgupta.httpserver.db.MongoUserDataSource
import com.subhamgupta.httpserver.domain.model.Access
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.FolderObj
import com.subhamgupta.httpserver.domain.model.NotificationObj
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.domain.model.UserBasedRoles
import com.subhamgupta.httpserver.domain.objects.setFolders
import com.subhamgupta.httpserver.domain.objects.setImages
import com.subhamgupta.httpserver.domain.objects.setVideoFiles
import com.subhamgupta.httpserver.hashing.SHA256HashingService
import com.subhamgupta.httpserver.security.generateToken
import com.subhamgupta.httpserver.server.HttpServerService
import com.subhamgupta.httpserver.utils.ConfirmToAcceptLoginEvent
import com.subhamgupta.httpserver.utils.NetworkUtils
import com.subhamgupta.httpserver.utils.SettingStorage
import com.subhamgupta.httpserver.utils.Transfer
import com.subhamgupta.httpserver.utils.UserSession
import com.subhamgupta.httpserver.utils.receiveEventHandler
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.notifications.InitialResults
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MainRepository @Inject constructor(
    private val realm: Realm,
    private val settingStorage: SettingStorage,
    private val application: Application,
    private val client: HttpClient
) {
    private val db = MongoUserDataSource
    private var userSession: UserSession? = null

    fun getStorage() = settingStorage
    private val hashingService = SHA256HashingService()
    var startTime: Long = 0

    fun getDb() = db

    suspend fun startServer(_hostAddress: MutableStateFlow<String>) {
        val simpleTextApi =
            String.format("http://%s:%s", NetworkUtils.getLocalIpAddress(), PORT)
        Log.e("ip", simpleTextApi)
        _hostAddress.value = simpleTextApi

//        realm.write {
//            val user = query<User>("username == $0", "subham").first().find()
//            user?.type = "Admin"
//        }
//        getFileFolder()

//        val mp = getFolderMap(File(Environment.getExternalStorageDirectory(), "Pictures/Gallery").toString())
//        Log.e("folder", Gson().toJson(mp))

        startTime = System.currentTimeMillis()
        ContextCompat.startForegroundService(
            application,
            Intent(application, HttpServerService::class.java)
        )
    }

    private fun getFolderMap(rootPath: String): Map<String, Any> {
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
                    size = file.length() / 1024 * 1024,
                    contentType = file.extension
                )
                fileList.add(fileObj)
                folderMap[rootDir.name] = fileList
            }
        }
        return folderMap
    }

    fun getRootFolders(file: File): List<String> {
        return file.listFiles()?.toList()!!.filter {
            it.isDirectory
        }.toList().map {
            it.name
        }
    }


    fun stopServer() {
        HttpServerService.instance?.stop()
    }

    fun handleEvents(_confirmLogin: MutableStateFlow<ConfirmToAcceptLoginEvent?>) {
        receiveEventHandler<Transfer> {
            Log.e("server", "event ${it}")
        }
    }

    private var selectionArgs: Array<String>? = arrayOf(
        "A",
    )

    fun getAllFolders(folders: MutableStateFlow<List<String>?>) {
        try {
            val folder = File(Environment.getExternalStorageDirectory().toURI())
            val folderList = folder.listFiles()?.filter { it.isDirectory }?.map { it.name }
            Log.e("folders", "$folderList")
            if (folderList != null) {
                val map = mutableListOf<FolderObj>()
                val r = folderList.stream().forEach {
                    val fs = File(folder, it).listFiles()?.size
                    if (fs != null && fs > 0) {
                        map.add(FolderObj(filename = it, size = fs))
                    }
                }
                Log.e("folder", "$map")
                setFolders(map)
            }
            folders.value = folderList
        } catch (e: Exception) {
            Log.e("folders", "$e")
        }
    }

    fun fetchVideoLocal(uris: MutableStateFlow<List<FileObj>>) {
        val videos = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED)
        val videoPaths = mutableListOf<FileObj>()
        val selection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?"
        val videoCursor = application.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videos,
            null,
            null,
            MediaStore.Video.Media.DATE_MODIFIED + " DESC"
        )
        val videoCount = videoCursor!!.count
        for (i in 0 until videoCount) {
            videoCursor.moveToPosition(i)
            val dataColumnIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DATA)
            val file = File(videoCursor.getString(dataColumnIndex))
            videoPaths.add(
                FileObj(
                    uri = file.absolutePath,
                    fileName = file.name,
                    contentType = "Video",
                    size = file.totalSpace
                )
            )
        }
        uris.value = videoPaths
        setVideoFiles(videoPaths)
        videoCursor.close()
    }

    fun fetchImageLocal(uris: MutableStateFlow<List<FileObj>>) {
        val images = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED)
        val imagesPaths = mutableListOf<FileObj>()
        val selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?"
        val imageCursor = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            images,
            null,
            null,
            MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        )
        val imageCount = imageCursor!!.count
        for (i in 0 until imageCount) {
            imageCursor.moveToPosition(i)
            val dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val file = File(imageCursor.getString(dataColumnIndex))
            imagesPaths.add(
                FileObj(
                    uri = file.absolutePath,
                    fileName = file.name,
                    contentType = "Image",
                    size = file.totalSpace
                )
            )
        }
        uris.value = imagesPaths
        setImages(imagesPaths)
        imageCursor.close()
    }

    suspend fun fetchUsers(users: MutableStateFlow<List<User>>) {
        db.getAllUser().collect {
            when (it) {
                // print out initial results
                is InitialResults<User> -> {
                    users.value = it.list
                }

                else -> {
                    // do nothing on changes
                }
            }
        }
    }

    suspend fun loginUser(
        username: String,
        password: String,
        userState: MutableStateFlow<Map<String, Any>>
    ) {
        val user = db.getUserByUsername(username)
        val map = mutableMapOf("isLoggedIn" to false, "isRegistered" to false)
        if (user != null) {

            val token = generateToken(user, password)
            if (token.isNotEmpty()) {
                settingStorage.setUserName(user.username)
                settingStorage.setToken(token)
            }
            Log.e("user", "${token.isNotEmpty()}")
            map["isLoggedIn"] = token.isNotEmpty()
            userState.value = map
        } else {
            Log.e("user", "user")
            userState.value = map
        }
    }

    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        userState: MutableStateFlow<Map<String, Any>>
    ) {
        val allUsers = db.getUserCount()
        val saltedHash = hashingService.generateSaltedHash(password)
        val user = User()
        user.username = username
        user.salt = saltedHash.salt
        user.password = saltedHash.hash
        user.email = email
        user.status = "Registered"
        if (allUsers == 0)
            user.type = "Admin"
        db.insertUser(user)
        val map = mutableMapOf("isLoggedIn" to false, "isRegistered" to true)
        userState.value = map
    }

    suspend fun registerUser(
        username: String,
        password: String,
        userState: MutableStateFlow<Map<String, Any>>,
        selectedFolder: List<FolderObj>,
        hasAccessTo: String
    ) {
        val saltedHash = hashingService.generateSaltedHash(password)
        val user = User()
        user.username = username
        user.salt = saltedHash.salt
        user.password = saltedHash.hash
        user.email = ""
        user.status = "Registered"
        db.insertUser(user)
        val userBasedRoles = UserBasedRoles()
        val fd = mutableListOf<String>()
        selectedFolder.forEach {
            fd.add(it.filename)
        }
        userBasedRoles.allowedFolder = fd.toRealmList()
        userBasedRoles.accessRole = Access.READ.toString()
        userBasedRoles.username = username
        userBasedRoles.roleName = "LEVEL1"
        realm.write {
            copyToRealm(userBasedRoles)
        }
        Log.e("user saved", "$userBasedRoles $user")
    }

    suspend fun checkUser(): Boolean {
        val token = settingStorage.getToken()
        return token.isNotEmpty()
    }

    suspend fun timer(period: Long, initialDelay: Long) = flow {
        delay(initialDelay)
        while (true) {
            emit(System.currentTimeMillis())
            delay(period)
        }
    }

    suspend fun updateUserStatus(id: ObjectId, string: String) {
        db.updateUserStatus(id, string)
    }

    suspend fun sendNotification(notificationObj: NotificationObj) {
        if (userSession != null) {
            val json = Gson().toJson(notificationObj)
            Log.e("notification", "sent $json")
            sendESP(notificationObj)
            userSession?.session?.send(Frame.Text(json))
        }
    }

    private suspend fun sendESP(notificationObj: NotificationObj) = coroutineScope {
        launch {
            var r = 10
            var g = 10
            var b = 10
            val bt = 15
            val pkg = notificationObj.notifyObj["package"]
            if (pkg != null) {

                if (pkg.toString().contains("whatsapp"))
                    g = 255
                if (pkg.toString().contains("instagram")){
                    r = 255
                    b = 255
                }
                if (pkg.toString().contains("gmail")) {
                    g = 200
                    b = 200
                    r = 255
                }

                val res = client.get {
                    url("http://192.168.29.67/")
                    parameter("r", r)
                    parameter("g", g)
                    parameter("b", b)
                    parameter("bt", bt)
                }
                Log.e("response", "${res.status} $res")
            }
        }
    }

    suspend fun manageSession() {
        receiveEventHandler<UserSession> {
            userSession = it
            Log.e("manageSession: session", "$it")
        }
    }

}
