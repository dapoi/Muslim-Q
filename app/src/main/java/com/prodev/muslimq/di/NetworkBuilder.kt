package com.prodev.muslimq.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.prodev.muslimq.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkBuilder {

    @Singleton
    @Provides
    fun provideMoshi(): MoshiConverterFactory = MoshiConverterFactory.create()

    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context))
            .maxContentLength(250000L)
            .alwaysReadResponseBody(true)
            .build()

        return if (BuildConfig.DEBUG) {
            OkHttpClient.Builder().addInterceptor(logging).addInterceptor(chuckerInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()
        } else {
            OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build()
        }
    }

    @Singleton
    @Provides
    @Quran
    fun provideQuranApi(
        @ApplicationContext context: Context,
    ): Retrofit = Retrofit.Builder().baseUrl("https://equran.id/api/")
        .addConverterFactory(MoshiConverterFactory.create()).client(provideOkHttpClient(context))
        .build()

    @Singleton
    @Provides
    @Area
    fun provideArea(
        @ApplicationContext context: Context,
    ): Retrofit = Retrofit.Builder().baseUrl("https://dapoi.github.io/api-wilayah-indonesia/api/")
        .addConverterFactory(MoshiConverterFactory.create()).client(provideOkHttpClient(context))
        .build()

    @Singleton
    @Provides
    @Shalat
    fun provideShalatApi(
        @ApplicationContext context: Context,
    ): Retrofit = Retrofit.Builder().baseUrl("https://api.aladhan.com/")
        .addConverterFactory(MoshiConverterFactory.create()).client(provideOkHttpClient(context))
        .build()

}