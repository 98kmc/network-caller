@file:Suppress("unused")
package com.kmc.networking.interfaces

import android.content.Context
import com.kmc.networking.Networking
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

interface NetworkCaller {

    fun NetworkCaller.networkingService(context: Context) = object : NetworkService {

        override fun NetworkService.provideNetworking() =
            EntryPoints.get(context, NetworkingEntryPoint::class.java).networking()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface NetworkingEntryPoint {

    fun networking(): Networking
}