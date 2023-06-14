package com.subhamgupta.httpserver.db

import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.ServerSession
import com.subhamgupta.httpserver.domain.model.User
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    suspend fun getCurrentUser(): User?
    suspend fun getCurrentUser(uuid: String): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun getAllUser(): Flow<ResultsChange<User>>
    suspend fun getAllSession(): Flow<ResultsChange<ServerSession>>
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(user: User)

    suspend fun invalidateToken(invalidateToken: InvalidateToken)
    suspend fun checkToken(token: String): Boolean

    suspend fun saveReceivedFile(receivedFile: ReceivedFile)
    suspend fun getReceivedFileList(): Flow<ResultsChange<ReceivedFile>>
}