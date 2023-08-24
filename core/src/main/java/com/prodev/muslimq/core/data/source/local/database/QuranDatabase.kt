package com.prodev.muslimq.core.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prodev.muslimq.core.data.source.local.model.BookmarkEntity
import com.prodev.muslimq.core.data.source.local.model.Converter
import com.prodev.muslimq.core.data.source.local.model.QuranDetailEntity
import com.prodev.muslimq.core.data.source.local.model.QuranEntity

@Database(
    entities = [QuranEntity::class, QuranDetailEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
@androidx.room.TypeConverters(Converter::class)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}