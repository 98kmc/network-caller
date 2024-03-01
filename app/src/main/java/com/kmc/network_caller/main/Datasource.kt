package com.kmc.network_caller.main

import android.content.Context
import com.kmc.networking.entity.HttpMethod
import com.kmc.networking.interfaces.NetworkCaller
import com.kmc.networking.interfaces.request
import com.kmc.networking.interfaces.safeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AnyDataType(
    val id: Int?,
    val title: String?
)

class DataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkCaller {

    private val service = networkingService(context)

    suspend fun fetchPost(): List<AnyDataType>? = service.request(endpoint = "posts/")

    suspend fun createPost(): Result<AnyDataType> = service.safeRequest(
        endpoint = "https://jsonplaceholder.typicode.com/posts/",
        method = HttpMethod.Post,
        withBody = mapOf(
            "title"  to "foo",
            "body"   to "bar",
            "userId" to "1"
        ),
        withHeaders = mapOf(
            "Content-type" to "application/json; charset=UTF-8"
        )
    )
}