@file:Suppress("unused")
package com.kmc.networking.interfaces

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.kmc.networking.Networking
import com.kmc.networking.entity.HttpMethod
import com.kmc.networking.entity.NetworkError
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.IllegalArgumentException
import java.net.MalformedURLException

interface NetworkService {

    fun NetworkService.provideNetworking(): Networking
}

suspend inline fun <reified T> NetworkService.request(
    endpoint: String,
    method: HttpMethod? = null,
    withBody: Map<String, Any>? = null,
    withHeaders: Map<String, String>? = null,
    withOkHttpClient: OkHttpClient? = null,
): T? {

    val task = provideNetworking().DataTaskBuilder(from = endpoint).apply {
        method?.let { withMethod(it) }
        withBody?.let {
            withBody(Gson().toJson(it).toRequestBody("application/json".toMediaTypeOrNull()))
        }
        withHeaders?.let { withHeaders(it) }
        withOkHttpClient?.let { withClient(it) }
    }.build()

    val (data, response) = task.execute()

    return if (response.isSuccessful && response.body != null) Gson().fromJson(data, T::class.java)
    else null
}

suspend inline fun <reified T> NetworkService.safeRequest(
    endpoint: String,
    method: HttpMethod? = null,
    withBody: Map<String, Any>? = null,
    withHeaders: Map<String, String>? = null,
    withOkHttpClient: OkHttpClient? = null,
): Result<T> {

    return try {

        val task = provideNetworking().DataTaskBuilder(from = endpoint).apply {
            method?.let { withMethod(it) }
            withBody?.let {
                withBody(Gson().toJson(it).toRequestBody("application/json".toMediaTypeOrNull()))
            }
            withHeaders?.let { withHeaders(it) }
            withOkHttpClient?.let { withClient(it) }
        }.build()

        val (data, response) = task.execute()

        when {
            !response.isSuccessful -> Result.failure(
                when (response.code) {
                    401 -> NetworkError.StatusCode(response.code, "UnauthorizedUnauthorized")
                    404 -> NetworkError.StatusCode(response.code, "Not Found")
                    500 -> NetworkError.StatusCode(response.code, "Internal Server Error")
                    // Add more status code validations if needed
                    else -> NetworkError.StatusCode(
                        response.code,
                        "Unknown Error: " + response.message
                    )
                }
            )

            response.body == null -> Result.failure(
                NetworkError.APIError(
                    error = "Error: Body expected but found null instead " + response.message
                )
            )

            else -> Result.success(
                Gson().fromJson(data, T::class.java)
            )
        }
    } catch (e: Throwable) {

        when (e) {
            is MalformedURLException,
            is IllegalArgumentException -> Result.failure(NetworkError.UrlConstructError(e.message))
            is ClassCastException,
            is JsonSyntaxException -> Result.failure(NetworkError.DecodingError(e.message))
            else -> Result.failure(NetworkError.APIError(e.message))
        }
    }
}