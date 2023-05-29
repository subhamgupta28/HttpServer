package com.subhamgupta.httpserver.db

import android.util.Log
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.User
import com.subhamgupta.httpserver.domain.model.UserBasedRoles
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId


object MongoUserDataSource : UserDataSource {

    private var realm: Realm = MyApp.instance.realm

    override suspend fun getUserByUsername(username: String): User? {
        return realm.query<User>(query = "username == $0", username).first().find()
    }

    override suspend fun insertUser(user: User): Boolean {
        val us = getUserByUsername(user.username)
        return if (us != null)
            false
        else {
            realm.write { copyToRealm(user) }
            true
        }
    }

    override suspend fun getAllUser(): Flow<ResultsChange<User>> {
        return realm.query<User>().asFlow()
    }

    override suspend fun updateUser(user: User): Boolean {
        try {
            realm.write {
                // fetch a frog from the realm by primary key
                val data: User? =
                    this.query<User>("username == $0", user.username).first().find()

                data?.let {
                    data.type = user.type
                    data.username = user.username
                    data.timestamp = user.timestamp
                    data.email = user.email
                    data.password = user.password
                    data.salt = user.salt
                }
            }
        } catch (e: Exception) {
            Log.e("error MongoUserDataSource", "$e")
        }

        return true
    }

    override suspend fun checkToken(token: String): Boolean {
        val res = realm.query<InvalidateToken>("token == $0", token).first().find()
        return res != null
    }

    override suspend fun updateUserStatus(id: ObjectId, status: String) {
        realm.write {
            // fetch a frog from the realm by primary key
            val data: User? =
                this.query<User>("_id == $0", id).first().find()

            data?.let {
                data.status = status
            }
        }
    }

    override suspend fun invalidateToken(invalidateToken: InvalidateToken) {
        realm.write {
            copyToRealm(invalidateToken)
        }
    }

    override suspend fun deleteUser(user: User) {
        realm.write {
            val data: User? =
                this.query<User>("username == $0", user.username).first().find()
            data?.let {
                delete(data)
            }
        }
    }

    override suspend fun getUserCount(): Int {
        return realm.query<User>().count().find().toInt()
    }


    override suspend fun getAllowedFiles(user: User): List<String> {
        val role = realm.query<UserBasedRoles>("username == $0", user.username).first().find()
        return role?.allowedFiles ?: emptyList()
    }

    override suspend fun getAllowedFolders(user: User): List<String> {
        val role = realm.query<UserBasedRoles>("username == $0", user.username).first().find()
        return role?.allowedFolder ?: emptyList()
    }

    override suspend fun getUserAccess(user: User): UserBasedRoles? {
        return realm.query<UserBasedRoles>("username == $0", user.username).first().find()
    }

    override suspend fun setUserRole(
        username: String,
        userBasedRoles: UserBasedRoles
    ) {
        realm.write {
            copyToRealm(userBasedRoles)
        }
    }

    override suspend fun saveReceivedFile(receivedFile: ReceivedFile) {
        realm.write { copyToRealm(receivedFile) }
    }

    override suspend fun getReceivedFileList(): Flow<ResultsChange<ReceivedFile>> {
        return realm.query<ReceivedFile>().asFlow()
    }
}