package com.subhamgupta.httpserver.db

import android.util.Log
import com.subhamgupta.httpserver.MyApp
import com.subhamgupta.httpserver.domain.model.InvalidateToken
import com.subhamgupta.httpserver.domain.model.ReceivedFile
import com.subhamgupta.httpserver.domain.model.ServerSession
import com.subhamgupta.httpserver.domain.model.User
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.flow.Flow


object MongoUserDataSource : UserDataSource {

    private var realm = MyApp.instance.realm
    private val settingStorage = MyApp.instance.settingStorage

    override suspend fun getCurrentUser(): User? {
        val uuid = settingStorage.getUUID()
        return realm.query<User>(query = "uuid == $0", uuid).first().find()
    }

    override suspend fun getCurrentUser(uuid: String): User? {
        return realm.query<User>(query = "uuid == $0", uuid).first().find()
    }

    override suspend fun insertUser(user: User): Boolean {
        realm.write { copyToRealm(user) }
        return true
    }
    override suspend fun getAllUser(): Flow<ResultsChange<User>> {
        return realm.query<User>().asFlow()
    }
    override suspend fun getAllSession(): Flow<ResultsChange<ServerSession>> {
        return realm.query<ServerSession>().asFlow()
    }

    override suspend fun updateUser(user: User): Boolean {
        try {
            realm.write {
                // fetch a frog from the realm by primary key
                val data: User? =
                    this.query<User>("uuid == $0", user.uuid).first().find()

                data?.let {
                    data.type = user.type
                    data.uuid = user.uuid
                    data.timestamp = user.timestamp
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

    override suspend fun invalidateToken(invalidateToken: InvalidateToken) {
        realm.write {
            copyToRealm(invalidateToken)
        }
    }

    override suspend fun deleteUser(user: User) {
        realm.write {
            val data: User? =
                this.query<User>("uuid == $0", user.uuid).first().find()
            data?.let {
                delete(data)
            }
        }
    }


    override suspend fun saveReceivedFile(receivedFile: ReceivedFile) {
        realm.write { copyToRealm(receivedFile) }
    }

    override suspend fun getReceivedFileList(): Flow<ResultsChange<ReceivedFile>> {
        return realm.query<ReceivedFile>().asFlow()
    }
}