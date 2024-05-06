package com.kmc.networking

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URL

abstract class DataRequest(
    private val url: URL, private val networking: Networking
) {

    private val urlPath get() = url.toString()
    private var method: HttpMethod = HttpMethod.GET
    private var body: RequestBody? = null
    private var client: OkHttpClient? = null
    private var headers: Map<String, String>? = null

    internal suspend fun getServiceResponse(): Pair<JsonElement?, Response> {

        var service = networking.service

        if (client != null || headers != null) {

            service = networking.buildService(client, headers)
        }

        val serverResponse = when (method) {

            HttpMethod.GET -> service.get(urlPath)

            HttpMethod.POST -> service.post(urlPath, body)

            HttpMethod.PUT -> service.put(urlPath, body)

            HttpMethod.PATCH -> service.patch(urlPath, body)

            HttpMethod.DELETE -> service.delete(urlPath)
        }

        return Pair(serverResponse.body(), serverResponse.raw())
    }

    open fun withMethod(method: HttpMethod): DataRequest {
        this.method = method
        return this
    }

    open fun withClient(client: OkHttpClient): DataRequest {
        this.client = client
        return this
    }

    open fun withHeaders(vararg headers: Pair<String, String>): DataRequest {
        this.headers = headers.toMap()
        return this
    }

    open fun withBody(vararg body: Pair<String, Any>): DataRequest {
        this.body = Gson().toJson(body).toRequestBody("application/json".toMediaTypeOrNull())
        return this
    }
}

class Request(
    url: URL, networking: Networking
) : DataRequest(url, networking), RequestExecutor {

    constructor(
        endpoint: String, networking: Networking
    ) : this(
        url = URL(networking.baseUrl + endpoint), networking = networking
    )

    override fun withMethod(method: HttpMethod): Request {
        super.withMethod(method)
        return this
    }

    override fun withClient(client: OkHttpClient): Request {
        super.withClient(client)
        return this
    }

    override fun withHeaders(vararg headers: Pair<String, String>): Request {
        super.withHeaders(*headers)
        return this
    }

    override fun withBody(vararg body: Pair<String, Any>): Request {
        super.withBody(*body)
        return this
    }

    suspend inline fun <reified T> execute(): T? {

        val (data, response) = executeRequest(this)

        return if (response.isSuccessful && response.body != null) Gson().fromJson(
            data, T::class.java
        ) else null
    }
}

class SafeRequest(
    url: URL, networking: Networking
) : DataRequest(url, networking), RequestExecutor {

    constructor(
        endpoint: String, networking: Networking
    ) : this(url = URL(networking.baseUrl + endpoint), networking = networking)

    override fun withMethod(method: HttpMethod): SafeRequest {
        super.withMethod(method)
        return this
    }

    override fun withClient(client: OkHttpClient): SafeRequest {
        super.withClient(client)
        return this
    }

    override fun withHeaders(vararg headers: Pair<String, String>): SafeRequest {
        super.withHeaders(*headers)
        return this
    }

    override fun withBody(vararg body: Pair<String, Any>): SafeRequest {
        super.withBody(*body)
        return this
    }

    suspend inline fun <reified T> execute(): Result<T> {

        return try {

            val (data, response) = executeRequest(this)

            when {
                !response.isSuccessful -> Result.failure(
                    when (response.code) {
                        401 -> NetworkError.StatusCode(response.code, "Unauthorized")
                        404 -> NetworkError.StatusCode(response.code, "Not Found")
                        500 -> NetworkError.StatusCode(response.code, "Internal Server Error")
                        else -> NetworkError.StatusCode(
                            response.code, "Unknown Error: " + response.message
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
                is MalformedURLException, is IllegalArgumentException -> Result.failure(
                    NetworkError.UrlConstructError(e.message)
                )

                is ClassCastException, is JsonSyntaxException -> Result.failure(
                    NetworkError.DecodingError(
                        e.message
                    )
                )

                else -> Result.failure(NetworkError.APIError(e.message))
            }
        }
    }
}