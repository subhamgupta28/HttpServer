package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import kotlinx.serialization.Serializable

@Serializable
data class UserObj(
    var username: String = "",
    var email: String = "",
    var type: String = "",
    var status: String = "",
    var timestamp: Long = 0L,
    var allowedFolder: List<String> = emptyList(),
    var allowedFiles: List<String> = emptyList(),
    var accessRole: String = Access.READ.toString()
)
