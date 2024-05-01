@file:Suppress("unused")

package com.kmc.networking

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface NetworkCaller {

    fun NetworkCaller.networkingService(context: Context) = object : NetworkingService {

        override fun NetworkingService.provideNetworking() =
            EntryPoints.get(context, NetworkingEntryPoint::class.java).networking()
    }

    @Deprecated(
        "Deprecated in v1.1.0",
        ReplaceWith(expression = "networkService(context: Context)")
    )
    fun NetworkCaller.networkService(context: Context) = object : NetworkService {

        override fun NetworkService.provideNetworking() =
            EntryPoints.get(context, NetworkingEntryPoint::class.java).networking()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface NetworkingEntryPoint {

    fun networking(): Networking
}