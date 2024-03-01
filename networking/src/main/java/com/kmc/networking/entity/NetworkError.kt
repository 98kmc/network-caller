package com.kmc.networking.entity

sealed class NetworkError : Error() {
    data class UrlConstructError(val msg: String? = null) : NetworkError()
    data class StatusCode(val code: Int, val msg: String?) : NetworkError()
    data class DecodingError(val error: String?) : NetworkError()
    data class APIError(val error: String?) : NetworkError()
}