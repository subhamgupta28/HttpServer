package com.subhamgupta.httpserver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(val data: T? = null, val error: String? = null)
