package com.dapascript.muslimq.di

import android.content.Context
import androidx.room.Room
import com.dapascript.muslimq.data.source.local.database.QuranDao
import com.dapascript.muslimq.data.source.local.database.QuranDatabase
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
    fun getInstance(@ApplicationContext context: Context): QuranDatabase = Room.databaseBuilder(
        context, QuranDatabase::class.java, "quran.db"
    ).build()

    @Provides
    fun getQuranDao(database: QuranDatabase): QuranDao = database.quranDao()
}