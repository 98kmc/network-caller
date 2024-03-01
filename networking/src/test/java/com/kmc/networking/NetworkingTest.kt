package com.kmc.networking

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class NetworkingTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var networking: Networking

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        networking = Networking(
            retrofit = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `execute get request with successfully response`() = runTest {
        // set the expected response
        val responseBody = """{"key":"value"}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody))

        // create the request
        val task = networking.DataTaskBuilder("test/").build()

        // create the request
        val (jsonElement, httpResponse) = task.execute()

        Assert.assertEquals(200, httpResponse.code)
        Assert.assertNotNull(jsonElement)
        Assert.assertEquals(responseBody, jsonElement?.toString())
    }

    @Test
    fun `execute get request with failure response`() = runTest {
        // set the expected response
        val responseBody = """{"key":"value"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody(responseBody))

        // create the request
        val task = networking.DataTaskBuilder("test/").build()

        // create the request
        val (jsonElement, httpResponse) = task.execute()

        Assert.assertEquals(500, httpResponse.code)
        Assert.assertNull(jsonElement)
    }

    @Test
    fun `execute post request with successfully response`() = runTest {

        val expectedBody = """{"key":"value"}"""
        mockWebServer.enqueue(MockResponse().setBody(expectedBody))

        val requestBody =
            """{"bodyParam":"bodyValue"}""".toRequestBody("application/json".toMediaTypeOrNull())

        val task = networking.DataTaskBuilder("test/")
            .withBody(requestBody)
            .build()

        val (jsonElement, httpResponse) = task.execute()

        Assert.assertEquals(200, httpResponse.code)
        Assert.assertNotNull(jsonElement)
        Assert.assertEquals(jsonElement?.toString(), expectedBody)
    }
}