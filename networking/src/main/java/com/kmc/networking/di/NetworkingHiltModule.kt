package com.kmc.networking.di

import com.kmc.networking.Networking
import com.kmc.networking.NetworkingBaseUrl
import com.kmc.networking.NetworkingOkHttp
import com.kmc.networking.NetworkingRetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkingHiltModule {

    @NetworkingRetrofitInstance
    @Singleton
    @Provides
    fun provideRetrofit(
        @NetworkingBaseUrl url: URL,
        @NetworkingOkHttp client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Singleton
    @Provides
    fun provideNetworking(@NetworkingRetrofitInstance retrofit: Retrofit): Networking {

        return Networking(retrofit)
    }
}