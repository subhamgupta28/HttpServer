package com.subhamgupta.httpserver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FileObj(
    var fileName: String = "",
    var size: Long = 0,
    var contentType: String = "",
    var uri: String = "",
    var lastModified: Long = 0L
)
