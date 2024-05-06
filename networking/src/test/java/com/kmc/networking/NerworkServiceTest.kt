package com.kmc.networking

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var networkService: NetworkingService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        networkService = object : NetworkingService {

            override fun NetworkingService.provideNetworking() = Networking(
                retrofit = Retrofit.Builder()
                    .baseUrl(mockWebServer.url("/"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            )
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `execute get request with Successfully response`() = runTest {

        val responseBody = """{"key": "value"}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody))

        val result: Map<String, Any>? = networkService.request(endpoint = "test/").execute()

        Assert.assertNotNull(result)
        Assert.assertEquals("value", result?.get("key"))
    }

    @Test
    fun `execute get request with Failure response`() = runTest {

        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result: Map<String, Any>? = networkService.request(endpoint = "test/").execute()

        Assert.assertNull(result)
    }

    @Test
    fun `execute get safeRequest with Successfully response`() = runTest {

        val responseBody = """{"key": "value"}"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody))

        val result: Result<Map<String, Any>> = networkService.safeRequest("test/").execute()
        val unwrappedValue = result.getOrNull()

        Assert.assertTrue(result.isSuccess)
        Assert.assertNotNull(unwrappedValue)
        Assert.assertEquals("value", unwrappedValue?.get("key"))
    }

    @Test
    fun `execute get safeRequest with Failure response`() = runTest {

        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result: Result<Map<String, Any>> = networkService.safeRequest("test/").execute()

        Assert.assertTrue(result.isFailure)
        assert(result.exceptionOrNull() is NetworkError.StatusCode)
    }

    @Test
    fun `execute impossible decoding get safeRequest with Failure response`() = runTest {

        val responseBody = """{"key":"value"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val result: Result<List<String>> = networkService.safeRequest("test/").execute()

        assert(result.isFailure)
        assert(result.exceptionOrNull() is NetworkError.DecodingError)
    }
}