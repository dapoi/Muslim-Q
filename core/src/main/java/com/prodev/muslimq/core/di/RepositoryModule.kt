package com.prodev.muslimq.core.di

import com.prodev.muslimq.core.data.repository.*
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

    @Binds
    abstract fun provideTasbihRepository(tasbihRepository: TasbihRepositoryImpl): TasbihRepository

    @Binds
    abstract fun provideAsmaulHusnaRepository(asmaulHusnaRepository: AsmaulHusnaRepositoryImpl): AsmaulHusnaRepository
}