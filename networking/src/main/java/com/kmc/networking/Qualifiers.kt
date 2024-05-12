package com.kmc.networking

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkingBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkingRetrofitInstance

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkingOkHttp