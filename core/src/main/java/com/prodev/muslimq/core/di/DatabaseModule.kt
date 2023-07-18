package com.prodev.muslimq.core.di

import android.content.Context
import androidx.room.Room
import com.prodev.muslimq.core.data.source.local.database.QuranDao
import com.prodev.muslimq.core.data.source.local.database.QuranDatabase
import com.prodev.muslimq.core.data.source.local.database.ShalatDao
import com.prodev.muslimq.core.data.source.local.database.ShalatDatabase
import com.prodev.muslimq.core.data.source.local.database.TasbihDao
import com.prodev.muslimq.core.data.source.local.database.TasbihDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun getQuranDB(@ApplicationContext context: Context): QuranDatabase = Room.databaseBuilder(
        context, QuranDatabase::class.java, "quran.db"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun getShalatDB(@ApplicationContext context: Context): ShalatDatabase = Room.databaseBuilder(
        context, ShalatDatabase::class.java, "shalat.db"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun getTasbihDB(@ApplicationContext context: Context): TasbihDatabase = Room.databaseBuilder(
        context, TasbihDatabase::class.java, "tasbih.db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun getQuranDao(database: QuranDatabase): QuranDao = database.quranDao()

    @Provides
    fun getShalatDao(database: ShalatDatabase): ShalatDao = database.shalatDao()

    @Provides
    fun getTasbihDao(database: TasbihDatabase): TasbihDao = database.tasbihDao()
}