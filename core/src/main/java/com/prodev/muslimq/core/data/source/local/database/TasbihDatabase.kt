package com.prodev.muslimq.core.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prodev.muslimq.core.data.source.local.model.TasbihEntity

@Database(
    entities = [TasbihEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TasbihDatabase : RoomDatabase() {

    abstract fun tasbihDao(): TasbihDao
}