package com.subhamgupta.httpserver.db

import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.domain.model.UserBasedRoles
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun getAllUser(): Flow<ResultsChange<User>>
    suspend fun updateUser(user: User): Boolean
    suspend fun deleteUser(user: User)
    suspend fun getUserCount(): Int


    suspend fun invalidateToken(invalidateToken: InvalidateToken)
    suspend fun checkToken(token: String): Boolean
    suspend fun updateUserStatus(id: ObjectId, status: String)


    suspend fun getAllowedFiles(user: User): List<String>
    suspend fun getAllowedFolders(user: User): List<String>
    suspend fun getUserAccess(user: User): UserBasedRoles?

    suspend fun setUserRole(
        username: String,
        userBasedRoles: UserBasedRoles
    )

    suspend fun saveReceivedFile(receivedFile: ReceivedFile)

    suspend fun getReceivedFileList(): Flow<ResultsChange<ReceivedFile>>
}