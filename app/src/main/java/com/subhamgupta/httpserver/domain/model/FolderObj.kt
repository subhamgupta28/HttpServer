package com.subhamgupta.httpserver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FolderObj(
    val filename: String = "",
    val size: Int = 0
)
