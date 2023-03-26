package com.prodev.muslimq.core.data.source.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tafsir")
data class QuranTafsirEntity(
    @PrimaryKey
    val ayahNumber: Int,
    val surahId: Int,
    val teks: String,
)
