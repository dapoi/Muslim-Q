package com.dapascript.muslimq.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dapascript.muslimq.data.source.local.model.Converter
import com.dapascript.muslimq.data.source.local.model.QuranDetailEntity
import com.dapascript.muslimq.data.source.local.model.QuranEntity

@Database(
    entities = [QuranEntity::class, QuranDetailEntity::class],
    version = 1,
    exportSchema = false
)
@androidx.room.TypeConverters(Converter::class)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}