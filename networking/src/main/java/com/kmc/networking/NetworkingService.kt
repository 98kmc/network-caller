package com.kmc.networking

interface NetworkingService {

    fun NetworkingService.provideNetworking(): Networking

    fun <T> NetworkingService.buildRequest(from: String, javaClass: Class<T>) =
        provideNetworking().Request(from = from, javaClass)

    fun <T> NetworkingService.buildSafeRequest(from: String, javaClass: Class<T>) =
        provideNetworking().SafeRequest(from = from, javaClass)
}

inline fun <reified T> NetworkingService.request(
    from: String
) = buildRequest(from, T::class.java)

inline fun <reified T> NetworkingService.safeRequest(
    from: String
) = buildSafeRequest(from, T::class.java)

