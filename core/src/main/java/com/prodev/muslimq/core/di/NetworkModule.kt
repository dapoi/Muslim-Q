package com.prodev.muslimq.core.di

import com.prodev.muslimq.core.data.source.remote.network.AreaApi
import com.prodev.muslimq.core.data.source.remote.network.QuranApi
import com.prodev.muslimq.core.data.source.remote.network.ShalatApi
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

    @Singleton
    @Provides
    fun provideAreaApi(@Area retrofit: Retrofit): AreaApi {
        return retrofit.create(AreaApi::class.java)
    }

    @Singleton
    @Provides
    fun provideShalatApi(@Shalat retrofit: Retrofit): ShalatApi {
        return retrofit.create(ShalatApi::class.java)
    }
}