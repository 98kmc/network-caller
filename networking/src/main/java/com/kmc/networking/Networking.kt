@file:Suppress("unused")

package com.kmc.networking

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response as HttpResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

class Networking @Inject internal constructor(
    @NetworkingRetrofit private val retrofit: Retrofit
) {

    private var service: ApiService
    private val currentBaseUrl
        get() = retrofit.baseUrl()

    init {
        service = retrofit.create(ApiService::class.java)
    }

    private interface ApiService {

        @GET
        suspend fun get(@Url url: String): Response<JsonElement>

        @POST
        suspend fun post(@Url url: String, @Body body: RequestBody?): Response<JsonElement>

        @PUT
        suspend fun put(@Url url: String, @Body body: RequestBody?): Response<JsonElement>

        @PATCH
        suspend fun patch(@Url url: String, @Body body: RequestBody?): Response<JsonElement>

        @DELETE
        suspend fun delete(@Url url: String): Response<JsonElement>
    }

    abstract inner class DataRequest(private val url: String) {

        private var method: HttpMethod = HttpMethod.GET
        private var body: RequestBody? = null
        private var client: OkHttpClient? = null
        private var headers: Map<String, String>? = null

        open fun withMethod(method: HttpMethod): DataRequest {
            this.method = method
            return this
        }

        open fun withClient(client: OkHttpClient): DataRequest {
            this.client = client
            return this
        }

        open fun withHeaders(headers: Map<String, String>): DataRequest {
            this.headers = headers
            return this
        }

        open fun withBody(body: Map<String, Any>): DataRequest {
            this.body = Gson().toJson(body).toRequestBody("application/json".toMediaTypeOrNull())
            return this
        }

        internal suspend fun getServiceResponse(): Pair<JsonElement?, HttpResponse> {

            if (client != null || headers != null) {

                service = buildService(client, headers)
            }

            val serverResponse = when (method) {

                HttpMethod.GET -> service.get(url)

                HttpMethod.POST -> service.post(url, body)

                HttpMethod.PUT -> service.put(url, body)

                HttpMethod.PATCH -> service.patch(url, body)

                HttpMethod.DELETE -> service.delete(url)
            }

            return Pair(serverResponse.body(), serverResponse.raw())
        }
    }

    inner class Request(endpoint: String) : DataRequest(buildUrl(endpoint)), RequestExecutor {

        override fun withMethod(method: HttpMethod): Request {
            super.withMethod(method)
            return this
        }

        override fun withClient(client: OkHttpClient): Request {
            super.withClient(client)
            return this
        }

        override fun withHeaders(headers: Map<String, String>): Request {
            super.withHeaders(headers)
            return this
        }

        override fun withBody(body: Map<String, Any>): Request {
            super.withBody(body)
            return this
        }

        suspend inline fun <reified T> execute(): T? {

            val (data, response) = executeRequest(this)

            return if (response.isSuccessful && response.body != null) Gson().fromJson(
                data, T::class.java
            ) else null
        }
    }

    inner class SafeRequest(endpoint: String) : DataRequest(buildUrl(endpoint)), RequestExecutor {

        override fun withMethod(method: HttpMethod): SafeRequest {
            super.withMethod(method)
            return this
        }

        override fun withClient(client: OkHttpClient): SafeRequest {
            super.withClient(client)
            return this
        }

        override fun withHeaders(headers: Map<String, String>): SafeRequest {
            super.withHeaders(headers)
            return this
        }

        override fun withBody(body: Map<String, Any>): SafeRequest {
            super.withBody(body)
            return this
        }

        suspend inline fun <reified T> execute(): Result<T> {

            return try {

                val (data, response) = executeRequest(this)

                when {
                    !response.isSuccessful -> Result.failure(
                        when (response.code) {
                            401 -> NetworkError.StatusCode(
                                response.code, "UnauthorizedUnauthorized"
                            )

                            404 -> NetworkError.StatusCode(response.code, "Not Found")
                            500 -> NetworkError.StatusCode(response.code, "Internal Server Error")
                            // Add more status code validations if needed
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

    private fun buildUrl(str: String) = try {
        URL(str)
    } catch (e: MalformedURLException) {
        URL(currentBaseUrl.toString() + str)
    }.toString()

    private fun buildService(
        client: OkHttpClient?, headers: Map<String, String>?
    ): ApiService {

        val retrofitBuilder = retrofit.newBuilder()

        client?.let { retrofitBuilder.client(it) }

        headers?.let {

            val newOkHttpClient = (client ?: retrofit.callFactory() as OkHttpClient).newBuilder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val requestBuilder = originalRequest.newBuilder()

                    it.forEach { (key, value) ->
                        requestBuilder.addHeader(key, value)
                    }

                    chain.proceed(requestBuilder.build())
                }.build()

            retrofitBuilder.client(newOkHttpClient)
        }

        return retrofitBuilder.build().create(ApiService::class.java)
    }

    inner class DataTask internal constructor(
        val url: URL,
        val method: HttpMethod,
        val body: RequestBody?,
    ) {

        suspend fun execute(): Pair<JsonElement?, HttpResponse> {

            val url = url.toString()

            val serverResponse = when (method) {

                HttpMethod.GET -> service.get(url)

                HttpMethod.POST -> service.post(url, body)

                HttpMethod.PUT -> service.put(url, body)

                HttpMethod.PATCH -> service.patch(url, body)

                HttpMethod.DELETE -> service.delete(url)
            }

            return Pair(serverResponse.body(), serverResponse.raw())
        }
    }

    inner class DataTaskBuilder(from: String) {

        private val url: URL = buildUrl(str = from)
        private var method: HttpMethod = HttpMethod.GET
        private var body: RequestBody? = null
        private var client: OkHttpClient? = null
        private var headers: Map<String, String>? = null

        fun build(): DataTask {

            if (client != null || headers != null) {

                service = buildService()
            }

            return DataTask(
                url = url,
                method = method,
                body = body,
            )
        }

        fun withMethod(method: HttpMethod): DataTaskBuilder {
            this.method = method
            return this
        }

        fun withClient(client: OkHttpClient): DataTaskBuilder {
            this.client = client
            return this
        }

        fun withHeaders(headers: Map<String, String>): DataTaskBuilder {
            this.headers = headers
            return this
        }

        fun withBody(body: RequestBody): DataTaskBuilder {
            this.body = body
            return this
        }

        private fun buildUrl(str: String) = try {
            URL(str)
        } catch (e: MalformedURLException) {
            URL(currentBaseUrl.toString() + str)
        }

        private fun buildService(): ApiService {

            val retrofitBuilder = retrofit.newBuilder()

            client?.let { retrofitBuilder.client(it) }

            headers?.let {

                val newOkHttpClient =
                    (client ?: retrofit.callFactory() as OkHttpClient).newBuilder()
                        .addInterceptor { chain ->
                            val originalRequest = chain.request()
                            val requestBuilder = originalRequest.newBuilder()

                            it.forEach { (key, value) ->
                                requestBuilder.addHeader(key, value)
                            }

                            chain.proceed(requestBuilder.build())
                        }.build()

                retrofitBuilder.client(newOkHttpClient)
            }

            return retrofitBuilder.build().create(ApiService::class.java)
        }
    }
}

