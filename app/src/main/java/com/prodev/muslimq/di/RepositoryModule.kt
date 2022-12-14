package com.prodev.muslimq.di

import com.prodev.muslimq.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [NetworkModule::class, DatabaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideQuranRepository(repositoryImpl: QuranRepositoryImpl): QuranRepository

    @Binds
    abstract fun provideShalatRepository(shalatRepository: ShalatRepositoryImpl): ShalatRepository

    @Binds
    abstract fun provideDoaRepository(doaRepository: DoaRepositoryImpl): DoaRepository
}