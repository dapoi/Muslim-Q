package com.prodev.muslimq.di

import com.prodev.muslimq.data.repository.QuranRepository
import com.prodev.muslimq.data.repository.QuranRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [NetworkModule::class, DatabaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideRepository(repositoryImpl: QuranRepositoryImpl): QuranRepository
}