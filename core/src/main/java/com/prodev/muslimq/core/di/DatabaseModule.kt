package com.prodev.muslimq.core.di

import android.content.Context
import androidx.room.Room
import com.prodev.muslimq.core.data.source.local.database.QuranDao
import com.prodev.muslimq.core.data.source.local.database.QuranDatabase
import com.prodev.muslimq.core.data.source.local.database.ShalatDao
import com.prodev.muslimq.core.data.source.local.database.ShalatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun getQuranDB(@ApplicationContext context: Context): QuranDatabase = Room.databaseBuilder(
        context, QuranDatabase::class.java, "quran.db"
    ).build()

    @Singleton
    @Provides
    fun getShalatDB(@ApplicationContext context: Context): ShalatDatabase = Room.databaseBuilder(
        context, ShalatDatabase::class.java, "shalat.db"
    ).build()

    @Provides
    fun getQuranDao(database: QuranDatabase): QuranDao = database.quranDao()

    @Provides
    fun getShalatDao(database: ShalatDatabase): ShalatDao = database.shalatDao()
}