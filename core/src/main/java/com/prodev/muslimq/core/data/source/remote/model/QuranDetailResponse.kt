package com.prodev.muslimq.core.data.source.remote.model

import com.squareup.moshi.Json

data class QuranDetailResponse(
    @Json(name = "code")
    val code: Int,

    @Json(name = "message")
    val message: String,

    @Json(name = "data")
    val data: QuranDetailItem
)

data class QuranDetailItem(

    @Json(name = "nomor")
    val nomor: Int,

    @Json(name = "nama")
    val nama: String,

    @Json(name = "namaLatin")
    val namaLatin: String,

    @Json(name = "jumlahAyat")
    val jumlahAyat: Int,

    @Json(name = "tempatTurun")
    val tempatTurun: String,

    @Json(name = "arti")
    val arti: String,

    @Json(name = "deskripsi")
    val deskripsi: String,

    @Json(name = "audioFull")
    val audioFull: AudioFull,

    @Json(name = "ayat")
    val ayat: List<AyatItem>
)

data class AudioFull(

    @Json(name = "05")
    val audio: String? = "",
)

data class AyatItem(

    @Json(name = "nomorAyat")
    val nomorAyat: Int,

    @Json(name = "teksArab")
    val teksArab: String,

    @Json(name = "teksLatin")
    val teksLatin: String,

    @Json(name = "teksIndonesia")
    val teksIndonesia: String,
)