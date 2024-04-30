package com.kmc.networking

internal interface RequestExecutor<T> {

    suspend fun execute(): T?
}
