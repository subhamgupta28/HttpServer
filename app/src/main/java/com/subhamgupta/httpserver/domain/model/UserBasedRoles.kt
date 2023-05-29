package com.subhamgupta.httpserver.domain.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class UserBasedRoles : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()
    var roleName: String = ""
    var allowedFolder: RealmList<String> = realmListOf()
    var allowedFiles: RealmList<String> = realmListOf()
    var accessRole: String = Access.READ.toString()
    var username : String = ""
}

enum class Access {
    READ, WRITE, DELETE, UPDATE
}