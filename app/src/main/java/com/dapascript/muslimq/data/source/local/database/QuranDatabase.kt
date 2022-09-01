package com.dapascript.muslimq.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dapascript.muslimq.data.source.local.model.QuranEntity

@Database(entities = [QuranEntity::class], version = 1, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}