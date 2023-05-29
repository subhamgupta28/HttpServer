package com.subhamgupta.httpserver.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhamgupta.httpserver.data.repository.MainRepository
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.FolderObj
import com.subhamgupta.httpserver.domain.model.NotificationObj
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.utils.ConfirmToAcceptLoginEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var repository: MainRepository
) : ViewModel() {

    private val _hostAddress = MutableStateFlow("")
    val hostAddress = _hostAddress.asStateFlow()

    private val _videos = MutableStateFlow<List<FileObj>>(emptyList())
    val videos = _videos.asStateFlow()

    private val _images = MutableStateFlow<List<FileObj>>(emptyList())
    val images = _images.asStateFlow()

    private val _confirmLogin = MutableStateFlow<ConfirmToAcceptLoginEvent?>(null)
    val confirmLogin = _confirmLogin.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _userPresent = MutableStateFlow(true)
    val userPresent = _userPresent.asStateFlow()

    val map = mutableMapOf("isLoggedIn" to false, "isRegistered" to false)

    private val _userState = MutableStateFlow<Map<String, Any>>(map)
    val userState = _userState.asStateFlow()

    private val _folders = MutableStateFlow<List<String>?>(emptyList())
    val folders = _folders.asStateFlow()

    fun getStorage() = repository.getStorage()
    fun getDb() = repository.getDb()

    fun init() = viewModelScope.launch {
        repository.handleEvents(_confirmLogin)
        repository.fetchVideoLocal(_videos)
        repository.fetchImageLocal(_images)
        repository.manageSession()
    }

    fun fetchUsers() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchUsers(_users)
    }

    fun startServer() = viewModelScope.launch(Dispatchers.IO) {
        repository.startServer(_hostAddress)

    }

    fun stopServer() {
        repository.stopServer()
    }

    fun registerUser(username: String, email: String, password: String) = viewModelScope.launch {
        repository.registerUser(username, email, password, _userState)
    }

    fun registerUser(
        username: String,
        password: String,
        selectedFolder: List<FolderObj>,
        hasAccessTo: String
    ) = viewModelScope.launch {
        repository.registerUser(username, password, _userState, selectedFolder, hasAccessTo)
    }

    fun loginUser(username: String, password: String) = viewModelScope.launch {
        repository.loginUser(username, password, _userState)
    }

    fun checkUser() = viewModelScope.launch {
        _userPresent.value = repository.checkUser()
    }
    fun sendNotification(notificationObj: NotificationObj) = viewModelScope.launch {
        repository.sendNotification(notificationObj)
    }

    fun updateUserStatus(id: ObjectId, string: String) = viewModelScope.launch {
        repository.updateUserStatus(id, string)
    }

    fun getAllFolders() = viewModelScope.launch {
        repository.getAllFolders(_folders)
    }
}