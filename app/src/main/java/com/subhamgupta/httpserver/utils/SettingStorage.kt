package com.subhamgupta.httpserver.utils

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingStorage @Inject constructor(
    private var context : Application
) {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private suspend fun save(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        context.dataStore.edit {
            it[dataStoreKey] = value
        }
    }
    private suspend fun read(key: String): String? {
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[dataStoreKey]
    }
    suspend fun getUUID(): String {
        return read("uuid") ?:""
    }

    suspend fun setUUID(str: String) {
        save("uuid", str)
    }
    suspend fun getPassword(): String {
        return read("pass") ?:""
    }

    suspend fun setPassword(str: String) {
        save("pass", str)
    }
}