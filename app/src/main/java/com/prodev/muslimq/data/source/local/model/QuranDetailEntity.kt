package com.prodev.muslimq.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "quran_detail")
data class QuranDetailEntity(
    @PrimaryKey(autoGenerate = true) val nomor: Int,
    val surahId: Int,
    val nama: String,
    val namaLatin: String,
    val jumlahAyat: Int,
    val tempatTurun: String,
    val artiQuran: String,
    val deskripsi: String,
    val audio: String,
    val ayat: List<Ayat>
)

@Entity(tableName = "ayat")
data class Ayat(
    val ayatId: Int,
    val ayatArab: String,
    val ayatLatin: String,
    val ayatTerjemahan: String,
)

class Converter {

    @TypeConverter
    fun fromAyat(value: List<Ayat>): String {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Ayat::class.java)
        val adapter = moshi.adapter<List<Ayat>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toAyat(value: String): List<Ayat> {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Ayat::class.java)
        val adapter = moshi.adapter<List<Ayat>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }
}
