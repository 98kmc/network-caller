package com.kmc.networking

interface NetworkingService {

    fun NetworkingService.provideNetworking(): Networking
}

fun NetworkingService.request(
    from: String
) = provideNetworking().Request(from = from)

fun NetworkingService.safeRequest(
    from: String
) = provideNetworking().SafeRequest(from = from)

