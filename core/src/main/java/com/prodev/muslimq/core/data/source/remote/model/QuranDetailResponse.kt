package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json

data class QuranDetailResponse(

    @Json(name = "nama")
    val nama: String,

    @Json(name = "ayat")
    val ayat: List<AyatItem>,

    @Json(name = "nama_latin")
    val nama_latin: String,

    @Json(name = "jumlah_ayat")
    val jumlah_ayat: Int,

    @Json(name = "tempat_turun")
    val tempat_turun: String,

    @Json(name = "arti")
    val arti: String,

    @Json(name = "deskripsi")
    val deskripsi: String,

    @Json(name = "audio")
    val audio: String,

    @Json(name = "nomor")
    val nomor: Int,

    @Json(name = "status")
    val status: Boolean
)

data class AyatItem(

    @Json(name = "ar")
    val ar: String,

    @Json(name = "idn")
    val idn: String,

    @Json(name = "id")
    val id: Int,

    @Json(name = "surah")
    val surah: Int,

    @Json(name = "nomor")
    val nomor: Int,

    @Json(name = "tr")
    val tr: String
)
