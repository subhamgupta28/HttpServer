package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class RequestLog : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var requestUrl: String = ""
    var timestamp: RealmInstant = RealmInstant.now()
    var requestByUser: String = ""

}