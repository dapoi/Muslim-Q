package com.prodev.muslimq.core.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey
    val surahId: Int,
    val nama: String,
    val namaLatin: String,
    val deskripsi: String,
    val jumlahAyat: Int,
    val artiQuran: String,
)
