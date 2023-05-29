package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class InvalidateToken: RealmObject{
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    @Index
    var token: String = ""
    var username: String = ""
}
