package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.mongodb.kbson.ObjectId


class User() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    @Index
    var username: String = ""

    @Index
    var email: String = ""
    var password: String = ""
    var salt: String = ""
    var type: String = ""
    var status: String = ""
    var timestamp: RealmInstant = RealmInstant.now()
}
