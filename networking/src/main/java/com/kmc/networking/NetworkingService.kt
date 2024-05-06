package com.kmc.networking

import java.net.URL

interface NetworkingService {

    fun NetworkingService.provideNetworking(): Networking
}

fun NetworkingService.request(
    endpoint: String
) = Request(endpoint, provideNetworking())

fun NetworkingService.safeRequest(
    endpoint: String
) = SafeRequest(endpoint, provideNetworking())

fun NetworkingService.request(
    url: URL
) = Request(url, provideNetworking())

fun NetworkingService.safeRequest(
    url: URL
) = SafeRequest(url , provideNetworking())
