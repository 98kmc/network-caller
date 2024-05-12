@file:Suppress("unused")

package com.kmc.networking

import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import javax.inject.Inject

class Networking @Inject internal constructor(
    @NetworkingRetrofitInstance private val retrofit: Retrofit
) {

    internal val service: ApiService = retrofit.create(ApiService::class.java)
    internal val baseUrl get() = retrofit.baseUrl().toString()

    internal interface ApiService {

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

    internal fun buildService(
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
}