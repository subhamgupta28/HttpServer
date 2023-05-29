package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Index
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ReceivedFile : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    @Index
    var username: String = ""
    var uri: String = ""
    var fileName: String = ""
    var timestamp: RealmInstant = RealmInstant.now()
}