@file:Suppress("unused")

package com.kmc.networking

import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

class Networking @Inject internal constructor(
    @NetworkingRetrofit private val retrofit: Retrofit
) {

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

    private var service: ApiService
    private val currentBaseUrl get() = retrofit.baseUrl()

    init {
        service = retrofit.create(ApiService::class.java)
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

    inner class DataTaskBuilder(
        from: String,
    ) {

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

                val newOkHttpClient = (client ?: retrofit.callFactory() as OkHttpClient)
                    .newBuilder()
                    .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val requestBuilder = originalRequest.newBuilder()

                        it.forEach { (key, value) ->
                            requestBuilder.addHeader(key, value)
                        }

                        chain.proceed(requestBuilder.build())
                    }
                    .build()

                retrofitBuilder.client(newOkHttpClient)
            }

            return retrofitBuilder.build().create(ApiService::class.java)
        }
    }
}

