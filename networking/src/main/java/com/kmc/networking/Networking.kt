@file:Suppress("unused")
package com.kmc.networking

import com.google.gson.JsonElement
import com.kmc.networking.entity.HttpMethod
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response as HttpResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
import javax.inject.Singleton

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

    private lateinit var service: ApiService

    inner class DataTask internal constructor(
        val url: URL,
        val method: HttpMethod,
        val body: RequestBody?,
    ) {

        suspend fun execute(): Pair<JsonElement?, HttpResponse> {

            val url = url.toString()

            val serverResponse = when (method) {

                HttpMethod.Get -> service.get(url)

                HttpMethod.Post -> service.post(url, body)

                HttpMethod.Put -> service.put(url, body)

                HttpMethod.Patch -> service.patch(url, body)

                HttpMethod.Delete -> service.delete(url)
            }

            return Pair(serverResponse.body(), serverResponse.raw())
        }
    }

    inner class DataTaskBuilder(
        from: String,
    ) {

        private val url: URL = buildUrl(str = from)
        private var method: HttpMethod = HttpMethod.Get
        private var body: RequestBody? = null
        private var client: OkHttpClient? = null
        private var headers: Map<String, String>? = null

        fun build(): DataTask {

            service = buildService()

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
            URL(retrofit.baseUrl().toString() + str)
        }

        private fun buildService(): ApiService {

            val retrofitBuilder = retrofit.newBuilder().baseUrl(url)

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

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkingHiltModule {

    @NetworkingRetrofit
    @Singleton
    @Provides
    fun provideRetrofit(
        @NetworkingBaseUrl url: URL,
        @NetworkingOkHttp client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Singleton
    @Provides
    fun provideNetworking(@NetworkingRetrofit retrofit: Retrofit): Networking {

        return Networking(retrofit)
    }
}