package com.kmc.networking

interface NetworkingService {

    fun NetworkingService.provideNetworking(): Networking

    fun <T> NetworkingService.buildRequest(from: String, javaClass: Class<T>) =
        provideNetworking().DefaultRequest(from = from, javaClass)

    fun <T> NetworkingService.buildSafeRequest(from: String, javaClass: Class<T>) =
        provideNetworking().SafeRequest<T>(from = from, javaClass)
}

suspend inline fun <reified T> NetworkingService.request(
    from: String
) = buildRequest(from, T::class.java)

suspend inline fun <reified T> NetworkingService.safeRequest(
    from: String
) = buildSafeRequest(from, T::class.java)

