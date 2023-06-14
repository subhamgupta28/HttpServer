package com.subhamgupta.httpserver.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhamgupta.httpserver.data.repository.MainRepository
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.NotificationObj
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.utils.ConfirmToAcceptLoginEvent
import com.subhamgupta.httpserver.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var repository: MainRepository
) : ViewModel() {

    private val _hostAddress = MutableStateFlow("")
    val hostAddress = _hostAddress.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

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
    fun getUserSession() = repository.userSession
    fun getDb() = repository.getDb()

    fun init() = viewModelScope.launch {
        repository.setupUser()
        repository.handleEvents(_confirmLogin)
        repository.fetchVideoLocal(_videos)
        repository.fetchImageLocal(_images)
        repository.manageSession()
    }
    fun fetchUsers() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchUsers(_users)
    }
    fun getPassword() = viewModelScope.launch {
        _password.value = repository.getPassword()
    }

    fun startServer() = viewModelScope.launch(Dispatchers.IO) {
        repository.startServer(_hostAddress)

    }

    fun stopServer() {
        repository.stopServer()
    }

    fun sendNotification(notificationObj: NotificationObj) = viewModelScope.launch {
        repository.sendNotification(notificationObj)
    }

    fun getAllFolders() = viewModelScope.launch {
        repository.getAllFolders(_folders)
    }

    fun setUserSession(userSession: UserSession) {
        repository.userSession = userSession
    }
}