package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ServerSession : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    var clientId: String = ""
    var clientIP: String = ""
    var osName: String = ""
    var osVersion: String = ""
    var browserName: String = ""
    var browserVersion: String = ""
    var token: String = ""
    var updatedAt: Long = 0
}

