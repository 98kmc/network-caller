package com.kmc.network_caller

import android.content.Context
import com.kmc.networking.HttpMethod
import com.kmc.networking.NetworkCaller
import com.kmc.networking.request
import com.kmc.networking.safeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AnyDataType(
    val id: Int?,
    val title: String?
)

private typealias PostList = List<AnyDataType>

class DataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkCaller {

    private val network = networkingService(context)

    suspend fun fetchPost(): PostList? = network.request(endpoint = "posts/").execute()

    suspend fun createPost(): Result<AnyDataType> {

        val request = network.safeRequest(endpoint = "https://jsonplaceholder.typicode.com/posts/")
            .withMethod(HttpMethod.POST)
            .withBody(
                "title" to "foo",
                "body" to "bar",
                "userId" to "1"
            )
            .withHeaders(
                "Content-type" to "application/json; charset=UTF-8"
            )

        return request.execute()
    }
}