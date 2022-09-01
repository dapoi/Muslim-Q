package com.dapascript.muslimq.di

import com.dapascript.muslimq.data.source.remote.network.QuranApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideQuranApi(@Quran retrofit: Retrofit): QuranApi {
        return retrofit.create(QuranApi::class.java)
    }
}