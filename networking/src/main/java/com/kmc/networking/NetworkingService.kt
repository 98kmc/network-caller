package com.kmc.networking

interface NetworkingService {

    fun NetworkingService.provideNetworking(): Networking
}

fun NetworkingService.request(
    endpoint: String
) = provideNetworking().Request(endpoint)

fun NetworkingService.safeRequest(
    endpoint: String
) = provideNetworking().SafeRequest(endpoint)

