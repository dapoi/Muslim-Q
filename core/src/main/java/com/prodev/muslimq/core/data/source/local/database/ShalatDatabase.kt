package com.prodev.muslimq.core.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prodev.muslimq.core.data.source.local.model.ShalatEntity

@Database(
    entities = [ShalatEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ShalatDatabase : RoomDatabase() {
    abstract fun shalatDao(): ShalatDao
}